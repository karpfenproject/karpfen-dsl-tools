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

import org.slf4j.event.Level
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Tests that [LoggingSyntaxErrorListener] turns syntax errors into warnings.
 */
class LoggingSyntaxErrorListenerTest {

    /**
     * A reported error is logged once, at warn level, with language, position and ANTLR message.
     */
    @Test
    fun `a syntax error becomes one warning with language, position and message`() {
        val logger = RecordingLogger()
        val listener = LoggingSyntaxErrorListener(logger, "KMeta")

        listener.syntaxError(null, null, 3, 7, "missing '}' at '<EOF>'", null)

        assertEquals(
            listOf(RecordingLogger.Record(Level.WARN, "KMeta syntax error at line 3:7 missing '}' at '<EOF>'")),
            logger.records
        )
    }

    /**
     * A file parser built with the listener logs the errors of a real parse as warnings only.
     */
    @Test
    fun `the errors of a parse are logged as warnings`() {
        val logger = RecordingLogger()
        val parser = KmetaFileParser(LoggingSyntaxErrorListener(logger, "KMeta"))

        parser.parseString("type \"A\" \"first\" { prop(\"x\", \"number\") ) }")

        assertTrue(logger.records.isNotEmpty())
        assertTrue(logger.records.all { it.level == Level.WARN })
        assertTrue(logger.records.all { it.message.startsWith("KMeta syntax error at line 1:") })
    }
}
