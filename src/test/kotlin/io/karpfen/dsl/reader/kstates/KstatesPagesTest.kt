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

import io.karpfen.dsl.diagnostics.Diagnostic
import io.karpfen.dsl.diagnostics.DiagnosticCode
import io.karpfen.dsl.dsl.textual.KstatesDSLConverter
import io.karpfen.dsl.fixtures.StructureDump
import io.karpfen.dsl.grammar.kstates.KstatesLexer
import io.karpfen.dsl.grammar.kstates.KstatesParser
import io.karpfen.dsl.parser.KstatesEntryRule
import io.karpfen.dsl.reader.ParseResult
import io.karpfen.dsl.states.History
import io.karpfen.dsl.states.StateMachine
import io.karpfen.dsl.states.Transition
import io.karpfen.dsl.states.conditions.CompositeCondition
import io.karpfen.dsl.states.conditions.DoneCondition
import io.karpfen.dsl.states.conditions.EventCondition
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Tests how the KStates reader and the converter read the constructs of pages: the fragment header, container states,
 * final states and `DONE`, and the checks of a single text about them.
 */
class KstatesPagesTest {

    /** Reads [text] and requires that the converter builds the same state machine when the reader finds no error. */
    private fun read(text: String): ParseResult<StateMachine> {
        val result = KstatesReader().read(text)
        if (!result.hasErrors) {
            assertEquals(StructureDump.of(KstatesDSLConverter.parseKstatesString(text)), StructureDump.of(result.model))
        }
        return result
    }

    /** Returns the codes of [diagnostics] with the text each marks in [text]. */
    private fun marked(text: String, diagnostics: List<Diagnostic>): List<Pair<DiagnosticCode, String>> =
        diagnostics.map { it.code to it.range.textIn(text) }

    /** Returns a fragment named `bulb test` with [states] and [transitions]. */
    private fun fragment(states: String, transitions: String = "", name: String = "\"bulb test\""): String = """
        |STATEMACHINE $name FRAGMENT {
        |    STATES {
        |$states
        |    }
        |    TRANSITIONS {
        |$transitions
        |    }
        |}
    """.trimMargin()

    /** Returns a state machine attached to the lamp with [states] and [transitions]. */
    private fun lamp(states: String, transitions: String = ""): String = """
        |STATEMACHINE ATTACHED TO "Lamp" {
        |    STATES {
        |$states
        |    }
        |    TRANSITIONS {
        |$transitions
        |    }
        |}
    """.trimMargin()

    /** The fragment of the contract, with an initial state, two final states and a condition. */
    private val bulbTest = fragment(
        """
        INITIAL STATE "lit" { ENTRY { SET("bulb->lit", "true") } }
        STATE "dark" { }
        FINAL STATE "passed" { }
        FINAL STATE "failed" { BREAKPOINT { ENTRY } }
        """,
        """
        TRANSITION "lit" -> "dark" { }
        TRANSITION "dark" -> "passed" { CONDITION { EVAL { return $(bulb->watts) > 0 } } }
        TRANSITION "dark" -> "failed" { }
        """
    )

    /** The state machine of the contract, with a container state and a transition on `DONE`. */
    private val lampWithTest = lamp(
        """
        INITIAL STATE "off" { }
        DEEP HISTORY CONTAINER STATE "self test" USE "bulb test" { BREAKPOINT { EXIT } TICK LIMIT { TIMEOUT: AFTER 9 TICKS TRANSITION TO "off" } }
        STATE "on" { }
        """,
        """
        TRANSITION "off" -> "self test" { }
        TRANSITION "self test" -> "on" { CONDITION { DONE } }
        TRANSITION "self test" -> "off" { CONDITION { EVENT("switch", "abort") DONE } }
        """
    )

    /** A fragment reads with its name, no type, and its final states. */
    @Test
    fun `a fragment reads with its name and its final states`() {
        val result = read(bulbTest)
        val machine = result.model!!

        assertEquals(emptyList(), result.diagnostics)
        assertTrue(machine.isFragment)
        assertFalse(machine.isAttachable)
        assertEquals("bulb test", machine.name)
        assertEquals(emptyList(), machine.types)
        assertEquals(listOf(false, false, true, true), machine.states.map { it.isFinal })
        assertTrue(machine.states[3].entryBreakpoint)
        assertEquals("bulb test", result.sourceMap.nameRangeOf(machine)!!.textIn(bulbTest))
    }

