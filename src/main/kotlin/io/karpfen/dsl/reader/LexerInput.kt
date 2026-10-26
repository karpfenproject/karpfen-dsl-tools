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
 * The text a reader hands to its lexer, made from the text being read, together with the way back from the
 * offsets of the one to those of the other.
 *
 * A reader builds the model its converter builds, so it lexes the text its converter parses. The converters of
 * KMeta and KModel parse the text with [String.trimIndent] applied, which drops a blank first and last line, cuts the
 * indentation all other lines share and turns every line break into `\n`; this changes the value of a string literal
 * that an escaped line break continues on the next line, and makes an escaped `\r\n` one token. The KStates converter
 * parses the text as written. The ranges a reader reports always refer to the text as written.
 *
 * @property source The text being read.
 * @property lexed The text the lexer reads.
 * @param sourceStarts For every char of [lexed], the offset in [source] of the char it stems from; null if [lexed]
 * is [source].
 * @param sourceEnds For every char of [lexed], the offset in [source] behind the chars it stems from, two for a line
 * break written as `\r\n`; null if [lexed] is [source].
 */
internal class LexerInput private constructor(
    val source: String,
    val lexed: String,
    private val sourceStarts: IntArray?,
    private val sourceEnds: IntArray?
) {

    /**
     * Returns the offset in [source] of the char at [lexedOffset] of [lexed]; the end of [lexed] maps to the end
     * of [source].
     */
    fun sourceStartOf(lexedOffset: Int): Int = when {
        sourceStarts == null -> lexedOffset
        lexedOffset < lexed.length -> sourceStarts[lexedOffset]
        else -> source.length
    }

    /**
     * Returns the offset in [source] behind the part of [lexed] from [lexedStart] to [lexedEnd], or the offset of
     * [lexedStart] for an empty part.
     */
    fun sourceEndOf(lexedStart: Int, lexedEnd: Int): Int = when {
        lexedEnd <= lexedStart -> sourceStartOf(lexedStart)
        sourceEnds == null -> lexedEnd
        else -> sourceEnds[lexedEnd - 1]
    }

    /** Makes the lexer input of a text. */
    companion object {

        /**
         * Returns the input that lexes [text] as written, as the KStates converter parses it.
         */
        fun asWritten(text: String): LexerInput = LexerInput(text, text, null, null)

        /**
         * Returns the input that lexes [text] with its common indentation trimmed the way [String.trimIndent]
         * trims it, as the KMeta and KModel converters parse it.
         */
        fun withIndentTrimmed(text: String): LexerInput = IndentTrimming(text).input()
    }

    /**
     * Trims the indentation of one text the way [String.trimIndent] does, recording where each char it keeps
     * stands in the text. The trimmed text is never longer than the text, which sizes the tables of offsets.
     *
     * @param text The text to trim.
     */
    private class IndentTrimming(private val text: String) {

        private val lines = SourceLine.linesOf(text)

        private val lexed = StringBuilder(text.length)

        private val starts = IntArray(text.length)

        private val ends = IntArray(text.length)

        /**
         * Returns the trimmed text with its offsets.
         */
        fun input(): LexerInput {
            val cut = lines.filterNot { it.isBlank(text) }.minOfOrNull { it.indentationWidth(text) } ?: 0
            val keptIndices = lines.indices.filterNot { isBlankEdge(it) }
            keptIndices.forEachIndexed { position, index ->
                if (position > 0) {
                    val previous = lines[index - 1]
                    append('\n', previous.end, previous.breakEnd)
                }
                val line = lines[index]
                for (offset in minOf(line.start + cut, line.end) until line.end) {
                    append(text[offset], offset, offset + 1)
                }
            }
            return LexerInput(text, lexed.toString(), starts.copyOf(lexed.length), ends.copyOf(lexed.length))
        }

        /**
         * Returns true if the line at [index] is the first or the last one and blank, which trimming drops.
         */
        private fun isBlankEdge(index: Int): Boolean = (index == 0 || index == lines.lastIndex) && lines[index].isBlank(text)

        private fun append(char: Char, sourceStart: Int, sourceEnd: Int) {
            starts[lexed.length] = sourceStart
            ends[lexed.length] = sourceEnd
            lexed.append(char)
        }
    }

    /**
     * One line of a text, split the way [String.lines] splits: at `\r\n`, `\n` and a lone `\r`.
     *
     * @property start The offset of its first char.
     * @property end The offset behind its last char, before its line break.
     * @property breakEnd The offset behind its line break; [end] for the last line.
     */
    private class SourceLine(val start: Int, val end: Int, val breakEnd: Int) {

        /**
         * Returns true if the line holds whitespace only, as [String.isBlank] decides.
         */
        fun isBlank(text: String): Boolean = (start until end).all { text[it].isWhitespace() }

        /**
         * Returns the number of whitespace chars the line starts with, all of them for a blank line.
         */
        fun indentationWidth(text: String): Int =
            ((start until end).firstOrNull { !text[it].isWhitespace() } ?: end) - start

        /** Splits texts into lines. */
        companion object {

            /**
             * Returns the lines of [text], one more than it has line breaks.
             */
            fun linesOf(text: String): List<SourceLine> {
                val lines = mutableListOf<SourceLine>()
                var start = 0
                var offset = 0
                while (offset < text.length) {
                    val char = text[offset]
                    if (char == '\r' || char == '\n') {
                        val breakLength = if (char == '\r' && text.getOrNull(offset + 1) == '\n') 2 else 1
                        lines.add(SourceLine(start, offset, offset + breakLength))
                        offset += breakLength
                        start = offset
                    } else {
                        offset += 1
                    }
                }
                lines.add(SourceLine(start, text.length, text.length))
                return lines
            }
        }
    }
}
