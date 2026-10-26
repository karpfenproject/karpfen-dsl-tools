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
 * [document] or [fragment], which also hold the printers to their nesting limit: the printers recurse once per level
 * of nesting, so an element that nests deeper than the stack of the thread allows overflows it, and that error becomes
 * an [IllegalArgumentException], the way the printers reject every other element they cannot write. The readers
 * likewise report such nesting as a failure of the read.
 *
 * @param startLevel The indentation level of the first line.
 * @param continuationIndentation The indentation added after every line break inside the text of a line, see [line].
 */
internal class CodeWriter private constructor(
    private val startLevel: Int,
    private val continuationIndentation: String
) {

    /** The lines written so far. */
    private val lines = mutableListOf<String>()

    /** The indentation level of the line that is written next. */
    private var level = startLevel

    /** The indentation of the line that is written next. */
    val indentation: String
        get() = indentationOf(level)

    /**
     * Adds [text] as a line at the current indentation level.
     *
     * Line breaks inside [text] are kept as they are, so the lines after the first one keep their own
     * indentation, such as the lines of a [codeBlock] or a string literal with an escaped line break. A fragment
     * adds the indentation its document shares in front of those lines, see [fragment].
     */
    fun line(text: String) {
        lines.add(lineOf(text))
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
            lines[headerIndex] = lineOf("$header { }")
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
     * The body is re-indented, not copied: its first line stands at the deeper level, and the lines after it keep
     * their indentation relative to each other, as the indentation they all share is replaced by the deeper level.
     * Their indentation relative to the first line is not kept, since the first line comes without its own. Blank
     * lines stay empty, and the lines of [unchangedLines] are written exactly as they are and share no indentation
     * with the others, such as lines whose leading whitespace belongs to a string literal. Line breaks are written as
     * [LINE_BREAK]. Code such as Python thus keeps its shape at whatever depth the block stands, wherever its lines
     * stood before, and writing the text that results once more changes nothing.
     *
     * @param unchangedLines The indices of the lines of [body], as [String.lines] splits them, to write unchanged.
     */
    fun codeBlock(opening: String, body: String, unchangedLines: Set<Int> = emptySet()): String {
        val contentIndentation = indentationOf(level + 1)
        val bodyLines = body.lines()
        val rebasedIndices = bodyLines.indices.drop(1).filter { it !in unchangedLines }
        val sharedIndentation = rebasedIndices.map { bodyLines[it] }.filter { it.isNotBlank() }
            .minOfOrNull { indentationWidthOf(it) } ?: 0
        val content = bodyLines.mapIndexed { index, line ->
            when {
                index == 0 || index in unchangedLines -> line
                line.isBlank() -> ""
                else -> contentIndentation + line.drop(sharedIndentation)
            }
        }.joinToString(LINE_BREAK)
        return "$opening {$LINE_BREAK$contentIndentation$content$LINE_BREAK$indentation}"
    }

    /**
     * Returns [text] as a line at the current indentation level, with the continuation indentation after every line
     * break inside it, see [line].
     */
    private fun lineOf(text: String): String =
        indentation + text.replace(LINE_BREAKS) { it.value + continuationIndentation }

    /**
     * Returns the lines as a document, each ending with a line break.
     */
    private fun documentText(): String = lines.joinToString(LINE_BREAK, postfix = LINE_BREAK)

    /**
     * Returns the lines as a fragment, without the indentation of its first line and without a line break at its end.
     */
    private fun fragmentText(): String = lines.joinToString(LINE_BREAK).removePrefix(indentationOf(startLevel))

    /**
     * Creates writers for whole documents and for fragments.
     */
    companion object {

        /** The indentation of one level: four spaces. */
        const val INDENTATION_UNIT = "    "

        /** The separator between lines. */
        const val LINE_BREAK = "\n"

        /** A line break, written as `\r\n`, `\n` or a lone `\r`. */
        private val LINE_BREAKS = Regex("\r\n|\r|\n")

        /**
         * Returns the text that [write] writes as a whole document: starting at indentation level zero and
         * ending with a line break.
         *
         * @throws IllegalArgumentException If [write] throws it, or the element nests deeper than the stack allows.
         */
        fun document(write: (CodeWriter) -> Unit): String =
            withinNestingLimit { CodeWriter(0, "").also(write).documentText() }

        /**
         * Returns the text that [write] writes as a fragment whose first line stands at [indentLevel], laid out
         * as [DocumentPrinter] describes fragments.
         *
         * The lines that a line break inside the text of a [line] starts, such as the continuation lines of a string
         * literal, get [sharedIndentation] spaces in front of their own indentation: the indentation that all lines of
         * the document the fragment stands in share, which KMeta and KModel cut from every line when they read it.
         *
         * @throws IllegalArgumentException If [indentLevel] or [sharedIndentation] is negative, [write] throws it, or
         * the element nests deeper than the stack allows.
         */
        fun fragment(indentLevel: Int, sharedIndentation: Int = 0, write: (CodeWriter) -> Unit): String {
            require(indentLevel >= 0) { "The indentation level must not be negative, but was $indentLevel." }
            require(sharedIndentation >= 0) {
                "The shared indentation must not be negative, but was $sharedIndentation."
            }
            val continuationIndentation = " ".repeat(sharedIndentation)
            return withinNestingLimit { CodeWriter(indentLevel, continuationIndentation).also(write).fragmentText() }
        }

        /**
         * Returns [text] with every line break, `\r\n`, `\n` or a lone `\r`, written as [LINE_BREAK], as the
         * writer writes the lines of a [codeBlock].
         */
        fun withLineBreaks(text: String): String = text.lines().joinToString(LINE_BREAK)

        /**
         * Returns what [print] returns, or throws an [IllegalArgumentException] if it overflows the stack, see
         * [CodeWriter].
         */
        private fun withinNestingLimit(print: () -> String): String =
            try {
                print()
            } catch (overflow: StackOverflowError) {
                throw IllegalArgumentException(
                    "The element nests deeper than the printer can recurse on the stack of this thread.",
                    overflow
                )
            }

        /**
         * Returns the indentation of [level].
         */
        private fun indentationOf(level: Int): String = INDENTATION_UNIT.repeat(level)

        /**
         * Returns the number of whitespace characters [line] starts with.
         */
        private fun indentationWidthOf(line: String): Int = line.indexOfFirst { !it.isWhitespace() }.let {
            if (it < 0) line.length else it
        }
    }
}
