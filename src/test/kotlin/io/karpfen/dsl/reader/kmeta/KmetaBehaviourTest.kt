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

import io.karpfen.dsl.diagnostics.DiagnosticCode
import io.karpfen.dsl.dsl.textual.KmetaDSLConverter
import io.karpfen.dsl.fixtures.StructureDump
import io.karpfen.dsl.meta.BehavesAfter
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Tests how the KMeta reader and the converter read the clause `behaves after`, where the reader records it, and the
 * report of the clause while dynamic attachment is switched off.
 */
class KmetaBehaviourTest {

    /** A metamodel whose bulb behaves after a state machine and whose lamp does not. */
    private val text = """
        type "Bulb" "A light bulb" behaves after "blinking" {
            prop("watts", "number")
        }
        type "Lamp" "A lamp" {
            has("bulb", "Bulb")
        }
    """.trimIndent()

    /** The clause names the state machine; the converter reads the same metamodel. */
    @Test
    fun `the clause names the state machine of a type`() {
        val result = KmetaReader().read(text)
        val (bulb, lamp) = result.model!!.types

        assertEquals(emptyList(), result.diagnostics)
        assertEquals(BehavesAfter("blinking"), bulb.behavesAfter)
        assertNull(lamp.behavesAfter)
        assertEquals(StructureDump.of(KmetaDSLConverter.parseKmetaString(text)), StructureDump.of(result.model))
    }

    /** The source map holds the whole clause and the name in it; the type keeps its own name. */
    @Test
    fun `the clause and its name have their ranges`() {
        val result = KmetaReader().read(text)
        val bulb = result.model!!.types.first()
        val clause = bulb.behavesAfter!!

        assertEquals("behaves after \"blinking\"", result.sourceMap.rangeOf(clause)!!.textIn(text))
        assertEquals("blinking", result.sourceMap.nameRangeOf(clause)!!.textIn(text))
        assertEquals("Bulb", result.sourceMap.nameRangeOf(bulb)!!.textIn(text))
    }

    /** The clause is part of the core language and reads without any finding. */
    @Test
    fun `the clause reads without findings`() {
        val result = KmetaReader().read(text)

        assertEquals(emptyList(), result.diagnostics)
        assertEquals(BehavesAfter("blinking"), result.model!!.types.first().behavesAfter)
    }

    /** The keywords are written in lower case like every KMeta keyword, and the clause needs its name. */
    @Test
    fun `the keywords are lower case and the name is required`() {
        val upper = KmetaReader().read(text.replace("behaves after", "BEHAVES AFTER"))
        assertTrue(upper.hasErrors)
        assertEquals(setOf(DiagnosticCode.UNRECOGNIZED_INPUT), upper.errors.map { it.code }.toSet() - DiagnosticCode.SYNTAX_ERROR)

        val nameless = KmetaReader().read(text.replace("behaves after \"blinking\"", "behaves after"))
        assertEquals(listOf(DiagnosticCode.SYNTAX_ERROR), nameless.diagnostics.map { it.code })
        assertNull(nameless.model!!.types.first().behavesAfter)
    }

    /** A letter that starts a keyword but no token is skipped alone, as before the keywords, so a string after it reads. */
    @Test
    fun `a stray first letter of a keyword is skipped alone`() {
        val stray = "type \"Bulb\" \"A bulb\" { prop(\"watts\", a\"number\") prop(\"lit\", b\"boolean\") }"
        val result = KmetaReader().read(stray)

        assertEquals(listOf("a", "b"), result.diagnostics.map { it.range.textIn(stray) })
        assertEquals(listOf("watts", "lit"), result.model!!.types.single().simpleProperties.map { it.key })
    }
}
