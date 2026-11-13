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

import io.karpfen.dsl.states.State
import io.karpfen.dsl.states.StateMachine

/**
 * The nesting of the states of a state machine whose state names are unique, and the stacks of active states the
 * runtime builds from it.
 *
 * A stack lists a state and all its outer states, the outermost first. The runtime enters a state by the stack of its
 * outer states and itself, without entering an inner state; only the start enters the initial state of each level: the
 * first state marked `INITIAL`, otherwise the first state, down to a state without inner states.
 *
 * @param machine The state machine.
 */
internal class StateTree(machine: StateMachine) {

    /** Every state, each before its inner states. */
    val states: List<State> = flattened(machine.states)

    /** The states by name. */
    private val byName: Map<String, State> = states.reversed().associateBy { it.name }

    /** The outer state of each inner state, by the name of the inner state. */
    private val outerOf: Map<String, State> = states.flatMap { outer -> outer.innerStates.map { it.name to outer } }.toMap()

    /** The innermost state of the stack the runtime starts with, or null for a machine without states. */
    val initial: State? = initialOf(machine.states)

    /** Returns the state named [name], or null. */
    fun stateNamed(name: String): State? = byName[name]

    /** Returns the outer state of [state], or null for a state at the top. */
    fun outerOf(state: State): State? = outerOf[state.name]

    /** Returns the stack of [state]: its outer states, the outermost first, and the state itself. */
    fun stackOf(state: State): List<State> = generateSequence(state) { outerOf(it) }.toList().reversed()

    /** Returns [state] and all the states nested in it. */
    fun withInnerStates(state: State): List<State> = listOf(state) + state.innerStates.flatMap { withInnerStates(it) }

    /** Returns the states of [states] and their inner states, each state before its inner states. */
    private fun flattened(states: List<State>): List<State> = states.flatMap { listOf(it) + flattened(it.innerStates) }

    /** Returns the innermost initial state among [states], as the runtime finds it. */
    private fun initialOf(states: List<State>): State? {
        val initial = states.firstOrNull { it.isInitial } ?: states.firstOrNull() ?: return null
        return initialOf(initial.innerStates) ?: initial
    }
}
