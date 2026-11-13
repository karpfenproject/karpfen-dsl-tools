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
import io.karpfen.dsl.parser.ConditionClauses
import io.karpfen.dsl.reader.ReadingSession
import io.karpfen.dsl.states.TransitionLike
import io.karpfen.dsl.states.conditions.EventCondition

/**
 * Reports the `EVENT` clauses of a `CONDITION` block that the runtime does not read as written: every `EVENT` clause
 * after the first one is an error, since the runtime matches only the first and ignores the others. A first `EVENT`
 * clause that other clauses precede is a warning: the runtime finds it in any place and lets every other clause read
 * its event, but written first it shows that. Each is reported at its event name. The check only reports.
 *
 * @param session The read the transitions belong to; it holds their locations and receives the problems.
 */
internal class EventClauseCheck(private val session: ReadingSession) {

    /**
     * Checks the conditions of [transitions].
     */
    fun check(transitions: List<TransitionLike>) {
        for (transition in transitions) {
            val clauses = ConditionClauses.clausesOf(transition.condition)
            val events = clauses.withIndex().filter { it.value is EventCondition }
            events.firstOrNull()?.takeIf { it.index > 0 }?.let { (_, clause) ->
                session.report(
                    DiagnosticCode.EVENT_CLAUSE_NOT_FIRST,
                    session.problemRangeOf(clause),
                    "Write the EVENT clause first; the runtime finds it in any place, and every other clause of the " +
                        "condition reads its event"
                )
            }
            events.drop(1).forEach { (_, clause) ->
                session.report(
                    DiagnosticCode.SEVERAL_EVENT_CLAUSES,
                    session.problemRangeOf(clause),
                    "The runtime matches only the first EVENT clause of a condition and ignores this one; split the " +
                        "transition into one per event"
                )
            }
        }
    }
}
