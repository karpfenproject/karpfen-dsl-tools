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
 * Finds the range of a part of a text when only a range and the text it covers are known, such as the code of an
 * `EVAL` body and its [SourceLocation.codeRange], without the whole text.
 *
 * Lines end as [SourceText] counts them: with `\n`, `\r\n` or a lone `\r`.
 */
internal object TextSpans {

    /**
     * Returns the range of the chars from [start] inclusive to [end] exclusive of [coveredText], the text that [range]
     * covers.
     *
     * @throws IllegalArgumentException If [start] or [end] lies outside [coveredText] or [end] lies before [start].
     */
    fun rangeWithin(range: SourceRange, coveredText: String, start: Int, end: Int): SourceRange {
        require(start in 0..coveredText.length && end in start..coveredText.length) {
            "The part $start until $end lies outside 0..${coveredText.length}"
        }
        return SourceRange(positionWithin(range.start, coveredText, start), positionWithin(range.start, coveredText, end))
    }

    /**
     * Returns the position of the char at [index] of [text], a text that starts at [origin].
     */
    private fun positionWithin(origin: SourcePosition, text: String, index: Int): SourcePosition {
        var line = origin.line
        var lineStart = -1
        for (position in 0 until index) {
            if (endsLine(text, position)) {
                line += 1
                lineStart = position + 1
            }
        }
        val column = if (lineStart < 0) origin.column + index else index - lineStart + 1
        return SourcePosition(origin.offset + index, line, column)
    }

    /**
     * Returns true if the char at [position] of [text] ends a line: a `\n`, or a `\r` that no `\n` follows.
     */
    private fun endsLine(text: String, position: Int): Boolean = when (text[position]) {
        '\n' -> true
        '\r' -> position + 1 >= text.length || text[position + 1] != '\n'
        else -> false
    }
}
