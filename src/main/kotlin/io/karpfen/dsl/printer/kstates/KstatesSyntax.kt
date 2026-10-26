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
import io.karpfen.dsl.parser.EvalCodeSyntax
import io.karpfen.dsl.parser.MacroTypeSyntax
import io.karpfen.dsl.parser.StringLiteralSyntax
import io.karpfen.dsl.printer.CodeWriter
import org.antlr.v4.runtime.CharStreams

/**
 * Writes the small pieces of KStates that every part of a state machine shares: string literals, macro
 * calls, `EVAL` blocks and the type expressions of macros.
 *
 * @param literals The string literal syntax of KStates.
 * @param evalCode Decides which code an `EVAL` block can hold.
 * @param continuations Finds the lines of code that continue a string literal.
 */
internal class KstatesSyntax(
    private val literals: StringLiteralSyntax = StringLiteralSyntax.KSTATES,
    private val evalCode: EvalCodeCheck = EvalCodeCheck(),
    private val continuations: StringContinuations = StringContinuations()
) {

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
     * Returns the block `EVAL { code }` with [code], to embed into the line [writer] writes next.
     *
     * Code on a single line stays on the line of the block. Code with several lines goes onto lines of its own
     * between the braces, re-indented by [CodeWriter.codeBlock]: the lines after the first keep their indentation
     * relative to each other, and a line that continues a string literal, see [StringContinuations], stays exactly as
     * it is, so the value of the literal does not change. Whitespace at the edges of [code] is not part of the code,
     * see [EvalCodeSyntax], so it is not written. The line breaks of the code are written as [CodeWriter.LINE_BREAK],
     * and the code is checked and its string continuations are found in that form, the form that reads back: a
     * backslash before a `\r\n` escapes only the `\r`, so KStates reads the same code differently with other line
     * breaks.
     *
     * @throws IllegalArgumentException If KStates cannot hold [code], see [EvalCodeCheck].
     */
    fun eval(code: String, writer: CodeWriter): String {
        val written = CodeWriter.withLineBreaks(EvalCodeSyntax.codeOf(code))
        evalCode.requireExpressible(written)
        return when {
            written.isEmpty() -> "EVAL { }"
            CodeWriter.LINE_BREAK in written -> writer.codeBlock("EVAL", written, continuations.linesIn(written))
            else -> "EVAL { $written }"
        }
    }

    /**
     * Returns the type expression of a `TAKES` directive, see [typeExpression]; `reference(...)` is not one.
     *
     * @throws IllegalArgumentException If [type] cannot be written in a `TAKES` directive.
     */
    fun takesType(type: String): String = typeExpression(type, allowsReference = false)

    /**
     * Returns the type expression of a `RETURNS` directive, see [typeExpression], which may also be
     * `reference(...)`.
     *
     * @throws IllegalArgumentException If [type] cannot be written in a `RETURNS` directive.
     */
    fun returnsType(type: String): String = typeExpression(type, allowsReference = true)

    /**
     * Returns a type expression of a macro directive.
     *
     * A type that the parser read from a typed call such as `list("Obstacle")` or `reference("Obstacle")` is
     * written as that call again; any other type is written as a string literal such as `"number"`. A typed call
     * names its function with an identifier, which no keyword of KStates may be, except for `reference`, which only
     * a `RETURNS` directive takes.
     *
     * @param allowsReference True if the directive takes `reference(...)`.
     * @throws IllegalArgumentException If the type is a call that the directive cannot hold, or the type, or the
     * type name of the call, cannot be written as a string literal.
     */
    private fun typeExpression(type: String, allowsReference: Boolean): String {
        val (functionName, typeName) = MacroTypeSyntax.typedCallOf(type) ?: return quote(type)
        val isReference = functionName == MacroTypeSyntax.REFERENCE_FUNCTION
        require(if (isReference) allowsReference else isIdentifier(functionName)) {
            "KStates cannot write the type <$type> here: a typed call names its function with an identifier that is " +
                "no keyword, and only RETURNS takes ${MacroTypeSyntax.REFERENCE_FUNCTION}(...)."
        }
        return "$functionName(${quote(typeName)})"
    }

    /**
     * Returns true if the KStates lexer reads all of [name] as one identifier, not as a keyword.
     */
    private fun isIdentifier(name: String): Boolean {
        val lexer = KstatesLexer(CharStreams.fromString(name)).apply { removeErrorListeners() }
        return lexer.allTokens.singleOrNull()?.type == KstatesLexer.IDENT
    }
}
