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

import io.karpfen.dsl.printer.CodeWriter
import io.karpfen.dsl.printer.StringLiteralSyntax

/**
 * Writes the small pieces of KStates that every part of a state machine shares: string literals, macro
 * calls, `EVAL` blocks and the type expressions of macros.
 *
 * @param literals The string literal syntax of KStates.
 */
internal class KstatesSyntax(private val literals: StringLiteralSyntax = StringLiteralSyntax.KSTATES) {

    /**
     * Returns [value] as a string literal.
     *
     * @throws IllegalArgumentException If [value] cannot be written as a KStates string literal.
     */
    fun quote(value: String): String = literals.quote(value)

    /**
     * Returns [values] as string literals separated by a comma and a space.
     *
     * @throws IllegalArgumentException If one of [values] cannot be written as a KStates string literal.
     */
    fun quoteAll(values: List<String>): String = literals.quoteAll(values)

    /**
     * Returns the macro call `MACRO("name", "argument", ...)`.
     *
     * @throws IllegalArgumentException If the name or an argument cannot be written as a string literal.
     */
    fun macroCall(macroName: String, arguments: List<String>): String = "MACRO(${quoteAll(listOf(macroName) + arguments)})"

    /**
     * Returns the block `EVAL { code }` with [code] verbatim, to embed into the line [writer] writes next.
     *
     * Code on a single line stays on the line of the block. Code with several lines, and code that contains
     * `//`, goes onto lines of its own between the braces: KStates reads `//` as the start of a comment even
     * inside `EVAL` blocks, which would otherwise swallow the closing brace. Only the first line of the code is
     * indented, the other lines keep the indentation they have, which leaves Python code unchanged. The parser
     * reads the code back trimmed, so code that came from a parser reads back unchanged.
     */
    fun eval(code: String, writer: CodeWriter): String = when {
        code.isEmpty() -> "EVAL { }"
        code.any { it == '\n' || it == '\r' } || code.contains("//") -> writer.verbatimBlock("EVAL", code)
        else -> "EVAL { $code }"
    }

    /**
     * Returns a type expression of a `TAKES` or `RETURNS` directive.
     *
     * A type that the parser read from a typed call such as `list("Obstacle")` or `reference("Obstacle")` is
     * written as that call again; any other type is written as a string literal such as `"number"`.
     *
     * @throws IllegalArgumentException If the type, or the type name of the call, cannot be written as a
     * string literal.
     */
    fun typeExpression(type: String): String {
        val call = TYPED_CALL.matchEntire(type) ?: return quote(type)
        val (functionName, typeName) = call.destructured
        return "$functionName(${quote(typeName)})"
    }

    private companion object {

        /** A call such as `list("Obstacle")`: a name followed by one string literal in parentheses. */
        val TYPED_CALL = Regex("""([A-Za-z_][A-Za-z0-9_]*)\("((?:\\.|[^"\\])*)"\)""")
    }
}
