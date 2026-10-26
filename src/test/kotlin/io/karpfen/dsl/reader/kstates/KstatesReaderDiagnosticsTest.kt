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
import io.karpfen.dsl.reader.ParseResult
import io.karpfen.dsl.states.Transition
import io.karpfen.dsl.states.actions.ActionRule
import io.karpfen.dsl.states.conditions.EvalCondition
import io.karpfen.dsl.states.conditions.ValueCondition
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

/**
 * Tests the problems the KStates reader reports on broken texts, where it reports them, and the partial state
 * machine it builds nevertheless.
 */
class KstatesReaderDiagnosticsTest {

    /**
     * A state machine without an initial state is reported at the `STATES` keyword, with the message of the
     * throwing converter.
     */
    @Test
    fun `a missing initial state is reported at the states keyword`() {
        val text = machine(states = "STATE \"a\" { }\nSTATE \"b\" { }")

        val result = KstatesReader().read(text)

        assertCodes(listOf(DiagnosticCode.INITIAL_STATE_COUNT), result)
        assertEquals("STATES", result.diagnostics.single().range.textIn(text))
        assertEquals(converterMessageFor(text), result.diagnostics.single().message)
    }

    /**
     * Several initial states, at any depth, are each reported at their name.
     */
    @Test
    fun `several initial states are each reported at their name`() {
        val text = machine(states = "INITIAL STATE \"a\" {\n    INITIAL STATE \"inner\" { }\n}\nINITIAL STATE \"b\" { }")

        val result = KstatesReader().read(text)

        assertCodes(List(3) { DiagnosticCode.INITIAL_STATE_COUNT }, result)
        assertEquals(listOf("a", "inner", "b"), result.diagnostics.map { it.range.textIn(text) })
        assertEquals(converterMessageFor(text), result.diagnostics.first().message)
    }

    /**
     * Rules with the wrong number of operands are reported at the rule, with the message of the throwing
     * converter, and left out; the correct rules around them stay.
     */
    @Test
    fun `action rules of the wrong arity are reported at the rule`() {
        val rules = listOf("SET(\"x\")", "DROPLIST(\"a\", \"b\")", "SETOBJ(\"a\", \"b\")")
        val text = machine(
            states = "INITIAL STATE \"a\" {\n    ENTRY {\n        APPEND(\"log\", \"start\")\n" +
                rules.joinToString("") { "        $it\n" } + "        DROPOBJ(\"o\")\n    }\n}"
        )

        val result = KstatesReader().read(text)

        assertCodes(List(3) { DiagnosticCode.ACTION_ARITY }, result)
        assertEquals(rules, result.diagnostics.map { it.range.textIn(text) })
        rules.zip(result.diagnostics).forEach { (rule, diagnostic) ->
            assertEquals(converterMessageFor(machine(states = "INITIAL STATE \"a\" { ENTRY { $rule } }")), diagnostic.message)
        }
        val kept = result.model!!.states.single().onEntry.items.map { (it as ActionRule).operationType.name }
        assertEquals(listOf("APPEND", "DROPOBJ"), kept)
    }

    /**
     * A state being typed, its closing brace still missing, is part of the model with what is complete in it.
     */
    @Test
    fun `a state being typed yields a partial model`() {
        val text = "STATEMACHINE ATTACHED TO \"Robot\" {\n    STATES {\n        INITIAL STATE \"idle\" { }\n" +
            "        STATE \"busy\" {\n            ENTRY {\n                SET(\"speed\", \"1\")\n                SET(\"x\", "

        val result = KstatesReader().read(text)

        assertTrue(result.hasErrors)
        val stateMachine = result.model!!
        assertEquals("Robot", stateMachine.attachedToClass)
        assertEquals(listOf("idle", "busy"), stateMachine.states.map { it.name })
        assertEquals(1, stateMachine.states[1].onEntry.items.size)
    }

    /**
     * A broken condition clause is left out; its transition and the other clauses stay.
     */
    @Test
    fun `a broken condition clause is left out`() {
        val text = machine(
            states = "INITIAL STATE \"a\" { }\nSTATE \"b\" { }",
            transitions = "TRANSITION \"a\" -> \"b\" {\n    CONDITION {\n        EVENT(\"public\" \"go\")\n" +
                "        EVAL { return 1 }\n    }\n}"
        )

        val result = KstatesReader().read(text)

        assertTrue(result.diagnostics.all { it.code == DiagnosticCode.SYNTAX_ERROR }, result.diagnostics.toString())
        val transition = result.model!!.transitions.single() as Transition
        assertEquals("return 1", (transition.condition as EvalCondition).code)
        assertEquals("return 1", result.sourceMap.codeRangeOf(transition.condition)?.textIn(text))
    }

    /**
     * A transition without a condition block gets the always-true condition, which has no location.
     */
    @Test
    fun `an unconditional transition has a condition without location`() {
        val text = machine(states = "INITIAL STATE \"a\" { }\nSTATE \"b\" { }", transitions = "TRANSITION \"a\" -> \"b\" { }")

        val result = KstatesReader().read(text)

        val condition = (result.model!!.transitions.single() as Transition).condition
        assertEquals("true", (condition as ValueCondition).boolVariable)
        assertEquals(null, result.sourceMap.locationOf(condition))
    }

    /**
     * Two equal rules are different elements and have different locations.
     */
    @Test
    fun `equal elements have their own locations`() {
        val text = machine(states = "INITIAL STATE \"a\" {\n    DO {\n        SET(\"x\", \"1\")\n        SET(\"x\", \"1\")\n    }\n}")

        val result = KstatesReader().read(text)

        val (first, second) = result.model!!.states.single().onDo.items
        assertEquals(first, second)
        assertNotEquals(result.sourceMap.rangeOf(first), result.sourceMap.rangeOf(second))
        assertEquals(listOf(5, 6), listOf(first, second).map { result.sourceMap.rangeOf(it)!!.start.line })
    }

    private fun machine(states: String, transitions: String = ""): String =
        "STATEMACHINE ATTACHED TO \"Robot\" {\n    STATES {\n" + states.prependIndent("        ") +
            "\n    }\n    TRANSITIONS {\n" + transitions.prependIndent("        ") + "\n    }\n}\n"

    private fun converterMessageFor(text: String): String? =
        assertFailsWith<IllegalArgumentException> { KstatesDSLConverter.parseKstatesString(text) }.message

    private fun assertCodes(expected: List<DiagnosticCode>, result: ParseResult<*>) {
        assertEquals(expected, result.diagnostics.map { it.code }, result.diagnostics.joinToString("\n"))
    }
}
