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

import io.karpfen.dsl.states.actions.ActionOperationType

/**
 * The syntax-level content of a KStates file, before it is converted into a state machine.
 *
 * @property attachedToClass The class type the state machine is attached to.
 * @property states The top-level states, in definition order.
 * @property transitions The transitions of all kinds, in definition order.
 * @property macros The macro definitions, in definition order.
 */
data class KstatesFile(
    val attachedToClass: String,
    val states: List<ParsedState>,
    val transitions: List<ParsedTransitionLike>,
    val macros: List<ParsedMacro>
)

/**
 * A `STATE` definition with its action blocks and nested states.
 *
 * @property name The state name.
 * @property isInitial True if the state is marked `INITIAL`.
 * @property onEntry The `ENTRY` block, or null if there is none.
 * @property onDo The `DO` block, or null if there is none.
 * @property innerStates The nested states, in definition order.
 */
data class ParsedState(
    val name: String,
    val isInitial: Boolean,
    val onEntry: ParsedActionBlock?,
    val onDo: ParsedActionBlock?,
    val innerStates: List<ParsedState>
)

/**
 * The items of an `ENTRY`, `DO`, `IF IN SCOPE` or `WITH` block.
 *
 * @property items The items, in definition order.
 */
data class ParsedActionBlock(val items: List<ParsedActionItem>)

/**
 * One item of an action block: a rule or a nested block.
 */
sealed class ParsedActionItem

/**
 * An action block item that is a single action rule.
 *
 * @property rule The action rule.
 */
data class ParsedActionRuleItem(val rule: ParsedActionRule) : ParsedActionItem()

/**
 * An `IF IN SCOPE("path", ...) { ... }` block whose body runs only when all paths are available.
 *
 * @property paths The access paths that must be available.
 * @property body The actions to run.
 */
data class ParsedInScopeBlock(
    val paths: List<String>,
    val body: ParsedActionBlock
) : ParsedActionItem()

/**
 * A `WITH macro_call AS "name" { ... }` binding block: the macro is evaluated once and its result is bound
 * to [name] for the duration of [body], readable as `$(name->...)` inside nested actions.
 *
 * @property macroName The name of the called macro.
 * @property args The arguments of the macro call.
 * @property name The binding name.
 * @property body The actions to run with the binding in scope.
 */
data class ParsedWithBlock(
    val macroName: String,
    val args: List<String>,
    val name: String,
    val body: ParsedActionBlock
) : ParsedActionItem()

/**
 * A single action rule. The meaning of the operands depends on the operation's arity:
 * - unary (DROPLIST, DROPOBJ): [leftSide] is the target path; [secondSide] and [rightSide] are null.
 * - binary (SET, APPEND, SETLIST, EVENT): [leftSide] is the target path; [rightSide] is the value.
 * - ternary (SETOBJ, APPENDOBJ): [leftSide] is the source path, [secondSide] the relation, [rightSide] the target.
 *
 * @property operationType The operation.
 * @property leftSide The first operand.
 * @property secondSide The relation of a ternary operation, null otherwise.
 * @property rightSide The value or target, null for a unary operation.
 */
data class ParsedActionRule(
    val operationType: ParsedActionOperationType,
    val leftSide: String,
    val secondSide: String? = null,
    val rightSide: ParsedActionRightSide? = null
)

/**
 * The operation keyword of an action rule; lowercase aliases map to the same operation.
 *
 * @property operation The operation of the state machine model the keyword stands for.
 */
enum class ParsedActionOperationType(val operation: ActionOperationType) {
    /** `SET`, overwrites a scalar property. */
    SET(ActionOperationType.SET),

    /** `APPEND`, appends to a simple list property. */
    APPEND(ActionOperationType.APPEND),

    /** `SETLIST`, overwrites a simple list property. */
    SETLIST(ActionOperationType.SETLIST),

    /** `DROPLIST`, clears a simple list property. */
    DROPLIST(ActionOperationType.DROPLIST),

    /** `SETOBJ`, replaces the target of a single-object relation. */
    SETOBJ(ActionOperationType.SETOBJ),

    /** `APPENDOBJ`, adds an object to a list relation. */
    APPENDOBJ(ActionOperationType.APPENDOBJ),

    /** `DROPOBJ`, removes an object from the model. */
    DROPOBJ(ActionOperationType.DROPOBJ),

    /** `EVENT`, sends an event. */
    EVENT(ActionOperationType.EVENT)
}

