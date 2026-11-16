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
import io.karpfen.dsl.dsl.textual.KstatesDSLConverter
import io.karpfen.dsl.printer.kstates.KstatesPrinter
import io.karpfen.dsl.reader.InvalidTextException
import io.karpfen.dsl.states.History
import io.karpfen.dsl.states.JoinTransition
import io.karpfen.dsl.states.SplitTransition
import io.karpfen.dsl.states.State
import io.karpfen.dsl.states.StateMachine
import io.karpfen.dsl.states.Transition
import io.karpfen.dsl.states.actions.ActionBlock
import io.karpfen.dsl.states.conditions.ValueCondition
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Tests what a state carries besides its actions and inner states, and the breakpoints of transitions: that the
 * throwing converter and the reader read the history, the tick limit and the breakpoints alike, what the reader
 * reports and leaves out, and how the printer writes them.
 */
class KstatesStateOptionsTest {

    /**
     * Both ways of reading yield the history, the delay, the timeout and the breakpoints of each state, and a
     * state without them has none. The history without inner states and the empty blocks are only warned about.
     */
    @Test
    fun `the history, the tick limit and the breakpoints of a state are read`() {
        val text = machine(
            states = """
                INITIAL DEEP HISTORY STATE "a" {
                    BREAKPOINT { EXIT ENTRY }
                    TICK LIMIT { TIMEOUT: AFTER 10 TICKS TRANSITION TO "b" DELAY: 1 TICK }
                    STATE "inner" { BREAKPOINT { EXIT } TICK LIMIT { DELAY: 3 TICKS } }
                }
                SHALLOW HISTORY STATE "b" { BREAKPOINT { ENTRY } TICK LIMIT { } }
                STATE "c" { BREAKPOINT { } }
            """.trimIndent()
        )

        val warnings = listOf(
            DiagnosticCode.HISTORY_WITHOUT_INNER_STATES,
            DiagnosticCode.EMPTY_TICK_LIMIT,
            DiagnosticCode.EMPTY_BREAKPOINT
        )
        for (machine in bothReadingsOf(text, warnings)) {
            val (a, b, c) = machine.states
            assertEquals(listOf(History.DEEP, true, true, 1L, 10L, "b"), optionsOf(a))
            assertEquals(listOf(History.NONE, false, true, 3L, null, null), optionsOf(a.innerStates.single()))
            assertEquals(listOf(History.SHALLOW, true, false, null, null, null), optionsOf(b))
            assertEquals(listOf(History.NONE, false, false, null, null, null), optionsOf(c))
            assertTrue(a.isInitial)
        }
    }

    /**
     * Both ways of reading yield the breakpoints of transitions of all three kinds.
     */
    @Test
    fun `the breakpoints of transitions of every kind are read`() {
        val text = machine(
            states = "INITIAL STATE \"a\" { }\nSTATE \"b\" { }\nSTATE \"c\" { }",
            transitions = """
                TRANSITION "a" -> "b" NOT LOOPING { BREAKPOINT { START } CONDITION { VALUE("ready") } }
                SPLIT "a" -> "b", "c" { BREAKPOINT { END START } }
                JOIN "b", "c" -> "a" { BREAKPOINT { END } }
                TRANSITION "b" -> "a" { BREAKPOINT { } }
            """.trimIndent()
        )

        for (machine in bothReadingsOf(text, listOf(DiagnosticCode.EMPTY_BREAKPOINT))) {
            assertEquals(
                listOf(true to false, true to true, false to true, false to false),
                machine.transitions.map { it.startBreakpoint to it.endBreakpoint }
            )
            assertTrue(machine.transitions[1] is SplitTransition && machine.transitions[2] is JoinTransition)
            assertEquals("ready", ((machine.transitions[0] as Transition).condition as ValueCondition).boolVariable)
        }
    }

    /**
     * A number of ticks that is not whole is reported at the number, and the reader leaves the delay or the
     * whole timeout out. The throwing converter rejects the text with both errors.
     */
    @Test
    fun `a number of ticks that is not whole is reported and left out`() {
        val text = machine(
            states = "INITIAL STATE \"a\" { TICK LIMIT { DELAY: 1.5 TICKS TIMEOUT: AFTER 2.5 TICKS TRANSITION TO \"b\" } }\nSTATE \"b\" { }"
        )

        val result = KstatesReader().read(text)

        assertEquals(List(2) { DiagnosticCode.TICK_COUNT }, result.diagnostics.map { it.code }, result.diagnostics.joinToString("\n"))
        assertEquals(listOf("1.5", "2.5"), result.diagnostics.map { it.range.textIn(text) })
        assertEquals("The DELAY of a TICK LIMIT counts whole ticks, but was written as 1.5.", result.diagnostics.first().message)
        assertEquals(listOf(History.NONE, false, false, null, null, null), optionsOf(result.model!!.states.first()))

        val rejection = assertFailsWith<InvalidTextException> { KstatesDSLConverter.parseKstatesString(text) }
        assertEquals(result.errors, rejection.errors)
    }

