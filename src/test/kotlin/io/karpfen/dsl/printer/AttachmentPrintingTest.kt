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

import io.karpfen.dsl.dsl.functional.MetamodelBuilder
import io.karpfen.dsl.fixtures.StructureDump
import io.karpfen.dsl.printer.kmeta.KmetaPrinter
import io.karpfen.dsl.printer.kstates.KstatesPrinter
import io.karpfen.dsl.reader.kmeta.KmetaReader
import io.karpfen.dsl.reader.kstates.KstatesReader
import io.karpfen.dsl.states.StateMachine
import io.karpfen.dsl.states.actions.ActionBlock
import io.karpfen.dsl.states.actions.ActionOperationType
import io.karpfen.dsl.states.actions.ActionRule
import io.karpfen.dsl.states.actions.AttachClause
import io.karpfen.dsl.states.actions.ValueActionRightSide
import io.karpfen.dsl.states.State
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

/**
 * Prints the constructs of dynamic attachment with the KStates and KMeta printers and reads them back.
 */
class AttachmentPrintingTest {

    /** An attachable state machine with every construct of dynamic attachment and each form of value. */
    private val attachable = """
        STATEMACHINE "keeper" ATTACHABLE TO "Room", "House" {
            STATES {
                INITIAL STATE "a" {
                    ENTRY {
                        APPENDOBJ("self", "spares", MACRO("new bulb", "40")) ATTACH "blinking"
                        SETOBJ("lamp", "bulb", EVAL { return {} }) ATTACH EVAL { return $(lamp->mode) }
                        SETOBJ("lamp", "bulb", EVAL { return {} }) ATTACH MACRO("pick", "lamp->mode")
                        EXCHANGE("lamp->bulb", "steady")
                        WITH MACRO("pick") AS "choice" {
                            EXCHANGE("lamp->bulb", EVAL { return $(choice) })
                        }
                    }
                }
            }

            TRANSITIONS { }
        }
    """.trimIndent() + "\n"

    /** The canonical text of an attachable state machine prints as it is, and reads back to the same structure. */
    @Test
    fun `attachable state machines print back to the same text`() {
        val machine = KstatesReader().read(attachable).model!!
        val printed = KstatesPrinter().print(machine)

        assertEquals(attachable, printed)
        assertEquals(StructureDump.of(machine), StructureDump.of(KstatesReader().read(printed).model))
    }

    /** Lower-case keywords are printed in capitals. */
    @Test
    fun `keywords are printed in capitals`() {
        val lower = attachable.replace("APPENDOBJ(", "appendobj(").replace(") ATTACH \"", ") attach \"").replace("EXCHANGE(", "exchange(")

        assertEquals(attachable, KstatesPrinter().print(KstatesReader().read(lower).model!!))
    }

    /** A clause on another operation and an attachable state machine without types cannot be written. */
    @Test
    fun `what KStates cannot read back is rejected`() {
        val rule = ActionRule(ActionOperationType.SET, "mode", ValueActionRightSide("on"), attach = AttachClause(ValueActionRightSide("x")))
        val state = State("a", ActionBlock(mutableListOf(rule)), ActionBlock(mutableListOf()), mutableListOf(), isInitial = true)
        val attached = StateMachine("Lamp", mutableListOf(state), mutableListOf(), mutableListOf())
        val typeless = StateMachine.attachable("blinking", emptyList(), mutableListOf(), mutableListOf(), mutableListOf())

        assertFailsWith<IllegalArgumentException> { KstatesPrinter().print(attached) }
        assertFailsWith<IllegalArgumentException> { KstatesPrinter().print(typeless) }
    }

    /** A type that behaves after a state machine prints its clause in the header and reads back to the same metamodel. */
    @Test
    fun `behaves after prints in the type header`() {
        val metamodel = MetamodelBuilder()
            .makeClass("Bulb", "A light bulb", listOf(MetamodelBuilder().makeNumber("watts")), behavesAfter = "blinking")
            .makeClass("Lamp", "A lamp")
            .apply { setRoot("Lamp") }
            .build()
        val printed = KmetaPrinter().print(metamodel)

        assertEquals(
            "type \"Bulb\" \"A light bulb\" behaves after \"blinking\" {\n    prop(\"watts\", \"number\")\n}\n\n" +
                "type \"Lamp\" \"A lamp\" { }\n",
            printed
        )
        assertEquals(StructureDump.of(metamodel.types), StructureDump.of(KmetaReader().read(printed).model!!.types))
    }
}
