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
package io.karpfen.dsl.reader.kstates

import io.karpfen.dsl.diagnostics.DiagnosticCode
import io.karpfen.dsl.states.History
import io.karpfen.dsl.states.KstatesFeature
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Tests the optional features of KStates: a reader reports every construct of a feature that is switched off once,
 * at the keyword that introduces it, and still builds the state machine; with every feature on, which is the
 * default, it reports nothing of them.
 */
class KstatesFeatureTest {

    /** A text that uses every optional feature, with the same words inside EVAL code and strings. */
    private val text = """
        STATEMACHINE ATTACHED TO "Robot" {
            STATES {
                INITIAL SHALLOW HISTORY STATE "a" {
                    BREAKPOINT { ENTRY }
                    TICKS LIMIT { DELAY: 2 TICKS }
                    ENTRY { SET("speed", MACRO("half", "speed")) WITH MACRO("half", "x") AS "h" { SET("y", "h") } }
                    STATE "x" { }
                }
                DEEP HISTORY STATE "b" { STATE "y" { DO { SET("z", EVAL { DEEP = "SPLIT"; return DEEP }) } } }
                STATE "c" { }
            }
            TRANSITIONS {
                TRANSITION "a" -> "b" { BREAKPOINT { START } }
                SPLIT "b" -> "a", "c" { }
                JOIN "a", "c" -> "b" { }
            }
            MACROS {
                MACRO "half" { TAKES("v", "number") RETURNS("number") DEFINITION { EVAL { return v / 2 } } }
            }
        }
    """.trimIndent()

    /** Every feature is on by default, and a reader then reports none of their constructs. */
    @Test
    fun `with every feature on nothing is reported`() {
        assertEquals(emptyList(), KstatesReader().read(text).diagnostics)
        assertEquals(emptyList(), KstatesReader(KstatesFeature.ALL).read(text).diagnostics)
    }

    /** With every feature off, each construct is reported once at its introducing keyword, in text order. */
    @Test
    fun `each construct of a switched-off feature is reported once at its keyword`() {
        val result = KstatesReader(emptySet()).read(text)

        assertTrue(result.diagnostics.all { it.code == DiagnosticCode.FEATURE_OFF }, result.diagnostics.joinToString("\n"))
        assertEquals(
            listOf("SHALLOW", "BREAKPOINT", "TICKS", "MACRO", "MACRO", "DEEP", "BREAKPOINT", "SPLIT", "JOIN", "MACROS"),
            result.diagnostics.map { it.range.textIn(text) },
        )
        assertEquals("Histories are switched off, so this history is not allowed.", result.diagnostics.first().message)
    }

    /** Only the features that are off are reported, and the state machine is built as with every feature on. */
    @Test
    fun `only the switched-off features are reported and the model stays complete`() {
        val result = KstatesReader(KstatesFeature.ALL - KstatesFeature.PARALLEL - KstatesFeature.MACROS).read(text)

        assertEquals(listOf("MACRO", "MACRO", "SPLIT", "JOIN", "MACROS"), result.diagnostics.map { it.range.textIn(text) })
        assertEquals(
            listOf(
                "Macros are switched off, so this macro is not allowed.",
                "Splits and joins are switched off, so this split or join is not allowed.",
            ),
            result.diagnostics.map { it.message }.distinct().sorted(),
        )
        val machine = result.model!!
        assertEquals(History.SHALLOW, machine.states.first().history)
        assertEquals(3, machine.transitions.size)
        assertEquals(1, machine.macros.size)
    }

    /** Each feature has its own identifier and is found by it. */
    @Test
    fun `features are found by their identifiers`() {
        assertEquals(listOf("history", "tickLimit", "breakpoints", "macros", "parallel", "attachment"), KstatesFeature.entries.map { it.id })
        KstatesFeature.entries.forEach { assertEquals(it, KstatesFeature.byId(it.id)) }
        assertNull(KstatesFeature.byId("teleport"))
    }
}
