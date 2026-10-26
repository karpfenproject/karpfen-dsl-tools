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

import io.karpfen.dsl.grammar.kmeta.KmetaLexer
import io.karpfen.dsl.grammar.kmodel.KmodelLexer
import io.karpfen.dsl.grammar.kstates.KstatesLexer
import org.antlr.v4.runtime.CharStream
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.Lexer

/**
 * How one karpfen language reads the value of a string literal and writes a value as a literal: the one home of
 * this rule, which the parse visitor, the reader and the printer of the language share.
 *
 * The languages keep escape sequences such as `\"` as written instead of decoding them. Reading a literal only
 * strips quote characters at its edges, by a rule of each language. Writing a value tries a few literal forms, the
 * value between double quotes first, and takes the first form that the lexer of the language reads as one string
 * token, also with the text that follows it on its line, and that reads back to the value. Unless the writer names
 * that text, the form must hold where more string literals of both kinds follow, which suits a literal wherever it
 * stands. The lexers of KMeta and KModel read a backslash as an escape or as a plain char, whichever makes the
 * longer token, so a literal whose closing quote follows a backslash could run on into the next literal. The other forms serve values the plain form
 * cannot carry: a value that ends in a lone backslash gets an extra escaped double quote that reading strips again,
 * a KModel value with an unescaped double quote goes between single quotes, and a KStates value with single quotes
 * at its edges gets another pair of them.
 *
 * A value no form can carry is rejected: one with a line break or a quote that no backslash escapes, or with quote
 * characters at its edges that reading strips. A line break that a backslash escapes stays inside the literal.
 *
 * @param languageName The name of the language, which starts the message of a rejection.
 * @param lexerOf Creates the lexer of the language on a text.
 * @param stringTokenType The type of the string tokens of that lexer.
 * @param readRule Turns the text of a string token into its value.
 */
internal class StringLiteralSyntax private constructor(
    private val languageName: String,
    private val lexerOf: (CharStream) -> Lexer,
    private val stringTokenType: Int,
    private val readRule: (String) -> String
) {

    /**
     * Returns the value of the string token [literal], that is its text with the quotes the language strips.
     */
    fun unquote(literal: String): String = readRule(literal)

    /**
     * Returns [value] as a literal that reads back unchanged where [followedBy] follows it on its line, in the first
     * of the forms described above that works.
     *
     * @param followedBy The text that follows the literal on its line, by default [FOLLOWING_LITERALS].
     * @throws IllegalArgumentException If no form of the language reads back to [value] there.
     */
    fun quote(value: String, followedBy: String = FOLLOWING_LITERALS): String =
        literalFormsOf(value).firstOrNull { isStringToken(it, followedBy) && unquote(it) == value }
            ?: throw IllegalArgumentException(
                "$languageName cannot write the value <$value> as a string literal that reads back unchanged: it has a " +
                    "line break or a quote that no backslash escapes, or quote characters at its edges that " +
                    "$languageName strips when it reads a literal."
            )

    /**
     * Returns [values] as literals separated by a comma and a space.
     *
     * @throws IllegalArgumentException If one of [values] cannot be written, see [quote].
     */
    fun quoteAll(values: List<String>): String = values.joinToString(", ") { quote(it) }

    /**
     * Returns true if the lexer of the language reads all of [literal] as a single string token, which ends with it
     * where [followedBy] follows it on its line.
     */
    fun isStringToken(literal: String, followedBy: String = FOLLOWING_LITERALS): Boolean {
        val lexer = lexerOf(CharStreams.fromString(literal + followedBy)).apply { removeErrorListeners() }
        val token = lexer.nextToken()
        return token.type == stringTokenType && token.text == literal
    }

    /**
     * Returns the literal forms of [value] in the order they are tried.
     */
    private fun literalFormsOf(value: String): List<String> = listOf(
        "\"$value\"",
        "\"$value\"\"",
        "'$value'",
        "'$value''",
        "\"'$value'\""
    )

    /**
     * The literal syntax of each karpfen language.
     */
    companion object {

        /** Literals of both kinds of quotes, the text that may follow a literal wherever it stands. */
        const val FOLLOWING_LITERALS = " \"x\" 'x'"

        /** KMeta strips every double quote at both ends of a literal. */
        val KMETA = StringLiteralSyntax("KMeta", ::KmetaLexer, KmetaLexer.STRING) { it.trim('"') }

        /** The statements of KModel strip every double and single quote at both ends of a literal. */
        val KMODEL = StringLiteralSyntax("KModel", ::KmodelLexer, KmodelLexer.STRING) { it.trim('"', '\'') }

        /**
         * The object signatures of KModel, `"key":"Class" {`, strip one pair of double quotes and then one pair of
         * single quotes. Nothing but the colon, the class type and the brace follows a key, and only the brace a class
         * type, so a printer writes them for that text.
         */
        val KMODEL_SIGNATURE = StringLiteralSyntax("KModel", ::KmodelLexer, KmodelLexer.STRING) {
            it.removeSurrounding("\"").removeSurrounding("'")
        }

        /** KStates strips one pair of double quotes and then one pair of single quotes. */
        val KSTATES = StringLiteralSyntax("KStates", ::KstatesLexer, KstatesLexer.STRING) {
            it.trim().removeSurrounding("\"").removeSurrounding("'")
        }
    }
}