    /** A container state reads with its fragment, history, breakpoint and tick limit, and `DONE` as a clause. */
    @Test
    fun `a container state and DONE read`() {
        val result = read(lampWithTest)
        val machine = result.model!!
        val container = machine.states[1]

        assertEquals(emptyList(), result.diagnostics)
        assertTrue(container.isContainer)
        assertEquals("bulb test", container.fragment)
        assertEquals(History.DEEP, container.history)
        assertTrue(container.exitBreakpoint)
        assertEquals(9L, container.timeout)
        assertEquals(listOf("bulb test", "off"), result.sourceMap.operandRangesOf(container).map { it.textIn(lampWithTest) })
        assertFalse(machine.states[0].isContainer)
        assertNull(machine.states[0].fragment)
        val transitions = machine.transitions.filterIsInstance<Transition>()
        assertIs<DoneCondition>(transitions[1].condition)
        val composite = assertIs<CompositeCondition>(transitions[2].condition)
        assertEquals(listOf(EventCondition::class, DoneCondition::class), composite.clauses.map { it::class })
        assertEquals("DONE", result.sourceMap.rangeOf(transitions[1].condition)!!.textIn(lampWithTest))
    }

    /** The entry rule is chosen by the tokens before the first brace, and the lexer knows the five keywords. */
    @Test
    fun `the entry rule follows the header`() {
        fun isFragment(text: String) = KstatesEntryRule.isFragment(CommonTokenStream(KstatesLexer(CharStreams.fromString(text))))
        assertTrue(isFragment("// a page\nSTATEMACHINE \"x\" FRAGMENT {"))
        assertTrue(isFragment("STATEMACHINE FRAGMENT {"))
        assertFalse(isFragment("STATEMACHINE ATTACHED TO \"Lamp\" {"))
        assertFalse(isFragment("STATEMACHINE \"x\" ATTACHABLE TO \"Lamp\" {"))
        assertFalse(isFragment(""))
        val parser = KstatesParser(CommonTokenStream(KstatesLexer(CharStreams.fromString(bulbTest))))
        assertIs<KstatesParser.Fragment_machineContext>(KstatesEntryRule.machineOf(KstatesEntryRule.parse(parser)))
        val lexer = KstatesLexer(CharStreams.fromString("FRAGMENT CONTAINER USE FINAL DONE done"))
        assertEquals(
            listOf(KstatesLexer.FRAGMENT, KstatesLexer.CONTAINER, KstatesLexer.USE, KstatesLexer.FINAL, KstatesLexer.DONE, KstatesLexer.IDENT),
            lexer.allTokens.filter { it.channel == 0 }.map { it.type }
        )
    }

    /** A fragment without a name is reported at `FRAGMENT` and read with an empty name. */
    @Test
    fun `a fragment needs a name`() {
        val text = bulbTest.replace("\"bulb test\" FRAGMENT", "FRAGMENT")
        val result = read(text)

        assertEquals(listOf(DiagnosticCode.MISSING_STATE_MACHINE_NAME to "FRAGMENT"), marked(text, result.diagnostics))
        assertEquals("", result.model!!.name)
        assertTrue(result.model.isFragment)
    }

    /** A fragment needs exactly one initial state and at least one final state at its top level. */
    @Test
    fun `a fragment needs an initial state and a final state`() {
        val withoutFinal = fragment("INITIAL STATE \"lit\" { }\nSTATE \"done\" { FINAL STATE \"nested\" { } }")
        assertEquals(
            listOf(DiagnosticCode.FRAGMENT_WITHOUT_FINAL_STATE to "STATES", DiagnosticCode.MISPLACED_FINAL_STATE to "nested"),
            marked(withoutFinal, read(withoutFinal).diagnostics)
        )
        val withoutInitial = fragment("STATE \"lit\" { }\nFINAL STATE \"passed\" { }")
        assertEquals(listOf(DiagnosticCode.INITIAL_STATE_COUNT), read(withoutInitial).diagnostics.map { it.code })
    }

    /** A final state in a state machine that is no fragment is reported at its name. */
    @Test
    fun `a final state belongs to a fragment`() {
        val text = lamp("INITIAL STATE \"off\" { }\nFINAL STATE \"broken\" { }")
        val result = read(text)

        assertEquals(listOf(DiagnosticCode.MISPLACED_FINAL_STATE to "broken"), marked(text, result.diagnostics))
        assertEquals(
            "A FINAL STATE ends a fragment, but this state machine is no fragment",
            result.diagnostics.single().message
        )
    }

