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
package io.karpfen.dsl.reader.kstates

import io.karpfen.dsl.diagnostics.DiagnosticCode
import io.karpfen.dsl.reader.ReadingSession
import io.karpfen.dsl.source.SourceRange
import io.karpfen.dsl.states.State

/**
 * Checks that a state machine marks exactly one state, at any depth, as `INITIAL`, with the message of the
 * throwing converter.
 *
 * Several initial states are each reported at their name; no initial state at all is reported at the range
 * that stands for the states as a whole.
 *
 * @param session The read the states belong to; it holds their locations.
 */
internal class InitialStateCheck(private val session: ReadingSession) {

    /**
     * Checks [states] and their nested states; [statesRange] marks a missing initial state.
     */
    fun check(states: List<State>, statesRange: SourceRange) {
        val initialStates = initialStatesIn(states)
        val message = "Exactly one state must be marked as INITIAL, but found ${initialStates.size}."
        when (initialStates.size) {
            1 -> Unit
            0 -> session.report(DiagnosticCode.INITIAL_STATE_COUNT, statesRange, message)
            else -> initialStates.forEach {
                session.report(DiagnosticCode.INITIAL_STATE_COUNT, session.problemRangeOf(it), message)
            }
        }
    }

    private fun initialStatesIn(states: List<State>): List<State> =
        states.flatMap { state -> listOfNotNull(state.takeIf { it.isInitial }) + initialStatesIn(state.innerStates) }
}
