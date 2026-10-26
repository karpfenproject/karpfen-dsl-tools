/**
 * Copyright 2026 Karl Kegel
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.karpfen.dsl.reader.kstates

import io.karpfen.dsl.diagnostics.DiagnosticCode
import io.karpfen.dsl.grammar.kstates.KstatesParser
import io.karpfen.dsl.parser.ActionRuleSyntax
import io.karpfen.dsl.parser.Arity
import io.karpfen.dsl.reader.ReadingSession
import io.karpfen.dsl.reader.containsSyntaxError
import io.karpfen.dsl.reader.presentToken
import io.karpfen.dsl.reader.presentTokens
import io.karpfen.dsl.states.actions.ActionBlock
import io.karpfen.dsl.states.actions.ActionItem
import io.karpfen.dsl.states.actions.ActionRightSide
import io.karpfen.dsl.states.actions.ActionRule
import io.karpfen.dsl.states.actions.EvalActionRightSide
import io.karpfen.dsl.states.actions.InScopeBlock
import io.karpfen.dsl.states.actions.MacroActionRightSide
import io.karpfen.dsl.states.actions.ValueActionRightSide
import io.karpfen.dsl.states.actions.WithBlock
import org.antlr.v4.runtime.ParserRuleContext

/**
 * Converts KStates action blocks with their rules, `IF IN SCOPE` and `WITH` blocks, checks the arity of every
 * rule and records where each block, item and right side is written.
 *
 * A rule that contains a syntax error is left out without further checks, since the syntax error is reported
 * already; a rule with the wrong arity is reported and left out.
 *
 * @param session The read the conversion belongs to.
 * @param evalBodies Reads the code of `EVAL` right sides.
 */
internal class ActionConverter(
    private val session: ReadingSession,
    private val evalBodies: EvalBodies
) {

    /**
     * Converts [block] and records it at [construct], the whole `ENTRY` or `DO` section by default the block.
     */
    fun convertBlock(block: KstatesParser.Action_blockContext, construct: ParserRuleContext = block): ActionBlock {
        val actionBlock = ActionBlock(block.action_item().mapNotNull { convertItem(it) }.toMutableList())
        session.record(actionBlock, construct)
        return actionBlock
    }

    private fun convertItem(item: KstatesParser.Action_itemContext): ActionItem? {
        item.action_rule()?.let { return convertRule(it) }
        item.with_block()?.let { return convertWithBlock(it) }
        item.in_scope_block()?.let { return convertInScopeBlock(it) }
        return null
    }

    private fun convertInScopeBlock(block: KstatesParser.In_scope_blockContext): InScopeBlock? {
        val body = block.action_block() ?: return null
        val inScope = InScopeBlock(block.presentTokens(KstatesParser.STRING).map { unquoted(it) }, convertBlock(body))
        session.record(inScope, block)
        return inScope
    }

    private fun convertWithBlock(block: KstatesParser.With_blockContext): WithBlock? {
        val nameToken = block.presentToken(KstatesParser.STRING) ?: return null
        val macroCall = block.macro_call()?.let { convertMacroCall(it) } ?: return null
        val body = block.action_block() ?: return null
        val withBlock = WithBlock(macroCall, unquoted(nameToken), convertBlock(body))
        session.record(withBlock, block, nameToken)
        return withBlock
    }

    private fun convertRule(rule: KstatesParser.Action_ruleContext): ActionRule? {
        if (rule.containsSyntaxError() || rule.action_operation() == null) {
            return null
        }
        val operation = ActionRuleSyntax.operationOf(rule).operation
        val operands = rule.presentTokens(KstatesParser.STRING).map { unquoted(it) }
        val rightSideContext = rule.action_right_side()
        val leftSide = operands.firstOrNull() ?: return null
        val writtenArity = ActionRuleSyntax.writtenArity(operands.size, rightSideContext != null)
        val expectedArity = ActionRuleSyntax.expectedArity(operation)
        if (writtenArity != expectedArity) {
            session.report(
                DiagnosticCode.ACTION_ARITY,
                session.locator.rangeOf(rule),
                ActionRuleSyntax.arityMismatch(operation, expectedArity, writtenArity)
            )
            return null
        }
        val actionRule = ActionRule(
            operationType = operation,
            leftSide = leftSide,
            rightSide = rightSideContext?.let { convertRightSide(it) },
            secondSide = if (writtenArity == Arity.TERNARY) operands[1] else null
        )
        session.record(actionRule, rule)
        return actionRule
    }

    /**
     * Converts the right side of a rule: a literal value, a macro call or an `EVAL` body.
     */
    private fun convertRightSide(rightSide: KstatesParser.Action_right_sideContext): ActionRightSide? {
        rightSide.presentToken(KstatesParser.STRING)?.let { token ->
            return ValueActionRightSide(unquoted(token)).also { session.record(it, rightSide) }
        }
        rightSide.macro_call()?.let { return convertMacroCall(it) }
        val statement = rightSide.eval_statement() ?: return null
        val body = evalBodies.read(statement)
        return EvalActionRightSide(body.code).also { session.record(it, statement, codeRange = body.codeRange) }
    }

    /**
     * Converts `MACRO("name", "arg", ...)` into a macro call named by its first string; null without a name.
     */
    private fun convertMacroCall(call: KstatesParser.Macro_callContext): MacroActionRightSide? {
        val strings = call.presentTokens(KstatesParser.STRING)
        val nameToken = strings.firstOrNull() ?: return null
        val macroCall = MacroActionRightSide(unquoted(nameToken), strings.drop(1).map { unquoted(it) })
        session.record(macroCall, call, nameToken)
        return macroCall
    }
}