    /**
     * A broken history, tick limit or breakpoint block is a syntax error, and the reader keeps the state with what
     * it could read of it.
     */
    @Test
    fun `broken state options are syntax errors and the state stays`() {
        val text = machine(
            states = "INITIAL DEEP STATE \"a\" { TICK LIMIT { TIMEOUT: AFTER 3 TICKS } ENTRY { SET(\"x\", \"1\") } }\nSTATE \"b\" { }"
        )

        val result = KstatesReader().read(text)

        assertTrue(result.diagnostics.isNotEmpty())
        assertTrue(result.diagnostics.all { it.code == DiagnosticCode.SYNTAX_ERROR }, result.diagnostics.joinToString("\n"))
        val state = result.model!!.states.first { it.name == "a" }
        assertEquals(listOf(History.NONE, false, false, null, null, null), optionsOf(state))
        assertEquals(1, state.onEntry.items.size)
    }

    /**
     * The printer writes the history in front of `STATE`, the breakpoints on one line and the tick limit with the
     * delay first, and counts one tick as `1 TICK`.
     */
    @Test
    fun `the printer writes the options of a state in the canonical layout`() {
        val state = State(
            name = "a",
            onEntry = ActionBlock(mutableListOf()),
            onDo = ActionBlock(mutableListOf()),
            innerStates = mutableListOf(),
            isInitial = true,
            entryBreakpoint = true,
            exitBreakpoint = true,
            history = History.SHALLOW,
            delay = 1,
            timeout = 12,
            timeoutTo = "b"
        )

        assertEquals(
            """
            INITIAL SHALLOW HISTORY STATE "a" {
                BREAKPOINT { ENTRY EXIT }
                TICK LIMIT {
                    DELAY: 1 TICK
                    TIMEOUT: AFTER 12 TICKS TRANSITION TO "b"
                }
            }
            """.trimIndent(),
            KstatesPrinter().printState(state)
        )
        assertEquals("STATE \"a\" { }", KstatesPrinter().printState(state.copy(isInitial = false, entryBreakpoint = false, exitBreakpoint = false, history = History.NONE, delay = null, timeout = null, timeoutTo = null)))
    }

    /**
     * The printer writes the breakpoints of a transition before its condition.
     */
    @Test
    fun `the printer writes the breakpoints of a transition before its condition`() {
        val transition = Transition("a", "b", allowLoops = false, condition = ValueCondition("ready"), startBreakpoint = true, endBreakpoint = true)

        assertEquals(
            """
            TRANSITION "a" -> "b" NOT LOOPING {
                BREAKPOINT { START END }
                CONDITION {
                    VALUE("ready")
                }
            }
            """.trimIndent(),
            KstatesPrinter().printTransition(transition)
        )
    }

    /**
     * A timeout without its target state, a target state without a timeout and a negative number of ticks cannot be
     * written.
     */
    @Test
    fun `the printer rejects a tick limit KStates cannot express`() {
        val state = State("a", ActionBlock(mutableListOf()), ActionBlock(mutableListOf()), mutableListOf())

        assertFailsWith<IllegalArgumentException> { KstatesPrinter().printState(state.copy(timeout = 3)) }
        assertFailsWith<IllegalArgumentException> { KstatesPrinter().printState(state.copy(timeoutTo = "b")) }
        assertFailsWith<IllegalArgumentException> { KstatesPrinter().printState(state.copy(delay = -1)) }
        assertNull(state.timeout)
    }

    /**
     * Returns the state machine of [text] as the throwing converter and as the reader build it; the reader reports
     * exactly the [warnings], in text order.
     */
    private fun bothReadingsOf(text: String, warnings: List<DiagnosticCode> = emptyList()): List<StateMachine> {
        val result = KstatesReader().read(text)
        assertEquals(warnings, result.diagnostics.map { it.code }, result.diagnostics.joinToString("\n"))
        return listOf(KstatesDSLConverter.parseKstatesString(text), result.model!!)
    }

    /** Returns the history, the entry and exit breakpoints, the delay, the timeout and its target of [state]. */
    private fun optionsOf(state: State): List<Any?> =
        listOf(state.history, state.entryBreakpoint, state.exitBreakpoint, state.delay, state.timeout, state.timeoutTo)

    private fun machine(states: String, transitions: String = ""): String =
        "STATEMACHINE ATTACHED TO \"Robot\" {\n    STATES {\n" + states.prependIndent("        ") +
            "\n    }\n    TRANSITIONS {\n" + transitions.prependIndent("        ") + "\n    }\n}\n"
}
