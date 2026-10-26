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

import io.karpfen.dsl.fixtures.Mutator
import io.karpfen.dsl.fixtures.TextLayout
import io.karpfen.dsl.fixtures.Vocabularies
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Tests that [LexerInput] trims a text exactly as [String.trimIndent] does and maps every char it keeps back to
 * the char it stems from.
 */
class LexerInputTest {

    /**
     * Texts with every kind of line break, indentation, blank line and whitespace trim as [String.trimIndent] trims
     * them.
     */
    @Test
    fun `the trimmed text is the one trimIndent returns`() {
        val texts = listOf(
            "", " ", "\n", "\n\n", "\r\n", "\r", "a", "  a\n  b\n", "\r\n  a\r\n    b\r\n", "\ta\n\t\tb",
            "a\rb\r\nc\nd", "  a\n\n  b", "  a\n \n  b", " \n  a\n   \n", " a\n b", "﻿a", "  \"x\\\n    y\"\n", "x\n  "
        )
        texts.forEach { assertEquals(it.trimIndent(), LexerInput.withIndentTrimmed(it).lexed, "<$it>") }
    }

    /**
     * The examples and thousands of variants of them, in every layout, trim as [String.trimIndent] trims them.
     */
    @Test
    fun `mutated examples trim as trimIndent trims them`() {
        val mutator = Mutator(Vocabularies.KMODEL)
        ReaderUnderTest.all().forEach { subject ->
            val texts = mutator.variantsOf(subject.path, subject.text, VARIANTS_PER_FILE).map { it.text } +
                TextLayout.entries.map { it.apply(subject.text) }
            texts.forEach { assertEquals(it.trimIndent(), LexerInput.withIndentTrimmed(it).lexed, "$subject") }
        }
    }

    /**
     * Every char of the trimmed text stems from the same char of the text, a line break from a line break, which
     * spans two chars for `\r\n`.
     */
    @Test
    fun `every kept char maps back to the char it stems from`() {
        val text = "\r\n  type \"A\"\r\n    \"d\\\r\n  x\" {\r\n\r\n  }\r\n  "
        val input = LexerInput.withIndentTrimmed(text)

        input.lexed.indices.forEach { offset ->
            val start = input.sourceStartOf(offset)
            val stemsFrom = text.substring(start, input.sourceEndOf(offset, offset + 1))
            val char = input.lexed[offset]
            val matches = if (char == '\n') stemsFrom in LINE_BREAKS else stemsFrom == char.toString()
            assertTrue(matches, "char $offset <$char> stems from <$stemsFrom>")
        }
        assertEquals(text.length, input.sourceStartOf(input.lexed.length))
    }

    /**
     * A text read as written maps every offset to itself.
     */
    @Test
    fun `a text read as written maps every offset to itself`() {
        val input = LexerInput.asWritten("  a\r\n  b")

        assertEquals("  a\r\n  b", input.lexed)
        assertEquals(listOf(0, 3, 8), listOf(0, 3, 8).map { input.sourceStartOf(it) })
        assertEquals(5, input.sourceEndOf(3, 5))
    }

    private companion object {

        /** The number of mutated variants of every example. */
        const val VARIANTS_PER_FILE = 100

        /** The ways a line break is written. */
        val LINE_BREAKS = setOf("\n", "\r\n", "\r")
    }
}
