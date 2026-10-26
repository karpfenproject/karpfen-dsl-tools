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
import org.antlr.v4.runtime.Token

/**
 * Decides whether code can stand in an `EVAL` block and read back unchanged, by lexing it the way KStates does.
 *
 * KStates reads the code of an `EVAL` block from its first to its last token. The braces of the code must therefore
 * balance, as the grammar counts them outside string literals and comments, or the block would end early or not at
 * all. No comment may stand before the first or after the last token, since reading would drop it; KStates reads
 * `//` as the start of a comment even where Python means floor division. Whitespace at the edges of the code is not
 * part of what reads back.
 */
internal class EvalCodeCheck {

    /**
     * Returns normally if KStates can hold [code] in an `EVAL` block.
     *
     * @throws IllegalArgumentException If the braces of [code] do not balance or a comment stands at one of its
     * ends.
     */
    fun requireExpressible(code: String) {
        val tokens = tokensOf(code)
        require(bracesBalance(tokens) && !endsInComment(tokens) && !endsInComment(tokens.asReversed())) {
            "KStates cannot hold the EVAL code <$code>: its braces must balance outside string literals and " +
                "comments, and no comment may stand before its first or after its last token, since reading the " +
                "block would drop it; KStates reads // as a comment even where Python means floor division."
        }
    }

    /**
     * Returns the tokens of [code] other than whitespace, comments included.
     */
    private fun tokensOf(code: String): List<Token> {
        val lexer = KstatesLexer(CharStreams.fromString(code)).apply { removeErrorListeners() }
        return lexer.allTokens.filter { it.type != KstatesLexer.WS }
    }

    /**
     * Returns true if the braces among [tokens] close in the order they open and none stays open.
     */
    private fun bracesBalance(tokens: List<Token>): Boolean {
        var depth = 0
        for (token in tokens) {
            when (token.type) {
                KstatesLexer.LBRACE -> depth += 1
                KstatesLexer.RBRACE -> depth -= 1
            }
            if (depth < 0) {
                return false
            }
        }
        return depth == 0
    }

    /**
     * Returns true if the first of [tokens] is a comment.
     */
    private fun endsInComment(tokens: List<Token>): Boolean = tokens.firstOrNull()?.type == KstatesLexer.LINE_COMMENT
}
