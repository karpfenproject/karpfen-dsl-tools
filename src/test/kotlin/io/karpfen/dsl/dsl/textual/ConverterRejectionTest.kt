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
package io.karpfen.dsl.dsl.textual

import io.karpfen.dsl.diagnostics.DiagnosticCode
import io.karpfen.dsl.diagnostics.Severity
import io.karpfen.dsl.fixtures.StructureDump
import io.karpfen.dsl.reader.InvalidTextException
import io.karpfen.dsl.reader.kmeta.KmetaReader
import io.karpfen.dsl.reader.kmodel.KmodelReader
import io.karpfen.dsl.reader.kstates.KstatesReader
import java.io.File
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertTrue

/**
 * Tests that the converters are the throwing form of the readers: they reject a text with at least one error, with an
 * [InvalidTextException] that carries every error the reader finds and names each in its message, and they return
 * the model of a text whose problems are only warnings.
 */
class ConverterRejectionTest {

    private val metamodel = KmetaDSLConverter.parseKmetaString(
        """
        type "Item" "An item" { prop("weight", "number") knows("next", "Item") }
        type "Root" "The root" { has("items", list("Item")) }
        """
    )

    /**
     * A KMeta text with a syntax error and a type defined twice is rejected with both errors.
     */
    @Test
    fun `a kmeta text with errors is rejected with all of them`() {
        val text = "type \"A\" \"first\" { prop(\"x\", \"number\") ) }\ntype \"A\" \"again\" { }"

        val rejection = assertFailsWith<InvalidTextException> { KmetaDSLConverter.parseKmetaString(text) }

        assertEquals(listOf(DiagnosticCode.SYNTAX_ERROR, DiagnosticCode.DUPLICATE_TYPE), rejection.errors.map { it.code })
        assertEquals(KmetaReader().read(text).errors, rejection.errors)
        assertEquals(
            "The text has 2 errors:\n" +
                "1:40: syntax.error: ${rejection.errors[0].message}\n" +
                "2:7: kmeta.duplicate-type: ${rejection.errors[1].message}",
            rejection.message
        )
    }

    /**
     * A KModel text with a syntax error and two objects of one id is rejected with both errors.
     */
    @Test
    fun `a kmodel text with errors is rejected with all of them`() {
        val text = """
            make object "root":"Root" {
                has("items") -> make object "a":"Item" { prop("weight") -> "1" ) }
                has("items") -> make object "a":"Item" { }
            }
        """

        val rejection = assertFailsWith<InvalidTextException> { KmodelDSLConverter.parseKmodelString(text, metamodel) }

        assertEquals(listOf(DiagnosticCode.SYNTAX_ERROR, DiagnosticCode.DUPLICATE_OBJECT_ID), rejection.errors.map { it.code })
        assertEquals(KmodelReader(metamodel).read(text).errors, rejection.errors)
    }

    /**
     * A KStates text with a syntax error and a state name used twice is rejected with both errors.
     */
    @Test
    fun `a kstates text with errors is rejected with all of them`() {
        val text = machine("INITIAL STATE \"off\" { } )\nSTATE \"off\" { }")

        val rejection = assertFailsWith<InvalidTextException> { KstatesDSLConverter.parseKstatesString(text) }

        assertEquals(listOf(DiagnosticCode.SYNTAX_ERROR, DiagnosticCode.DUPLICATE_STATE), rejection.errors.map { it.code })
        assertEquals(KstatesReader().read(text).errors, rejection.errors)
    }

    /**
     * The rules of pages hold for the converter: a final state outside a fragment and `DONE` on a transition from a
     * state that is no container state are errors.
     */
    @Test
    fun `a kstates text that breaks the rules of pages is rejected`() {
        val text = machine(
            "INITIAL STATE \"off\" { }\nSTATE \"on\" { }\nFINAL STATE \"broken\" { }",
            "TRANSITION \"off\" -> \"on\" { CONDITION { DONE } }"
        )

        val rejection = assertFailsWith<InvalidTextException> { KstatesDSLConverter.parseKstatesString(text) }

        assertEquals(
            setOf(DiagnosticCode.MISPLACED_FINAL_STATE, DiagnosticCode.MISPLACED_DONE),
            rejection.errors.map { it.code }.toSet()
        )
        assertEquals(KstatesReader().read(text).errors, rejection.errors)
    }

    /**
     * A text whose problems are only warnings converts to the model the reader reads.
     */
    @Test
    fun `a text with only warnings converts`() {
        val text = machine("INITIAL DEEP HISTORY STATE \"off\" { }\nSTATE \"on\" { }")
        val read = KstatesReader().read(text)
        assertEquals(listOf(DiagnosticCode.HISTORY_WITHOUT_INNER_STATES), read.diagnostics.map { it.code })
        assertTrue(read.diagnostics.all { it.severity == Severity.WARNING })

        val machine = KstatesDSLConverter.parseKstatesString(text)

        assertEquals(StructureDump.of(read.model), StructureDump.of(machine))
    }

    /**
     * A single object reads as the whole text it is: it converts to the root object of the model, and it is rejected
     * with the errors of the whole text, such as a `knows` to an object outside it.
     */
    @Test
    fun `a single object accepts and rejects what a whole text does`() {
        val valid = """make object "a":"Item" { prop("weight") -> "2" }"""
        assertEquals(
            StructureDump.of(KmodelDSLConverter.parseKmodelString(valid, metamodel).objects.single()),
            StructureDump.of(KmodelDSLConverter.parseSingleObject(valid, metamodel))
        )

        val dangling = """make object "a":"Item" { knows("next") -> "elsewhere" }"""
        val rejection = assertFailsWith<InvalidTextException> { KmodelDSLConverter.parseSingleObject(dangling, metamodel) }
        assertEquals(listOf(DiagnosticCode.UNRESOLVED_REFERENCE), rejection.errors.map { it.code })
        assertEquals(KmodelReader(metamodel).read(dangling).errors, rejection.errors)

        val broken = """make object "a":"Item" { prop("weight") -> }"""
        val syntaxRejection = assertFailsWith<InvalidTextException> { KmodelDSLConverter.parseSingleObject(broken, metamodel) }
        assertEquals(KmodelReader(metamodel).read(broken).errors, syntaxRejection.errors)
    }

    /**
     * The conversion of a file names the file in every line of the message, and a rejection is an
     * [IllegalArgumentException].
     */
    @Test
    fun `a rejected file is named in the message`() {
        val file = Files.createTempFile("broken", ".kstates").toFile()
        try {
            file.writeText(machine("STATE \"off\" { }"))

            val failure = assertFailsWith<IllegalArgumentException> { KstatesDSLConverter.parseKstatesFile(file.path) }

            val rejection = assertIs<InvalidTextException>(failure)
            assertEquals(file.path, rejection.source)
            assertEquals(
                "${file.path} has 1 error:\n${file.path}:2:5: kstates.initial-state-count: ${rejection.errors.single().message}",
                rejection.message
            )
        } finally {
            File(file.path).delete()
        }
    }

    private fun machine(states: String, transitions: String = ""): String =
        "STATEMACHINE ATTACHED TO \"Lamp\" {\n    STATES {\n" + states.prependIndent("        ") +
            "\n    }\n    TRANSITIONS {\n" + transitions.prependIndent("        ") + "\n    }\n}\n"
}
