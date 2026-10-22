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
package io.karpfen.dsl.parser

import io.karpfen.dsl.grammar.kstates.KstatesBaseVisitor
import io.karpfen.dsl.grammar.kstates.KstatesParser
import org.antlr.v4.runtime.misc.Interval

/**
 * Turns a KStates parse tree into a [KstatesFile].
 *
 * Besides reading the tree, the visitor checks that every action rule is written with the number of
 * operands its operation expects, see [Arity].
 */
class KstatesParseVisitor : KstatesBaseVisitor<KstatesFile>() {

    /**
     * Reads the state machine with its states, transitions and macros.
     *
     * @throws IllegalArgumentException If an action rule is written with the wrong number of operands.
     */
    override fun visitKstates_file(ctx: KstatesParser.Kstates_fileContext): KstatesFile {
        val machineCtx = ctx.statemachine()
        val attachedToClass = unquote(machineCtx.STRING().text)
        val states = machineCtx.states_block().state_definition().map { parseState(it) }
        val transitions = machineCtx.transitions_block().transition_definition().map { parseTransition(it) }
        val macros = machineCtx.macros_block()?.macro_definition()?.map { parseMacro(it) } ?: emptyList()

        return KstatesFile(attachedToClass, states, transitions, macros)
    }

    private fun parseState(ctx: KstatesParser.State_definitionContext): ParsedState {
        val name = unquote(ctx.STRING().text)
        val isInitial = ctx.INITIAL() != null

        val onEntry = ctx.entry_block()?.let { parseActionBlock(it.action_block()) }
        val onDo = ctx.do_block()?.let { parseActionBlock(it.action_block()) }
        val innerStates = ctx.state_definition().map { parseState(it) }.toMutableList()

        return ParsedState(name, isInitial, onEntry, onDo, innerStates)
    }

    private fun parseActionBlock(ctx: KstatesParser.Action_blockContext): ParsedActionBlock {
        val items = ctx.action_item().map { parseActionItem(it) }
        return ParsedActionBlock(items)
    }

    private fun parseActionItem(ctx: KstatesParser.Action_itemContext): ParsedActionItem {
        ctx.action_rule()?.let { return ParsedActionRuleItem(parseActionRule(it)) }
        ctx.with_block()?.let { return parseWithBlock(it) }
        return parseInScopeBlock(ctx.in_scope_block())
    }

    private fun parseInScopeBlock(ctx: KstatesParser.In_scope_blockContext): ParsedInScopeBlock {
        val paths = ctx.STRING().map { unquote(it.text) }
        val body = parseActionBlock(ctx.action_block())
        return ParsedInScopeBlock(paths, body)
    }

    private fun parseWithBlock(ctx: KstatesParser.With_blockContext): ParsedWithBlock {
        val strings = ctx.macro_call().STRING().map { unquote(it.text) }
        val macroName = strings.firstOrNull() ?: ""
        val args = if (strings.size > 1) strings.drop(1) else emptyList()
        val name = unquote(ctx.STRING().text)
        val body = parseActionBlock(ctx.action_block())
        return ParsedWithBlock(macroName, args, name, body)
    }

    /**
     * Reads an action rule and checks that the arity actually written, `OP("a")`, `OP("a", rhs)` or
     * `OP("a", "b", rhs)`, is the one its operation expects.
     */
    private fun parseActionRule(ctx: KstatesParser.Action_ruleContext): ParsedActionRule {
        val operation = operationOf(ctx.action_operation())
        val strings = ctx.STRING().map { unquote(it.text) }
        val rightSide = ctx.action_right_side()?.let { parseActionRightSide(it) }

        val actualArity = when {
            rightSide == null -> Arity.UNARY
            strings.size == 1 -> Arity.BINARY
            else -> Arity.TERNARY
        }
        val expectedArity = expectedArity(operation)
        require(actualArity == expectedArity) {
            "Operation $operation expects ${expectedArity.usage}, but was called as ${actualArity.usage}."
        }

        return when (actualArity) {
            Arity.UNARY -> ParsedActionRule(operation, strings[0], null, null)
            Arity.BINARY -> ParsedActionRule(operation, strings[0], null, rightSide)
            Arity.TERNARY -> ParsedActionRule(operation, strings[0], strings[1], rightSide)
        }
    }

    private fun operationOf(op: KstatesParser.Action_operationContext): ParsedActionOperationType = when {
        op.SET() != null -> ParsedActionOperationType.SET
        op.APPEND() != null -> ParsedActionOperationType.APPEND
        op.SETLIST() != null -> ParsedActionOperationType.SETLIST
        op.DROPLIST() != null -> ParsedActionOperationType.DROPLIST
        op.SETOBJ() != null -> ParsedActionOperationType.SETOBJ
        op.APPENDOBJ() != null -> ParsedActionOperationType.APPENDOBJ
        op.DROPOBJ() != null -> ParsedActionOperationType.DROPOBJ
        else -> ParsedActionOperationType.EVENT
    }

