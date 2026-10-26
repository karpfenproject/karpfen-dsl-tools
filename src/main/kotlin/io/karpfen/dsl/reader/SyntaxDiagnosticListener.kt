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

import io.karpfen.dsl.diagnostics.DiagnosticCode
import org.antlr.v4.runtime.BaseErrorListener
import org.antlr.v4.runtime.Lexer
import org.antlr.v4.runtime.RecognitionException
import org.antlr.v4.runtime.Recognizer
import org.antlr.v4.runtime.Token

/**
 * Turns the syntax errors of an ANTLR lexer or parser into diagnostics with ranges.
 *
 * A lexer error covers the characters the lexer could not match, a parser error the token the parser
 * stumbled over, which is the empty range at the end of the text for an unexpected end of input. ANTLR's own
 * message describes the error. Recovery stays with ANTLR.
 *
 * @param locator Turns the positions ANTLR reports into ranges.
 * @param diagnostics Receives the diagnostics.
 */
internal class SyntaxDiagnosticListener(
    private val locator: TokenLocator,
    private val diagnostics: DiagnosticCollector
) : BaseErrorListener() {

    /**
     * Reports one syntax error as an [DiagnosticCode.UNRECOGNIZED_INPUT] of the lexer or a
     * [DiagnosticCode.SYNTAX_ERROR] of the parser.
     */
    override fun syntaxError(
        recognizer: Recognizer<*, *>?,
        offendingSymbol: Any?,
        line: Int,
        charPositionInLine: Int,
        msg: String?,
        e: RecognitionException?
    ) {
        val message = msg ?: "syntax error"
        if (recognizer is Lexer) {
            val range = locator.rangeOfCodePoints(recognizer._tokenStartCharIndex, recognizer.inputStream.index() + 1)
            diagnostics.report(DiagnosticCode.UNRECOGNIZED_INPUT, range, message)
        } else {
            val range = (offendingSymbol as? Token)?.let { locator.rangeOf(it) } ?: locator.startOfText()
            diagnostics.report(DiagnosticCode.SYNTAX_ERROR, range, message)
        }
    }
}
