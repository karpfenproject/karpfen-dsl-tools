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
import io.karpfen.dsl.meta.AssociationType
import io.karpfen.dsl.meta.ClassType
import io.karpfen.dsl.reader.ReadingSession

/**
 * Resolves the target types of the relations of KMeta class types and reports those it cannot resolve at the
 * target type name.
 *
 * @param session The read the resolution belongs to; it holds the locations of the references.
 */
internal class TargetTypeResolver(private val session: ReadingSession) {

    /**
     * Resolves the relations of [classTypes] against [resolvableTypes], taking the first type of a name; an
     * unresolvable reference keeps a null class type.
     */
    fun resolve(classTypes: List<ClassType>, resolvableTypes: List<ClassType>) {
        for (classType in classTypes) {
            for (relation in classType.objectProperties) {
                val reference = relation.reference
                val target = resolvableTypes.find { it.name == reference.classTypeName }
                if (target != null) {
                    reference.classType = target
                } else {
                    session.report(
                        DiagnosticCode.UNKNOWN_TARGET_TYPE,
                        session.problemRangeOf(reference),
                        "Unknown class type '${reference.classTypeName}' of ${keywordOf(relation.associationType)} " +
                            "'${relation.key}' in type '${classType.name}'"
                    )
                }
            }
        }
    }

    private fun keywordOf(associationType: AssociationType): String = when (associationType) {
        AssociationType.EMBEDDED -> "has"
        AssociationType.LINK -> "knows"
    }
}
