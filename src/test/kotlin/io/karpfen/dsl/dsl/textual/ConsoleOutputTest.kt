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

import org.slf4j.LoggerFactory
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Tests that the converters leave the console to the application: model dumps are logged at debug level
 * and syntax errors are logged as warnings instead of being printed.
 *
 * The test logging backend writes to standard output at info level and above, so standard error only
 * receives what is printed directly.
 */
class ConsoleOutputTest {

    /**
     * Initializes the logging backend before any console is captured.
     */
    @BeforeTest
    fun initializeLogging() {
        LoggerFactory.getILoggerFactory()
    }

    /**
     * Converting the example documents writes nothing to the console.
     */
    @Test
    fun `converting valid documents prints nothing`() {
        val console = captureConsole {
            val metamodel = KmetaDSLConverter.parseKmetaFile("example/metamodel_dsl_example.kmeta")
            KmodelDSLConverter.parseKmodelFile("example/model_dsl_example.kmodel", metamodel)
            KstatesDSLConverter.parseKstatesFile("example/statemachine_full_example/cleaning_robot.kstates")
        }

        assertEquals(ConsoleOutput("", ""), console)
    }

    /**
     * A syntax error is logged as a warning and never printed to standard error.
     */
    @Test
    fun `syntax errors are logged instead of printed`() {
        val console = captureConsole {
            KmetaDSLConverter.parseKmetaString("type \"A\" \"first\" { prop(\"x\", \"number\") ) }")
        }

        assertEquals("", console.err)
        assertTrue(console.out.contains("WARN"))
        assertTrue(console.out.contains("KMeta syntax error at line 1:"))
    }
}
