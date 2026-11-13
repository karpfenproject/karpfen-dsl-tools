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
 * A `$(path)` reference in the code of an `EVAL` body.
 *
 * @property path The path between the parentheses, without the whitespace around it.
 * @property start The index of the `$` in the code.
 * @property end The index after the closing parenthesis in the code.
 */
internal class CodeReference(val path: String, val start: Int, val end: Int)

/**
 * An `@name(...)` call of a macro in the code of an `EVAL` body.
 *
 * @property name The name of the macro.
 * @property start The index of the `@` in the code.
 * @property nameEnd The index after the name in the code.
 * @property argumentCount The number of arguments the call passes as Python reads them, or null if that cannot be told
 * from the code alone, such as for an argument unpacked with `*`.
 * @property end The index after the closing parenthesis of the call, or [nameEnd] if the arguments cannot be told.
 */
internal class CodeMacroCall(val name: String, val start: Int, val nameEnd: Int, val argumentCount: Int?, val end: Int)

/**
 * What the code of an `EVAL` body refers to, as the karpfen runtime finds it before it runs the code as Python: the
 * `$(path)` references to the model, the event in scope, the bindings of `WITH` blocks and the parameters of a macro,
 * and the `@name(...)` calls of macros.
 *
 * The runtime finds both with a regular expression on the raw code, in strings and Python comments too; so does this
 * object. The arguments of a macro call are counted the way Python reads the call, which the runtime builds from it.
 */
internal object EvalCodeReferences {

    /** A `$(path)` reference, as the runtime matches it. */
    private val MODEL_REFERENCE = Regex("""\$\(([^)]+)\)""")

    /** An `@name(...)` macro call, as the runtime matches it. */
    private val MACRO_CALL = Regex("""@(\w+)\(([^)]*)\)""")

    /** The keyword `return` as a word of its own. */
    private val RETURN = Regex("""(?<![A-Za-z0-9_])return(?![A-Za-z0-9_])""")

    /**
     * Returns the `$(path)` references of [code] in the order they are written.
     */
    fun referencesIn(code: String): List<CodeReference> =
        MODEL_REFERENCE.findAll(code).map { CodeReference(it.groupValues[1].trim(), it.range.first, it.range.last + 1) }.toList()

    /**
     * Returns the `@name(...)` macro calls of [code] in the order they are written.
     */
    fun macroCallsIn(code: String): List<CodeMacroCall> = MACRO_CALL.findAll(code).map { match ->
        val name = match.groupValues[1]
        val nameEnd = match.range.first + 1 + name.length
        val arguments = PythonCallArguments.countFrom(code, nameEnd)
        CodeMacroCall(name, match.range.first, nameEnd, arguments?.first, arguments?.second ?: nameEnd)
    }.toList()

    /**
     * Returns true if [code] holds the word `return`.
     */
    fun hasReturn(code: String): Boolean = RETURN.containsMatchIn(code)
}

/**
 * Counts the arguments of a Python call from its opening parenthesis, the way Python reads them.
 *
 * The count knows parentheses, brackets and braces, strings in single and double quotes with their escapes, and
 * comments; it gives up on what it cannot count for sure: a triple-quoted string, an argument unpacked with `*` or
 * `**`, and a call that does not end.
 */
internal object PythonCallArguments {

    /** The brackets that open a nesting, mapped to the brackets that close it. */
    private val CLOSING = mapOf('(' to ')', '[' to ']', '{' to '}')

    /**
     * Returns the number of arguments of the call whose opening parenthesis is at [open] in [code], and the index after
     * its closing parenthesis; null if the arguments cannot be counted for sure.
     */
    fun countFrom(code: String, open: Int): Pair<Int, Int>? {
        if (open >= code.length || code[open] != '(') return null
        val closers = ArrayDeque<Char>()
        var commas = 0
        var lastArgumentHasContent = false
        var index = open + 1
        while (index < code.length) {
            val char = code[index]
            when {
                char == '"' || char == '\'' -> {
                    index = endOfString(code, index) ?: return null
                    lastArgumentHasContent = true
                    continue
                }
                char == '#' -> index = endOfLine(code, index)
                char in CLOSING -> {
                    closers.addLast(CLOSING.getValue(char))
                    lastArgumentHasContent = true
                }
                closers.isEmpty() && char == ')' -> {
                    val count = if (lastArgumentHasContent) commas + 1 else commas
                    return count to index + 1
                }
                char == ')' || char == ']' || char == '}' -> if (closers.removeLastOrNull() != char) return null
                closers.isEmpty() && char == ',' -> {
                    commas += 1
                    lastArgumentHasContent = false
                }
                closers.isEmpty() && char == '*' && !lastArgumentHasContent -> return null
                !char.isWhitespace() -> lastArgumentHasContent = true
            }
            index += 1
        }
        return null
    }

    /**
     * Returns the index after the string literal that starts with the quote at [start] of [code], or null for a
     * triple-quoted string or a string that does not end on its line.
     */
    private fun endOfString(code: String, start: Int): Int? {
        val quote = code[start]
        if (code.startsWith("$quote$quote$quote", start)) return null
        var index = start + 1
        while (index < code.length) {
            when (code[index]) {
                '\\' -> index += 1
                quote -> return index + 1
                '\n', '\r' -> return null
            }
            index += 1
        }
        return null
    }

    /** Returns the index of the line break that ends the line holding [start] of [code], or the end of the code. */
    private fun endOfLine(code: String, start: Int): Int {
        var index = start
        while (index < code.length && code[index] != '\n' && code[index] != '\r') {
            index += 1
        }
        return index
    }
}
