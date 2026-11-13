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

import io.karpfen.dsl.diagnostics.DiagnosticCode
import io.karpfen.dsl.grammar.kstates.KstatesLexer
import io.karpfen.dsl.grammar.kstates.KstatesParser
import io.karpfen.dsl.parser.EvalCodeReferences
import io.karpfen.dsl.reader.ReadingSession
import io.karpfen.dsl.reader.presentToken
import io.karpfen.dsl.reader.presentTokens
import org.antlr.v4.runtime.ParserRuleContext
import org.antlr.v4.runtime.TokenStream

/**
 * Warns about the code of `EVAL` bodies that does not run as meant: a `//` in the code, which starts a comment of
 * KStates there, so that a brace after it on the same line does not count and a comment at the end of the code is
 * dropped, and code without a `return`, which yields no value.
 *
 * A `//` is found among the comment tokens between the braces of a body, so it is reported also when it cut the
 * closing brace and the parser met a syntax error afterwards. The check only reports.
 *
 * @param session The read the bodies belong to; it holds their locations and receives the problems.
 */
internal class EvalBodyCheck(private val session: ReadingSession) {

    /**
     * Checks the `EVAL` statements of [tree], whose tokens [tokens] holds with the comments, and the code of
     * [evalBodies], the elements that hold the code of an `EVAL` with that code.
     */
    fun check(tree: ParserRuleContext?, tokens: TokenStream, evalBodies: List<Pair<Any, String>>) {
        tree?.let { visit(it, tokens) }
        for ((holder, code) in evalBodies) {
            if (!EvalCodeReferences.hasReturn(code)) {
                val range = session.locationOf(holder)?.range ?: continue
                session.report(
                    DiagnosticCode.EVAL_WITHOUT_RETURN,
                    range,
                    "This EVAL has no return, so it yields no value; return the value it computes"
                )
            }
        }
    }

    /** Checks [context] if it is an `EVAL` statement and visits the contexts nested in it. */
    private fun visit(context: ParserRuleContext, tokens: TokenStream) {
        if (context is KstatesParser.Eval_statementContext) {
            checkComments(context, tokens)
        }
        context.children.orEmpty().filterIsInstance<ParserRuleContext>().forEach { visit(it, tokens) }
    }

    /** Warns at every comment between the braces of [statement]. */
    private fun checkComments(statement: KstatesParser.Eval_statementContext, tokens: TokenStream) {
        val open = statement.presentToken(KstatesParser.LBRACE)?.symbol?.tokenIndex ?: return
        val close = statement.presentTokens(KstatesParser.RBRACE).lastOrNull()?.symbol?.tokenIndex
            ?: statement.stop?.tokenIndex
            ?: return
        for (index in open + 1 until close) {
            val token = tokens.get(index)
            if (token.type == KstatesLexer.LINE_COMMENT) {
                session.report(
                    DiagnosticCode.COMMENT_IN_EVAL,
                    session.locator.rangeOf(token),
                    "In an EVAL, // starts a KStates comment, so a closing brace after it on the same line does not " +
                        "count; write int(a / b) instead of a // b and Python comments with #"
                )
            }
        }
    }
}
