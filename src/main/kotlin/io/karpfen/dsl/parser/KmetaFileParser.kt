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

import io.karpfen.dsl.grammar.kmeta.KmetaLexer
import io.karpfen.dsl.grammar.kmeta.KmetaParser
import org.antlr.v4.runtime.ANTLRErrorListener
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.slf4j.LoggerFactory
import java.io.File

/**
 * Parses KMeta text into the syntax-level [KmetaFile] with the generated [KmetaLexer] and [KmetaParser].
 *
 * Syntax errors go to [errorListener]; the parse recovers from them and continues.
 *
 * @param errorListener Receives the syntax errors of the lexer and the parser; by default they are logged
 * as warnings, see [LoggingSyntaxErrorListener].
 */
class KmetaFileParser(
    private val errorListener: ANTLRErrorListener = LoggingSyntaxErrorListener(
        LoggerFactory.getLogger(KmetaFileParser::class.java),
        "KMeta"
    )
) {

    /**
     * Parses the KMeta file at [filePath].
     */
    fun parseFile(filePath: String): KmetaFile {
        val file = File(filePath)
        return parseString(file.readText())
    }

    /**
     * Parses the KMeta text [content].
     */
    fun parseString(content: String): KmetaFile {
        val inputStream = CharStreams.fromString(content)
        val lexer = KmetaLexer(inputStream).apply { reportSyntaxErrorsTo(errorListener) }
        val tokens = CommonTokenStream(lexer)
        val parser = KmetaParser(tokens).apply { reportSyntaxErrorsTo(errorListener) }

        val parseTree = parser.kmeta_file()
        val visitor = KmetaParseVisitor()

        return visitor.visit(parseTree) ?: KmetaFile(emptyList())
    }
}
