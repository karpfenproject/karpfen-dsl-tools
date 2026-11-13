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
import io.karpfen.dsl.diagnostics.DiagnosticCode.COMMENT_IN_EVAL
import io.karpfen.dsl.diagnostics.DiagnosticCode.DELAY_BEYOND_TIMEOUT
import io.karpfen.dsl.diagnostics.DiagnosticCode.DUPLICATE_MACRO
import io.karpfen.dsl.diagnostics.DiagnosticCode.DUPLICATE_PARAMETER
import io.karpfen.dsl.diagnostics.DiagnosticCode.DUPLICATE_STATE
import io.karpfen.dsl.diagnostics.DiagnosticCode.EMPTY_BREAKPOINT
import io.karpfen.dsl.diagnostics.DiagnosticCode.EMPTY_TICK_LIMIT
import io.karpfen.dsl.diagnostics.DiagnosticCode.EVAL_WITHOUT_RETURN
import io.karpfen.dsl.diagnostics.DiagnosticCode.EVENT_CLAUSE_NOT_FIRST
import io.karpfen.dsl.diagnostics.DiagnosticCode.HISTORY_WITHOUT_INNER_STATES
import io.karpfen.dsl.diagnostics.DiagnosticCode.MACRO_ARGUMENT_COUNT
import io.karpfen.dsl.diagnostics.DiagnosticCode.MISSING_MACRO_ARGUMENTS
import io.karpfen.dsl.diagnostics.DiagnosticCode.REPEATED_JOIN_SOURCE
import io.karpfen.dsl.diagnostics.DiagnosticCode.REPEATED_SPLIT_TARGET
import io.karpfen.dsl.diagnostics.DiagnosticCode.SEVERAL_EVENT_CLAUSES
import io.karpfen.dsl.diagnostics.DiagnosticCode.UNKNOWN_MACRO
import io.karpfen.dsl.diagnostics.DiagnosticCode.UNKNOWN_STATE
import io.karpfen.dsl.diagnostics.Severity
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests what the KStates reader reports about a state machine beyond its syntax: names defined twice or not at all,
 * calls of macros that do not fit, misplaced `EVENT` clauses, options without effect and `EVAL` code that does not run
 * as meant.
 */
class KstatesConsistencyTest {

    /** Returns the problems of [text], each with the part of the text it marks. */
    private fun marked(text: String): List<Pair<DiagnosticCode, String>> =
        KstatesReader().read(text).diagnostics.map { it.code to it.range.textIn(text) }

    /** Returns a state machine of [states], [transitions] and [macros]. */
    private fun machine(states: String, transitions: String = "", macros: String = ""): String =
        "STATEMACHINE ATTACHED TO \"R\" {\n    STATES {\n$states\n    }\n    TRANSITIONS {\n$transitions\n    }\n" +
            (if (macros.isEmpty()) "" else "    MACROS {\n$macros\n    }\n") + "}\n"

    /** A state name used a second time, at any depth, is reported at the later name. */
    @Test
    fun `a state name used twice is reported`() {
        val text = machine("INITIAL STATE \"a\" { STATE \"b\" { } }\nSTATE \"c\" { STATE \"a\" { } }\nSTATE \"d\" { }")

        assertEquals(listOf(DUPLICATE_STATE to "a"), marked(text))
        assertEquals(Severity.ERROR, KstatesReader().read(text).diagnostics.single().severity)
    }

    /** Transitions, splits, joins and timeouts that name an unknown state are reported at the name. */
    @Test
    fun `an unknown state is reported at its name`() {
        val text = machine(
            "INITIAL STATE \"a\" { TICK LIMIT { TIMEOUT: AFTER 2 TICKS TRANSITION TO \"x\" } }\nSTATE \"b\" { }",
            """
            TRANSITION "a" -> "y" { }
            SPLIT "z" -> "a", "b" { }
            JOIN "a", "b" -> "w" { }
            TRANSITION "b" -> "a" { }
            """
        )

        assertEquals(
            listOf(UNKNOWN_STATE to "x", UNKNOWN_STATE to "y", UNKNOWN_STATE to "z", UNKNOWN_STATE to "w"),
            marked(text)
        )
    }

    /** A split that names a target twice, or a join that names a source twice, is reported at the repetition. */
    @Test
    fun `a repeated split target or join source is reported`() {
        val text = machine(
            "INITIAL STATE \"a\" { }\nSTATE \"b\" { }\nSTATE \"c\" { }",
            """
            SPLIT "a" -> "b", "b" { }
            JOIN "b", "c", "b" -> "a" { }
            SPLIT "a" -> "b", "c" { }
            """
        )

        assertEquals(listOf(REPEATED_SPLIT_TARGET to "b", REPEATED_JOIN_SOURCE to "b"), marked(text))
    }

