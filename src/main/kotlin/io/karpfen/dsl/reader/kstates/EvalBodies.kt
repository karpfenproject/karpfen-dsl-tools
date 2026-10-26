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
package io.karpfen.dsl.reader.kstates

import io.karpfen.dsl.grammar.kstates.KstatesParser
import io.karpfen.dsl.reader.ReadingSession
import io.karpfen.dsl.reader.presentToken
import io.karpfen.dsl.source.SourceRange

/**
 * The code of an `EVAL` body and where it is written.
 *
 * @property code The code, exactly as the model holds it.
 * @property codeRange The range of [code] in the text.
 */
internal class EvalBody(val code: String, val codeRange: SourceRange)

/**
 * Reads the code of `EVAL { ... }` statements the way the throwing converter does: the source text from the
 * first to the last token between the braces, comments and whitespace in between included, trimmed.
 *
 * @param session The read the statements belong to.
 */
internal class EvalBodies(private val session: ReadingSession) {

    /**
     * Reads the body of [statement]; an empty body yields empty code at the position after the opening brace.
     */
    fun read(statement: KstatesParser.Eval_statementContext): EvalBody {
        val locator = session.locator
        val block = statement.eval_code_block()
        val first = block?.start
        val last = block?.stop
        if (block == null || block.childCount == 0 || first == null || last == null || last.stopIndex < first.startIndex) {
            val brace = statement.presentToken(KstatesParser.LBRACE)
            val emptyCode = brace?.let { locator.rangeOf(it).end } ?: locator.rangeOf(statement).start
            return EvalBody("", SourceRange(emptyCode, emptyCode))
        }
        val codeRange = locator.trimmed(locator.rangeOfCodePoints(first.startIndex, last.stopIndex + 1))
        return EvalBody(locator.textOf(codeRange), codeRange)
    }
}
