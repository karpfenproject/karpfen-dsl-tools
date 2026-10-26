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

/**
 * Collects the lines of a printed document or fragment in the canonical layout of the karpfen languages.
 *
 * The layout indents by [INDENTATION_UNIT] per level, separates lines by [LINE_BREAK] and opens a block on
 * the line of its header. A block without content is closed on its header line as `{ }`.
 *
 * A writer collects the text of one print call and is then dropped, it is never shared. Create one with
 * [document] or [fragment].
 *
 * @param startLevel The indentation level of the first line.
 */
internal class CodeWriter private constructor(private val startLevel: Int) {

    private val lines = mutableListOf<String>()
    private var level = startLevel

    /** The indentation of the line that is written next. */
    val indentation: String
        get() = indentationOf(level)

    /**
     * Adds [text] as a line at the current indentation level.
     *
     * Line breaks inside [text] are kept as they are, so the lines after the first one keep their own
     * indentation, such as the lines of a [verbatimBlock] or a string literal with an escaped line break.
     */
    fun line(text: String) {
        lines.add(indentation + text)
    }

    /**
     * Adds an empty line, without indentation.
     */
    fun blankLine() {
        lines.add("")
    }

    /**
     * Adds the block `header { ... }` whose content [body] writes one level deeper.
     *
     * When [body] writes nothing, the block is written as `header { }` on one line.
     */
    fun block(header: String, body: () -> Unit) {
        val headerIndex = lines.size
        line("$header {")
        level++
        body()
        level--
        if (lines.size == headerIndex + 1) {
            lines[headerIndex] = "$indentation$header { }"
        } else {
            line("}")
        }
    }

    /**
     * Writes each of [items] with [write], separating consecutive items by a blank line.
     */
    fun <T> paragraphs(items: List<T>, write: (T) -> Unit) {
        items.forEachIndexed { index, item ->
            if (index > 0) {
                blankLine()
            }
            write(item)
        }
    }

    /**
     * Returns `opening {`, then [body] on lines of its own one level deeper, then `}` at the current level, as
     * text to embed into the line that is written next.
     *
     * The first line of [body] stands at the deeper level. The lines after it keep their indentation relative to
     * each other: the indentation they all share is replaced by the deeper level, and blank lines stay empty. Code
     * such as Python thus keeps its shape at whatever depth the block stands, wherever it was written before, and
     * writing the text that results once more changes nothing.
     */
    fun verbatimBlock(opening: String, body: String): String {
        val contentIndentation = indentationOf(level + 1)
        val bodyLines = body.lines()
        val followingLines = bodyLines.drop(1)
        val sharedIndentation = followingLines.filter { it.isNotBlank() }.minOfOrNull { indentationWidthOf(it) } ?: 0
        val rebasedLines = followingLines.map { if (it.isBlank()) "" else contentIndentation + it.drop(sharedIndentation) }
        val content = (listOf(bodyLines.first()) + rebasedLines).joinToString(LINE_BREAK)
        return "$opening {$LINE_BREAK$contentIndentation$content$LINE_BREAK$indentation}"
    }

    private fun documentText(): String = lines.joinToString(LINE_BREAK, postfix = LINE_BREAK)

    private fun fragmentText(): String = lines.joinToString(LINE_BREAK).removePrefix(indentationOf(startLevel))

    /**
     * Creates writers for whole documents and for fragments.
     */
    companion object {

        /** The indentation of one level: four spaces. */
        const val INDENTATION_UNIT = "    "

        /** The separator between lines. */
        const val LINE_BREAK = "\n"

        /**
         * Returns the text that [write] writes as a whole document: starting at indentation level zero and
         * ending with a line break.
         */
        fun document(write: (CodeWriter) -> Unit): String = CodeWriter(0).also(write).documentText()

        /**
         * Returns the text that [write] writes as a fragment whose first line stands at [indentLevel], laid out
         * as [DocumentPrinter] describes fragments.
         *
         * @throws IllegalArgumentException If [indentLevel] is negative.
         */
        fun fragment(indentLevel: Int, write: (CodeWriter) -> Unit): String {
            require(indentLevel >= 0) { "The indentation level must not be negative, but was $indentLevel." }
            return CodeWriter(indentLevel).also(write).fragmentText()
        }

        private fun indentationOf(level: Int): String = INDENTATION_UNIT.repeat(level)

        /**
         * Returns the number of whitespace characters [line] starts with.
         */
        private fun indentationWidthOf(line: String): Int = line.indexOfFirst { !it.isWhitespace() }.let {
            if (it < 0) line.length else it
        }
    }
}
