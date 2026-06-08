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
package parser

import kstates.KstatesBaseVisitor
import kstates.KstatesLexer
import kstates.KstatesParser
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.misc.Interval
import java.io.File

/**
 * Parser for KStates DSL files using ANTLR-generated parser classes.
 */
class KStatesFileParser {

    /**
     * Parses a KStates state machine from a file.
     *
     * @param filePath The path to the KStates file to parse.
     * @return The parsed state machine structure.
     */
    fun parseFile(filePath: String): KstatesFile {
        val file = File(filePath)
        return parseString(file.readText())
    }

    /**
     * Parses a KStates state machine from a string.
     *
     * @param content The KStates content as a string.
     * @return The parsed state machine structure.
     */
    fun parseString(content: String): KstatesFile {
        val inputStream = CharStreams.fromString(content)
        val lexer = KstatesLexer(inputStream)
        val tokens = CommonTokenStream(lexer)
        val parser = KstatesParser(tokens)

        val parseTree = parser.kstates_file()
        val visitor = KstatesParseVisitor()

        return visitor.visit(parseTree) ?: KstatesFile("", emptyList(), emptyList(), emptyList())
    }
}

class KstatesParseVisitor : KstatesBaseVisitor<KstatesFile>() {

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
        val ruleCtx = ctx.action_rule()
        return if (ruleCtx != null) {
            ParsedActionRuleItem(parseActionRule(ruleCtx))
        } else {
            parseInScopeBlock(ctx.in_scope_block())
        }
    }

    private fun parseInScopeBlock(ctx: KstatesParser.In_scope_blockContext): ParsedInScopeBlock {
        val paths = ctx.STRING().map { unquote(it.text) }
        val body = parseActionBlock(ctx.action_block())
        return ParsedInScopeBlock(paths, body)
    }

    private fun parseActionRule(ctx: KstatesParser.Action_ruleContext): ParsedActionRule {
        val operation = when {
            ctx.action_operation().SET() != null -> ParsedActionOperationType.SET
            ctx.action_operation().APPEND() != null -> ParsedActionOperationType.APPEND
            else -> ParsedActionOperationType.EVENT
        }

        val leftSide = unquote(ctx.STRING().text)
        val rightSideCtx = ctx.action_right_side()
        val rightSide = when {
            rightSideCtx.STRING() != null -> ParsedValueActionRightSide(unquote(rightSideCtx.STRING().text))
            rightSideCtx.macro_call() != null -> {
                val strings = rightSideCtx.macro_call().STRING().map { unquote(it.text) }
                val macroName = strings.firstOrNull() ?: ""
                val args = if (strings.size > 1) strings.drop(1) else emptyList()
                ParsedMacroActionRightSide(macroName, args)
            }
            else -> {
                val code = extractInnerBlockText(rightSideCtx.eval_statement().eval_code_block())
                ParsedEvalActionRightSide(code)
            }
        }

        return ParsedActionRule(operation, leftSide, rightSide)
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

    private fun extractInnerBlockText(ctx: KstatesParser.Eval_code_blockContext): String {
        // With the new grammar: eval_statement is EVAL LBRACE eval_code_block RBRACE
        // So eval_code_block contains just the content between the braces
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

data class KstatesFile(
    val attachedToClass: String,
    val states: List<ParsedState>,
    val transitions: List<ParsedTransitionLike>,
    val macros: List<ParsedMacro>
)

data class ParsedState(
    val name: String,
    val isInitial: Boolean,
    val onEntry: ParsedActionBlock?,
    val onDo: ParsedActionBlock?,
    val innerStates: List<ParsedState>
)

data class ParsedActionBlock(val items: List<ParsedActionItem>)

sealed class ParsedActionItem

data class ParsedActionRuleItem(val rule: ParsedActionRule) : ParsedActionItem()

data class ParsedInScopeBlock(
    val paths: List<String>,
    val body: ParsedActionBlock
) : ParsedActionItem()

data class ParsedActionRule(
    val operationType: ParsedActionOperationType,
    val leftSide: String,
    val rightSide: ParsedActionRightSide
)

enum class ParsedActionOperationType {
    SET,
    APPEND,
    EVENT
}

sealed class ParsedActionRightSide

data class ParsedMacroActionRightSide(val macroName: String, val args: List<String>) : ParsedActionRightSide()

data class ParsedEvalActionRightSide(val code: String) : ParsedActionRightSide()

data class ParsedValueActionRightSide(val value: String) : ParsedActionRightSide()

sealed interface ParsedTransitionLike

data class ParsedTransition(
    val fromState: String,
    val toState: String,
    val allowLoops: Boolean,
    val condition: List<ParsedCondition>
) : ParsedTransitionLike

data class ParsedSplitTransition(
    val fromState: String,
    val toStates: List<String>,
    val allowLoops: Boolean,
    val condition: List<ParsedCondition>
) : ParsedTransitionLike

data class ParsedJoinTransition(
    val fromStates: List<String>,
    val toState: String,
    val allowLoops: Boolean,
    val condition: List<ParsedCondition>
) : ParsedTransitionLike

sealed class ParsedCondition

data class ParsedEvalCondition(val code: String) : ParsedCondition()

data class ParsedEventCondition(val eventDomain: String, val eventValue: String) : ParsedCondition()

data class ParsedValueCondition(val variableKey: String) : ParsedCondition()

data class ParsedMacro(
    val name: String,
    val takes: List<ParsedTakesDirective>,
    val returns: ParsedReturnsDirective,
    val definitionCode: String
)

data class ParsedTakesDirective(val paramName: String, val paramType: String)

data class ParsedReturnsDirective(val returnType: String)

