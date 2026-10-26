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
package io.karpfen.dsl.printer.kmodel

import io.karpfen.dsl.dsl.functional.MetamodelConstructorExample
import io.karpfen.dsl.dsl.functional.ModelConstructorExample
import io.karpfen.dsl.dsl.textual.KmetaDSLConverter
import io.karpfen.dsl.dsl.textual.KmodelDSLConverter
import io.karpfen.dsl.fixtures.ExampleCatalog
import io.karpfen.dsl.fixtures.ModelExample
import io.karpfen.dsl.grammar.kmodel.KmodelLexer
import io.karpfen.dsl.grammar.kmodel.KmodelParser
import io.karpfen.dsl.instance.ClassTypeAtomicPropertyObject
import io.karpfen.dsl.instance.ClassTypeListPropertyObject
import io.karpfen.dsl.instance.DataObject
import io.karpfen.dsl.instance.Model
import io.karpfen.dsl.instance.ObjectReference
import io.karpfen.dsl.printer.ElementRange
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.TestFactory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Prints the KModel examples and small models with [KmodelPrinter] and reads the text back.
 */
class KmodelPrinterTest {

    private val printer = KmodelPrinter()
    private val examples = ExampleCatalog().models()

    private val metamodel = KmetaDSLConverter.parseKmetaString(
        """
        type "Point" "A point" { prop("x", "number") }
        type "Robot" "A robot" {
            prop("name", "string")
            prop("log", list("string"))
            prop("on", "boolean")
            has("position", "Point")
            knows("buddies", list("Robot"))
        }
        type "Room" "The root" {
            has("robots", list("Robot"))
            has("origin", "Point")
        }
        """
    )

    /**
     * Parsing an example, printing it and parsing the text again yields an equal model.
     */
    @TestFactory
    fun `printed models parse back to equal models`(): List<DynamicTest> = examples.map { example ->
        dynamicTest(example.toString()) {
            val parsed = example.parse()
            assertEquals(parsed.structure(), example.parse(printer.print(parsed)).structure())
        }
    }

    /**
     * Printing the model read from printed text yields the same text again.
     */
    @TestFactory
    fun `printing is idempotent`(): List<DynamicTest> = examples.map { example ->
        dynamicTest(example.toString()) {
            val printed = printer.print(example.parse())
            assertEquals(printed, printer.print(example.parse(printed)))
        }
    }

    /**
     * Replacing the text of each object of an example with the printed object keeps the model.
     */
    @TestFactory
    fun `a printed object replaces its block in the document`(): List<DynamicTest> = examples.map { example ->
        dynamicTest(example.toString()) {
            val parsed = example.parse()
            val objectsById = KmodelDSLConverter.collectAllObjects(parsed.objects).groupBy { it.id }
            objectBlocksOf(syntaxTreeOf(example).make_object_block()).forEach { block ->
                val obj = objectsById.getValue(block.object_signature().STRING(0).text.trim('"', '\'')).single()
                val range = ElementRange.of(block, example.text)
                val spliced = range.replaceIn(example.text, printer.printObject(obj, range.indentLevelIn(example.text)))
                assertEquals(parsed.structure(), example.parse(spliced).structure(), spliced)
            }
        }
    }

    /**
     * Each printed object stands in the printed document, so documents and fragments share one layout.
     */
    @TestFactory
    fun `printed objects appear in the printed document`(): List<DynamicTest> = examples.map { example ->
        dynamicTest(example.toString()) {
            val parsed = example.parse()
            val document = printer.print(parsed)
            depthsOf(parsed.objects.single(), 0).forEach { (obj, depth) ->
                assertTrue(document.contains(printer.printObject(obj, depth)), obj.id)
            }
        }
    }

    /**
     * Properties come before relations, list values become one statement each, numbers are written as
     * doubles, empty objects close on their header line and comments are gone.
     */
    @Test
    fun `the canonical layout`() {
        val text = """
            make object "room":"Room" {
                // a comment
                has("robots") -> make object "r1":"Robot" { knows("buddies") -> "r2" prop("name") -> "Robby"
                    has("position") -> make object "p1":"Point" { prop("x") -> "1" }
                    prop("log") -> "a" prop("log") -> "b" }
                has("robots") -> make object "r2":"Robot" { prop("on") -> "true" }
                has("origin") -> make object "":"Point" {}
            }
        """
        val expected = """
            |make object "room":"Room" {
            |    has("robots") -> make object "r1":"Robot" {
            |        prop("name") -> "Robby"
            |        prop("log") -> "a"
            |        prop("log") -> "b"
            |        has("position") -> make object "p1":"Point" {
            |            prop("x") -> "1.0"
            |        }
            |        knows("buddies") -> "r2"
            |    }
            |    has("robots") -> make object "r2":"Robot" {
            |        prop("on") -> "true"
            |    }
            |    has("origin") -> make object "":"Point" { }
            |}
            |""".trimMargin()
        assertEquals(expected, printer.print(KmodelDSLConverter.parseKmodelString(text, metamodel)))
    }

