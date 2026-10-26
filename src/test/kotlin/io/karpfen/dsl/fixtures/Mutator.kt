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
package io.karpfen.dsl.fixtures

import java.util.Random

/**
 * One mutated text.
 *
 * @property index The number of the variant, which together with the file path determines the mutation.
 * @property text The mutated text.
 */
data class Variant(val index: Int, val text: String)

/**
 * Derives broken and altered variants of a text by one to three random edits, deterministically: the same file
 * and variant number always yield the same text, on every machine.
 *
 * The edits delete, duplicate and swap characters and lines, insert words of [vocabulary], replace the content of
 * string literals with that of others and cut the text off, which produces syntax errors as well as unknown
 * names, wrong references and wrong arities.
 *
 * @param vocabulary The words to insert, typically keywords, punctuation and snippets of the language.
 */
class Mutator(private val vocabulary: List<String>) {

    /**
     * Returns the variants number 0 until [count] of [text], the content of the file at [path], which together
     * with the variant number determines each mutation.
     */
    fun variantsOf(path: String, text: String, count: Int): Sequence<Variant> =
        (0 until count).asSequence().map { Variant(it, mutate(text, seedOf(path, it))) }

    /**
     * Applies one to three edits chosen by [seed] to [text].
     */
    fun mutate(text: String, seed: Long): String {
        val random = Random(seed)
        var mutated = text
        repeat(1 + random.nextInt(3)) {
            val edit = Edit.entries[random.nextInt(Edit.entries.size)]
            mutated = edit.apply(mutated, random, vocabulary)
        }
        return mutated
    }

    private fun seedOf(path: String, index: Int): Long = path.hashCode().toLong() * 1_000_003L + index

    /**
     * The kinds of edits.
     */
    private enum class Edit {

        /** Deletes up to twelve characters. */
        DELETE_SPAN {
            override fun apply(text: String, random: Random, vocabulary: List<String>): String {
                if (text.isEmpty()) {
                    return text
                }
                val start = random.nextInt(text.length)
                return text.removeRange(start, minOf(text.length, start + 1 + random.nextInt(12)))
            }
        },

        /** Inserts a word of the vocabulary. */
        INSERT_WORD {
            override fun apply(text: String, random: Random, vocabulary: List<String>): String =
                text.insert(random.nextInt(text.length + 1), vocabulary[random.nextInt(vocabulary.size)])
        },

        /** Copies up to forty characters to another place. */
        DUPLICATE_SPAN {
            override fun apply(text: String, random: Random, vocabulary: List<String>): String {
                if (text.isEmpty()) {
                    return text
                }
                val start = random.nextInt(text.length)
                val span = text.substring(start, minOf(text.length, start + 1 + random.nextInt(40)))
                return text.insert(random.nextInt(text.length + 1), span)
            }
        },

        /** Deletes a line. */
        DELETE_LINE {
            override fun apply(text: String, random: Random, vocabulary: List<String>): String {
                val lines = text.split('\n').toMutableList()
                lines.removeAt(random.nextInt(lines.size))
                return lines.joinToString("\n")
            }
        },

        /** Copies a line to another place. */
        DUPLICATE_LINE {
            override fun apply(text: String, random: Random, vocabulary: List<String>): String {
                val lines = text.split('\n').toMutableList()
                lines.add(random.nextInt(lines.size + 1), lines[random.nextInt(lines.size)])
                return lines.joinToString("\n")
            }
        },

        /** Swaps two lines. */
        SWAP_LINES {
            override fun apply(text: String, random: Random, vocabulary: List<String>): String {
                val lines = text.split('\n').toMutableList()
                val first = random.nextInt(lines.size)
                val second = random.nextInt(lines.size)
                lines[first] = lines[second].also { lines[second] = lines[first] }
                return lines.joinToString("\n")
            }
        },

        /** Replaces the content of a string literal with that of another one, an altered or an empty one. */
        REPLACE_STRING {
            override fun apply(text: String, random: Random, vocabulary: List<String>): String {
                val literals = STRING_LITERAL.findAll(text).toList()
                if (literals.isEmpty()) {
                    return INSERT_WORD.apply(text, random, vocabulary)
                }
                val target = literals[random.nextInt(literals.size)]
                val donor = literals[random.nextInt(literals.size)].value.removeSurrounding("\"")
                val content = when (random.nextInt(4)) {
                    0, 1 -> donor
                    2 -> donor.dropLast(1) + "x"
                    else -> ""
                }
                return text.replaceRange(target.range, "\"$content\"")
            }
        },

        /** Cuts the text off. */
        TRUNCATE {
            override fun apply(text: String, random: Random, vocabulary: List<String>): String =
                text.substring(0, random.nextInt(text.length + 1))
        };

        /**
         * Applies the edit to [text], with choices taken from [random].
         */
        abstract fun apply(text: String, random: Random, vocabulary: List<String>): String
    }

    private companion object {

        /** A double-quoted string literal on one line. */
        val STRING_LITERAL = Regex("\"[^\"\\r\\n]*\"")

        fun String.insert(index: Int, insertion: String): String = substring(0, index) + insertion + substring(index)
    }
}

/**
 * The vocabularies the [Mutator] inserts for each language: keywords, punctuation, strings and small snippets,
 * plus a few characters the languages do not know.
 */
object Vocabularies {

    private val common = listOf("{", "}", "(", ")", ",", "\"", "'", "//", "\n", " ", "#", "😀")

    /** The vocabulary for KMeta texts. */
    val KMETA: List<String> = common + listOf(
        "type", "prop", "has", "knows", "list", "\"x\"", "\"number\"", "\"string\"", "\"boolean\"", "\"nmbr\"",
        "list(\"Point\")", "prop(\"p\", \"number\")", "has(\"h\", \"Missing\")", "knows(\"k\", list(\"Point\"))",
        "type \"T\" \"d\" { }"
    )

    /** The vocabulary for KModel texts. */
    val KMODEL: List<String> = common + listOf(
        "make", "object", "prop", "has", "knows", "->", ":", "\"x\"", "\"1.0\"", "\"abc\"", "\"true\"",
        "prop(\"x\") -> \"1\"", "knows(\"k\") -> \"id\"", "has(\"h\") -> make object \"o\":\"T\" { }",
        "make object \"n\":\"Point\" { }"
    )

    /** The vocabulary for KStates texts. */
    val KSTATES: List<String> = common + listOf(
        "STATEMACHINE", "ATTACHED", "TO", "STATES", "STATE", "INITIAL ", "ENTRY", "DO", "SET", "APPEND", "SETLIST",
        "DROPLIST", "SETOBJ", "APPENDOBJ", "DROPOBJ", "EVENT", "WITH", "AS", "MACRO", "EVAL", "TRANSITIONS",
        "TRANSITION", "SPLIT", "JOIN", "NOT LOOPING", "CONDITION", "VALUE", "MACROS", "TAKES", "RETURNS",
        "DEFINITION", "reference", "IF IN SCOPE", "->", "\"x\"", "EVAL { return 1 }", "SET(\"x\")",
        "DROPLIST(\"a\", \"b\")", "SETOBJ(\"a\", \"b\")", "STATE \"s\" { }", "INITIAL STATE \"i\" { }",
        "TRANSITION \"a\" -> \"b\" { }", "CONDITION { VALUE(\"v\") }"
    )
}
