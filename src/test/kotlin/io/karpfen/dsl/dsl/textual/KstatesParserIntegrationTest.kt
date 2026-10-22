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

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Converts the cleaning robot example state machine file.
 */
class KstatesParserIntegrationTest {

    /**
     * The state machine holds its states, transitions and macros.
     */
    @Test
    fun testKstatesParserIntegration() {
        val filePath = "example/statemachine_full_example/cleaning_robot.kstates"
        val stateMachine = KstatesDSLConverter.parseKstatesFile(filePath)

        assertEquals("Robot", stateMachine.attachedToClass)
        assertEquals(3, stateMachine.states.size)
        assertTrue(stateMachine.states.any { it.name == "ready" && it.isInitial })
        assertEquals(7, stateMachine.transitions.size)
        assertEquals(7, stateMachine.macros.size)
    }
}