    /**
     * A model built in code holds all objects it created; the printer starts from the room, which nothing
     * embeds, and the text parses back.
     */
    @Test
    fun `a model built in code prints from its root object`() {
        val builtMetamodel = MetamodelConstructorExample().setupRobotRoomMetamodel()
        val built = ModelConstructorExample().setupRobotRoomModel(builtMetamodel)

        val printed = printer.print(built)

        assertTrue(printed.startsWith("make object \"APB 2101\":\"Room\" {"), printed)
        assertEquals(printed, printer.print(KmodelDSLConverter.parseKmodelString(printed, builtMetamodel)))
    }

    /**
     * A model without a unique root object cannot be written as one KModel document.
     */
    @Test
    fun `a model without a unique root is rejected`() {
        val point = KmodelDSLConverter.parseSingleObject("""make object "a":"Point" { }""", metamodel)
        val otherPoint = KmodelDSLConverter.parseSingleObject("""make object "b":"Point" { }""", metamodel)

        assertFailsWith<IllegalArgumentException> { printer.print(Model(mutableListOf(point, otherPoint), metamodel)) }
        assertFailsWith<IllegalArgumentException> { printer.print(Model(mutableListOf(), metamodel)) }
    }

    /**
     * An object that embeds itself is rejected instead of being written forever.
     */
    @Test
    fun `an object that embeds itself is rejected`() {
        val nodeMetamodel = KmetaDSLConverter.parseKmetaString("""type "Node" "A node" { has("child", "Node") }""")
        val nodeType = nodeMetamodel.rootClass
        val node = DataObject(nodeType, "n", mutableListOf(), mutableListOf())
        node.relations.add(ClassTypeAtomicPropertyObject(nodeType.getObjectProperty("child"), "child", ObjectReference("n", node)))

        assertFailsWith<IllegalArgumentException> { printer.printObject(node) }
    }

    /**
     * An object embedded under two relations, or twice in one list, would read back as several objects, so it is
     * rejected instead of written twice.
     */
    @Test
    fun `an object embedded more than once is rejected`() {
        val point = objectOf("Point", "p")
        val sharedPoint = objectOf("Room", "room", "origin" to listOf(point), "robots" to listOf(objectOf("Robot", "r", "position" to listOf(point))))
        val robot = objectOf("Robot", "r")
        val twiceInList = objectOf("Room", "room", "robots" to listOf(robot, robot))

        assertFailsWith<IllegalArgumentException> { printer.printObject(sharedPoint) }
        assertFailsWith<IllegalArgumentException> { printer.printObject(twiceInList) }
    }

    /**
     * KModel refers to a linked object by its id, so a link to an object of the document without an id is rejected
     * instead of left out; a link to such an object outside the document is the default object of a builder and is
     * left out.
     */
    @Test
    fun `a link to an object without an id is rejected inside the document and left out outside it`() {
        val anonymous = objectOf("Robot", "")
        val linking = objectOf("Robot", "r", "buddies" to listOf(anonymous))
        val inside = objectOf("Room", "room", "robots" to listOf(anonymous, linking))
        val outside = objectOf("Room", "room", "robots" to listOf(linking))

        assertFailsWith<IllegalArgumentException> { printer.printObject(inside) }
        assertFalse("knows" in printer.printObject(outside), printer.printObject(outside))
    }

    /**
     * The converter writes models with the printer.
     */
    @Test
    fun `the converter delegates to the printer`() {
        val model = examples.first().parse()
        assertEquals(printer.print(model), KmodelDSLConverter.convertToKmodelString(model))
    }

    /**
     * Returns an object of the class type [typeName] of [metamodel] with the id [id] and the given [relations], each a
     * key with the objects it holds, resolved.
     */
    private fun objectOf(typeName: String, id: String, vararg relations: Pair<String, List<DataObject>>): DataObject {
        val type = metamodel.getTypeByName(typeName)!!
        val obj = DataObject(type, id, mutableListOf(), mutableListOf())
        relations.forEach { (key, targets) ->
            val property = type.getObjectProperty(key)
            val references = targets.map { ObjectReference(it.id, it) }
            val relation = if (property.isList) {
                ClassTypeListPropertyObject(property, key, references.toMutableList())
            } else {
                ClassTypeAtomicPropertyObject(property, key, references.single())
            }
            obj.relations.add(relation)
        }
        return obj
    }

    private fun syntaxTreeOf(example: ModelExample): KmodelParser.Kmodel_fileContext =
        KmodelParser(CommonTokenStream(KmodelLexer(CharStreams.fromString(example.text)))).kmodel_file()

    private fun objectBlocksOf(block: KmodelParser.Make_object_blockContext): List<KmodelParser.Make_object_blockContext> =
        listOf(block) + block.statement().mapNotNull { it.has_statement()?.make_object_block() }.flatMap { objectBlocksOf(it) }

    private fun depthsOf(obj: DataObject, depth: Int): List<Pair<DataObject, Int>> =
        listOf(obj to depth) + ObjectTree().embeddedObjectsOf(obj).flatMap { depthsOf(it, depth + 1) }
}