    private fun parseActionRightSide(ctx: KstatesParser.Action_right_sideContext): ParsedActionRightSide {
        return when {
            ctx.STRING() != null -> ParsedValueActionRightSide(unquote(ctx.STRING().text))
            ctx.macro_call() != null -> {
                val strings = ctx.macro_call().STRING().map { unquote(it.text) }
                val macroName = strings.firstOrNull() ?: ""
                val args = if (strings.size > 1) strings.drop(1) else emptyList()
                ParsedMacroActionRightSide(macroName, args)
            }
            else -> ParsedEvalActionRightSide(extractInnerBlockText(ctx.eval_statement().eval_code_block()))
        }
    }

    private fun expectedArity(op: ParsedActionOperationType): Arity = when (op) {
        ParsedActionOperationType.DROPLIST,
        ParsedActionOperationType.DROPOBJ -> Arity.UNARY
        ParsedActionOperationType.SETOBJ,
        ParsedActionOperationType.APPENDOBJ -> Arity.TERNARY
        else -> Arity.BINARY
    }

    private fun parseTransition(ctx: KstatesParser.Transition_definitionContext): ParsedTransitionLike {
        ctx.normal_transition()?.let { return parseNormalTransition(it) }
        ctx.split_transition()?.let { return parseSplitTransition(it) }
        return parseJoinTransition(ctx.join_transition())
    }

    private fun parseNormalTransition(ctx: KstatesParser.Normal_transitionContext): ParsedTransition {
        val fromState = unquote(ctx.STRING(0).text)
        val toState = unquote(ctx.STRING(1).text)
        val allowLoops = ctx.not_looping() == null
        val condition = parseCondition(ctx.condition_block())

        return ParsedTransition(fromState, toState, allowLoops, condition)
    }

    private fun parseSplitTransition(ctx: KstatesParser.Split_transitionContext): ParsedSplitTransition {
        val strings = ctx.STRING().map { unquote(it.text) }
        val fromState = strings.first()
        val toStates = strings.drop(1)
        val allowLoops = ctx.not_looping() == null
        val condition = parseCondition(ctx.condition_block())

        return ParsedSplitTransition(fromState, toStates, allowLoops, condition)
    }

    private fun parseJoinTransition(ctx: KstatesParser.Join_transitionContext): ParsedJoinTransition {
        val strings = ctx.STRING().map { unquote(it.text) }
        val toState = strings.last()
        val fromStates = strings.dropLast(1)
        val allowLoops = ctx.not_looping() == null
        val condition = parseCondition(ctx.condition_block())

        return ParsedJoinTransition(fromStates, toState, allowLoops, condition)
    }

    /**
     * Parses a CONDITION block into its ordered list of clauses. An empty list means there was no
     * CONDITION block at all (an unconditional transition).
     */
    private fun parseCondition(ctx: KstatesParser.Condition_blockContext?): List<ParsedCondition> {
        if (ctx == null) {
            return emptyList()
        }
        return ctx.condition_clause().map { parseConditionClause(it) }
    }

    private fun parseConditionClause(ctx: KstatesParser.Condition_clauseContext): ParsedCondition {
        if (ctx.eval_statement() != null) {
            val code = extractInnerBlockText(ctx.eval_statement().eval_code_block())
            return ParsedEvalCondition(code)
        }
        if (ctx.event_condition() != null) {
            val eventCtx = ctx.event_condition()
            return ParsedEventCondition(
                unquote(eventCtx.STRING(0).text),
                unquote(eventCtx.STRING(1).text)
            )
        }

        val valueCtx = ctx.value_condition()
        return ParsedValueCondition(unquote(valueCtx.STRING().text))
    }

    private fun parseMacro(ctx: KstatesParser.Macro_definitionContext): ParsedMacro {
        val name = unquote(ctx.STRING().text)
        val takes = ctx.takes_directive().map {
            ParsedTakesDirective(unquote(it.STRING().text), parseTypeExpression(it.type_expression()))
        }
        val returnType = parseReturnExpression(ctx.returns_directive().return_expression())
        val code = extractInnerBlockText(ctx.definition_block().eval_statement().eval_code_block())

        return ParsedMacro(name, takes, ParsedReturnsDirective(returnType), code)
    }

    private fun parseReturnExpression(ctx: KstatesParser.Return_expressionContext): String {
        return if (ctx.reference_expression() != null) {
            val typeName = unquote(ctx.reference_expression().STRING().text)
            "reference(\"$typeName\")"
        } else {
            parseTypeExpression(ctx.type_expression())
        }
    }

    private fun parseTypeExpression(ctx: KstatesParser.Type_expressionContext): String {
        if (ctx.STRING() != null) {
            return unquote(ctx.STRING().text)
        }

        val typedCall = ctx.typed_call_expression()
        val functionName = typedCall.IDENT().text
        val typeName = unquote(typedCall.STRING().text)
        return "$functionName(\"$typeName\")"
    }

    private fun unquote(text: String): String {
        return text.trim().removeSurrounding("\"").removeSurrounding("'")
    }

    /**
     * Returns the source text between the braces of an `EVAL { ... }` block, trimmed, exactly as written,
     * hidden-channel whitespace and comments included.
     */
    private fun extractInnerBlockText(ctx: KstatesParser.Eval_code_blockContext): String {
        if (ctx.childCount == 0) {
            return ""
        }
        val input = ctx.start.inputStream
        val start = ctx.start.startIndex
        val stop = ctx.stop.stopIndex
        if (stop < start) {
            return ""
        }
        return input.getText(Interval.of(start, stop)).trim()
    }
}
