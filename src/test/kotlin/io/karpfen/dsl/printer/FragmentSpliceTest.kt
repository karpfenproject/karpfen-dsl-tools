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
package io.karpfen.dsl.printer

import io.karpfen.dsl.dsl.textual.KmetaDSLConverter
import io.karpfen.dsl.dsl.textual.KmodelDSLConverter
import io.karpfen.dsl.fixtures.ExampleCatalog
import io.karpfen.dsl.fixtures.TextLayout
import io.karpfen.dsl.printer.kmeta.KmetaPrinter
import io.karpfen.dsl.printer.kmeta.structure
import io.karpfen.dsl.printer.kmodel.KmodelPrinter
import io.karpfen.dsl.printer.kmodel.structure
import io.karpfen.dsl.reader.kmeta.KmetaReader
import io.karpfen.dsl.reader.kmodel.KmodelReader
import io.karpfen.dsl.source.SourceRange
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.TestFactory
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Tests that the KMeta and KModel fragments keep the document they are spliced into, also when all its lines share
 * an indentation and its string literals continue on the next line after an escaped line break: reading cuts the
 * shared indentation from those continuation lines too, so a fragment has to add it in front of its own.
 *
 * Every example is read in the layouts of its language; the reader locates each type, rule and object, which is
 * printed as a fragment for the indentation level of its first line and the indentation the document shares, and
 * spliced in at its range. The document must then read to a metamodel or model of the same structure.
 */
class FragmentSpliceTest {

    private val catalog = ExampleCatalog()

    /**
     * Types and their rules.
     */
    @TestFactory
    fun `printed types and rules keep an indented metamodel`(): List<DynamicTest> = LAYOUTS.map { layout ->
        dynamicTest("$layout") {
            val printer = KmetaPrinter()
            var spliced = 0
            catalog.metamodels().forEach { example ->
                val text = layout.apply(example.text)
                val read = KmetaReader(example.baseTypes).read(text)
                val metamodel = read.model?.takeUnless { read.hasErrors } ?: return@forEach
                val structureOf = { content: String -> example.parse(content).structure() }
                val expected = structureOf(text)
                val shared = sharedIndentationOf(text)
                val spliceKeeps = { element: Any, print: (Int) -> String ->
                    assertSpliceKeeps(expected, text, read.sourceMap.rangeOf(element)!!, structureOf, print)
                }
                metamodel.types.filter { read.sourceMap.rangeOf(it) != null }.forEach { type ->
                    spliceKeeps(type) { level -> printer.printType(type, level, shared) }
                    type.simpleProperties.forEach { property -> spliceKeeps(property) { printer.printRule(property, shared) } }
                    type.objectProperties.forEach { property -> spliceKeeps(property) { printer.printRule(property, shared) } }
                    spliced += 1
                }
            }
            assertTrue(spliced > 0, "no type was spliced in the layout $layout")
        }
    }

    /**
     * Objects with the objects they embed. The layouts with escaped line breaks continue every key of a statement,
     * which KModel then does not find, so a model whose string values continue on the next line stands in for them.
     */
    @TestFactory
    fun `printed objects keep an indented model`(): List<DynamicTest> = MODEL_LAYOUTS.map { layout ->
        dynamicTest("$layout") {
            val printer = KmodelPrinter()
            var spliced = 0
            val documents = catalog.models().map { it.text to it.metamodel } + (CONTINUED_VALUES to robotMetamodel)
            documents.forEach { (document, metamodel) ->
                val text = layout.apply(document)
                val read = KmodelReader(metamodel).read(text)
                val model = read.model?.takeUnless { read.hasErrors } ?: return@forEach
                val structureOf = { content: String -> KmodelDSLConverter.parseKmodelString(content, metamodel).structure() }
                val shared = sharedIndentationOf(text)
                KmodelDSLConverter.collectAllObjects(model.objects).forEach { obj ->
                    val range = read.sourceMap.rangeOf(obj)!!
                    assertSpliceKeeps(structureOf(text), text, range, structureOf) { printer.printObject(obj, it, shared) }
                    spliced += 1
                }
            }
            assertTrue(spliced > 0, "no object was spliced in the layout $layout")
        }
    }

    /** The metamodel of [CONTINUED_VALUES]. */
    private val robotMetamodel = KmetaDSLConverter.parseKmetaString(
        "type \"Arm\" \"an arm\" { prop(\"label\", \"string\") }\n" +
            "type \"Robot\" \"a robot\" {\n    prop(\"name\", \"string\")\n    prop(\"log\", list(\"string\"))\n" +
            "    has(\"arm\", \"Arm\")\n}"
    )

    /**
     * Replaces [range] of [text] with the fragment [print] returns for the indentation level of the line the range
     * starts on, and checks that [structureOf] the result is [expected].
     */
    private fun assertSpliceKeeps(
        expected: Any,
        text: String,
        range: SourceRange,
        structureOf: (String) -> Any,
        print: (Int) -> String
    ) {
        val elementRange = ElementRange(range.start.offset, range.end.offset - 1)
        val spliced = elementRange.replaceIn(text, print(elementRange.indentLevelIn(text)))
        assertEquals(expected, structureOf(spliced), spliced)
    }

    /**
     * Returns the number of whitespace chars that every line of [text] that is not blank starts with, at least, the
     * indentation that [String.trimIndent] cuts.
     */
    private fun sharedIndentationOf(text: String): Int =
        text.lines().filter { it.isNotBlank() }.minOfOrNull { line -> line.indexOfFirst { !it.isWhitespace() } } ?: 0

    private companion object {

        /** A model whose string values continue on the next line after an escaped line break, at several depths. */
        const val CONTINUED_VALUES = "make object \"r\":\"Robot\" {\n    prop(\"name\") -> \"Rob\\\n          by\"\n" +
            "    prop(\"log\") -> \"a\\\nb\"\n    prop(\"log\") -> \"c\"\n" +
            "    has(\"arm\") -> make object \"a\":\"Arm\" {\n        prop(\"label\") -> \"le\\\n  ft\"\n    }\n}\n"

        /** The layouts the models are read in: as written and indented. */
        val MODEL_LAYOUTS = listOf(TextLayout.AS_WRITTEN, TextLayout.SPACE_INDENTED)

        /** The layouts the metamodels are read in: those of the models, each also with continued literals. */
        val LAYOUTS = MODEL_LAYOUTS + listOf(TextLayout.ESCAPED_LINE_BREAKS, TextLayout.INDENTED_ESCAPED_LINE_BREAKS)
    }
}
