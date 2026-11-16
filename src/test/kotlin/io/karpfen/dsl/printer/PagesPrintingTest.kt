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

import io.karpfen.dsl.dsl.textual.KstatesDSLConverter
import io.karpfen.dsl.fixtures.StructureDump
import io.karpfen.dsl.printer.kstates.KstatesPrinter
import io.karpfen.dsl.reader.kstates.KstatesReader
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Prints the constructs of pages with the KStates printer and reads them back: the fragment header, final states,
 * container states with their options, and `DONE`.
 */
class PagesPrintingTest {

    /** A fragment in the canonical layout, with final states and a nested container state. */
    private val fragment = """
        STATEMACHINE "bulb test" FRAGMENT {
            STATES {
                INITIAL STATE "lit" {
                    ENTRY {
                        SET("bulb->lit", "true")
                    }
                }
                SHALLOW HISTORY CONTAINER STATE "wiring" USE "wiring test" {
                    BREAKPOINT { ENTRY EXIT }
                    TICK LIMIT {
                        TIMEOUT: AFTER 5 TICKS TRANSITION TO "failed"
                    }
                }
                FINAL STATE "passed" { }
                FINAL STATE "failed" {
                    BREAKPOINT { ENTRY }
                }
            }

            TRANSITIONS {
                TRANSITION "lit" -> "wiring" { }
                TRANSITION "wiring" -> "passed" {
                    CONDITION {
                        EVENT("service", "checked")
                        DONE
                    }
                }
            }
        }
    """.trimIndent() + "\n"

    /** A state machine attached to the lamp that runs two pages side by side. */
    private val lamp = """
        STATEMACHINE ATTACHED TO "Lamp" {
            STATES {
                INITIAL STATE "off" { }
                CONTAINER STATE "bulb" USE "bulb test" { }
                CONTAINER STATE "switch" USE "switch test" { }
                STATE "on" { }
            }

            TRANSITIONS {
                SPLIT "off" -> "bulb", "switch" { }
                JOIN "bulb", "switch" -> "on" {
                    CONDITION {
                        DONE
                    }
                }
            }
        }
    """.trimIndent() + "\n"

    /** Both texts read without problems, print back to the same text and read back to the same structure. */
    @Test
    fun `pages print back to the same text`() {
        for (text in listOf(fragment, lamp)) {
            val read = KstatesReader().read(text)
            assertEquals(emptyList(), read.diagnostics)
            val printed = KstatesPrinter().print(read.model!!)

            assertEquals(text, printed)
            assertEquals(StructureDump.of(read.model), StructureDump.of(KstatesReader().read(printed).model))
            assertEquals(text, KstatesDSLConverter.convertToKstatesString(KstatesDSLConverter.parseKstatesString(text)))
        }
    }

    /** A single container state prints as a fragment of text. */
    @Test
    fun `a container state prints on its own`() {
        val state = KstatesReader().read(lamp).model!!.states[1]

        assertEquals("CONTAINER STATE \"bulb\" USE \"bulb test\" { }", KstatesPrinter().printState(state))
    }
}
