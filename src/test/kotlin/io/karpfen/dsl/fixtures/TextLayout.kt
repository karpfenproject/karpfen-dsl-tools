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

/**
 * Ways of writing a document that the parsers, the readers and version 1.0 may treat differently although the
 * document stays the same or nearly so: other line breaks, an indentation every line shares, blank lines and a byte
 * order mark at the edges, string literals that an escaped line break continues on the next line, and single quotes
 * instead of double quotes.
 *
 * The differential tests read every example in each layout, and the [Mutator] writes some of its variants in one.
 */
enum class TextLayout {

    /** The text as it is. */
    AS_WRITTEN {
        override fun apply(text: String): String = text
    },

    /** Line breaks written as `\r\n`. */
    CRLF_LINE_BREAKS {
        override fun apply(text: String): String = text.replace("\n", "\r\n")
    },

    /** Line breaks written as a lone `\r`. */
    CR_LINE_BREAKS {
        override fun apply(text: String): String = text.replace("\n", "\r")
    },

    /** Every line indented by four more spaces. */
    SPACE_INDENTED {
        override fun apply(text: String): String = text.lines().joinToString("\n") { "    $it" }
    },

    /** Every line indented by one more tab. */
    TAB_INDENTED {
        override fun apply(text: String): String = text.lines().joinToString("\n") { "\t$it" }
    },

    /** Blank lines with whitespace before and after the text. */
    BLANK_EDGES {
        override fun apply(text: String): String = "\n  \n$text\n\t\n"
    },

    /** A byte order mark before the text. */
    BYTE_ORDER_MARK {
        override fun apply(text: String): String = "﻿$text"
    },

    /** Every double-quoted literal of two or more chars continued on an indented next line after its first char. */
    ESCAPED_LINE_BREAKS {
        override fun apply(text: String): String = DOUBLE_QUOTED.replace(text) { literal ->
            val content = literal.value.removeSurrounding("\"")
            if (content.length < 2) literal.value else "\"${content.first()}\\\n      ${content.drop(1)}\""
        }
    },

    /** Escaped line breaks in an indented text, where trimming the indentation changes the literals. */
    INDENTED_ESCAPED_LINE_BREAKS {
        override fun apply(text: String): String = SPACE_INDENTED.apply(ESCAPED_LINE_BREAKS.apply(text))
    },

    /** Escaped line breaks in a text with `\r\n` line breaks, where the backslash escapes the `\r`. */
    CRLF_ESCAPED_LINE_BREAKS {
        override fun apply(text: String): String = CRLF_LINE_BREAKS.apply(ESCAPED_LINE_BREAKS.apply(text))
    },

    /** Every double-quoted literal without a single quote written between single quotes. */
    SINGLE_QUOTED {
        override fun apply(text: String): String = DOUBLE_QUOTED.replace(text) { literal ->
            val content = literal.value.removeSurrounding("\"")
            if ('\'' in content || '\\' in content) literal.value else "'$content'"
        }
    };

    /**
     * Returns [text] written in this layout.
     */
    abstract fun apply(text: String): String

    /** Holds the patterns and texts the layouts share. */
    companion object {

        /** Texts without any content, which no example has. */
        val DEGENERATE_TEXTS: List<String> = listOf("", " \n\t\r\n ", "﻿")

        /** A double-quoted string literal on one line. */
        private val DOUBLE_QUOTED = Regex("\"(?:\\\\.|[^\"\\\\\\r\\n])*\"")
    }
}
