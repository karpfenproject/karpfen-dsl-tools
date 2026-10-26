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
package io.karpfen.dsl.printer.kstates

import io.karpfen.dsl.grammar.kstates.KstatesLexer
import org.antlr.v4.runtime.CharStreams

/**
 * Finds the lines of `EVAL` code that continue a string literal, by lexing the code the way KStates does.
 *
 * KStates reads a string literal between double or single quotes on one line, unless a backslash escapes the line
 * break, which then continues the literal on the next line. The whitespace at the start of such a line belongs to the
 * value of the literal. Python's triple-quoted strings are no string literals of KStates.
 */
internal class StringContinuations {

    /**
     * Returns the indices of the lines of [code], as [String.lines] splits them, that start inside a string literal.
     */
    fun linesIn(code: String): Set<Int> {
        val lexer = KstatesLexer(CharStreams.fromString(code)).apply { removeErrorListeners() }
        val literals = lexer.allTokens
            .filter { it.type == KstatesLexer.STRING || it.type == KstatesLexer.SSTRING }
            .map { it.startIndex..it.stopIndex }
        return lineStartsOf(code).withIndex()
            .filter { (_, start) -> literals.any { start > it.first && start in it } }
            .map { it.index }
            .toSet()
    }

    /**
     * Returns the index of the first code point of each line of [code], counted in code points as the lexer counts;
     * a line break is `\r\n`, `\n` or `\r`, as [String.lines] takes it.
     */
    private fun lineStartsOf(code: String): List<Int> {
        val starts = mutableListOf(0)
        var codePoint = 0
        var index = 0
        while (index < code.length) {
            val char = code[index]
            val breakLength = when {
                char == '\r' && code.startsWith("\n", index + 1) -> 2
                char == '\r' || char == '\n' -> 1
                else -> 0
            }
            if (breakLength > 0) {
                index += breakLength
                codePoint += breakLength
                starts.add(codePoint)
            } else {
                index += Character.charCount(code.codePointAt(index))
                codePoint += 1
            }
        }
        return starts
    }
}