    /** A final state holds a breakpoint block at most and is neither initial nor has a history. */
    @Test
    fun `a final state holds nothing but a breakpoint`() {
        val text = fragment(
            """
            STATE "lit" { }
            INITIAL SHALLOW HISTORY FINAL STATE "passed" USE "x" {
                TICK LIMIT { DELAY: 1 TICK }
                ENTRY { SET("bulb->lit", "false") }
                DO { SET("bulb->lit", "false") }
                STATE "inner" { }
            }
            """
        )
        val result = read(text)

        assertEquals(
            listOf(
                DiagnosticCode.FINAL_STATE_CONTENT to "INITIAL",
                DiagnosticCode.FINAL_STATE_CONTENT to "SHALLOW HISTORY",
                DiagnosticCode.CONTAINER_USE to "USE",
                DiagnosticCode.FINAL_STATE_CONTENT to "TICK",
                DiagnosticCode.FINAL_STATE_CONTENT to "ENTRY",
                DiagnosticCode.FINAL_STATE_CONTENT to "DO",
                DiagnosticCode.FINAL_STATE_CONTENT to "inner"
            ),
            marked(text, result.diagnostics)
        )
    }

    /** A transition, split or join that leaves a final state is reported at the final state; one into it is fine. */
    @Test
    fun `nothing leaves a final state`() {
        val text = fragment(
            """
            INITIAL STATE "lit" { }
            STATE "dark" { }
            FINAL STATE "passed" { }
            """,
            """
            TRANSITION "lit" -> "passed" { }
            TRANSITION "passed" -> "lit" { }
            SPLIT "passed" -> "lit", "dark" { }
            JOIN "lit", "passed" -> "dark" { }
            """
        )
        val result = read(text)

        assertEquals(
            List(3) { DiagnosticCode.TRANSITION_FROM_FINAL_STATE to "passed" },
            marked(text, result.diagnostics)
        )
        assertEquals(
            listOf(text.indexOf("\"passed\" -> \"lit\"") + 1, text.indexOf("\"passed\" -> \"lit\", ") + 1, text.indexOf("\"passed\" -> \"dark\"") + 1),
            result.diagnostics.map { it.range.start.offset }
        )
    }

    /** A container state names its fragment with `USE`, and only a container state does. */
    @Test
    fun `USE belongs to a container state`() {
        val text = lamp(
            """
            INITIAL STATE "off" USE "bulb test" { }
            CONTAINER STATE "self test" { }
            """
        )
        val result = read(text)

        assertEquals(
            listOf(DiagnosticCode.CONTAINER_USE to "USE", DiagnosticCode.CONTAINER_USE to "CONTAINER"),
            marked(text, result.diagnostics)
        )
        assertEquals("", result.model!!.states[1].fragment)
        assertNull(result.model.states[0].fragment)
    }

    /** A container state has no actions and no inner states; a breakpoint, a tick limit and a history are fine. */
    @Test
    fun `a container state has no content of its own`() {
        val text = lamp(
            """
            INITIAL STATE "off" { }
            CONTAINER STATE "self test" USE "bulb test" {
                ENTRY { SET("mode", "test") }
                DO { SET("mode", "test") }
                STATE "inner" { }
            }
            """
        )
        assertEquals(
            listOf(
                DiagnosticCode.CONTAINER_STATE_CONTENT to "ENTRY",
                DiagnosticCode.CONTAINER_STATE_CONTENT to "DO",
                DiagnosticCode.CONTAINER_STATE_CONTENT to "inner"
            ),
            marked(text, read(text).diagnostics)
        )
        assertEquals(emptyList(), read(lampWithTest).diagnostics)
    }

    /** `DONE` on a transition or split needs a container state as its source, on a join at least one. */
    @Test
    fun `DONE speaks about container states`() {
        val text = lamp(
            """
            INITIAL STATE "off" { }
            CONTAINER STATE "bulb" USE "bulb test" { }
            CONTAINER STATE "switch" USE "switch test" { }
            STATE "on" { }
            """,
            """
            TRANSITION "off" -> "on" { CONDITION { DONE } }
            SPLIT "off" -> "bulb", "switch" { CONDITION { DONE } }
            SPLIT "bulb" -> "on", "off" { CONDITION { DONE } }
            TRANSITION "bulb" -> "on" { CONDITION { DONE } }
            JOIN "bulb", "on" -> "off" { CONDITION { DONE } }
            JOIN "bulb", "switch" -> "on" { CONDITION { DONE } }
            JOIN "off", "on" -> "bulb" { CONDITION { DONE } }
            """
        )
        val result = read(text)

        assertEquals(List(3) { DiagnosticCode.MISPLACED_DONE to "DONE" }, marked(text, result.diagnostics))
        val lines = result.diagnostics.map { it.range.start.line }
        assertEquals(listOf(12, 13, 18), lines)
        assertEquals(
            "DONE on a join speaks about its sources that are container states, but none of 'off', 'on' is one",
            result.diagnostics.last().message
        )
    }