/**
 * Number of operands an action operation is written with.
 *
 * @property usage The written form, used in error messages.
 * @property operandCount The number of operands, as [ActionOperationType.operandCount] counts them.
 */
enum class Arity(val usage: String, val operandCount: Int) {
    /** One operand, the target path. */
    UNARY("OP(\"path\")", 1),

    /** A target path and a value. */
    BINARY("OP(\"path\", value)", 2),

    /** A source path, a relation and a target. */
    TERNARY("OP(\"source\", \"relation\", target)", 3)
}

/**
 * The right side of an action rule.
 */
sealed class ParsedActionRightSide

/**
 * A `MACRO("name", "arg", ...)` call.
 *
 * @property macroName The name of the called macro.
 * @property args The arguments.
 */
data class ParsedMacroActionRightSide(val macroName: String, val args: List<String>) : ParsedActionRightSide()

/**
 * An `EVAL { ... }` block.
 *
 * @property code The code between the braces.
 */
data class ParsedEvalActionRightSide(val code: String) : ParsedActionRightSide()

/**
 * A literal string value.
 *
 * @property value The value without quotes.
 */
data class ParsedValueActionRightSide(val value: String) : ParsedActionRightSide()

/**
 * A transition of any kind.
 */
sealed interface ParsedTransitionLike

/**
 * A `TRANSITION "from" -> "to"` definition.
 *
 * @property fromState The source state.
 * @property toState The target state.
 * @property allowLoops False if the transition is marked `NOT LOOPING`.
 * @property condition The clauses of its CONDITION block; empty for an unconditional transition.
 */
data class ParsedTransition(
    val fromState: String,
    val toState: String,
    val allowLoops: Boolean,
    val condition: List<ParsedCondition>
) : ParsedTransitionLike

/**
 * A `SPLIT "from" -> "a", "b", ...` definition.
 *
 * @property fromState The source state.
 * @property toStates The target states, one parallel branch each.
 * @property allowLoops False if the transition is marked `NOT LOOPING`.
 * @property condition The clauses of its CONDITION block; empty for an unconditional transition.
 */
data class ParsedSplitTransition(
    val fromState: String,
    val toStates: List<String>,
    val allowLoops: Boolean,
    val condition: List<ParsedCondition>
) : ParsedTransitionLike

/**
 * A `JOIN "a", "b", ... -> "to"` definition.
 *
 * @property fromStates The source states of the parallel branches.
 * @property toState The target state.
 * @property allowLoops False if the transition is marked `NOT LOOPING`.
 * @property condition The clauses of its CONDITION block; empty for an unconditional transition.
 */
data class ParsedJoinTransition(
    val fromStates: List<String>,
    val toState: String,
    val allowLoops: Boolean,
    val condition: List<ParsedCondition>
) : ParsedTransitionLike

/**
 * One clause of a CONDITION block.
 */
sealed class ParsedCondition

/**
 * An `EVAL { ... }` clause.
 *
 * @property code The code between the braces.
 */
data class ParsedEvalCondition(val code: String) : ParsedCondition()

/**
 * An `EVENT("domain", "event")` clause.
 *
 * @property eventDomain The event domain.
 * @property eventValue The event name.
 */
data class ParsedEventCondition(val eventDomain: String, val eventValue: String) : ParsedCondition()

/**
 * A `VALUE("variable")` clause.
 *
 * @property variableKey The boolean variable.
 */
data class ParsedValueCondition(val variableKey: String) : ParsedCondition()

/**
 * A `MACRO "name" { TAKES ... RETURNS ... DEFINITION ... }` definition.
 *
 * @property name The macro name.
 * @property takes The parameter declarations, in definition order.
 * @property returns The return type declaration.
 * @property definitionCode The code of the `DEFINITION` block.
 */
data class ParsedMacro(
    val name: String,
    val takes: List<ParsedTakesDirective>,
    val returns: ParsedReturnsDirective,
    val definitionCode: String
)

/**
 * A `TAKES("name", type)` parameter declaration.
 *
 * @property paramName The parameter name.
 * @property paramType The parameter type as written, for example `number` or `list("Point")`.
 */
data class ParsedTakesDirective(val paramName: String, val paramType: String)

/**
 * A `RETURNS(type)` declaration.
 *
 * @property returnType The return type as written, for example `number` or `reference("Obstacle")`.
 */
data class ParsedReturnsDirective(val returnType: String)
