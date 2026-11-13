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
import io.karpfen.dsl.meta.ClassType
import io.karpfen.dsl.reader.ReadingSession
import io.karpfen.dsl.reader.repetitionsBy

/**
 * Reports the names a KMeta text defines more than once: two types of the same name, and two properties or
 * relations, or a property and a relation, with the same key in one type.
 *
 * Every definition after the first one of a name is reported at its name; the first one stands. The check only
 * reports and leaves the metamodel as it is.
 *
 * @param session The read the types belong to; it holds their locations and receives the problems.
 */
internal class DuplicateDefinitionCheck(private val session: ReadingSession) {

    /**
     * Checks the types of the text, [classTypes], in text order.
     */
    fun check(classTypes: List<ClassType>) {
        for (repetition in classTypes.repetitionsBy { it.name }) {
            session.report(
                DiagnosticCode.DUPLICATE_TYPE,
                session.problemRangeOf(repetition.element),
                "Type '${repetition.element.name}' is defined twice; the first definition is in line " +
                    "${lineOf(repetition.first)}. Rename or remove one of them."
            )
        }
        classTypes.forEach { checkKeys(it) }
    }

    /** Reports the properties and relations of [classType] whose key an earlier one of the type has. */
    private fun checkKeys(classType: ClassType) {
        val members = classType.simpleProperties.map { it.key to it } + classType.objectProperties.map { it.key to it }
        val inTextOrder = members.sortedBy { (_, member) -> session.locationOf(member)?.range?.start?.offset ?: Int.MAX_VALUE }
        for (repetition in inTextOrder.repetitionsBy { it.first }) {
            val (key, member) = repetition.element
            session.report(
                DiagnosticCode.DUPLICATE_KEY,
                session.problemRangeOf(member),
                "Type '${classType.name}' has two properties or relations with the key '$key'; " +
                    "the first one is in line ${lineOf(repetition.first.second)}. Rename or remove one of them."
            )
        }
    }

    /** Returns the line in which [element] is defined, or 0 if it has no location. */
    private fun lineOf(element: Any): Int = session.locationOf(element)?.range?.start?.line ?: 0
}
