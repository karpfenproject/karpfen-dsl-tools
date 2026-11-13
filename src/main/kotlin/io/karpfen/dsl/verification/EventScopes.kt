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
package io.karpfen.dsl.verification

import io.karpfen.dsl.parser.ConditionClauses
import io.karpfen.dsl.states.JoinTransition
import io.karpfen.dsl.states.SplitTransition
import io.karpfen.dsl.states.State
import io.karpfen.dsl.states.StateMachine
import io.karpfen.dsl.states.Transition
import io.karpfen.dsl.states.TransitionLike
import io.karpfen.dsl.states.conditions.EventCondition

/**
 * The events that can be in scope where code of a state machine runs, by the name of the event.
 *
 * A transition or a split whose condition has an `EVENT` clause brings the event it matched into scope at its target,
 * where it stays while the target is active. Code reads the event of the innermost active state that has one. So the
 * code of a state may see the event of a transition into the state, into one of its outer states, or into one of its
 * inner states, which enters the state on the way. A join, a timeout and a history bring no event.
 *
 * The conditions of a transition see the event of their own `EVENT` clause; without one they see the event in scope
 * where the transition starts, and the conditions of a join see none.
 *
 * @param machine The state machine; its state names are unique.
 * @param tree The nesting of its states.
 */
internal class EventScopes(private val machine: StateMachine, private val tree: StateTree) {

    /** The names of the events each transition and split brings into scope, by the name of each target. */
    private val eventsByTarget: Map<String, Set<String>> by lazy {
        machine.transitions.flatMap { transition ->
            val event = eventOf(transition) ?: return@flatMap emptyList()
            when (transition) {
                is Transition -> listOf(transition.toState to event)
                is SplitTransition -> transition.toStates.map { it to event }
                is JoinTransition -> emptyList()
            }
        }.groupBy({ it.first }, { it.second }).mapValues { it.value.toSet() }
    }

    /** Returns the names of the events that can be in scope in the actions of [state]. */
    fun ofState(state: State): Set<String> =
        (tree.stackOf(state) + tree.withInnerStates(state)).flatMap { eventsByTarget[it.name].orEmpty() }.toSet()

    /** Returns the names of the events the conditions of [transition] can read. */
    fun ofCondition(transition: TransitionLike): Set<String> {
        eventOf(transition)?.let { return setOf(it) }
        val source = when (transition) {
            is Transition -> transition.fromState
            is SplitTransition -> transition.fromState
            is JoinTransition -> return emptySet()
        }
        return tree.stateNamed(source)?.let { ofState(it) }.orEmpty()
    }

    /** Returns the name of the event of the `EVENT` clause of [transition], the first one, or null without one. */
    fun eventOf(transition: TransitionLike): String? =
        ConditionClauses.clausesOf(transition.condition).filterIsInstance<EventCondition>().firstOrNull()?.eventValue
}