    /** A macro name used twice, and a parameter name used twice in one macro, are reported at the later name. */
    @Test
    fun `a macro or parameter defined twice is reported`() {
        val macros = """
            MACRO "m" { TAKES("v", "number") TAKES("v", "number") RETURNS("number") DEFINITION { EVAL { return 1 } } }
            MACRO "m" { TAKES("v", "number") RETURNS("number") DEFINITION { EVAL { return 1 } } }
            MACRO "n" { TAKES("v", "number") RETURNS("number") DEFINITION { EVAL { return 1 } } }
        """

        assertEquals(
            listOf(DUPLICATE_PARAMETER to "v", DUPLICATE_MACRO to "m"),
            marked(machine("INITIAL STATE \"a\" { }", macros = macros))
        )
    }

    /**
     * Calls of unknown macros and calls with another number of arguments are reported, in actions and in code; a
     * `MACRO(...)` call with too few arguments is only warned about.
     */
    @Test
    fun `a call of an unknown macro or with the wrong arguments is reported`() {
        val macros = """
            MACRO "two" { TAKES("a", "number") TAKES("b", "number") RETURNS("number") DEFINITION { EVAL { return @one(${'$'}(a)) } } }
            MACRO "one" { TAKES("a", "number") RETURNS("number") DEFINITION { EVAL { return @two(${'$'}(a), [1, 2]) + @nope(1) } } }
        """
        val states = """
            INITIAL STATE "a" { DO {
                SET("x", MACRO("two", "p", "q"))
                SET("x", MACRO("two", "p"))
                SET("x", MACRO("three", "p"))
                WITH MACRO("one", "p", "q") AS "r" { SET("x", "1") }
                SET("x", EVAL { return @one(f(1, 2)) + @two(1) })
            } }
        """

        assertEquals(
            listOf(
                MISSING_MACRO_ARGUMENTS to "two",
                UNKNOWN_MACRO to "three",
                MACRO_ARGUMENT_COUNT to "one",
                MACRO_ARGUMENT_COUNT to "@two",
                UNKNOWN_MACRO to "@nope"
            ),
            marked(machine(states, macros = macros)).filter { it.first != EVAL_WITHOUT_RETURN }
        )
    }

    /** A second `EVENT` clause is an error, and one that other clauses precede a warning. */
    @Test
    fun `a misplaced or second EVENT clause is reported`() {
        val text = machine(
            "INITIAL STATE \"a\" { }\nSTATE \"b\" { }",
            """
            TRANSITION "a" -> "b" { CONDITION { VALUE("ready") EVENT("d", "first") } }
            TRANSITION "a" -> "b" { CONDITION { EVENT("d", "one") EVENT("d", "two") } }
            TRANSITION "b" -> "a" { CONDITION { EVENT("d", "only") VALUE("ready") } }
            """
        )

        assertEquals(listOf(EVENT_CLAUSE_NOT_FIRST to "first", SEVERAL_EVENT_CLAUSES to "two"), marked(text))
        assertEquals(
            listOf(Severity.WARNING, Severity.ERROR),
            KstatesReader().read(text).diagnostics.map { it.severity }
        )
    }

    /** Options without effect are warned about; a history with inner states and full blocks are fine. */
    @Test
    fun `options without effect are warned about`() {
        val states = """
            INITIAL DEEP HISTORY STATE "a" { STATE "i" { } }
            SHALLOW HISTORY STATE "b" { BREAKPOINT { } TICK LIMIT { } }
            STATE "c" { TICK LIMIT { DELAY: 5 TICKS TIMEOUT: AFTER 3 TICKS TRANSITION TO "a" } }
            STATE "d" { BREAKPOINT { ENTRY } TICK LIMIT { DELAY: 3 TICKS TIMEOUT: AFTER 3 TICKS TRANSITION TO "a" } }
        """
        val result = KstatesReader().read(machine(states, "TRANSITION \"a\" -> \"b\" { BREAKPOINT { } }"))

        assertEquals(
            listOf(HISTORY_WITHOUT_INNER_STATES, EMPTY_BREAKPOINT, EMPTY_TICK_LIMIT, DELAY_BEYOND_TIMEOUT, EMPTY_BREAKPOINT),
            result.diagnostics.map { it.code }
        )
        assertEquals(setOf(Severity.WARNING), result.diagnostics.map { it.severity }.toSet())
    }

    /**
     * A `//` in `EVAL` code is warned about, also where it hides the closing brace and the parser fails after it; code
     * without `return` is warned about too. `#` comments and a return are fine.
     */
    @Test
    fun `EVAL code that does not run as meant is warned about`() {
        val hidden = machine("INITIAL STATE \"a\" { DO { SET(\"x\", EVAL { return 7 // 2 }) } }")
        val states = """
            INITIAL STATE "a" { DO {
                SET("x", EVAL {
                    y = 1 // 2
                    return y
                })
                SET("x", EVAL { y = 1 })
                SET("x", EVAL { return 1 # a comment })
                SET("x", EVAL { returned = 1
                    return returned })
            } }
        """
        val text = machine(states)

        assertEquals(COMMENT_IN_EVAL to "// 2 }) } }", marked(hidden).first())
        assertEquals(listOf(COMMENT_IN_EVAL to "// 2", EVAL_WITHOUT_RETURN to "EVAL { y = 1 }"), marked(text))
    }
}
