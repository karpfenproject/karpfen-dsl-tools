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
package io.karpfen.dsl.reader

import io.karpfen.dsl.dsl.textual.KmodelDSLConverter
import io.karpfen.dsl.fixtures.StructureDump
import io.karpfen.dsl.instance.ClassTypeAtomicPropertyObject
import io.karpfen.dsl.instance.ClassTypeListPropertyObject
import io.karpfen.dsl.instance.SimpleListPropertyObject
import io.karpfen.dsl.meta.AssociationType
import io.karpfen.dsl.printer.ExampleCatalog
import io.karpfen.dsl.reader.kmeta.KmetaReader
import io.karpfen.dsl.reader.kmodel.KmodelReader
import io.karpfen.dsl.reader.kstates.KstatesReader
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Tests the readers on every example file: they find no problem, build the model the throwing converter builds,
 * and locate every element at its construct, its name and its code.
 */
class ExampleSourceMapTest {

    private val catalog = ExampleCatalog()

    /**
     * The readers report nothing for the examples and build the converted models.
     */
    @Test
    fun `the examples read without diagnostics into the converted models`() {
        val examples = ReaderUnderTest.all()
        for (example in examples) {
            val result = example.reader.read(example.text)

            assertEquals(emptyList(), result.diagnostics, "$example")
            assertEquals(example.convertedStructureOf(example.text), StructureDump.of(result.model), "$example")
        }
        assertEquals(21, examples.size)
    }

    /**
     * Every KMeta type, property, relation and relation target is located at its keyword and its name.
     */
    @Test
    fun `kmeta elements are located at their constructs and names`() {
        for (example in catalog.metamodels()) {
            val result = KmetaReader(example.baseTypes).read(example.text)
            val checker = LocationChecker(example.text, result.sourceMap)
            val fileTypes = result.model!!.types.filter { type -> example.baseTypes.none { it === type } }

            for (type in fileTypes) {
                checker.expect(type, "type ${type.name}", "type", type.name)
                for (property in type.simpleProperties) {
                    checker.expect(property, "prop ${property.key}", "prop", property.key)
                    checker.expectWithin(type, property, "prop ${property.key}")
                }
                for (relation in type.objectProperties) {
                    val keyword = if (relation.associationType == AssociationType.EMBEDDED) "has" else "knows"
                    checker.expect(relation, "$keyword ${relation.key}", keyword, relation.key)
                    checker.expect(relation.reference, "target of ${relation.key}", name = relation.reference.classTypeName)
                    checker.expectWithin(relation, relation.reference, "target of ${relation.key}")
                }
            }
            example.baseTypes.forEach { assertEquals(null, result.sourceMap.locationOf(it), "base type ${it.name}") }
            checker.assertNoDeviations("$example")
            assertTrue(fileTypes.isNotEmpty() && checker.checked >= fileTypes.size, "$example has its elements checked")
        }
    }

    /**
     * Every KModel object, assignment and reference is located at its keyword and its name, list assignments
     * once per statement.
     */
    @Test
    fun `kmodel elements are located at their constructs and names`() {
        for (example in catalog.models()) {
            val result = KmodelReader(example.metamodel).read(example.text)
            val checker = LocationChecker(example.text, result.sourceMap)
            val objects = KmodelDSLConverter.collectAllObjects(result.model!!.objects)

            for (dataObject in objects) {
                checker.expect(dataObject, "object ${dataObject.id}", "make object", dataObject.id)
                for (property in dataObject.properties) {
                    val count = if (property is SimpleListPropertyObject) property.getValues().size else 1
                    checker.expectEach(property, "prop ${property.key} of ${dataObject.id}", count, "prop", property.key)
                    checker.expectWithin(dataObject, property, "prop ${property.key} of ${dataObject.id}")
                }
                for (relation in dataObject.relations) {
                    val keyword = if (relation.ofType.associationType == AssociationType.EMBEDDED) "has" else "knows"
                    val references = when (relation) {
                        is ClassTypeAtomicPropertyObject -> listOf(relation.getValueRef())
                        is ClassTypeListPropertyObject -> relation.getValueRefs()
                        else -> emptyList()
                    }
                    val what = "$keyword ${relation.key} of ${dataObject.id}"
                    checker.expectEach(relation, what, references.size, keyword, relation.key)
                    references.forEach { checker.expect(it, "reference to ${it.objectId} in $what", keyword, it.objectId) }
                }
            }
            checker.assertNoDeviations("$example")
            assertTrue(checker.checked > objects.size, "$example has its elements checked")
        }
    }

    /**
     * Every element of a KStates state machine is located, see [StateMachineLocations].
     */
    @Test
    fun `kstates elements are located at their constructs, names and code`() {
        for (example in catalog.stateMachines()) {
            val result = KstatesReader().read(example.text)
            val checker = LocationChecker(example.text, result.sourceMap)

            StateMachineLocations(checker).check(result.model!!)

            checker.assertNoDeviations("$example")
            assertTrue(checker.checked > 50, "$example has its elements checked")
        }
    }
}
