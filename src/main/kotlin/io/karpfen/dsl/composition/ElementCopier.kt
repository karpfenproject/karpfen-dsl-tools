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
package io.karpfen.dsl.composition

import io.karpfen.dsl.states.JoinTransition
import io.karpfen.dsl.states.SplitTransition
import io.karpfen.dsl.states.State
import io.karpfen.dsl.states.StateMachine
import io.karpfen.dsl.states.Transition
import io.karpfen.dsl.states.TransitionLike
import io.karpfen.dsl.states.actions.ActionBlock
import io.karpfen.dsl.states.actions.ActionItem
import io.karpfen.dsl.states.actions.ActionRightSide
import io.karpfen.dsl.states.actions.ActionRule
import io.karpfen.dsl.states.actions.AttachClause
import io.karpfen.dsl.states.actions.EvalActionRightSide
import io.karpfen.dsl.states.actions.InScopeBlock
import io.karpfen.dsl.states.actions.MacroActionRightSide
import io.karpfen.dsl.states.actions.ValueActionRightSide
import io.karpfen.dsl.states.actions.WithBlock
import io.karpfen.dsl.states.conditions.CompositeCondition
import io.karpfen.dsl.states.conditions.Condition
import io.karpfen.dsl.states.conditions.DoneCondition
import io.karpfen.dsl.states.conditions.EvalCondition
import io.karpfen.dsl.states.conditions.EventCondition
import io.karpfen.dsl.states.conditions.ValueCondition
import java.util.IdentityHashMap

/**
 * Copies the elements of one state machine of a composition into a flattened state machine and records for each copy
 * the element it copies, so that a problem found in the copy can be marked in the text of the original.
 *
 * Every state, action item, right side, transition and condition clause is copied, so that two copies of a fragment
 * share nothing. The names of states are mapped by [qualified], which puts the names of a fragment below the container
 * state that uses it.
 *
 * @param machine The state machine whose elements are copied.
 * @param qualified Maps a name of a state of [machine] to its name in the flattened state machine.
 * @param sources Receives the original of each copy, by identity.
 */
internal class ElementCopier(
    private val machine: StateMachine,
    private val qualified: (String) -> String,
    private val sources: IdentityHashMap<Any, ElementSource>
) {

    /**
     * Returns a copy of [state] without its inner states, renamed by [qualified], inside [outerState]; the target of
     * its timeout is renamed the same way.
     */
    fun state(state: State, outerState: State?): State = recorded(
        state,
        State(
            name = qualified(state.name),
            onEntry = block(state.onEntry),
            onDo = block(state.onDo),
            innerStates = mutableListOf(),
            outerState = outerState,
            isInitial = state.isInitial,
            entryBreakpoint = state.entryBreakpoint,
            exitBreakpoint = state.exitBreakpoint,
            history = state.history,
            delay = state.delay,
            timeout = state.timeout,
            timeoutTo = state.timeoutTo?.let(qualified),
            fragment = state.fragment,
            isFinal = state.isFinal
        )
    )

    /** Returns a copy of [transition] whose states are renamed by [qualified]. */
    fun transition(transition: TransitionLike): TransitionLike = recorded(
        transition,
        when (transition) {
            is Transition -> transition.copy(
                fromState = qualified(transition.fromState),
                toState = qualified(transition.toState),
                condition = condition(transition.condition)
            )
            is SplitTransition -> transition.copy(
                fromState = qualified(transition.fromState),
                toStates = transition.toStates.map(qualified),
                condition = condition(transition.condition)
            )
            is JoinTransition -> transition.copy(
                fromStates = transition.fromStates.map(qualified),
                toState = qualified(transition.toState),
                condition = condition(transition.condition)
            )
        }
    )

    /** Returns a copy of [block] with copies of its items. */
    private fun block(block: ActionBlock): ActionBlock =
        recorded(block, ActionBlock(block.items.map { item(it) }.toMutableList()))

    /** Returns a copy of [item]: a rule with copies of its values, or a block with a copy of its body. */
    private fun item(item: ActionItem): ActionItem = recorded(
        item,
        when (item) {
            is ActionRule -> item.copy(
                rightSide = item.rightSide?.let { value(it) },
                attach = item.attach?.let { recorded(it, AttachClause(value(it.stateMachineName))) }
            )
            is InScopeBlock -> item.copy(body = block(item.body))
            is WithBlock -> item.copy(macro = value(item.macro) as MacroActionRightSide, body = block(item.body))
        }
    )

    /** Returns a copy of the right side or `ATTACH` value [value]; a kind this class does not know stays shared. */
    private fun value(value: ActionRightSide): ActionRightSide = recorded(
        value,
        when (value) {
            is MacroActionRightSide -> value.copy()
            is EvalActionRightSide -> value.copy()
            is ValueActionRightSide -> value.copy()
            else -> value
        }
    )

    /** Returns a copy of [condition] with copies of its clauses; a kind this class does not know stays shared. */
    private fun condition(condition: Condition): Condition = recorded(
        condition,
        when (condition) {
            is CompositeCondition -> CompositeCondition(condition.clauses.map { condition(it) })
            is EventCondition -> condition.copy()
            is EvalCondition -> condition.copy()
            is ValueCondition -> ValueCondition(condition.boolVariable)
            is DoneCondition -> DoneCondition()
            else -> condition
        }
    )

    /** Records that [copy] copies [original] of the state machine and returns [copy]. */
    private fun <T : Any> recorded(original: Any, copy: T): T {
        if (copy !== original) {
            sources[copy] = ElementSource(original, machine)
        }
        return copy
    }
}
