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

import io.karpfen.dsl.grammar.kmodel.KmodelLexer
import io.karpfen.dsl.grammar.kmodel.KmodelParser
import org.antlr.v4.runtime.ANTLRErrorListener
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.slf4j.LoggerFactory
import java.io.File

/**
 * Parses KModel text into the syntax-level [KmodelFile] with the generated [KmodelLexer] and [KmodelParser].
 *
 * Syntax errors go to [errorListener]; the parse recovers from them and continues.
 *
 * @param errorListener Receives the syntax errors of the lexer and the parser; by default they are logged
 * as warnings, see [LoggingSyntaxErrorListener].
 */
class KmodelFileParser(
    private val errorListener: ANTLRErrorListener = LoggingSyntaxErrorListener(
        LoggerFactory.getLogger(KmodelFileParser::class.java),
        "KModel"
    )
) {

    /**
     * Parses the KModel file at [filePath].
     */
    fun parseFile(filePath: String): KmodelFile {
        val file = File(filePath)
        return parseString(file.readText())
    }

    /**
     * Parses the KModel text [content].
     */
    fun parseString(content: String): KmodelFile {
        val inputStream = CharStreams.fromString(content)
        val lexer = KmodelLexer(inputStream).apply { reportSyntaxErrorsTo(errorListener) }
        val tokens = CommonTokenStream(lexer)
        val parser = KmodelParser(tokens).apply { reportSyntaxErrorsTo(errorListener) }

        val parseTree = parser.kmodel_file()
        val visitor = KmodelParseVisitor()

        return visitor.visit(parseTree) ?: KmodelFile(emptyList())
    }
}
