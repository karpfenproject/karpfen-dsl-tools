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
package io.karpfen.dsl.parser

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Tests that the file parsers report the syntax errors of both their lexer and their parser to the injected
 * error listener and recover from them, continuing with the rest of the input.
 */
class SyntaxErrorReportingTest {

    /**
     * A character the KMeta lexer does not know is reported by the lexer and skipped.
     */
    @Test
    fun `kmeta lexer errors are reported and the parse continues`() {
        val listener = RecordingErrorListener()

        val file = KmetaFileParser(listener).parseString(
            """
            type "A" "first" { prop("x", "number") }
            #
            type "B" "second" { prop("y", "number") }
            """.trimIndent()
        )

        assertTrue(listener.errors.any { it.fromLexer })
        assertEquals(listOf("A", "B"), file.types.map { it.name })
    }

    /**
     * A superfluous token is reported by the KMeta parser and dropped.
     */
    @Test
    fun `kmeta parser errors are reported and the parse continues`() {
        val listener = RecordingErrorListener()

        val file = KmetaFileParser(listener).parseString(
            """
            type "A" "first" { prop("x", "number") ) }
            type "B" "second" { prop("y", "number") }
            """.trimIndent()
        )

        assertTrue(listener.errors.any { !it.fromLexer })
        assertEquals(listOf("A", "B"), file.types.map { it.name })
        assertEquals(listOf("x"), file.types.first().props.map { it.key })
    }

    /**
     * Errors of the KModel lexer and parser are both reported and both statements survive.
     */
    @Test
    fun `kmodel errors are reported and the parse continues`() {
        val listener = RecordingErrorListener()

        val file = KmodelFileParser(listener).parseString(
            """
            make object "r":"Room" {
                prop("a") -> "1" #
                prop("b") -> "2" )
            }
            """.trimIndent()
        )

        assertTrue(listener.errors.any { it.fromLexer })
        assertTrue(listener.errors.any { !it.fromLexer })
        assertEquals(listOf("a", "b"), file.objects.single().props.map { it.key })
    }

    /**
     * A superfluous token is reported by the KStates parser and dropped.
     */
    @Test
    fun `kstates parser errors are reported and the parse continues`() {
        val listener = RecordingErrorListener()

        val file = KStatesFileParser(listener).parseString(
            """
            STATEMACHINE ATTACHED TO "Robot" {
                STATES {
                    INITIAL STATE "idle" { } )
                    STATE "busy" { }
                }
                TRANSITIONS { }
            }
            """.trimIndent()
        )

        assertTrue(listener.errors.isNotEmpty())
        assertEquals(listOf("idle", "busy"), file.states.map { it.name })
    }
}
