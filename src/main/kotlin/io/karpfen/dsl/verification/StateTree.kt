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

import io.karpfen.dsl.states.InitialStates
import io.karpfen.dsl.states.State
import io.karpfen.dsl.states.StateMachine

/**
 * The nesting of the states of a state machine whose state names are unique, and the stacks of active states the
 * runtime builds from it.
 *
 * A stack lists a state and all its outer states, the outermost first. The runtime enters a state by the stack of its
 * outer states and itself, without entering an inner state, except for a container state of a flattened composition,
 * which it enters along the start path of its fragment, see [InitialStates.enteredBelow]; the start enters the start
 * path of the state machine, see [InitialStates.startPath].
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
    val initial: State? = InitialStates.startPath(machine).lastOrNull()

    /** Returns the state named [name], or null. */
    fun stateNamed(name: String): State? = byName[name]

    /** Returns the outer state of [state], or null for a state at the top. */
    fun outerOf(state: State): State? = outerOf[state.name]

    /** Returns the stack of [state]: its outer states, the outermost first, and the state itself. */
    fun stackOf(state: State): List<State> = generateSequence(state) { outerOf(it) }.toList().reversed()

    /**
     * Returns the innermost state a branch rests on after entering [state]: [state] itself, or for a container state
     * with inner states the innermost state of the start path of its fragment.
     */
    fun entered(state: State): State =
        state.takeIf { it.isContainer }?.let { InitialStates.enteredBelow(it).lastOrNull() } ?: state

    /** Returns [state] and all the states nested in it. */
    fun withInnerStates(state: State): List<State> = listOf(state) + state.innerStates.flatMap { withInnerStates(it) }

    /** Returns the states of [states] and their inner states, each state before its inner states. */
    private fun flattened(states: List<State>): List<State> = states.flatMap { listOf(it) + flattened(it.innerStates) }

}
