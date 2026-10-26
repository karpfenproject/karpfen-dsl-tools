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

import io.karpfen.dsl.instance.ClassTypeAtomicPropertyObject
import io.karpfen.dsl.instance.ClassTypeListPropertyObject
import io.karpfen.dsl.instance.ClassTypePropertyObject
import io.karpfen.dsl.instance.ObjectReference
import io.karpfen.dsl.meta.ClassTypeProperty
import io.karpfen.dsl.parser.KmodelSemantics
import io.karpfen.dsl.reader.ReadingSession
import io.karpfen.dsl.source.SourceLocation

/**
 * Turns the checked `has` and `knows` statements of one object into its [ClassTypePropertyObject]s, the way the
 * throwing converter does: the statements of one key form a single reference or a list, see [KmodelSemantics].
 *
 * A `has` reference points to its embedded object right away; a `knows` reference names its object by id and
 * is handed to [pendingLinks] for resolution.
 *
 * @param session The read the statements belong to.
 * @param pendingLinks Receives the `knows` references.
 */
internal class RelationAssignments(
    private val session: ReadingSession,
    private val pendingLinks: PendingLinks
) {

    /**
     * Converts the `has` [statements] into relation objects, one per key in the order the keys first occur.
     */
    fun embed(statements: List<HasStatement>): List<ClassTypePropertyObject> =
        statements.groupBy { it.key }.map { (key, group) -> embedKey(key, group) }

    /**
     * Converts the `knows` [statements] of the object with key [ownerKey] into relation objects with unresolved
     * references, one per key in the order the keys first occur.
     */
    fun link(statements: List<KnowsStatement>, ownerKey: String): List<ClassTypePropertyObject> =
        statements.groupBy { it.key }.map { (key, group) -> linkKey(key, group, ownerKey) }

    private fun embedKey(key: String, group: List<HasStatement>): ClassTypePropertyObject {
        val references = group.map { ObjectReference(it.child.id, it.child) }
        val relationObject = relationObjectOf(group.first().relation, key, references)
        for ((statement, reference) in group.zip(references)) {
            session.record(relationObject, statement.context, statement.keyToken)
            val childName = session.locationOf(statement.child)?.nameRange
            session.record(reference, SourceLocation(session.locator.rangeOf(statement.context), childName))
        }
        return relationObject
    }

    private fun linkKey(key: String, group: List<KnowsStatement>, ownerKey: String): ClassTypePropertyObject {
        val references = group.map { ObjectReference(it.targetId, null) }
        val relationObject = relationObjectOf(group.first().relation, key, references)
        for ((statement, reference) in group.zip(references)) {
            session.record(relationObject, statement.context, statement.keyToken)
            session.record(reference, statement.context, statement.targetToken)
            val targetRange = session.valueProblemRangeOf(statement.targetToken)
            pendingLinks.add(PendingLink(reference, relationObject, ownerKey, targetRange, statement.belongsToObject))
        }
        return relationObject
    }

    private fun relationObjectOf(
        relation: ClassTypeProperty,
        key: String,
        references: List<ObjectReference>
    ): ClassTypePropertyObject =
        if (KmodelSemantics.formsList(references.size, relation.isList)) {
            ClassTypeListPropertyObject(relation, key, references.toMutableList())
        } else {
            ClassTypeAtomicPropertyObject(relation, key, references.single())
        }
}
