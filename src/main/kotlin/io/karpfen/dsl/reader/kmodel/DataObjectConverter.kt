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
import io.karpfen.dsl.meta.ClassType
import io.karpfen.dsl.meta.Metamodel
import io.karpfen.dsl.parser.StringLiteralSyntax
import io.karpfen.dsl.reader.ReadingSession
import io.karpfen.dsl.reader.presentToken
import io.karpfen.dsl.reader.presentTokens
import org.antlr.v4.runtime.tree.TerminalNode

/**
 * Converts the `make object` blocks of a KModel parse tree into [DataObject]s of [metamodel], checks every
 * statement against the class type of its object with a [StatementMatcher] and records where each object is written.
 *
 * The `knows` references of the objects are left unresolved; they wait in the [PendingLinks] for the
 * [KnowsResolver]. An object that cannot become part of the model is still converted, so that the problems
 * inside it are found, and kept among the [detachedObjects].
 *
 * @param session The read the conversion belongs to.
 * @param metamodel The metamodel that defines the class types.
 * @param pendingLinks Receives the unresolved `knows` references.
 * @param ownership Tells which blocks may hold the statements of other objects, whose problems are not reported.
 */
internal class DataObjectConverter(
    private val session: ReadingSession,
    private val metamodel: Metamodel,
    pendingLinks: PendingLinks,
    private val ownership: StatementOwnership
) {

    /** Turns the matched `prop` statements of an object into its property values. */
    private val propertyAssignments = PropertyAssignments(session)

    /** Turns the matched `has` and `knows` statements of an object into its relation values. */
    private val relationAssignments = RelationAssignments(session, pendingLinks)

    /** The objects converted so far that are not part of the model, in text order. */
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
     * from the statements that match its class type.
     */
    private fun objectOf(
        block: KmodelParser.Make_object_blockContext,
        classType: ClassType,
        keyToken: TerminalNode,
        key: String
    ): DataObject {
        val matcher = StatementMatcher(session, classType, key, ownership.ownsItsStatements(block))
        val props = mutableListOf<PropStatement>()
        val embeddings = mutableListOf<HasStatement>()
        val links = mutableListOf<KnowsStatement>()
        for (statement in block.statement()) {
            statement.prop_statement()?.let { matcher.propStatementOf(it) }?.let { props.add(it) }
            statement.has_statement()?.let { hasStatementOf(it, matcher) }?.let { embeddings.add(it) }
            statement.knows_statement()?.let { matcher.knowsStatementOf(it) }?.let { links.add(it) }
        }
        val relations = relationAssignments.embed(embeddings) + relationAssignments.link(links, key)
        val dataObject = DataObject(classType, key, propertyAssignments.assign(props), relations.toMutableList())
        session.record(dataObject, block, keyToken)
        return dataObject
    }

    /**
     * Returns the class type that [classToken] names for the object with [key]; reports and returns null if the
     * metamodel has no such class type.
     */
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

    /**
     * Converts the object a `has` statement embeds and matches the statement with [matcher]; the object becomes
     * detached if its key names no embedding relation of the class type.
     */
    private fun hasStatementOf(statement: KmodelParser.Has_statementContext, matcher: StatementMatcher): HasStatement? {
        val childBlock = statement.make_object_block()
        val child = childBlock?.let { convert(it) }
        val keyToken = statement.presentToken(KmodelParser.STRING)
        val relation = keyToken?.let { matcher.embeddingNamedBy(it) }
        if (childBlock == null || child == null) {
            return null
        }
        if (keyToken == null || relation == null) {
            detached.add(child)
            return null
        }
        matcher.checkEmbeddedType(relation, child, childBlock)
        return HasStatement(statement, relation, keyToken, child)
    }

    /**
     * Returns the value of a string token of an object signature.
     */
    private fun signatureText(token: TerminalNode): String = StringLiteralSyntax.KMODEL_SIGNATURE.unquote(token.text)
}
