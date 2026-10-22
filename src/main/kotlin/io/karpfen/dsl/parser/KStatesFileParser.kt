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

import io.karpfen.dsl.grammar.kstates.KstatesLexer
import io.karpfen.dsl.grammar.kstates.KstatesParser
import org.antlr.v4.runtime.ANTLRErrorListener
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.slf4j.LoggerFactory
import java.io.File

/**
 * Parses KStates text into the syntax-level [KstatesFile] with the generated [KstatesLexer] and
 * [KstatesParser].
 *
 * Syntax errors go to [errorListener]; the parse recovers from them and continues.
 *
 * @param errorListener Receives the syntax errors of the lexer and the parser; by default they are logged
 * as warnings, see [LoggingSyntaxErrorListener].
 */
class KStatesFileParser(
    private val errorListener: ANTLRErrorListener = LoggingSyntaxErrorListener(
        LoggerFactory.getLogger(KStatesFileParser::class.java),
        "KStates"
    )
) {

    /**
     * Parses the KStates file at [filePath].
     *
     * @throws IllegalArgumentException If an action rule is written with the wrong number of operands.
     */
    fun parseFile(filePath: String): KstatesFile {
        val file = File(filePath)
        return parseString(file.readText())
    }

    /**
     * Parses the KStates text [content].
     *
     * @throws IllegalArgumentException If an action rule is written with the wrong number of operands.
     */
    fun parseString(content: String): KstatesFile {
        val inputStream = CharStreams.fromString(content)
        val lexer = KstatesLexer(inputStream).apply { reportSyntaxErrorsTo(errorListener) }
        val tokens = CommonTokenStream(lexer)
        val parser = KstatesParser(tokens).apply { reportSyntaxErrorsTo(errorListener) }

        val parseTree = parser.kstates_file()
        val visitor = KstatesParseVisitor()

        return visitor.visit(parseTree) ?: KstatesFile("", emptyList(), emptyList(), emptyList())
    }
}
