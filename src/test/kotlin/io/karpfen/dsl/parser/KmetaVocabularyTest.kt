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

import io.karpfen.dsl.meta.SimplePropertyType
import io.karpfen.dsl.printer.kmeta.KmetaPrinter
import io.karpfen.dsl.reader.kmeta.KmetaReader
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Checks that the keyword of each primitive property type, [SimplePropertyType.keyword], is the name by which KMeta
 * texts write it: the reader reads the type from it, the printer writes it, and the vocabulary knows it by that name.
 */
class KmetaVocabularyTest {

    /** Every primitive type reads from its keyword and prints with it again. */
    @Test
    fun `each primitive type is read from and printed with its keyword`() {
        SimplePropertyType.entries.forEach { type ->
            val rule = "prop(\"p\", \"${type.keyword}\")"

            val result = KmetaReader().read("type \"T\" \"t\" {\n    $rule\n}\n")

            assertEquals(emptyList(), result.diagnostics, type.name)
            val property = result.model!!.getTypeByName("T")!!.simpleProperties.single()
            assertEquals(type, property.propertyType)
            assertEquals(type, KmetaVocabulary.primitiveTypeNamed(type.keyword))
            assertEquals(rule, KmetaPrinter().printRule(property))
        }
    }

    /** The vocabulary lists every primitive type once, by its keyword, in the order the documentation lists them. */
    @Test
    fun `the vocabulary names every primitive type in the documented order`() {
        assertEquals(listOf("string", "number", "boolean"), KmetaVocabulary.primitiveTypeNames)
        assertEquals(SimplePropertyType.entries.map { it.keyword }.toSet(), KmetaVocabulary.primitiveTypeNames.toSet())
    }
}
