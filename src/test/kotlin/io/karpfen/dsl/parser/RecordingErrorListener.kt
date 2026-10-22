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
import org.antlr.v4.runtime.Lexer
import org.antlr.v4.runtime.RecognitionException
import org.antlr.v4.runtime.Recognizer

/**
 * An ANTLR error listener that records the syntax errors it receives together with their source.
 */
class RecordingErrorListener : BaseErrorListener() {

    /**
     * One reported syntax error.
     *
     * @property fromLexer True if the lexer reported it, false if the parser did.
     * @property message The ANTLR error message.
     */
    data class Error(val fromLexer: Boolean, val message: String)

    /** The reported errors, in reporting order. */
    val errors = mutableListOf<Error>()

    override fun syntaxError(
        recognizer: Recognizer<*, *>?,
        offendingSymbol: Any?,
        line: Int,
        charPositionInLine: Int,
        msg: String?,
        e: RecognitionException?
    ) {
        errors.add(Error(recognizer is Lexer, msg.orEmpty()))
    }
}
