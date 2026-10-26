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

/**
 * A text together with the index of its line starts, which turns offsets into [SourcePosition]s and
 * [SourceRange]s.
 *
 * A line ends with `\n`, `\r\n` or a lone `\r`. The index is built once; the object is immutable and may be
 * shared between threads.
 *
 * @property text The indexed text.
 */
class SourceText(val text: String) {

    /** The offset of the first char of every line. */
    private val lineStarts: IntArray = lineStartsOf(text)

    /** The number of lines; an empty text and a text without a line break have one line. */
    val lineCount: Int
        get() = lineStarts.size

    /**
     * Returns the position of the char at [offset]; [offset] may also be the length of the text.
     *
     * @throws IllegalArgumentException If [offset] lies outside `0..text.length`.
     */
    fun positionAt(offset: Int): SourcePosition {
        require(offset in 0..text.length) { "Offset $offset lies outside 0..${text.length}" }
        val lineIndex = lineIndexOf(offset)
        return SourcePosition(offset, lineIndex + 1, offset - lineStarts[lineIndex] + 1)
    }

    /**
     * Returns the range from [startOffset] inclusive to [endOffset] exclusive.
     *
     * @throws IllegalArgumentException If an offset lies outside `0..text.length` or [endOffset] lies before
     * [startOffset].
     */
    fun rangeOf(startOffset: Int, endOffset: Int): SourceRange =
        SourceRange(positionAt(startOffset), positionAt(endOffset))

    /**
     * Returns the offset of the first char of the one-based [line].
     *
     * @throws IllegalArgumentException If [line] lies outside `1..lineCount`.
     */
    fun lineStartOffset(line: Int): Int {
        require(line in 1..lineCount) { "Line $line lies outside 1..$lineCount" }
        return lineStarts[line - 1]
    }

    /**
     * Finds the index of the last line start at or before [offset] by binary search.
     */
    private fun lineIndexOf(offset: Int): Int {
        var low = 0
        var high = lineStarts.size - 1
        while (low < high) {
            val middle = (low + high + 1) ushr 1
            if (lineStarts[middle] <= offset) {
                low = middle
            } else {
                high = middle - 1
            }
        }
        return low
    }

    /** Indexes texts. */
    private companion object {

        /**
         * Collects the offset of the first char of every line of [text].
         */
        fun lineStartsOf(text: String): IntArray {
            val starts = mutableListOf(0)
            var index = 0
            while (index < text.length) {
                val char = text[index]
                if (char == '\r' && index + 1 < text.length && text[index + 1] == '\n') {
                    index += 1
                }
                if (char == '\r' || char == '\n') {
                    starts.add(index + 1)
                }
                index += 1
            }
            return starts.toIntArray()
        }
    }
}
