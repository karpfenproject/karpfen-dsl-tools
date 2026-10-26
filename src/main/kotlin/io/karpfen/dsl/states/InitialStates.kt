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
 * The rule that a state machine marks exactly one state, at any depth, as `INITIAL`, which the converter enforces
 * and the reader reports.
 */
internal object InitialStates {

    /**
     * Returns the states marked `INITIAL` among [states] and all their nested states, each state before its nested
     * ones.
     */
    fun of(states: List<State>): List<State> =
        states.flatMap { state -> listOfNotNull(state.takeIf { it.isInitial }) + of(state.innerStates) }

    /**
     * Returns the message for a state machine that marks [found] states instead of exactly one as `INITIAL`.
     */
    fun countMessage(found: Int): String = "Exactly one state must be marked as INITIAL, but found $found."
}
