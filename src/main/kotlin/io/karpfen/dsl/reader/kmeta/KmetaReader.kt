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
package io.karpfen.dsl.reader.kmeta

import io.karpfen.dsl.diagnostics.DiagnosticCode
import io.karpfen.dsl.grammar.kmeta.KmetaLexer
import io.karpfen.dsl.grammar.kmeta.KmetaParser
import io.karpfen.dsl.meta.ClassType
import io.karpfen.dsl.meta.Metamodel
import io.karpfen.dsl.reader.LexerInput
import io.karpfen.dsl.reader.ParseResult
import io.karpfen.dsl.reader.ReadingSession
import io.karpfen.dsl.reader.TextReader
import org.antlr.v4.runtime.CommonTokenStream
import org.slf4j.LoggerFactory

/**
 * Reads KMeta text into a [Metamodel] without throwing, see [TextReader].
 *
 * A text without errors yields the metamodel that
 * [io.karpfen.dsl.dsl.textual.KmetaDSLConverter.parseKmetaString] builds from it: the types of the text
 * followed by the base types, with the last type of the text as root. Like the converter, the reader reads the text
 * with its common indentation trimmed, see [LexerInput.withIndentTrimmed]. The reader reports syntax errors, a
 * `prop` of a type other than `string`, `number` and `boolean` at that type, a relation to an unknown class
 * type at the type name, and a text without any type, which yields no model. A text with a syntax error gets no
 * report of unknown class types, since the parser may have lost the definition of a type while it recovered.
 *
 * In a partial metamodel, a type without a name, a rule without key or type, and a `prop` of an unknown type
 * are left out. A relation to an unknown class type stays in, with a [io.karpfen.dsl.meta.ClassTypeReference]
 * whose class type is null.
 *
 * @param baseTypes Class types defined elsewhere that the types of the text may refer to without defining
 * them, such as the domain types the payload of an event embeds; they become part of the metamodel, but not
 * of its source map.
 */
class KmetaReader(baseTypes: List<ClassType> = emptyList()) : TextReader<Metamodel> {

    private val baseTypes = baseTypes.toList()

    private val logger = LoggerFactory.getLogger(KmetaReader::class.java)

    override fun read(text: String): ParseResult<Metamodel> =
        ReadingSession.read(LexerInput.withIndentTrimmed(text), logger) { session ->
            val classTypes = ClassTypeConverter(session).convert(parse(session).type_definition())
            val resolver = TargetTypeResolver(session, reportsUnknownTypes = !session.hasSyntaxErrors())
            resolver.resolve(classTypes, classTypes + baseTypes)
            metamodelOf(classTypes, session)
        }

    private fun parse(session: ReadingSession): KmetaParser.Kmeta_fileContext {
        val lexer = session.reportingSyntaxErrors(KmetaLexer(session.charStream()))
        val parser = session.reportingSyntaxErrors(KmetaParser(CommonTokenStream(lexer)))
        return parser.kmeta_file()
    }

    /**
     * Builds the metamodel of [classTypes] and the base types, rooted in the last of [classTypes]; reports a
     * text without types and returns null for it.
     */
    private fun metamodelOf(classTypes: List<ClassType>, session: ReadingSession): Metamodel? {
        if (classTypes.isEmpty()) {
            session.report(
                DiagnosticCode.NO_TYPES,
                session.locator.startOfText(),
                "The text defines no type; a KMeta text needs at least one, and its last type is the root type"
            )
            return null
        }
        return Metamodel((classTypes + baseTypes).toMutableList(), classTypes.last())
    }
}
