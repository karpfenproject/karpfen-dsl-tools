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

/**
 * Converts the code point indices of an ANTLR stream made by `CharStreams.fromString` into the UTF-16 char
 * offsets of the text it was made from.
 *
 * The two differ only behind a character outside the Basic Multilingual Plane, which is one code point but two
 * chars; for a text without such characters the conversion is the identity and needs no table.
 *
 * @param text The text the stream was made from.
 */
internal class CodePointOffsets(text: String) {

    /** The length of the text in chars. */
    private val textLength = text.length

    /** The char offset of every code point and the length of the text last; null if every code point is one char. */
    private val charOffsets: IntArray? = if (text.any { it.isSurrogate() }) charOffsetTable(text) else null

    /** The number of code points of the text. */
    val codePointCount: Int = charOffsets?.let { it.size - 1 } ?: textLength

    /**
     * Returns the char offset of the code point at [codePointIndex]; indices outside the text are clamped to
     * its start or end.
     */
    fun charOffsetOf(codePointIndex: Int): Int {
        val clamped = codePointIndex.coerceIn(0, codePointCount)
        return charOffsets?.get(clamped) ?: clamped
    }

    /** Builds the table of char offsets. */
    private companion object {

        /**
         * Builds the table of the char offset of every code point of [text], with the length of the text as
         * the last entry.
         */
        fun charOffsetTable(text: String): IntArray {
            val offsets = IntArray(text.codePointCount(0, text.length) + 1)
            var charOffset = 0
            var codePointIndex = 0
            while (charOffset < text.length) {
                offsets[codePointIndex] = charOffset
                charOffset += Character.charCount(text.codePointAt(charOffset))
                codePointIndex += 1
            }
            offsets[codePointIndex] = text.length
            return offsets
        }
    }
}
