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

import io.karpfen.dsl.source.SourceRange

/**
 * Checks the positions of a [SourceRange] against the text it was taken from, with lines and columns counted
 * independently of the code under test.
 */
object TextPositions {

    /**
     * Describes how [range] fails to be a range of [text]: an end beyond the text, an offset between the two chars
     * of a surrogate pair, or a line or column that does not match its offset; null if it is a proper range.
     */
    fun deviationOf(text: String, range: SourceRange): String? {
        if (range.end.offset > text.length) {
            return "the range $range leaves the text of length ${text.length}"
        }
        for (position in listOf(range.start, range.end)) {
            val offset = position.offset
            if (offset in 1 until text.length && text[offset - 1].isHighSurrogate() && text[offset].isLowSurrogate()) {
                return "offset $offset splits a surrogate pair"
            }
            val (line, column) = lineAndColumnOf(text, offset)
            if (position.line != line || position.column != column) {
                return "offset $offset is at $line:$column, not ${position.line}:${position.column}"
            }
        }
        return null
    }

    /**
     * Counts the one-based line and column of [offset] by walking [text]; `\r\n` counts as one line break.
     */
    private fun lineAndColumnOf(text: String, offset: Int): Pair<Int, Int> {
        var line = 1
        var lineStart = 0
        var index = 0
        while (index < offset) {
            val char = text[index]
            val isCrBeforeLf = char == '\r' && index + 1 < text.length && text[index + 1] == '\n'
            if ((char == '\n' || char == '\r') && !isCrBeforeLf) {
                line += 1
                lineStart = index + 1
            }
            index += 1
        }
        return line to offset - lineStart + 1
    }
}
