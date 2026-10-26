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

import io.karpfen.dsl.diagnostics.DiagnosticCode
import io.karpfen.dsl.grammar.kmodel.KmodelParser
import io.karpfen.dsl.instance.DataObject
import io.karpfen.dsl.meta.AssociationType
import io.karpfen.dsl.meta.ClassType
import io.karpfen.dsl.meta.ClassTypeProperty
import io.karpfen.dsl.parser.KmetaVocabulary
import io.karpfen.dsl.parser.StringLiteralSyntax
import io.karpfen.dsl.reader.ReadingSession
import io.karpfen.dsl.reader.presentTokens
import io.karpfen.dsl.source.SourceRange
import org.antlr.v4.runtime.tree.TerminalNode

/**
 * Matches the statements of one `make object` block to the class type of its object: finds the property or relation
 * that the key of each statement names, checks that a relation is of the kind the statement writes, and checks the
 * class type of an embedded object against its relation.
 *
 * A statement whose key the class type does not define, or that writes the other kind of relation, is reported at its
 * key and left out; an embedded object of another class type than its relation declares gets a warning. When the
 * statements may belong to another object, see [StatementOwnership], the matcher reports nothing: it leaves out what
 * does not match without a report, and marks the statements it matches, so that their values are not reported either.
 *
 * @param session The read the statements belong to.
 * @param classType The class type of the object.
 * @param objectKey The key of the object, which the messages name.
 * @param belongToObject False if the statements of the block may belong to other objects.
 */
internal class StatementMatcher(
    private val session: ReadingSession,
    private val classType: ClassType,
    private val objectKey: String,
    private val belongToObject: Boolean
) {

    /**
     * Matches a `prop` statement to the simple property its key names; null if its key or value is missing or the
     * class type has no such property.
     */
    fun propStatementOf(statement: KmodelParser.Prop_statementContext): PropStatement? {
        val strings = statement.presentTokens(KmodelParser.STRING)
        val keyToken = strings.getOrNull(0) ?: return null
        val valueToken = strings.getOrNull(1) ?: return null
        val key = statementText(keyToken)
        val property = classType.simpleProperties.find { it.key == key }
        if (property == null) {
            report(
                DiagnosticCode.UNKNOWN_PROPERTY,
                session.valueProblemRangeOf(keyToken),
                "Class type '${classType.name}' has no property '$key' (object '$objectKey')"
            )
            return null
        }
        return PropStatement(statement, property, keyToken, valueToken, belongToObject)
    }

    /**
     * Matches a `knows` statement to the linking relation its key names; null if its key or target is missing or
     * the key names no linking relation.
     */
    fun knowsStatementOf(statement: KmodelParser.Knows_statementContext): KnowsStatement? {
        val strings = statement.presentTokens(KmodelParser.STRING)
        val keyToken = strings.getOrNull(0) ?: return null
        val targetToken = strings.getOrNull(1) ?: return null
        val relation = relationOf(keyToken, AssociationType.LINK) ?: return null
        return KnowsStatement(statement, relation, keyToken, targetToken, belongToObject)
    }

    /**
     * Returns the embedding relation that the key of a `has` statement, [keyToken], names; null if it names none.
     */
    fun embeddingNamedBy(keyToken: TerminalNode): ClassTypeProperty? = relationOf(keyToken, AssociationType.EMBEDDED)

    /**
     * Warns at the class type of [childBlock] if [child], the object it holds, is of another class type than the
     * embedding [relation] declares.
     */
    fun checkEmbeddedType(relation: ClassTypeProperty, child: DataObject, childBlock: KmodelParser.Make_object_blockContext) {
        if (child.ofType.name != relation.reference.classTypeName) {
            report(
                DiagnosticCode.EMBEDDED_TYPE_MISMATCH,
                classNameRangeOf(childBlock),
                "Relation '${relation.key}' of class type '${classType.name}' embeds a " +
                    "'${relation.reference.classTypeName}', but object '${child.id}' is a '${child.ofType.name}'"
            )
        }
    }

    /**
     * Finds the relation [keyToken] names in the class type and checks that it is of the [expectedKind]; reports and
     * returns null otherwise.
     */
    private fun relationOf(keyToken: TerminalNode, expectedKind: AssociationType): ClassTypeProperty? {
        val key = statementText(keyToken)
        val relation = classType.objectProperties.find { it.key == key }
        val problemRange = session.valueProblemRangeOf(keyToken)
        if (relation == null) {
            report(
                DiagnosticCode.UNKNOWN_RELATION,
                problemRange,
                "Class type '${classType.name}' has no relation '$key' (object '$objectKey')"
            )
            return null
        }
        if (relation.associationType != expectedKind) {
            report(DiagnosticCode.WRONG_RELATION_KIND, problemRange, wrongKindMessage(relation))
            return null
        }
        return relation
    }

    /**
     * Returns the message for a statement that writes [relation] as the other kind of relation, with the statement
     * that writes it right.
     */
    private fun wrongKindMessage(relation: ClassTypeProperty): String {
        val keyword = KmetaVocabulary.relationKeyword(relation.associationType)
        val usage = when (relation.associationType) {
            AssociationType.LINK -> "refer to an existing object with $keyword(\"${relation.key}\") -> \"id\""
            AssociationType.EMBEDDED -> "embed an object with $keyword(\"${relation.key}\") -> make object"
        }
        return "'${relation.key}' is a $keyword relation of class type '${classType.name}'; $usage"
    }

    /**
     * Returns the range of the class type that the signature of [block] names, or the whole block without one.
     */
    private fun classNameRangeOf(block: KmodelParser.Make_object_blockContext): SourceRange {
        val classToken = block.object_signature()?.presentTokens(KmodelParser.STRING)?.getOrNull(1)
        return classToken?.let { session.valueProblemRangeOf(it, StringLiteralSyntax.KMODEL_SIGNATURE) }
            ?: session.locator.rangeOf(block)
    }

    /**
     * Reports a problem of kind [code] at [range], unless the statements may belong to other objects.
     */
    private fun report(code: DiagnosticCode, range: SourceRange, message: String) {
        if (belongToObject) {
            session.report(code, range, message)
        }
    }

    /**
     * Returns the value of a string token of a statement.
     */
    private fun statementText(token: TerminalNode): String = StringLiteralSyntax.KMODEL.unquote(token.text)
}
