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
}

/**
 * Finds the comments of a Kotlin source or of a Groovy Gradle script: its line comments, block comments and
 * documentation comments, but not text inside a string, a char or a backticked name that only looks like one.
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

    private val comments = mutableListOf<SourceComment>()

    private var index = 0

    /**
     * Returns the comments of the source in the order they stand in.
     */
    fun scan(): List<SourceComment> {
        comments.clear()
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
                }
                source[index] == '}' -> {
                    index += 1
                    if (inTemplate && braces == 0) {
                        return
                    }
                    braces -= 1
                }
                else -> index += 1
            }
        }
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

    private fun record(start: Int) {
        val line = source.substring(0, start).count { it == '\n' } + 1
        comments.add(SourceComment(line, source.substring(start, index)))
    }

    private fun startsWith(text: String): Boolean = source.startsWith(text, index)
}
