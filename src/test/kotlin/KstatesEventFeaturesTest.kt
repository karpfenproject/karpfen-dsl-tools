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
import dsl.textual.KstatesDSLConverter
import states.State
import states.actions.ActionRule
import states.actions.InScopeBlock
import states.conditions.CompositeCondition
import states.conditions.ConditionType
import states.conditions.EventCondition
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Tests for the kstates additions: multi-clause CONDITION blocks and IF IN SCOPE action blocks.
 */
class KstatesEventFeaturesTest {

    // Deliberately uses no $(...) so the test string needs no escaping; the guard body is captured raw.
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

    @Test
    fun `multi-clause condition becomes a composite with the event clause first`() {
        val machine = KstatesDSLConverter.parseKstatesString(machineText)
        val transition = machine.transitions.filterIsInstance<states.Transition>().first { it.fromState == "idle" }
        val condition = transition.condition
        assertEquals(ConditionType.COMPOSITE, condition.conditionType)
        val composite = condition as CompositeCondition
        assertEquals(2, composite.clauses.size)
        assertTrue(composite.clauses.first() is EventCondition, "event clause must come first")
    }

    @Test
    fun `IF IN SCOPE parses into an InScopeBlock alongside plain rules`() {
        val machine = KstatesDSLConverter.parseKstatesString(machineText)
        val boost = findState(machine.states, "boost")!!
        val items = boost.onDo.items

        val inScope = items.filterIsInstance<InScopeBlock>().single()
        assertEquals(listOf("event->factor"), inScope.paths)
        assertTrue(inScope.body.items.any { it is ActionRule })

        // The APPEND after the IF IN SCOPE block is a top-level rule in the same block.
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
