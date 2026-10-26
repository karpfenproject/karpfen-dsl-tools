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

/** The kind of brace block a piece of code stands in. */
enum class BlockKind {

    /** The top level of a file or script, outside every brace. */
    TOP_LEVEL,

    /** The body of a class, interface or object, which holds member declarations. */
    DECLARATIONS,

    /** The body of an enum class, which holds its entries and member declarations. */
    ENUM_BODY,

    /** Any other block: a function body, a lambda or closure, an initializer, an accessor or a control-flow block. */
    CODE
}

/**
 * Follows the brace blocks of Kotlin or Groovy code while [SourceComments] scans it, so that a comment knows the kind
 * of block it stands in, [current], and how deep it is nested in parentheses there, [parenDepth]. The scanner hands
 * over the code it reads, its words, parentheses, braces and semicolons; strings, chars and comments never reach this
 * class.
 *
 * Whether a `{` opens a body of declarations is decided by the last declaration keyword written at the same depth of
 * parentheses since the previous statement boundary: `class`, `interface`, `object` and Groovy's `trait` open a body
 * of declarations, `enum class` and Groovy's `enum` an enum body, and any other keyword, such as `fun`, `val`, `init`,
 * `get` or `if`, or none opens a block of code. The exception is a `{` without keyword directly in an enum body,
 * outside parentheses, which opens the body of an enum entry, an anonymous class whose members are declarations. A
 * `constructor` right after a class keyword keeps the class, and `Foo::class` is no keyword. The boundaries are `;`,
 * `{` and the `}` of a block that was not opened inside parentheses; a lambda argument, whose block was, leaves the
 * header around it intact. The rule is lexical and can be fooled by unusual code, such as a local class without a
 * body followed by a lambda statement, which then counts as a body of declarations.
 */
class CodeBlocks {

    /**
     * One open brace block.
     *
     * @property kind What the block holds, which decides whether KDoc may stand in it.
     * @property outerHeaders The header levels of the enclosing block when this one opened, restored when it closes.
     */
    private class Frame(val kind: BlockKind, val outerHeaders: ArrayDeque<HeaderLevel>)

    /**
     * The header read so far at one depth of parentheses of the current block.
     *
     * @property keyword The last declaration keyword at this depth since the previous boundary.
     */
    private class HeaderLevel(var keyword: String? = null)

    /** The blocks open around the code read so far, innermost last. */
    private val frames = ArrayDeque<Frame>()

    /** The header levels of the current block, one per open parenthesis plus the block's own level. */
    private var headers = ArrayDeque(listOf(HeaderLevel()))

    /** The previous word read, to recognize `enum class`. */
    private var previousWord: String? = null

    /** The kind of the block the scanner is in. */
    val current: BlockKind
        get() = frames.lastOrNull()?.kind ?: BlockKind.TOP_LEVEL

    /** The number of parentheses open in the current block. */
    val parenDepth: Int
        get() = headers.size - 1

    /**
     * Reads the identifier or keyword [word]; [isMemberReference] marks a word written right after `::`.
     */
    fun word(word: String, isMemberReference: Boolean) {
        val level = headers.last()
        when {
            isMemberReference || word !in KEYWORDS -> Unit
            word == "class" && previousWord == "enum" -> level.keyword = ENUM_CLASS
            word == "enum" -> level.keyword = ENUM_CLASS
            word == "constructor" && level.keyword in CLASS_KEYWORDS -> Unit
            else -> level.keyword = word
        }
        previousWord = word
    }

    /**
     * Reads `(`.
     */
    fun openParen() {
        headers.addLast(HeaderLevel())
    }

    /**
     * Reads `)`; an unbalanced one is ignored.
     */
    fun closeParen() {
        if (headers.size > 1) {
            headers.removeLast()
        }
    }

    /**
     * Reads `;`, which ends a statement at the block's own level.
     */
    fun semicolon() {
        if (headers.size == 1) {
            headers.last().keyword = null
        }
    }

    /**
     * Reads `{`, which opens a block whose kind the header before it decides.
     */
    fun openBrace() {
        val keyword = headers.last().keyword
        val kind = when {
            keyword == ENUM_CLASS -> BlockKind.ENUM_BODY
            keyword in CLASS_KEYWORDS || opensEnumEntryBody(keyword) -> BlockKind.DECLARATIONS
            else -> BlockKind.CODE
        }
        frames.addLast(Frame(kind, headers))
        headers = ArrayDeque(listOf(HeaderLevel()))
    }

    /**
     * Reads `}`, which closes the current block; an unbalanced one is ignored.
     */
    fun closeBrace() {
        val frame = frames.removeLastOrNull() ?: return
        headers = frame.outerHeaders
        if (headers.size == 1) {
            headers.last().keyword = null
        }
    }

    /**
     * Returns true if a `{` after [keyword] opens the body of an enum entry: no keyword, directly in an enum body.
     */
    private fun opensEnumEntryBody(keyword: String?): Boolean =
        keyword == null && current == BlockKind.ENUM_BODY && parenDepth == 0

    /** The keywords that decide the kind of the next block. */
    private companion object {

        /** The keyword of an enum class header. */
        const val ENUM_CLASS = "enum class"

        /** The keywords whose block holds member declarations. */
        val CLASS_KEYWORDS = setOf("class", "interface", "object", "trait", ENUM_CLASS)

        /** The keywords that decide the kind of the next block. */
        val KEYWORDS = setOf(
            "class", "interface", "object", "trait", "enum", "fun", "val", "var", "init", "constructor", "get", "set",
            "typealias", "if", "else", "when", "for", "while", "do", "try", "catch", "finally", "return", "throw"
        )
    }
}
