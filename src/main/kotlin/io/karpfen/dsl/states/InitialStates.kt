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
package io.karpfen.dsl.states

/**
 * The initial states of a state machine: the rule that a text marks exactly one state, at any depth, as `INITIAL`,
 * and the start path, the states a state machine starts in.
 *
 * The start path below a list of sibling states is found level by level. At each level it takes the state that is
 * marked `INITIAL` or that holds the mark somewhere below it without passing through a container state, otherwise the
 * first state of the level, and goes on below the chosen state the same way while it has inner states. A marked inner
 * state therefore starts the state machine in it and in every state around it, wherever the states around it stand in
 * their levels.
 *
 * In a flattened composition, see [io.karpfen.dsl.composition.StateMachineFlattener], the inner states of a container
 * state are the copy of its fragment, so inside a container the mark of that fragment counts. The marks of fragments
 * further down lie behind another container state and are not seen from above. Entering a container state is the same
 * as starting its fragment: the states entered are the start path below the container, see [enteredBelow].
 *
 * For a state machine with states `off` and `on`, where `on` holds the marked state `full`, the start path is `on`,
 * `full`, although `off` is the first state of its level.
 */
object InitialStates {

    /** Returns the states a state machine with [machine] starts in, the outermost first; empty without states. */
    fun startPath(machine: StateMachine): List<State> = pathBelow(machine.states)

    /**
     * Returns the states entered below the container state [container] when it is entered, the outermost first: the
     * start path of its fragment. Empty for a state without inner states, such as a container state of a composition
     * that is not flattened.
     */
    fun enteredBelow(container: State): List<State> = pathBelow(container.innerStates)

    /**
     * Returns the start path below the sibling states [states], the outermost first: the state chosen at their level by
     * [initialAmong] and, while the chosen state has inner states, the path below it.
     */
    fun pathBelow(states: List<State>): List<State> {
        val chosen = initialAmong(states) ?: return emptyList()
        return listOf(chosen) + pathBelow(chosen.innerStates)
    }

    /**
     * Returns the state of the sibling states [states] that the start path takes: the first one that is marked
     * `INITIAL` or holds the mark below it without passing through a container state, otherwise the first one; null if
     * [states] is empty.
     */
    fun initialAmong(states: List<State>): State? = states.firstOrNull { it.isInitial || holdsMark(it) } ?: states.firstOrNull()

    /** Returns true if a state below [state] is marked `INITIAL` and no container state, [state] included, lies between. */
    private fun holdsMark(state: State): Boolean =
        !state.isContainer && state.innerStates.any { it.isInitial || holdsMark(it) }

    /**
     * Returns the states marked `INITIAL` among [states] and all their nested states, each state before its nested
     * ones.
     */
    internal fun of(states: List<State>): List<State> =
        states.flatMap { state -> listOfNotNull(state.takeIf { it.isInitial }) + of(state.innerStates) }

    /**
     * Returns the message for a state machine that marks [found] states instead of exactly one as `INITIAL`.
     */
    internal fun countMessage(found: Int): String = "Exactly one state must be marked as INITIAL, but found $found."
}
