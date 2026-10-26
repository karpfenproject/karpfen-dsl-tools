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
package io.karpfen.dsl.printer.kstates

import io.karpfen.dsl.fixtures.CodeShape
import io.karpfen.dsl.states.JoinTransition
import io.karpfen.dsl.states.Macro
import io.karpfen.dsl.states.SplitTransition
import io.karpfen.dsl.states.State
import io.karpfen.dsl.states.StateMachine
import io.karpfen.dsl.states.Transition
import io.karpfen.dsl.states.TransitionLike
import io.karpfen.dsl.states.actions.ActionBlock
import io.karpfen.dsl.states.actions.ActionItem
import io.karpfen.dsl.states.actions.ActionRightSide
import io.karpfen.dsl.states.actions.ActionRule
import io.karpfen.dsl.states.actions.EvalActionRightSide
import io.karpfen.dsl.states.actions.InScopeBlock
import io.karpfen.dsl.states.actions.WithBlock
import io.karpfen.dsl.states.conditions.CompositeCondition
import io.karpfen.dsl.states.conditions.Condition
import io.karpfen.dsl.states.conditions.ConditionType
import io.karpfen.dsl.states.conditions.EvalCondition
import io.karpfen.dsl.states.conditions.EventCondition
import io.karpfen.dsl.states.conditions.ValueCondition
import io.karpfen.dsl.states.macros.CodeBlock
import io.karpfen.dsl.states.macros.Definition

/**
 * The structure of a [StateMachine] as plain values, so that two state machines compare with `equals`.
 *
 * States do not serve for that, as a nested state refers to its outer state and comparing them would never
 * end, and some conditions compare by identity. Action blocks and macros compare by value already. The code of
 * every `EVAL` body is held as its [CodeShape], which a round trip through the printer keeps.
 *
 * @property attachedTo The class type the state machine is attached to.
 * @property states The top-level states in order.
 * @property transitions The transitions in order.
 * @property macros The macros in order.
 */
data class StateMachineStructure(
    val attachedTo: String,
    val states: List<StateStructure>,
    val transitions: List<TransitionStructure>,
    val macros: List<Macro>
)

/**
 * The structure of a [State] and its inner states.
 *
 * @property name The state name.
 * @property isInitial True for an initial state.
 * @property outerName The name of the outer state, null for a top-level state.
 * @property entry The `ENTRY` actions.
 * @property onDo The `DO` actions.
 * @property innerStates The inner states in order.
 */
data class StateStructure(
    val name: String,
    val isInitial: Boolean,
    val outerName: String?,
    val entry: ActionBlock,
    val onDo: ActionBlock,
    val innerStates: List<StateStructure>
)

/**
 * The structure of a transition of any kind.
 *
 * @property kind `TRANSITION`, `SPLIT` or `JOIN`.
 * @property sources The source states.
 * @property targets The target states.
 * @property allowLoops False for `NOT LOOPING`.
 * @property condition The structure of the condition.
 */
data class TransitionStructure(
    val kind: String,
    val sources: List<String>,
    val targets: List<String>,
    val allowLoops: Boolean,
    val condition: ConditionStructure
)

/**
 * The structure of a condition: its type, its operands and, for a composite condition, its clauses.
 *
 * @property type The condition type.
 * @property operands The domain and event, the code, or the variable.
 * @property clauses The structures of the clauses of a composite condition.
 */
data class ConditionStructure(val type: ConditionType, val operands: List<String>, val clauses: List<ConditionStructure>)

/**
 * Returns the structure of this state machine.
 */
fun StateMachine.structure(): StateMachineStructure = StateMachineStructure(
    attachedToClass,
    states.map { it.structure() },
    transitions.map { it.structure() },
    macros.map { it.copy(definition = Definition(CodeBlock(CodeShape.of(it.definition.codeBlock.code)))) }
)

/**
 * Returns the structure of this state and its inner states.
 */
fun State.structure(): StateStructure = StateStructure(
    name,
    isInitial,
    outerState?.name,
    onEntry.shaped(),
    onDo.shaped(),
    innerStates.map { it.structure() }
)

/**
 * Returns a copy of this action block whose `EVAL` code is held as its [CodeShape].
 */
fun ActionBlock.shaped(): ActionBlock = ActionBlock(items.map { it.shaped() }.toMutableList())

private fun ActionItem.shaped(): ActionItem = when (this) {
    is ActionRule -> copy(rightSide = rightSide?.shaped())
    is InScopeBlock -> copy(body = body.shaped())
    is WithBlock -> copy(body = body.shaped())
}

private fun ActionRightSide.shaped(): ActionRightSide =
    if (this is EvalActionRightSide) EvalActionRightSide(CodeShape.of(code)) else this

/**
 * Returns the structure of this transition.
 */
fun TransitionLike.structure(): TransitionStructure = when (this) {
    is Transition -> TransitionStructure("TRANSITION", listOf(fromState), listOf(toState), allowLoops, condition.structure())
    is SplitTransition -> TransitionStructure("SPLIT", listOf(fromState), toStates, allowLoops, condition.structure())
    is JoinTransition -> TransitionStructure("JOIN", fromStates, listOf(toState), allowLoops, condition.structure())
}

/**
 * Returns the structure of this condition.
 */
fun Condition.structure(): ConditionStructure = when (this) {
    is EventCondition -> ConditionStructure(conditionType, listOf(eventDomain, eventValue), emptyList())
    is EvalCondition -> ConditionStructure(conditionType, listOf(CodeShape.of(code)), emptyList())
    is ValueCondition -> ConditionStructure(conditionType, listOf(boolVariable), emptyList())
    is CompositeCondition -> ConditionStructure(conditionType, emptyList(), clauses.map { it.structure() })
    else -> error("Unknown kind of condition $this")
}
