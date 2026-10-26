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

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Checks for every literal up to a small length that [StringLiteralSyntax] can write the value the literal reads
 * back, so that a printer never rejects a value its language has read.
 *
 * The literals are built from the chars that decide how a literal is read: a letter, a space, a backslash, both kinds
 * of quotes and a line break, between double or single quotes. Each language reads them where they stand in its
 * documents: a literal of a statement anywhere on a line, the key of a KModel object before the colon, the class type
 * and the opening brace of its block, and the class type before the brace.
 */
class StringLiteralCoverageTest {

    /**
     * Every value that a literal of a language reads back in a place can be written as a literal for that place.
     */
    @Test
    fun `every value a literal reads back can be written again`() {
        val places = listOf(
            Place("KMeta", StringLiteralSyntax.KMETA, StringLiteralSyntax.FOLLOWING_LITERALS),
            Place("KModel statements", StringLiteralSyntax.KMODEL, StringLiteralSyntax.FOLLOWING_LITERALS),
            Place("KModel object keys", StringLiteralSyntax.KMODEL_SIGNATURE, ":\"Class\" {"),
            Place("KModel class types", StringLiteralSyntax.KMODEL_SIGNATURE, " {"),
            Place("KStates", StringLiteralSyntax.KSTATES, StringLiteralSyntax.FOLLOWING_LITERALS)
        )
        places.forEach { place ->
            val readable = literals().filter { place.syntax.isStringToken(it, place.followedBy) }.toList()
            val unwritable = readable.map { place.syntax.unquote(it) }.distinct().filter { !place.writes(it) }

            assertTrue(readable.size > MINIMUM_READABLE_LITERALS, "${place.name}: only ${readable.size} literals read")
            assertEquals(emptyList(), unwritable, "${place.name}: values read from literals that cannot be written")
        }
    }

    /**
     * Returns every literal whose content has up to [MAXIMUM_CONTENT_LENGTH] of the [CONTENT_CHARS], between double
     * and between single quotes.
     */
    private fun literals(): Sequence<String> =
        contents(MAXIMUM_CONTENT_LENGTH).flatMap { content -> QUOTES.asSequence().map { "$it$content$it" } }

    private fun contents(maximumLength: Int): Sequence<String> = (0..maximumLength).asSequence().flatMap { length ->
        (0 until pow(CONTENT_CHARS.length, length)).asSequence().map { index -> contentNumbered(index, length) }
    }

    /**
     * Returns the content of [length] chars whose digits in the base of [CONTENT_CHARS] are [index].
     */
    private fun contentNumbered(index: Int, length: Int): String {
        var rest = index
        return buildString {
            repeat(length) {
                append(CONTENT_CHARS[rest % CONTENT_CHARS.length])
                rest /= CONTENT_CHARS.length
            }
        }
    }

    private fun pow(base: Int, exponent: Int): Int = (0 until exponent).fold(1) { product, _ -> product * base }

    /**
     * A place in the documents of a language where a literal stands.
     *
     * @property name The name of the place, for failure messages.
     * @property syntax The literal syntax of the language.
     * @property followedBy The text that follows a literal there.
     */
    private class Place(val name: String, val syntax: StringLiteralSyntax, val followedBy: String) {

        /**
         * Returns true if [value] can be written as a literal for this place.
         */
        fun writes(value: String): Boolean = runCatching { syntax.quote(value, followedBy) }.isSuccess
    }

    private companion object {

        /** The chars the content of a literal is built from. */
        const val CONTENT_CHARS = "a \\\"'\n"

        /** The quotes a literal is put between. */
        const val QUOTES = "\"'"

        /** The greatest length of the content of a literal. */
        const val MAXIMUM_CONTENT_LENGTH = 5

        /** The number of readable literals below which the test would prove too little. */
        const val MINIMUM_READABLE_LITERALS = 1000
    }
}
