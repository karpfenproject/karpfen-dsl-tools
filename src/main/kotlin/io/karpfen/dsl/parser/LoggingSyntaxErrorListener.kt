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

import org.antlr.v4.runtime.BaseErrorListener
import org.antlr.v4.runtime.RecognitionException
import org.antlr.v4.runtime.Recognizer
import org.slf4j.Logger

/**
 * Reports the syntax errors of an ANTLR lexer or parser as warnings to [logger] instead of printing them to
 * the console.
 *
 * Error recovery stays with ANTLR: the lexer and the parser keep going after an error, exactly as they do
 * with the console listener this one replaces. The file parsers [KmetaFileParser], [KmodelFileParser] and
 * [KStatesFileParser] use it unless another listener is passed to their constructor.
 *
 * @param logger The logger the warnings go to.
 * @param languageName The name of the language being parsed, which starts every warning.
 */
class LoggingSyntaxErrorListener(
    private val logger: Logger,
    private val languageName: String
) : BaseErrorListener() {

    /**
     * Logs the error as a warning in the form `<language> syntax error at line <line>:<column> <message>`.
     */
    override fun syntaxError(
        recognizer: Recognizer<*, *>?,
        offendingSymbol: Any?,
        line: Int,
        charPositionInLine: Int,
        msg: String?,
        e: RecognitionException?
    ) {
        logger.warn("{} syntax error at line {}:{} {}", languageName, line, charPositionInLine, msg)
    }
}
