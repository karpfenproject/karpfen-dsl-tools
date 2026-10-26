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

import io.karpfen.dsl.diagnostics.Diagnostic
import io.karpfen.dsl.diagnostics.DiagnosticCode
import io.karpfen.dsl.diagnostics.Severity
import io.karpfen.dsl.dsl.textual.KmetaDSLConverter
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertSame

/**
 * Tests the problems the KMeta reader reports on broken texts, where it reports them, and the partial
 * metamodel it builds nevertheless.
 */
class KmetaReaderDiagnosticsTest {

    /**
     * A prop of an unknown type is reported at the type and left out; the other props stay.
     */
    @Test
    fun `an unknown property type is reported at the type`() {
        val text = """
            type "A" "first" {
                prop("x", "nmber")
                prop("y", "number")
            }
        """.trimIndent()

        val result = KmetaReader().read(text)

        val diagnostic = result.diagnostics.single()
        assertDiagnostic(DiagnosticCode.UNKNOWN_PROPERTY_TYPE, "nmber", 2, 16, diagnostic, text)
        assertEquals(Severity.ERROR, diagnostic.severity)
        assertEquals(listOf("y"), result.model!!.rootClass.simpleProperties.map { it.key })
    }

    /**
     * A relation to an unknown type is reported at the type name and stays in the model, unresolved.
     */
    @Test
    fun `an unknown target type is reported at the type name`() {
        val text = """
            type "Point" "a point" { prop("x", "number") }
            type "Shape" "a shape" {
                has("corner", "Pointt")
                knows("others", list("Point"))
            }
        """.trimIndent()

        val result = KmetaReader().read(text)

        assertDiagnostic(DiagnosticCode.UNKNOWN_TARGET_TYPE, "Pointt", 3, 20, result.diagnostics.single(), text)
        val relations = result.model!!.rootClass.objectProperties
        assertEquals(listOf("corner", "others"), relations.map { it.key })
        assertNull(relations[0].reference.classType)
        assertSame(result.model!!.types[0], relations[1].reference.classType)
    }

    /**
     * Base types resolve relations of an event definition, as with the throwing converter.
     */
    @Test
    fun `base types resolve the relations of event definitions`() {
        val domain = KmetaDSLConverter.parseKmetaString("type \"Vector\" \"v\" { prop(\"x\", \"number\") }")
        val events = "type \"teleport\" \"moves\" { has(\"position\", \"Vector\") }"

        val withBaseTypes = KmetaReader(domain.types).read(events)
        val withoutBaseTypes = KmetaReader().read(events)

        assertEquals(emptyList(), withBaseTypes.diagnostics)
        assertSame(domain.types.single(), withBaseTypes.model!!.rootClass.objectProperties.single().reference.classType)
        assertEquals(listOf("teleport", "Vector"), withBaseTypes.model!!.types.map { it.name })
        assertEquals(listOf(DiagnosticCode.UNKNOWN_TARGET_TYPE), withoutBaseTypes.diagnostics.map { it.code })
    }

    /**
     * A text without types yields no metamodel.
     */
    @Test
    fun `a text without types yields no model`() {
        for (text in listOf("", "// only a comment\n")) {
            val result = KmetaReader().read(text)

            assertNull(result.model)
            assertEquals(listOf(DiagnosticCode.NO_TYPES), result.diagnostics.map { it.code })
            assertEquals(0, result.diagnostics.single().range.start.offset)
        }
    }

    /**
     * A superfluous token is reported where it stands, and the types after it are still read.
     */
    @Test
    fun `a syntax error is reported at the offending token and the reading continues`() {
        val text = """
            type "A" "first" { prop("x", "number") ) }
            type "B" "second" { prop("y", "number") }
        """.trimIndent()

        val result = KmetaReader().read(text)

        assertDiagnostic(DiagnosticCode.SYNTAX_ERROR, ")", 1, 40, result.diagnostics.single(), text)
        assertEquals(listOf("A", "B"), result.model!!.types.map { it.name })
        assertEquals("B", result.model!!.rootClass.name)
    }

    /**
     * Characters the lexer does not know are reported where they stand.
     */
    @Test
    fun `unknown characters are reported by the lexer`() {
        val text = "type \"A\" \"first\" { # }"

        val result = KmetaReader().read(text)

        assertDiagnostic(DiagnosticCode.UNRECOGNIZED_INPUT, "#", 1, 20, result.diagnostics.single(), text)
        assertEquals("A", result.model!!.rootClass.name)
    }

    /**
     * A text cut off in the middle of a rule keeps the type and the complete rules before it, and the missing
     * end is reported at the end of the text.
     */
    @Test
    fun `a text being typed yields a partial model`() {
        val text = "type \"A\" \"first\" {\n    prop(\"x\", \"number\")\n    prop(\"y\", "

        val result = KmetaReader().read(text)

        assertEquals(listOf("x"), result.model!!.rootClass.simpleProperties.map { it.key })
        val atEnd = result.diagnostics.last()
        assertEquals(DiagnosticCode.SYNTAX_ERROR, atEnd.code)
        assertEquals(text.length, atEnd.range.start.offset)
        assertEquals(0, atEnd.range.length)
    }

    private fun assertDiagnostic(
        code: DiagnosticCode,
        markedText: String,
        line: Int,
        column: Int,
        diagnostic: Diagnostic,
        text: String
    ) {
        assertEquals(code, diagnostic.code, diagnostic.message)
        assertEquals(markedText, diagnostic.range.textIn(text))
        assertEquals(line to column, diagnostic.range.start.line to diagnostic.range.start.column)
    }
}
