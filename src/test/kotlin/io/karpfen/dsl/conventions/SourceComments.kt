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
package io.karpfen.dsl.conventions

/**
 * A comment of a source file.
 *
 * @property line The line the comment starts on, counted from 1.
 * @property text The comment with its delimiters.
 */
data class SourceComment(val line: Int, val text: String) {

    /** True for a documentation comment: a block comment whose opening delimiter has a second star. */
    val isDocumentation: Boolean
        get() = text.startsWith("/**") && text != "/**/"

    /**
     * Returns the line of the char at [offset] of [text], counted from the line the comment starts on.
     */
    fun lineAt(offset: Int): Int = line + text.substring(0, offset).count { it == '\n' }
}

/**
 * A comment with its place in the source.
 *
 * @property comment The comment.
 * @property offset The offset of its first char in the source.
 * @property block The kind of brace block it stands in.
 * @property parenDepth The number of parentheses open around it in that block.
 */
data class PlacedComment(val comment: SourceComment, val offset: Int, val block: BlockKind, val parenDepth: Int)

/**
 * Finds the comments of a Kotlin source or of a Groovy Gradle script: its line comments, block comments and
 * documentation comments, but not text inside a string, a char or a backticked name that only looks like one. It
 * hands the words, parentheses, braces and semicolons of the code to [CodeBlocks], which tells each comment the block
 * it stands in; the code inside the templates of strings is read like any other.
 *
 * Both languages write strings between double quotes, with backslash escapes, and between three double quotes; the
 * templates `${...}` of these strings hold code again, with strings of their own. Kotlin reads its strings between three
 * double quotes without escapes, writes a char between single quotes, names between backticks and nests block
 * comments. Groovy writes strings without templates between single quotes and between three single quotes.
 *
 * @param source The text of the file.
 * @param dialect The language of the file.
 */
class SourceComments(private val source: String, private val dialect: Dialect) {

    /**
     * The languages whose comments are found.
     */
    enum class Dialect {

        /** Kotlin sources and scripts. */
        KOTLIN,

        /** Groovy Gradle scripts. */
        GROOVY
    }

    /** The comments found so far, with their places. */
    private val comments = mutableListOf<PlacedComment>()

    /** The blocks of the code read so far. */
    private var blocks = CodeBlocks()

    /** The offset of the next char to read. */
    private var index = 0

    /**
     * Returns the comments of the source in the order they stand in.
     */
    fun scan(): List<SourceComment> = scanPlaced().map { it.comment }

    /**
     * Returns the comments of the source with their places, in the order they stand in.
     */
    fun scanPlaced(): List<PlacedComment> {
        comments.clear()
        blocks = CodeBlocks()
        index = 0
        code(inTemplate = false)
        return comments.toList()
    }

    /**
     * Reads code up to the end of the source or, [inTemplate], up to the brace that closes the template.
     */
    private fun code(inTemplate: Boolean) {
        var braces = 0
        while (index < source.length) {
            when {
                startsWith("//") -> lineComment()
                startsWith("/*") -> blockComment()
                startsWith("\"\"\"") -> string("\"\"\"", escapes = dialect == Dialect.GROOVY, templates = true)
                startsWith("\"") -> string("\"", escapes = true, templates = true)
                dialect == Dialect.GROOVY && startsWith("'''") -> string("'''", escapes = true, templates = false)
                startsWith("'") -> string("'", escapes = true, templates = false)
                dialect == Dialect.KOTLIN && startsWith("`") -> string("`", escapes = false, templates = false)
                source[index] == '{' -> {
                    braces += 1
                    index += 1
                    blocks.openBrace()
                }
                source[index] == '}' -> {
                    index += 1
                    if (inTemplate && braces == 0) {
                        return
                    }
                    braces -= 1
                    blocks.closeBrace()
                }
                source[index].isLetter() || source[index] == '_' -> word()
                else -> punctuation()
            }
        }
    }

    /**
     * Reads the identifier or keyword that starts at the current char and hands it to [blocks].
     */
    private fun word() {
        val start = index
        while (index < source.length && (source[index].isLetterOrDigit() || source[index] == '_')) {
            index += 1
        }
        blocks.word(source.substring(start, index), isMemberReference = source.startsWith("::", start - 2))
    }

    /**
     * Reads one char of code other than a brace or a word, and hands parentheses and semicolons to [blocks].
     */
    private fun punctuation() {
        when (source[index]) {
            '(' -> blocks.openParen()
            ')' -> blocks.closeParen()
            ';' -> blocks.semicolon()
        }
        index += 1
    }

    /**
     * Reads a string, a char or a backticked name that starts with [delimiter] and ends with it.
     */
    private fun string(delimiter: String, escapes: Boolean, templates: Boolean) {
        index += delimiter.length
        while (index < source.length) {
            when {
                escapes && source[index] == '\\' -> index += 2
                templates && startsWith("\${") -> {
                    index += 2
                    code(inTemplate = true)
                }
                startsWith(delimiter) -> {
                    index += delimiter.length
                    skipQuotesThatEndRawString(delimiter)
                    return
                }
                else -> index += 1
            }
        }
    }

    /**
     * Skips the double quotes after the closing [delimiter] of a Kotlin raw string, where a run of more than three
     * quotes ends the string with its last three.
     */
    private fun skipQuotesThatEndRawString(delimiter: String) {
        if (dialect == Dialect.KOTLIN && delimiter == "\"\"\"") {
            while (index < source.length && source[index] == '"') {
                index += 1
            }
        }
    }

    /**
     * Reads a line comment up to the end of its line.
     */
    private fun lineComment() {
        val start = index
        while (index < source.length && source[index] != '\n' && source[index] != '\r') {
            index += 1
        }
        record(start)
    }

    /**
     * Reads a block comment, which in Kotlin may hold nested block comments.
     */
    private fun blockComment() {
        val start = index
        var depth = 0
        while (index < source.length) {
            when {
                startsWith("/*") && (depth == 0 || dialect == Dialect.KOTLIN) -> {
                    depth += 1
                    index += 2
                }
                startsWith("*/") -> {
                    depth -= 1
                    index += 2
                    if (depth == 0) {
                        break
                    }
                }
                else -> index += 1
            }
        }
        record(start)
    }

    /**
     * Records the comment from [start] to the current char, placed in the current block.
     */
    private fun record(start: Int) {
        val line = source.substring(0, start).count { it == '\n' } + 1
        val comment = SourceComment(line, source.substring(start, index))
        comments.add(PlacedComment(comment, start, blocks.current, blocks.parenDepth))
    }

    /**
     * Returns true if the source continues with [text] at the current char.
     */
    private fun startsWith(text: String): Boolean = source.startsWith(text, index)
}
