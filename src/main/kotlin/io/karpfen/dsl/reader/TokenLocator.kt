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
package io.karpfen.dsl.reader

import io.karpfen.dsl.parser.StringLiteralSyntax
import io.karpfen.dsl.source.SourceRange
import io.karpfen.dsl.source.SourceText
import org.antlr.v4.runtime.ParserRuleContext
import org.antlr.v4.runtime.Token
import org.antlr.v4.runtime.tree.TerminalNode

/**
 * Turns the tokens and contexts of an ANTLR parse of the lexed text of [input] into [SourceRange]s of [sourceText],
 * the text being read.
 *
 * ANTLR counts the code points of the lexed text, a [SourceRange] counts the UTF-16 chars of the text being read;
 * the locator converts with [CodePointOffsets] and [LexerInput]. Indices outside the text, such as those of tokens
 * the parser conjured up while recovering from an error, are clamped to the text, so the locator never fails.
 *
 * @param sourceText The text being read.
 * @param input The text the lexer read, made from [sourceText].
 */
internal class TokenLocator(private val sourceText: SourceText, private val input: LexerInput) {

    /** Turns the code point indices of ANTLR into char offsets of the lexed text. */
    private val offsets = CodePointOffsets(input.lexed)

    /**
     * Returns the range from the code point [startIndex] inclusive to [endIndex] exclusive of the lexed text.
     */
    fun rangeOfCodePoints(startIndex: Int, endIndex: Int): SourceRange {
        val start = offsets.charOffsetOf(startIndex)
        return rangeOfLexedChars(start, offsets.charOffsetOf(endIndex).coerceAtLeast(start))
    }

    /**
     * Returns the range of the text of [token].
     */
    fun rangeOf(token: Token): SourceRange = rangeOfCodePoints(token.startIndex, token.stopIndex + 1)

    /**
     * Returns the range of the text of the token of [node].
     */
    fun rangeOf(node: TerminalNode): SourceRange = rangeOf(node.symbol)

    /**
     * Returns the range from the first to the last token of [context]; an empty range at its start if the
     * context did not consume any token.
     */
    fun rangeOf(context: ParserRuleContext): SourceRange {
        val start = context.start ?: return startOfText()
        val stop = context.stop
        if (stop == null || stop.tokenIndex < start.tokenIndex) {
            return rangeOfCodePoints(start.startIndex, start.startIndex)
        }
        return rangeOfCodePoints(start.startIndex, stop.stopIndex + 1)
    }

    /**
     * Returns the range of the value of a string token: its text without the quote characters that [literals]
     * strips, so that the text of the range is the value.
     */
    fun valueRangeOf(quoted: TerminalNode, literals: StringLiteralSyntax): SourceRange {
        val token = quoted.symbol
        val start = offsets.charOffsetOf(token.startIndex)
        val end = offsets.charOffsetOf(token.stopIndex + 1).coerceAtLeast(start)
        val span = literals.valueSpanIn(input.lexed.substring(start, end))
        return rangeOfLexedChars(start + span.start, start + span.end)
    }

    /**
     * Returns the range to mark when the value of a string token is at fault: the value, see [valueRangeOf], or the
     * whole token if the value is empty, so that the mark stays visible.
     */
    fun problemRangeOf(quoted: TerminalNode, literals: StringLiteralSyntax): SourceRange =
        valueRangeOf(quoted, literals).takeIf { it.length > 0 } ?: rangeOf(quoted)

    /**
     * Returns the part of [range] that [bounds] finds: given the text being read and the start inclusive and the end
     * exclusive of [range] in it, [bounds] returns those of the part.
     */
    fun narrowed(range: SourceRange, bounds: (CharSequence, Int, Int) -> Pair<Int, Int>): SourceRange {
        val (start, end) = bounds(sourceText.text, range.start.offset, range.end.offset)
        return sourceText.rangeOf(start, end)
    }

    /**
     * Returns the text [range] covers.
     */
    fun textOf(range: SourceRange): String = range.textIn(sourceText.text)

    /**
     * Returns the empty range at the start of the text.
     */
    fun startOfText(): SourceRange = sourceText.rangeOf(0, 0)

    /**
     * Returns the range of the text being read that the chars of the lexed text from [start] inclusive to [end]
     * exclusive stem from.
     */
    private fun rangeOfLexedChars(start: Int, end: Int): SourceRange =
        sourceText.rangeOf(input.sourceStartOf(start), input.sourceEndOf(start, end))
}
