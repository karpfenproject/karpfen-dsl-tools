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

/**
 * The part of the text of a string literal that holds its value, from [start] inclusive to [end] exclusive.
 *
 * Each karpfen language reads the value of a literal by stripping quote characters at its edges, see
 * [StringLiteralSyntax], and keeps escape sequences as written, so the value is always a part of the literal as
 * written. A span starts as the whole literal and is narrowed down by the steps of that rule.
 *
 * @param literal The text of the literal.
 * @property start The offset in the literal of the first char of the value.
 * @property end The offset in the literal behind the last char of the value.
 */
internal class LiteralValueSpan private constructor(private val literal: String, val start: Int, val end: Int) {

    /** The value, the text of the literal from [start] to [end]. */
    val value: String
        get() = literal.substring(start, end)

    /**
     * Returns the span without the chars at both of its ends that [strips], as [String.trim] strips them; a span that
     * loses all its chars becomes the empty span in its middle.
     */
    fun trimmed(strips: (Char) -> Boolean): LiteralValueSpan {
        var trimmedStart = start
        while (trimmedStart < end && strips(literal[trimmedStart])) {
            trimmedStart += 1
        }
        var trimmedEnd = end
        while (trimmedEnd > trimmedStart && strips(literal[trimmedEnd - 1])) {
            trimmedEnd -= 1
        }
        if (trimmedStart == trimmedEnd) {
            val middle = (start + end) / 2
            return LiteralValueSpan(literal, middle, middle)
        }
        return LiteralValueSpan(literal, trimmedStart, trimmedEnd)
    }

    /**
     * Returns the span without [quote] at both of its ends, as [String.removeSurrounding] removes it, or this span if
     * [quote] does not stand at both of them.
     */
    fun withoutSurrounding(quote: Char): LiteralValueSpan =
        if (end - start >= 2 && literal[start] == quote && literal[end - 1] == quote) {
            LiteralValueSpan(literal, start + 1, end - 1)
        } else {
            this
        }

    /** Makes the span a read rule starts from. */
    companion object {

        /**
         * Returns the span of the whole [literal].
         */
        fun of(literal: String): LiteralValueSpan = LiteralValueSpan(literal, 0, literal.length)
    }
}
