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
package io.karpfen.dsl.printer

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

/**
 * Checks the canonical layout that [CodeWriter] produces.
 */
class CodeWriterTest {

    /**
     * Blocks indent their content by four spaces, close an empty block on the header line, and a document
     * ends with a line break.
     */
    @Test
    fun `documents nest blocks by four spaces`() {
        val text = CodeWriter.document { writer ->
            writer.block("outer") {
                writer.line("statement")
                writer.block("empty") { }
            }
        }
        assertEquals("outer {\n    statement\n    empty { }\n}\n", text)
    }

    /**
     * Paragraphs are separated by an empty line without indentation.
     */
    @Test
    fun `paragraphs are separated by a blank line`() {
        val text = CodeWriter.document { writer ->
            writer.block("section") { writer.paragraphs(listOf("a", "b")) { writer.line(it) } }
        }
        assertEquals("section {\n    a\n\n    b\n}\n", text)
    }

    /**
     * A fragment leaves its first line unindented, indents the others fully and ends without a line break.
     */
    @Test
    fun `fragments start without indentation`() {
        val text = CodeWriter.fragment(1) { writer -> writer.block("block") { writer.line("statement") } }
        assertEquals("block {\n        statement\n    }", text)
        assertFailsWith<IllegalArgumentException> { CodeWriter.fragment(-1) { } }
    }

    /**
     * A verbatim block indents only the first line of its body and closes at the current level.
     */
    @Test
    fun `verbatim blocks keep the indentation of their body`() {
        val text = CodeWriter.document { writer ->
            writer.block("outer") { writer.line("call(" + writer.verbatimBlock("CODE", "first\n  second") + ")") }
        }
        assertEquals("outer {\n    call(CODE {\n        first\n  second\n    })\n}\n", text)
    }
}
