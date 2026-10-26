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
package io.karpfen.dsl.printer.kmeta

import io.karpfen.dsl.dsl.functional.MetamodelConstructorExample
import io.karpfen.dsl.dsl.textual.KmetaDSLConverter
import io.karpfen.dsl.fixtures.ExampleCatalog
import io.karpfen.dsl.fixtures.MetamodelExample
import io.karpfen.dsl.grammar.kmeta.KmetaLexer
import io.karpfen.dsl.grammar.kmeta.KmetaParser
import io.karpfen.dsl.printer.ElementRange
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.TestFactory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Prints the KMeta examples and small metamodels with [KmetaPrinter] and reads the text back.
 */
class KmetaPrinterTest {

    private val printer = KmetaPrinter()
    private val examples = ExampleCatalog().metamodels()

    /**
     * Parsing an example, printing it and parsing the text again yields an equal metamodel.
     */
    @TestFactory
    fun `printed metamodels parse back to equal metamodels`(): List<DynamicTest> = examples.map { example ->
        dynamicTest(example.toString()) {
            val parsed = example.parse()
            val reparsed = example.parse(printer.print(parsed, example.baseTypes))
            assertEquals(parsed.structure(), reparsed.structure())
        }
    }

    /**
     * Printing the metamodel read from printed text yields the same text again.
     */
    @TestFactory
    fun `printing is idempotent`(): List<DynamicTest> = examples.map { example ->
        dynamicTest(example.toString()) {
            val printed = printer.print(example.parse(), example.baseTypes)
            assertEquals(printed, printer.print(example.parse(printed), example.baseTypes))
        }
    }

    /**
     * Replacing the text of each type definition of an example with the printed type keeps the metamodel.
     */
    @TestFactory
    fun `a printed type replaces its definition in the document`(): List<DynamicTest> = examples.map { example ->
        dynamicTest(example.toString()) {
            val parsed = example.parse()
            syntaxTreeOf(example).type_definition().forEachIndexed { index, definition ->
                val range = ElementRange.of(definition, example.text)
                val spliced = range.replaceIn(example.text, printer.printType(parsed.types[index], range.indentLevelIn(example.text)))
                assertEquals(parsed.structure(), example.parse(spliced).structure(), spliced)
            }
        }
    }

    /**
     * Replacing the text of each rule of an example with the printed rule keeps the metamodel.
     */
    @TestFactory
    fun `a printed rule replaces its rule in the document`(): List<DynamicTest> = examples.map { example ->
        dynamicTest(example.toString()) {
            val parsed = example.parse()
            syntaxTreeOf(example).type_definition().forEachIndexed { index, definition ->
                val type = parsed.types[index]
                definition.rule_list()?.rule_().orEmpty().forEach { rule ->
                    val printed = rule.prop_rule()?.let { printer.printRule(type.getSimpleProperty(keyOf(it.STRING().text))) }
                        ?: printer.printRule(type.getObjectProperty(keyOf((rule.has_rule()?.STRING() ?: rule.knows_rule().STRING()).text)))
                    val spliced = ElementRange.of(rule, example.text).replaceIn(example.text, printed)
                    assertEquals(parsed.structure(), example.parse(spliced).structure(), spliced)
                }
            }
        }
    }

    /**
     * Each printed type stands in the printed document, so documents and fragments share one layout.
     */
    @TestFactory
    fun `printed types appear in the printed document`(): List<DynamicTest> = examples.map { example ->
        dynamicTest(example.toString()) {
            val parsed = example.parse()
            val document = printer.print(parsed)
            parsed.types.forEach { assertTrue(document.contains(printer.printType(it)), it.name) }
        }
    }

    /**
     * Rules are ordered and spaced canonically, empty types close on their header line and comments are gone.
     */
    @Test
    fun `the canonical layout`() {
        val text = """
            // a comment
            type "Point" "A point" { prop("x","number")   prop("y", "number") // trailing
            }
            type "Empty" "Nothing to see" {}
            type "Room" "The root" {
                knows("favourite", "Point")
                has("corners", list("Point"))
                prop("tags", list("string"))
                prop("open", "boolean")
            }
        """
        val expected = """
            |type "Point" "A point" {
            |    prop("x", "number")
            |    prop("y", "number")
            |}
            |
            |type "Empty" "Nothing to see" { }
            |
            |type "Room" "The root" {
            |    prop("tags", list("string"))
            |    prop("open", "boolean")
            |    has("corners", list("Point"))
            |    knows("favourite", "Point")
            |}
            |""".trimMargin()
        assertEquals(expected, printer.print(KmetaDSLConverter.parseKmetaString(text)))
    }

    /**
     * Base types are left out, and the root type is written last wherever the metamodel lists it.
     */
    @Test
    fun `base types are left out and the root type comes last`() {
        val domain = KmetaDSLConverter.parseKmetaString("""type "Vector" "A vector" { prop("x", "number") }""")
        val events = KmetaDSLConverter.parseKmetaString(
            """
            type "start" "No payload" { }
            type "teleport" "Moves" { has("to", "Vector") }
            """,
            domain.types
        )

        val withoutBase = printer.print(events, domain.types)
        val withBase = printer.print(events)

        assertFalse(withoutBase.contains("Vector\" \"A vector"))
        assertEquals(listOf("start", "teleport"), KmetaDSLConverter.parseKmetaString(withoutBase, domain.types).types.take(2).map { it.name })
        assertEquals(listOf("start", "Vector", "teleport"), KmetaDSLConverter.parseKmetaString(withBase).types.map { it.name })
    }

    /**
     * A type of the document that shares its name with a base type is not a base type: it is written, so that its
     * relations resolve to it again when the text is read back with the base types.
     */
    @Test
    fun `a type sharing the name of a base type is written`() {
        val domain = KmetaDSLConverter.parseKmetaString("""type "Vector" "domain" { prop("x", "number") }""")
        val events = KmetaDSLConverter.parseKmetaString(
            """
            type "Vector" "event local" { prop("y", "number") }
            type "teleport" "event" { has("to", "Vector") }
            """,
            domain.types
        )

        val reread = KmetaDSLConverter.parseKmetaString(printer.print(events, domain.types), domain.types)

        val target = reread.types.first { it.name == "teleport" }.objectProperties.single().reference.classType!!
        assertEquals(listOf("y"), target.simpleProperties.map { it.key })
    }

    /**
     * A metamodel built in code prints and parses back to an equal metamodel.
     */
    @Test
    fun `a metamodel built in code prints`() {
        val built = MetamodelConstructorExample().setupRobotRoomMetamodel()
        assertEquals(built.structure(), KmetaDSLConverter.parseKmetaString(printer.print(built)).structure())
    }

    /**
     * The converter writes metamodels with the printer.
     */
    @Test
    fun `the converter delegates to the printer`() {
        val metamodel = examples.first().parse()
        assertEquals(printer.print(metamodel), KmetaDSLConverter.convertToKmetaString(metamodel))
    }

    private fun syntaxTreeOf(example: MetamodelExample): KmetaParser.Kmeta_fileContext =
        KmetaParser(CommonTokenStream(KmetaLexer(CharStreams.fromString(example.text)))).kmeta_file()

    private fun keyOf(stringToken: String): String = stringToken.trim('"')
}
