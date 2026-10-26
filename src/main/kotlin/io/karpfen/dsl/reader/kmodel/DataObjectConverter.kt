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
import io.karpfen.dsl.meta.Metamodel
import io.karpfen.dsl.parser.KmetaVocabulary
import io.karpfen.dsl.parser.StringLiteralSyntax
import io.karpfen.dsl.reader.ReadingSession
import io.karpfen.dsl.reader.presentToken
import io.karpfen.dsl.reader.presentTokens
import io.karpfen.dsl.source.SourceRange
import org.antlr.v4.runtime.tree.TerminalNode

/**
 * Converts the `make object` blocks of a KModel parse tree into [DataObject]s of [metamodel], checks every
 * statement against the class type of its object and records where each object is written.
 *
 * The `knows` references of the objects are left unresolved; they wait in the [PendingLinks] for the
 * [KnowsResolver]. An object that cannot become part of the model is still converted, so that the problems
 * inside it are found, and kept among the [detachedObjects].
 *
 * @param session The read the conversion belongs to.
 * @param metamodel The metamodel that defines the class types.
 * @param pendingLinks Receives the unresolved `knows` references.
 */
internal class DataObjectConverter(
    private val session: ReadingSession,
    private val metamodel: Metamodel,
    pendingLinks: PendingLinks
) {

    private val propertyAssignments = PropertyAssignments(session)

    private val relationAssignments = RelationAssignments(session, pendingLinks)

    private val detached = mutableListOf<DataObject>()

    /** The objects converted so far whose parent object or relation was left out of the model, in text order. */
    val detachedObjects: List<DataObject>
        get() = detached.toList()

    /**
     * Converts [block] and the objects embedded in it; returns null if the class type of the block is missing
     * or unknown.
     */
    fun convert(block: KmodelParser.Make_object_blockContext): DataObject? {
        val signature = block.object_signature()?.presentTokens(KmodelParser.STRING).orEmpty()
        val keyToken = signature.getOrNull(0)
        val classToken = signature.getOrNull(1)
        if (keyToken == null || classToken == null) {
            detachChildrenOf(block)
            return null
        }
        val key = signatureText(keyToken)
        val classType = classTypeOf(classToken, key)
        if (classType == null) {
            detachChildrenOf(block)
            return null
        }
        return objectOf(block, classType, keyToken, key)
    }

    /**
     * Builds the object of [block], which has the class type [classType] and the [key] written as [keyToken],
     * from its statements.
     */
    private fun objectOf(
        block: KmodelParser.Make_object_blockContext,
        classType: ClassType,
        keyToken: TerminalNode,
        key: String
    ): DataObject {
        val props = mutableListOf<PropStatement>()
        val embeddings = mutableListOf<HasStatement>()
        val links = mutableListOf<KnowsStatement>()
        for (statement in block.statement()) {
            statement.prop_statement()?.let { propStatementOf(it, classType, key) }?.let { props.add(it) }
            statement.has_statement()?.let { hasStatementOf(it, classType, key) }?.let { embeddings.add(it) }
            statement.knows_statement()?.let { knowsStatementOf(it, classType, key) }?.let { links.add(it) }
        }
        val relations = relationAssignments.embed(embeddings) + relationAssignments.link(links, key)
        val dataObject = DataObject(classType, key, propertyAssignments.assign(props), relations.toMutableList())
        session.record(dataObject, block, keyToken)
        return dataObject
    }

    private fun classTypeOf(classToken: TerminalNode, key: String): ClassType? {
        val className = signatureText(classToken)
        val classType = metamodel.types.find { it.name == className }
        if (classType == null) {
            session.report(
                DiagnosticCode.UNKNOWN_CLASS_TYPE,
                session.locator.problemRangeOf(classToken),
                "Unknown class type '$className' of object '$key'"
            )
        }
        return classType
    }

    /**
     * Converts the objects embedded in [block], whose own object is left out of the model, as detached objects.
     */
    private fun detachChildrenOf(block: KmodelParser.Make_object_blockContext) {
        for (statement in block.statement()) {
            statement.has_statement()?.make_object_block()?.let { convert(it) }?.let { detached.add(it) }
        }
    }

    private fun propStatementOf(
        statement: KmodelParser.Prop_statementContext,
        classType: ClassType,
        objectKey: String
    ): PropStatement? {
        val strings = statement.presentTokens(KmodelParser.STRING)
        val keyToken = strings.getOrNull(0) ?: return null
        val valueToken = strings.getOrNull(1) ?: return null
        val key = statementText(keyToken)
        val property = classType.simpleProperties.find { it.key == key }
        if (property == null) {
            session.report(
                DiagnosticCode.UNKNOWN_PROPERTY,
                session.locator.problemRangeOf(keyToken),
                "Class type '${classType.name}' has no property '$key' (object '$objectKey')"
            )
            return null
        }
        return PropStatement(statement, property, keyToken, valueToken)
    }

    /**
     * Converts the object a `has` statement embeds and checks the relation; the object becomes detached if the
     * relation is not an embedding relation of [classType].
     */
    private fun hasStatementOf(
        statement: KmodelParser.Has_statementContext,
        classType: ClassType,
        objectKey: String
    ): HasStatement? {
        val childBlock = statement.make_object_block()
        val child = childBlock?.let { convert(it) }
        val keyToken = statement.presentToken(KmodelParser.STRING)
        val relation = keyToken?.let { relationOf(it, classType, objectKey, AssociationType.EMBEDDED) }
        if (child == null) {
            return null
        }
        if (keyToken == null || relation == null) {
            detached.add(child)
            return null
        }
        if (child.ofType.name != relation.reference.classTypeName) {
            session.report(
                DiagnosticCode.EMBEDDED_TYPE_MISMATCH,
                classNameRangeOf(childBlock),
                "Relation '${relation.key}' of class type '${classType.name}' embeds a " +
                    "'${relation.reference.classTypeName}', but object '${child.id}' is a '${child.ofType.name}'"
            )
        }
        return HasStatement(statement, relation, keyToken, child)
    }

    private fun knowsStatementOf(
        statement: KmodelParser.Knows_statementContext,
        classType: ClassType,
        objectKey: String
    ): KnowsStatement? {
        val strings = statement.presentTokens(KmodelParser.STRING)
        val keyToken = strings.getOrNull(0) ?: return null
        val targetToken = strings.getOrNull(1) ?: return null
        val relation = relationOf(keyToken, classType, objectKey, AssociationType.LINK) ?: return null
        return KnowsStatement(statement, relation, keyToken, targetToken)
    }

    /**
     * Finds the relation [keyToken] names in [classType] and checks that it is of the [expectedKind]; reports
     * and returns null otherwise.
     */
    private fun relationOf(
        keyToken: TerminalNode,
        classType: ClassType,
        objectKey: String,
        expectedKind: AssociationType
    ): ClassTypeProperty? {
        val key = statementText(keyToken)
        val relation = classType.objectProperties.find { it.key == key }
        val problemRange = session.locator.problemRangeOf(keyToken)
        if (relation == null) {
            session.report(
                DiagnosticCode.UNKNOWN_RELATION,
                problemRange,
                "Class type '${classType.name}' has no relation '$key' (object '$objectKey')"
            )
            return null
        }
        if (relation.associationType != expectedKind) {
            session.report(DiagnosticCode.WRONG_RELATION_KIND, problemRange, wrongKindMessage(relation, classType))
            return null
        }
        return relation
    }

    private fun wrongKindMessage(relation: ClassTypeProperty, classType: ClassType): String {
        val keyword = KmetaVocabulary.relationKeyword(relation.associationType)
        val usage = when (relation.associationType) {
            AssociationType.LINK -> "refer to an existing object with $keyword(\"${relation.key}\") -> \"id\""
            AssociationType.EMBEDDED -> "embed an object with $keyword(\"${relation.key}\") -> make object"
        }
        return "'${relation.key}' is a $keyword relation of class type '${classType.name}'; $usage"
    }

    private fun classNameRangeOf(block: KmodelParser.Make_object_blockContext): SourceRange {
        val classToken = block.object_signature()?.presentTokens(KmodelParser.STRING)?.getOrNull(1)
        return classToken?.let { session.locator.problemRangeOf(it) } ?: session.locator.rangeOf(block)
    }

    /**
     * Returns the value of a string token of an object signature.
     */
    private fun signatureText(token: TerminalNode): String = StringLiteralSyntax.KMODEL_SIGNATURE.unquote(token.text)

    /**
     * Returns the value of a string token of a statement.
     */
    private fun statementText(token: TerminalNode): String = StringLiteralSyntax.KMODEL.unquote(token.text)
}
