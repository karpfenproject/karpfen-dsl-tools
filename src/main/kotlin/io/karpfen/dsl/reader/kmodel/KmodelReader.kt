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
package io.karpfen.dsl.reader.kmodel

import io.karpfen.dsl.dsl.textual.KmodelDSLConverter
import io.karpfen.dsl.grammar.kmodel.KmodelLexer
import io.karpfen.dsl.grammar.kmodel.KmodelParser
import io.karpfen.dsl.instance.DataObject
import io.karpfen.dsl.instance.Model
import io.karpfen.dsl.meta.Metamodel
import io.karpfen.dsl.reader.LexerInput
import io.karpfen.dsl.reader.ParseResult
import io.karpfen.dsl.reader.ReadingSession
import io.karpfen.dsl.reader.TextReader
import org.antlr.v4.runtime.CommonTokenStream
import org.slf4j.LoggerFactory

/**
 * Reads KModel text into a [Model] of [metamodel] without throwing, see [TextReader].
 *
 * A text without errors yields the model that [KmodelDSLConverter.parseKmodelString] builds from it, with
 * every `knows` reference resolved. Like the converter, the reader reads the text with its common indentation
 * trimmed, see [LexerInput.withIndentTrimmed]. The reader reports syntax errors, objects of unknown class types,
 * unknown properties and relations, `has` and `knows` used for the other kind of relation, values that are
 * not numbers for number properties, and `knows` references to missing objects or to objects of another class
 * type; it warns about embedded objects of another class type than their relation declares. A text with a syntax
 * error gets no report of references to missing objects, since the parser may have lost an object while it
 * recovered.
 *
 * A partial model leaves out what it cannot build: an object without a known class type, with its whole
 * content, and every statement with an unknown key, a wrong kind of relation or an invalid value. An
 * unresolvable single-valued `knows` stays in with an [io.karpfen.dsl.instance.ObjectReference] to no object,
 * an unresolvable entry of a list-valued `knows` is left out of its list. Objects whose parent is left out are
 * not part of the model but can still be referred to, so that one wrong class type does not turn every
 * reference to the objects inside it into an error; an object of the model takes precedence over such an object
 * with the same id.
 *
 * @param metamodel The metamodel the text must conform to.
 */
class KmodelReader(private val metamodel: Metamodel) : TextReader<Model> {

    private val logger = LoggerFactory.getLogger(KmodelReader::class.java)

    override fun read(text: String): ParseResult<Model> =
        ReadingSession.read(LexerInput.withIndentTrimmed(text), logger) { session ->
            val pendingLinks = PendingLinks()
            val converter = DataObjectConverter(session, metamodel, pendingLinks)
            val root = parse(session).make_object_block()?.let { converter.convert(it) }
            val rootObjects = listOfNotNull(root).toMutableList()
            val resolver = KnowsResolver(session, reportsMissingObjects = !session.hasSyntaxErrors())
            resolver.resolve(pendingLinks.all, referableObjects(rootObjects, converter.detachedObjects))
            Model(rootObjects, metamodel)
        }

    private fun parse(session: ReadingSession): KmodelParser.Kmodel_fileContext {
        val lexer = session.reportingSyntaxErrors(KmodelLexer(session.charStream()))
        val parser = session.reportingSyntaxErrors(KmodelParser(CommonTokenStream(lexer)))
        return parser.kmodel_file()
    }

    /**
     * Collects the objects `knows` may refer to: the objects outside the model, followed by the model objects in
     * the order of the throwing converter. Of several objects with the same id the last one wins, so a model
     * object wins over an object outside the model, and among model objects the converter decides.
     */
    private fun referableObjects(rootObjects: MutableList<DataObject>, detachedObjects: List<DataObject>): List<DataObject> =
        detachedObjects.flatMap { KmodelDSLConverter.collectAllObjects(mutableListOf(it)) } +
            KmodelDSLConverter.collectAllObjects(rootObjects)
}
