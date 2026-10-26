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
import io.karpfen.dsl.instance.ClassTypeListPropertyObject
import io.karpfen.dsl.instance.DataObject
import io.karpfen.dsl.parser.KmodelSemantics
import io.karpfen.dsl.reader.ReadingSession

/**
 * Resolves `knows` references to the objects they name by id, the way the throwing converter does, and
 * reports those it cannot resolve at the id.
 *
 * A reference is resolved in place, so its location in the source map stays valid. An unresolvable entry of a
 * list is removed from the list, so that the list can still be read; a single unresolvable reference stays,
 * pointing to no object. A reference whose statement may belong to another object, see [StatementOwnership], is
 * resolved the same way, but its problems are not reported.
 *
 * @param session The read the references belong to.
 * @param reportsMissingObjects False to leave out the report of a reference to a missing object, for a text whose
 * syntax errors may have cost the object; the reference is left unresolved all the same.
 */
internal class KnowsResolver(
    private val session: ReadingSession,
    private val reportsMissingObjects: Boolean
) {

    /**
     * Resolves [links] against those of [objects] that can be referred to, see [KmodelSemantics.referableById].
     */
    fun resolve(links: List<PendingLink>, objects: Collection<DataObject>) {
        val objectsById = KmodelSemantics.referableById(objects)
        for (link in links) {
            val target = objectsById[link.reference.objectId]
            when {
                target == null -> drop(
                    link,
                    DiagnosticCode.UNRESOLVED_REFERENCE.takeIf { reportsMissingObjects },
                    "No object has the id '${link.reference.objectId}' that knows '${link.relation.key}' " +
                        "of object '${link.ownerKey}' refers to"
                )
                target.ofType.name != link.relation.getClassType() -> drop(
                    link,
                    DiagnosticCode.REFERENCE_TYPE_MISMATCH,
                    "Object '${target.id}' is a '${target.ofType.name}', but knows '${link.relation.key}' " +
                        "of object '${link.ownerKey}' refers to a '${link.relation.getClassType()}'"
                )
                else -> {
                    link.reference.obj = target
                    link.reference.objectId = target.id
                }
            }
        }
    }

    /**
     * Reports why [link] cannot be resolved, unless [code] is null or the statement of the link may belong to another
     * object, and takes its reference out of a list relation.
     */
    private fun drop(link: PendingLink, code: DiagnosticCode?, message: String) {
        code?.takeIf { link.belongsToOwner }?.let { session.report(it, link.targetRange, message) }
        val relation = link.relation
        if (relation is ClassTypeListPropertyObject) {
            relation.removeById(link.reference.objectId)
            session.forget(link.reference)
        }
    }
}
