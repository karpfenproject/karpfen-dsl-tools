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
 * Writes string values as the double-quoted string literals of one karpfen language.
 *
 * The parsers of the karpfen languages keep escape sequences such as `\"` as written instead of decoding
 * them, so a value is written verbatim between double quotes, and every value that was read from a literal
 * is written back as the same literal. A value that would not read back unchanged is rejected: one with a
 * line break, a double quote that no backslash escapes, a lone backslash at its end, or quote characters at
 * its edges that the language strips when it reads the literal.
 *
 * @param languageName The name of the language, which starts the message of a rejection.
 * @param readBack Turns a literal back into its value the way the parse visitor of the language does.
 */
internal class StringLiteralSyntax(
    private val languageName: String,
    private val readBack: (String) -> String
) {

    /**
     * Returns [value] as a double-quoted literal.
     *
     * @throws IllegalArgumentException If the language cannot write [value] as a literal that reads back
     * unchanged.
     */
    fun quote(value: String): String {
        val literal = "\"$value\""
        require(isClosedLiteralContent(value) && readBack(literal) == value) {
            "$languageName cannot write the value <$value> as a string literal: it must not contain line breaks " +
                "or unescaped double quotes, must not end in a lone backslash and must read back unchanged."
        }
        return literal
    }

    /**
     * Returns [values] as literals separated by a comma and a space.
     *
     * @throws IllegalArgumentException If one of [values] cannot be written, see [quote].
     */
    fun quoteAll(values: List<String>): String = values.joinToString(", ") { quote(it) }

    /**
     * Returns true if [content] between two double quotes forms exactly one string token: no line breaks,
     * every double quote escaped by a backslash, and no backslash left to escape the closing quote.
     */
    private fun isClosedLiteralContent(content: String): Boolean {
        var escaping = false
        for (character in content) {
            when {
                character == '\r' || character == '\n' -> return false
                escaping -> escaping = false
                character == '\\' -> escaping = true
                character == '"' -> return false
            }
        }
        return !escaping
    }

    /**
     * The literal syntax of each karpfen language, matching how its parse visitor reads string tokens.
     */
    companion object {

        /** KMeta strips every double quote at both ends of a literal. */
        val KMETA = StringLiteralSyntax("KMeta") { it.trim('"') }

        /**
         * KModel strips every double and single quote at both ends of a literal; object signatures strip
         * less, so this rule is the stricter one of the two.
         */
        val KMODEL = StringLiteralSyntax("KModel") { it.trim('"', '\'') }

        /** KStates strips one pair of double quotes and then one pair of single quotes. */
        val KSTATES = StringLiteralSyntax("KStates") {
            it.trim().removeSurrounding("\"").removeSurrounding("'")
        }
    }
}
