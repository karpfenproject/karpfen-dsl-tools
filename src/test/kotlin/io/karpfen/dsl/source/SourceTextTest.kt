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
package io.karpfen.dsl.source

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Tests how [SourceText] turns offsets into one-based lines and columns, and the basic operations of
 * [SourceRange].
 */
class SourceTextTest {

    /**
     * `\n`, `\r\n` and a lone `\r` each end a line; a `\r\n` counts once.
     */
    @Test
    fun `every kind of line break ends a line`() {
        val text = SourceText("a\nbc\r\nd\re")

        assertEquals(4, text.lineCount)
        assertEquals(SourcePosition(0, 1, 1), text.positionAt(0))
        assertEquals(SourcePosition(1, 1, 2), text.positionAt(1))
        assertEquals(SourcePosition(3, 2, 2), text.positionAt(3))
        assertEquals(SourcePosition(5, 2, 4), text.positionAt(5))
        assertEquals(SourcePosition(6, 3, 1), text.positionAt(6))
        assertEquals(SourcePosition(8, 4, 1), text.positionAt(8))
        assertEquals(SourcePosition(9, 4, 2), text.positionAt(9))
        assertEquals(listOf(0, 2, 6, 8), (1..4).map { text.lineStartOffset(it) })
    }

    /**
     * The end of the text is a position, at the start of a new line after a final line break.
     */
    @Test
    fun `the end of the text is a position`() {
        assertEquals(SourcePosition(0, 1, 1), SourceText("").positionAt(0))
        assertEquals(SourcePosition(3, 2, 1), SourceText("ab\n").positionAt(3))
    }

    /**
     * Offsets outside the text are rejected.
     */
    @Test
    fun `offsets outside the text are rejected`() {
        val text = SourceText("abc")

        assertFailsWith<IllegalArgumentException> { text.positionAt(-1) }
        assertFailsWith<IllegalArgumentException> { text.positionAt(4) }
        assertFailsWith<IllegalArgumentException> { text.rangeOf(2, 1) }
    }

    /**
     * Columns count UTF-16 chars, like offsets, so a character outside the Basic Multilingual Plane takes two.
     */
    @Test
    fun `columns count chars`() {
        val text = SourceText("😀x")

        assertEquals(SourcePosition(2, 1, 3), text.positionAt(2))
    }

    /**
     * A range covers its start but not its end and knows its text.
     */
    @Test
    fun `a range covers its start but not its end`() {
        val source = SourceText("hello world")
        val range = source.rangeOf(6, 11)

        assertEquals("world", range.textIn(source.text))
        assertEquals(5, range.length)
        assertTrue(range.contains(6))
        assertFalse(range.contains(11))
        assertTrue(range.encloses(source.rangeOf(7, 11)))
        assertFalse(range.encloses(source.rangeOf(5, 8)))
    }
}
