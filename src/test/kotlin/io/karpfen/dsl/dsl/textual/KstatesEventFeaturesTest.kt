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
package io.karpfen.dsl.dsl.textual

import io.karpfen.dsl.states.State
import io.karpfen.dsl.states.Transition
import io.karpfen.dsl.states.actions.ActionRule
import io.karpfen.dsl.states.actions.InScopeBlock
import io.karpfen.dsl.states.conditions.CompositeCondition
import io.karpfen.dsl.states.conditions.ConditionType
import io.karpfen.dsl.states.conditions.EventCondition
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Tests for the kstates additions: multi-clause CONDITION blocks and IF IN SCOPE action blocks.
 */
class KstatesEventFeaturesTest {

    /**
     * A state machine whose guard uses no `$(...)` paths, so the text needs no escaping and the guard body
     * is captured raw.
     */
    private val machineText = """
        STATEMACHINE ATTACHED TO "Robot" {
            STATES {
                INITIAL STATE "idle" { }
                STATE "boost" {
                    DO {
                        IF IN SCOPE("event->factor") {
                            SET("d_closest_wall", "1.0")
                        }
                        APPEND("log", "boosting")
                    }
                }
            }
            TRANSITIONS {
                TRANSITION "idle" -> "boost" {
                    CONDITION {
                        EVENT("public", "setSpeed")
                        EVAL { return 1.0 > 0.0 }
                    }
                }
            }
        }
    """.trimIndent()

    /**
     * Two clauses become a composite condition whose first clause is the event.
     */
    @Test
    fun `multi-clause condition becomes a composite with the event clause first`() {
        val machine = KstatesDSLConverter.parseKstatesString(machineText)
        val transition = machine.transitions.filterIsInstance<Transition>().first { it.fromState == "idle" }
        val condition = transition.condition
        assertEquals(ConditionType.COMPOSITE, condition.conditionType)
        val composite = condition as CompositeCondition
        assertEquals(2, composite.clauses.size)
        assertTrue(composite.clauses.first() is EventCondition, "event clause must come first")
    }

    /**
     * The IF IN SCOPE block and the APPEND rule after it are items of the same DO block.
     */
    @Test
    fun `IF IN SCOPE parses into an InScopeBlock alongside plain rules`() {
        val machine = KstatesDSLConverter.parseKstatesString(machineText)
        val boost = findState(machine.states, "boost")!!
        val items = boost.onDo.items

        val inScope = items.filterIsInstance<InScopeBlock>().single()
        assertEquals(listOf("event->factor"), inScope.paths)
        assertTrue(inScope.body.items.any { it is ActionRule })

        assertTrue(items.any { it is ActionRule })
    }

    private fun findState(states: List<State>, name: String): State? {
        for (state in states) {
            if (state.name == name) return state
            findState(state.innerStates, name)?.let { return it }
        }
        return null
    }
}
