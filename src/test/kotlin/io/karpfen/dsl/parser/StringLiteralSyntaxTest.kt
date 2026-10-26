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
import io.karpfen.dsl.dsl.textual.KstatesDSLConverter
import io.karpfen.dsl.printer.kmeta.KmetaPrinter
import io.karpfen.dsl.printer.kmodel.KmodelPrinter
import io.karpfen.dsl.printer.kstates.KstatesPrinter
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

/**
 * Checks which values [StringLiteralSyntax] writes and that the values it writes read back unchanged through
 * the real parsers.
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
     * Line breaks, unescaped double quotes and a lone backslash at the end would not form one string token.
     */
    @Test
    fun `values that do not form one string token are rejected`() {
        listOf(StringLiteralSyntax.KMETA, StringLiteralSyntax.KMODEL, StringLiteralSyntax.KSTATES).forEach { syntax ->
            assertFailsWith<IllegalArgumentException> { syntax.quote("two\nlines") }
            assertFailsWith<IllegalArgumentException> { syntax.quote("a \"quote\"") }
            assertFailsWith<IllegalArgumentException> { syntax.quote("ends in \\") }
        }
    }

    /**
     * Each language strips different quote characters at the edges of a literal; a value that would lose
     * them is rejected, one that keeps them is written.
     */
    @Test
    fun `values whose edges a language strips are rejected by that language only`() {
        assertEquals("\"'kept'\"", StringLiteralSyntax.KMETA.quote("'kept'"))
        assertFailsWith<IllegalArgumentException> { StringLiteralSyntax.KMODEL.quote("'stripped'") }
        assertFailsWith<IllegalArgumentException> { StringLiteralSyntax.KSTATES.quote("'stripped'") }
        assertEquals("\"'kept\"", StringLiteralSyntax.KSTATES.quote("'kept"))
        assertFailsWith<IllegalArgumentException> { StringLiteralSyntax.KMETA.quote("ends in \\\"") }
        assertEquals("\"ends in \\\"\"", StringLiteralSyntax.KSTATES.quote("ends in \\\""))
    }

    /**
     * Values with escaped quotes survive printing and parsing in all three languages.
     */
    @Test
    fun `escaped quotes survive a round trip through the parsers`() {
        val metamodel = KmetaDSLConverter.parseKmetaString("""type "Note" "A \"quoted\" note" { prop("text", "string") }""")
        val reprintedMetamodel = KmetaDSLConverter.parseKmetaString(KmetaPrinter().print(metamodel))
        assertEquals("A \\\"quoted\\\" note", reprintedMetamodel.rootClass.comment)

        val model = KmodelDSLConverter.parseKmodelString("""make object "n":"Note" { prop("text") -> "say \"hi\" now" }""", metamodel)
        val reprintedModel = KmodelDSLConverter.parseKmodelString(KmodelPrinter().print(model), metamodel)
        assertEquals(listOf("say \\\"hi\\\" now"), reprintedModel.objects.single().getProp("text"))

        val machine = KstatesDSLConverter.parseKstatesString(
            """STATEMACHINE ATTACHED TO "Note" { STATES { INITIAL STATE "s" { ENTRY { SET("text", "say \"hi\"") } } } TRANSITIONS { } }"""
        )
        val reprinted = KstatesPrinter().print(machine)
        assertEquals(reprinted, KstatesPrinter().print(KstatesDSLConverter.parseKstatesString(reprinted)))
    }
}