    /** A container state of a fragment may use another fragment but not the fragment itself. */
    @Test
    fun `a fragment does not use itself`() {
        val text = fragment(
            """
            INITIAL CONTAINER STATE "wiring" USE "wiring test" { }
            CONTAINER STATE "again" USE "bulb test" { }
            FINAL STATE "passed" { }
            """,
            """
            TRANSITION "wiring" -> "again" { CONDITION { DONE } }
            """
        )
        val result = read(text)

        assertEquals(listOf(DiagnosticCode.FRAGMENT_USES_ITSELF), result.diagnostics.map { it.code })
        assertEquals(text.lastIndexOf("bulb test"), result.diagnostics.single().range.start.offset)
    }

    /** A history on a container state, whose inner states come from its fragment, is no warning. */
    @Test
    fun `a container state may have a history`() {
        val withHistory = lamp("INITIAL SHALLOW HISTORY CONTAINER STATE \"self test\" USE \"bulb test\" { }")
        assertEquals(emptyList(), read(withHistory).diagnostics)
        val plain = lamp("INITIAL SHALLOW HISTORY STATE \"self test\" { }")
        assertEquals(listOf(DiagnosticCode.HISTORY_WITHOUT_INNER_STATES), read(plain).diagnostics.map { it.code })
    }

    /**
     * A fragment, and a state machine with a container state, may call macros it does not define; any other state
     * machine may not.
     */
    @Test
    fun `macros of other files are allowed in a composition`() {
        val calls = "ENTRY { SET(\"bulb->watts\", MACRO(\"half\", \"bulb->maxWatts\")) SET(\"bulb->watts\", EVAL { return @quarter(4) }) }"
        val inFragment = fragment("INITIAL STATE \"lit\" { $calls }\nFINAL STATE \"passed\" { }")
        assertEquals(emptyList(), read(inFragment).diagnostics)
        val withContainer = lamp("INITIAL STATE \"off\" { $calls }\nCONTAINER STATE \"self test\" USE \"bulb test\" { }")
        assertEquals(emptyList(), read(withContainer).diagnostics)
        val plain = lamp("INITIAL STATE \"off\" { $calls }")
        assertEquals(
            listOf(DiagnosticCode.UNKNOWN_MACRO to "half", DiagnosticCode.UNKNOWN_MACRO to "@quarter"),
            marked(plain, read(plain).diagnostics)
        )
    }

    /** A macro the fragment defines itself is still checked for its number of arguments. */
    @Test
    fun `local macros of a fragment are checked as before`() {
        val text = fragment(
            "INITIAL STATE \"lit\" { ENTRY { SET(\"bulb->watts\", MACRO(\"half\", \"a\", \"b\")) } }\nFINAL STATE \"passed\" { }"
        ).replace(
            "    }\n}",
            "    }\n    MACROS {\n        MACRO \"half\" { TAKES(\"w\", \"number\") RETURNS(\"number\") DEFINITION { EVAL { return \$(w) / 2 } } }\n    }\n}"
        )
        assertEquals(listOf(DiagnosticCode.MACRO_ARGUMENT_COUNT), read(text).diagnostics.map { it.code })
    }

    /** A text whose header lacks its keyword still reads as an attached state machine, as before fragments. */
    @Test
    fun `a broken header is read as before`() {
        val text = "STATEMACHINE TO \"Lamp\" {\n    STATES {\n        INITIAL STATE \"off\" { }\n    }\n    TRANSITIONS { }\n}"
        val result = KstatesReader().read(text)

        assertEquals(listOf(DiagnosticCode.SYNTAX_ERROR), result.diagnostics.map { it.code })
        assertFalse(result.model!!.isFragment)
        assertEquals("Lamp", result.model.attachedToClass)
        assertEquals(listOf("off"), result.model.states.map { it.name })
    }
}
