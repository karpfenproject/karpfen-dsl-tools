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

import io.karpfen.dsl.meta.AssociationType
import io.karpfen.dsl.meta.SimplePropertyType

/**
 * The words of KMeta that the converter, the readers and the printers share: the keyword of each kind of relation,
 * which KModel uses as well, and the primitive property types by their names, which [SimplePropertyType.keyword]
 * holds.
 */
internal object KmetaVocabulary {

    /** The primitive property types in the order the documentation lists them. */
    private val primitiveTypesInDocumentedOrder =
        listOf(SimplePropertyType.STRING, SimplePropertyType.NUMBER, SimplePropertyType.BOOLEAN)

    /** The names of the primitive property types, in the order the documentation lists them. */
    val primitiveTypeNames: List<String> = primitiveTypesInDocumentedOrder.map { it.keyword }

    /**
     * Returns the keyword of a relation of [associationType]: `has` for an embedding, `knows` for a link.
     */
    fun relationKeyword(associationType: AssociationType): String = when (associationType) {
        AssociationType.EMBEDDED -> "has"
        AssociationType.LINK -> "knows"
    }

    /**
     * Returns the primitive property type called [name], or null if no primitive type has that name.
     */
    fun primitiveTypeNamed(name: String): SimplePropertyType? = SimplePropertyType.entries.firstOrNull { it.keyword == name }
}
