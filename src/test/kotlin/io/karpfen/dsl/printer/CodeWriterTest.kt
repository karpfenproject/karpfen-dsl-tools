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
     * A verbatim block puts its body one level deeper and closes at the current level; the lines after the first
     * keep their indentation relative to each other, and blank lines stay empty.
     */
    @Test
    fun `verbatim blocks keep the relative indentation of their body`() {
        val text = CodeWriter.document { writer ->
            writer.block("outer") { writer.line("call(" + writer.verbatimBlock("CODE", "first\n  second\n\n    third") + ")") }
        }
        assertEquals("outer {\n    call(CODE {\n        first\n        second\n\n          third\n    })\n}\n", text)
    }

    /**
     * The body of a verbatim block is laid out the same at every depth, wherever its lines stood before, and laying
     * out the result once more changes nothing.
     */
    @Test
    fun `verbatim blocks lay out their body independently of its former depth`() {
        val shallow = verbatimBlockAt(1, "if a:\nb = 1\nendif")
        val deep = verbatimBlockAt(1, "if a:\n                b = 1\n                endif")
        val again = verbatimBlockAt(1, shallow.substringAfter("CODE {\n").substringBefore("\n    }").trim())

        assertEquals("CODE {\n        if a:\n        b = 1\n        endif\n    }", shallow)
        assertEquals(shallow, deep)
        assertEquals(shallow, again)
        assertEquals("CODE {\n            if a:\n            b = 1\n            endif\n        }", verbatimBlockAt(2, "if a:\nb = 1\nendif"))
    }

    private fun verbatimBlockAt(level: Int, body: String): String {
        var block = ""
        CodeWriter.fragment(level) { writer -> block = writer.verbatimBlock("CODE", body) }
        return block
    }
}
