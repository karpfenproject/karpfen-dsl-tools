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

import io.karpfen.dsl.grammar.kstates.KstatesParser

/**
 * Which source text is the code of a KStates `EVAL { ... }` body, as the parse visitor and the reader read it and
 * the printer writes it.
 *
 * The body spans the text from its first to its last token between the braces, with the comments and the whitespace
 * in between. The code is that text without the whitespace at its edges; a body without tokens holds empty code.
 */
internal object EvalCodeSyntax {

    /**
     * Returns the indices of the first and the last code point of the body [block] spans in the text the parser
     * read, or null if the body holds no token or [block] is missing.
     */
    fun spanOf(block: KstatesParser.Eval_code_blockContext?): IntRange? {
        if (block == null || block.childCount == 0) {
            return null
        }
        val first = block.start ?: return null
        val last = block.stop ?: return null
        return if (last.stopIndex < first.startIndex) null else first.startIndex..last.stopIndex
    }

    /**
     * Returns the start inclusive and the end exclusive of the code in the source text of a body, which stands in
     * [text] from [start] inclusive to [end] exclusive.
     */
    fun codeBoundsIn(text: CharSequence, start: Int, end: Int): Pair<Int, Int> {
        var codeStart = start
        var codeEnd = end
        while (codeStart < codeEnd && text[codeStart].isWhitespace()) {
            codeStart += 1
        }
        while (codeEnd > codeStart && text[codeEnd - 1].isWhitespace()) {
            codeEnd -= 1
        }
        return codeStart to codeEnd
    }

    /**
     * Returns the code in [bodyText], the source text of a body or code to write as one.
     */
    fun codeOf(bodyText: String): String {
        val (start, end) = codeBoundsIn(bodyText, 0, bodyText.length)
        return bodyText.substring(start, end)
    }
}
