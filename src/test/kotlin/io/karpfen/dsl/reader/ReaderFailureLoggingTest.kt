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
package io.karpfen.dsl.reader

import io.karpfen.dsl.diagnostics.DiagnosticCode
import io.karpfen.dsl.parser.RecordingLogger
import io.karpfen.dsl.parser.StringLiteralSyntax
import org.slf4j.event.Level
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * Tests that a failing read leaves a trace in the log besides its diagnostic.
 */
class ReaderFailureLoggingTest {

    /**
     * A defect of the reader becomes one error in the log and a diagnostic.
     */
    @Test
    fun `a defect of the reader is logged once as an error`() {
        val logger = RecordingLogger()

        val result = ReadingSession.read<Any>(LexerInput.asWritten("text"), StringLiteralSyntax.KSTATES, logger) { throw IllegalStateException("defect") }

        assertEquals(listOf(Level.ERROR), logger.records.map { it.level })
        assertEquals(listOf(DiagnosticCode.READER_FAILURE), result.diagnostics.map { it.code })
        assertNull(result.model)
    }

    /**
     * Nesting deeper than the parser can recurse is a limit, not a defect: one warning in the log and a diagnostic.
     */
    @Test
    fun `nesting beyond the parser's reach is logged once as a warning`() {
        val logger = RecordingLogger()

        val result = ReadingSession.read<Any>(LexerInput.asWritten("text"), StringLiteralSyntax.KSTATES, logger) { throw StackOverflowError() }

        assertEquals(listOf(Level.WARN), logger.records.map { it.level })
        assertEquals(listOf(DiagnosticCode.READER_FAILURE), result.diagnostics.map { it.code })
    }

    /**
     * A read that does not fail logs nothing.
     */
    @Test
    fun `a successful read logs nothing`() {
        val logger = RecordingLogger()

        ReadingSession.read(LexerInput.asWritten("text"), StringLiteralSyntax.KSTATES, logger) { "model" }

        assertEquals(emptyList(), logger.records)
    }
}
