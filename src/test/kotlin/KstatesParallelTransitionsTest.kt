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
import states.JoinTransition
import states.SplitTransition
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests that SPLIT (1-to-N) and JOIN (N-to-1) transitions parse into the right transition types.
 */
class KstatesParallelTransitionsTest {

    private val machineText = """
        STATEMACHINE ATTACHED TO "Thing" {
            STATES {
                INITIAL STATE "init" { }
                STATE "a" { }
                STATE "b" { }
                STATE "done" { }
            }
            TRANSITIONS {
                SPLIT "init" -> "a", "b" { }
                JOIN "a", "b" -> "done" { }
            }
        }
    """.trimIndent()

    @Test
    fun `split and join transitions parse into the right types`() {
        val sm = KstatesDSLConverter.parseKstatesString(machineText)

        val split = sm.transitions.filterIsInstance<SplitTransition>().single()
        assertEquals("init", split.fromState)
        assertEquals(listOf("a", "b"), split.toStates)

        val join = sm.transitions.filterIsInstance<JoinTransition>().single()
        assertEquals(listOf("a", "b"), join.fromStates)
        assertEquals("done", join.toState)
    }
}
