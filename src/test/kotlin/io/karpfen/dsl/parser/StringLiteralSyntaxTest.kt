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

import io.karpfen.dsl.dsl.textual.KmetaDSLConverter
import io.karpfen.dsl.dsl.textual.KmodelDSLConverter
import io.karpfen.dsl.dsl.textual.KstatesDSLConverter
import io.karpfen.dsl.printer.kmeta.KmetaPrinter
import io.karpfen.dsl.printer.kmodel.KmodelPrinter
import io.karpfen.dsl.printer.kstates.KstatesPrinter
import io.karpfen.dsl.reader.kmodel.KmodelReader
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

/**
 * Checks how [StringLiteralSyntax] reads and writes the string literals of each language, and that every value it
 * writes reads back unchanged through the real parsers.
 */
class StringLiteralSyntaxTest {

    /**
     * Escape sequences are kept as written, since the parsers do not decode them either.
     */
    @Test
    fun `values are written verbatim between double quotes`() {
        assertEquals("\"say \\\"hi\\\" now\"", StringLiteralSyntax.KSTATES.quote("say \\\"hi\\\" now"))
        assertEquals("\"\"", StringLiteralSyntax.KMETA.quote(""))
        assertEquals("\"it's\"", StringLiteralSyntax.KMODEL.quote("it's"))
    }

    /**
     * Each language reads a literal by its own rule for the quotes at its edges.
     */
    @Test
    fun `each language strips the quotes at the edges by its own rule`() {
        assertEquals("a\\", StringLiteralSyntax.KMETA.unquote("\"a\\\"\""))
        assertEquals("a", StringLiteralSyntax.KMODEL.unquote("'\"a\"'"))
        assertEquals("\"a\"", StringLiteralSyntax.KMODEL_SIGNATURE.unquote("'\"a\"'"))
        assertEquals("'a'", StringLiteralSyntax.KSTATES.unquote("\"''a''\""))
    }

    /**
     * A line break or a double quote that no backslash escapes cannot stand in a double-quoted literal; only KModel
     * has single-quoted literals, in which a double quote needs no escape.
     */
    @Test
    fun `values that no literal form carries are rejected`() {
        listOf(StringLiteralSyntax.KMETA, StringLiteralSyntax.KMODEL, StringLiteralSyntax.KSTATES).forEach { syntax ->
            assertFailsWith<IllegalArgumentException> { syntax.quote("two\nlines") }
        }
        assertFailsWith<IllegalArgumentException> { StringLiteralSyntax.KMETA.quote("a \"quote\"") }
        assertFailsWith<IllegalArgumentException> { StringLiteralSyntax.KSTATES.quote("a \"quote\"") }
        assertEquals("'He said \"go\" now'", StringLiteralSyntax.KMODEL.quote("He said \"go\" now"))
        assertFailsWith<IllegalArgumentException> { StringLiteralSyntax.KMODEL.quote("it's a \"quote\" too") }
    }

    /**
     * A value read from a literal that ends in an escaped quote ends in a lone backslash; an extra escaped quote,
     * which reading strips again, carries it. KMeta and KModel take a backslash before the closing quote for an
     * escape where that makes a longer token, so they write every value that ends in a backslash this way.
     */
    @Test
    fun `a value ending in a lone backslash is written with an extra escaped quote`() {
        assertEquals("\"A \\\"point\\\"\"", StringLiteralSyntax.KMETA.quote("A \\\"point\\"))
        assertEquals("\"say \\\"hi\\\"\"", StringLiteralSyntax.KMODEL.quote("say \\\"hi\\"))
        assertEquals("\"'ends in \\'\"", StringLiteralSyntax.KSTATES.quote("ends in \\"))
        assertEquals("\"ends in \\\\\"\"", StringLiteralSyntax.KMETA.quote("ends in \\\\"))
    }

    /**
     * Each language strips different quote characters at the edges of a literal; a value that would lose them is
     * written in another form where the language has one, and rejected otherwise.
     */
    @Test
    fun `values with quotes at their edges are written in a form that keeps them`() {
        assertEquals("\"'kept'\"", StringLiteralSyntax.KMETA.quote("'kept'"))
        assertFailsWith<IllegalArgumentException> { StringLiteralSyntax.KMODEL.quote("'stripped'") }
        assertEquals("\"'kept\"", StringLiteralSyntax.KMODEL_SIGNATURE.quote("'kept"))
        assertEquals("\"''kept''\"", StringLiteralSyntax.KSTATES.quote("'kept'"))
        assertEquals("\"'kept\"", StringLiteralSyntax.KSTATES.quote("'kept"))
        assertFailsWith<IllegalArgumentException> { StringLiteralSyntax.KMETA.quote("ends in \\\"") }
        assertEquals("\"ends in \\\"\"", StringLiteralSyntax.KSTATES.quote("ends in \\\""))
    }

