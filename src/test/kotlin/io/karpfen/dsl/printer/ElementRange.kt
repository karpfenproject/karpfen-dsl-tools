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

import org.antlr.v4.runtime.ParserRuleContext

/**
 * The text range of one element in a document, from its first token to its last, as an editor replaces it.
 *
 * @property start The offset of the first character of the element.
 * @property stopInclusive The offset of the last character of the element.
 */
data class ElementRange(val start: Int, val stopInclusive: Int) {

    /**
     * Returns the indentation level of the line of [text] the element starts on, counted in units of the
     * canonical layout; an element that follows other tokens on its line, such as an object after the arrow
     * of a `has` statement, has the level of that line.
     */
    fun indentLevelIn(text: String): Int {
        val lineStart = text.lastIndexOf('\n', start - 1) + 1
        val indentation = text.substring(lineStart, start).takeWhile { it == ' ' }
        return indentation.length / CodeWriter.INDENTATION_UNIT.length
    }

    /**
     * Returns [text] with this range replaced by [replacement].
     */
    fun replaceIn(text: String, replacement: String): String =
        text.substring(0, start) + replacement + text.substring(stopInclusive + 1)

    /**
     * Creates ranges from parse trees.
     */
    companion object {

        /**
         * Returns the range the parse tree [element] of [text] covers.
         *
         * ANTLR counts the code points of a stream that `CharStreams.fromString` made, while the range counts the
         * chars of [text], which differ behind a character outside the Basic Multilingual Plane.
         */
        fun of(element: ParserRuleContext, text: String): ElementRange =
            ElementRange(charOffsetOf(element.start.startIndex, text), charOffsetOf(element.stop.stopIndex + 1, text) - 1)

        /**
         * Returns the char offset in [text] of the code point at [codePointIndex].
         */
        private fun charOffsetOf(codePointIndex: Int, text: String): Int = text.offsetByCodePoints(0, codePointIndex)
    }
}