    /**
     * A line break that a backslash escapes stays inside the literal.
     */
    @Test
    fun `an escaped line break stays inside the literal`() {
        assertEquals("\"a\\\nb\"", StringLiteralSyntax.KMETA.quote("a\\\nb"))
        assertEquals("\"a\\\nb\"", StringLiteralSyntax.KSTATES.quote("a\\\nb"))
    }

    /**
     * KMeta documents with escaped quotes, also at the end of a value, survive printing and parsing.
     */
    @Test
    fun `kmeta values survive a round trip through the parser`() {
        val text = """type "Note" "A \"quoted\"" { prop("text", "string") }""" + "\n" +
            """type "Line" "first\""" + "\n" + """second" { prop("a \"b\"", "string") }"""
        val metamodel = KmetaDSLConverter.parseKmetaString(text)
        val printed = KmetaPrinter().print(metamodel)
        val reread = KmetaDSLConverter.parseKmetaString(printed)

        assertEquals(listOf("A \\\"quoted\\", "first\\\nsecond"), reread.types.map { it.comment })
        assertEquals(printed, KmetaPrinter().print(reread))
    }

    /**
     * KModel documents with single-quoted literals, escaped quotes and a trailing escaped quote survive printing and
     * parsing.
     */
    @Test
    fun `kmodel values survive a round trip through the parser`() {
        val metamodel = KmetaDSLConverter.parseKmetaString(
            """type "Point" "A point" { prop("label", "string") knows("buddy", "Point") }"""
        )
        val text = """make object 'p"1':"Point" { prop("label") -> 'He said "go" now' """ +
            """prop("label") -> "say \"hi\"" knows("buddy") -> 'p"1' }"""
        val model = KmodelDSLConverter.parseKmodelString(text, metamodel)
        val printed = KmodelPrinter().print(model)
        val reread = KmodelDSLConverter.parseKmodelString(printed, metamodel)

        val point = reread.objects.single()
        assertEquals("p\"1", point.id)
        assertEquals(listOf("He said \"go\" now", "say \\\"hi\\"), point.getProp("label"))
        assertEquals("p\"1", point.getRel("buddy").single().id)
        assertEquals(printed, KmodelPrinter().print(reread))
    }

    /**
     * An object key with a double quote that ends in a backslash can only be written between single quotes, which
     * hold where nothing but the class type and the brace of the block follows the key.
     */
    @Test
    fun `a kmodel key with a double quote and a trailing backslash survives a round trip`() {
        val metamodel = KmetaDSLConverter.parseKmetaString("""type "P" "A point" { prop("label", "string") }""")
        val text = "make object 'p\"1\\':\"P\" {\n    prop(\"label\") -> \"x\"\n}\n"
        val read = KmodelReader(metamodel).read(text)
        assertEquals(emptyList(), read.diagnostics)

        val printed = KmodelPrinter().print(read.model!!)
        val reread = KmodelReader(metamodel).read(printed)

        assertEquals(emptyList(), reread.diagnostics, printed)
        assertEquals("p\"1\\", reread.model!!.objects.single().id)
        assertEquals(printed, KmodelPrinter().print(reread.model!!))
    }

    /**
     * KStates documents with escaped quotes and quotes at the edges of a value survive printing and parsing.
     */
    @Test
    fun `kstates values survive a round trip through the parser`() {
        val machine = KstatesDSLConverter.parseKstatesString(
            """STATEMACHINE ATTACHED TO "Note" { STATES { INITIAL STATE "''a''" { """ +
                """ENTRY { SET("text", "say \"hi\"") } } } TRANSITIONS { } }"""
        )
        val printed = KstatesPrinter().print(machine)
        val reread = KstatesDSLConverter.parseKstatesString(printed)

        assertEquals("'a'", reread.states.single().name)
        assertEquals(printed, KstatesPrinter().print(reread))
    }
}
