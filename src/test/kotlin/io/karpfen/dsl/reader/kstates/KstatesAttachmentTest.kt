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
import io.karpfen.dsl.fixtures.StructureDump
import io.karpfen.dsl.grammar.kstates.KstatesLexer
import io.karpfen.dsl.reader.ParseResult
import io.karpfen.dsl.states.StateMachine
import io.karpfen.dsl.states.actions.ActionOperationType
import io.karpfen.dsl.states.actions.ActionRule
import io.karpfen.dsl.states.actions.EvalActionRightSide
import io.karpfen.dsl.states.actions.MacroActionRightSide
import io.karpfen.dsl.states.actions.ValueActionRightSide
import org.antlr.v4.runtime.CharStreams
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Tests how the KStates reader and the converter read the constructs of dynamic attachment: the attachable form of a
 * state machine, the `ATTACH` clause and the rule `EXCHANGE`, the checks of the header and where the elements are
 * written.
 */
class KstatesAttachmentTest {

    /** Returns a state machine with [header] after `STATEMACHINE` and [entry] in the `ENTRY` block of its one state. */
    private fun machine(header: String, entry: String = ""): String = """
        STATEMACHINE $header {
            STATES {
                INITIAL STATE "a" { ENTRY { $entry } }
            }
            TRANSITIONS { }
        }
    """.trimIndent()

    /** Reads [text] with every feature on and requires that the converter builds the same state machine. */
    private fun read(text: String): ParseResult<StateMachine> {
        val result = KstatesReader().read(text)
        if (!result.hasErrors) {
            assertEquals(StructureDump.of(KstatesDSLConverter.parseKstatesString(text)), StructureDump.of(result.model))
        }
        return result
    }

    /** Returns the action rules of the `ENTRY` block of the first state of [machine]. */
    private fun rulesOf(machine: StateMachine): List<ActionRule> = machine.states.first().onEntry.items.filterIsInstance<ActionRule>()

    /** The attached form reads as before: no name, one type, the type as its name in the source map. */
    @Test
    fun `the attached form has no name and one type`() {
        val text = machine("ATTACHED TO \"Lamp\"")
        val result = read(text)
        val machine = result.model!!

        assertEquals(emptyList(), result.diagnostics)
        assertFalse(machine.isAttachable)
        assertNull(machine.name)
        assertEquals("Lamp", machine.attachedToClass)
        assertEquals(listOf("Lamp"), machine.types)
        assertEquals(emptyList(), machine.attachableTo)
        assertEquals("Lamp", result.sourceMap.nameRangeOf(machine)!!.textIn(text))
        assertEquals(emptyList(), result.sourceMap.operandRangesOf(machine))
    }

    /** The attachable form has a name and its types, which the source map holds as its name and its operands. */
    @Test
    fun `the attachable form has a name and several types`() {
        val text = machine("\"blinking\" ATTACHABLE TO \"Bulb\", \"Lamp\"")
        val result = read(text)
        val machine = result.model!!

        assertEquals(emptyList(), result.diagnostics)
        assertTrue(machine.isAttachable)
        assertEquals("blinking", machine.name)
        assertEquals("", machine.attachedToClass)
        assertEquals(listOf("Bulb", "Lamp"), machine.attachableTo)
        assertEquals(listOf("Bulb", "Lamp"), machine.types)
        assertEquals("blinking", result.sourceMap.nameRangeOf(machine)!!.textIn(text))
        assertEquals(listOf("Bulb", "Lamp"), result.sourceMap.operandRangesOf(machine).map { it.textIn(text) })
        assertTrue(result.sourceMap.rangeOf(machine)!!.textIn(text).startsWith("STATEMACHINE \"blinking\""))
    }

    /** An attachable state machine without a name is reported at `ATTACHABLE` and read with an empty name. */
    @Test
    fun `an attachable state machine needs a name`() {
        val text = machine("ATTACHABLE TO \"Bulb\"")
        val result = read(text)

        assertEquals(listOf(DiagnosticCode.MISSING_STATE_MACHINE_NAME), result.diagnostics.map { it.code })
        assertEquals("ATTACHABLE", result.diagnostics.single().range.textIn(text))
        assertEquals("", result.model!!.name)
        assertEquals(listOf("Bulb"), result.model.attachableTo)
    }

    /** An attachable state machine with an empty name has no name either, and the empty string is marked. */
    @Test
    fun `an attachable state machine with an empty name needs a name`() {
        val text = machine("\"\" ATTACHABLE TO \"Bulb\"")
        val result = read(text)

        assertEquals(listOf(DiagnosticCode.MISSING_STATE_MACHINE_NAME), result.diagnostics.map { it.code })
        assertEquals("\"\"", result.diagnostics.single().range.textIn(text))
        assertEquals("", result.model!!.name)
    }

    /** A header without a type is a syntax error, for either form. */
    @Test
    fun `a state machine needs a type`() {
        listOf("\"blinking\" ATTACHABLE TO", "ATTACHED TO").forEach { header ->
            val result = KstatesReader().read(machine(header))
            assertEquals(listOf(DiagnosticCode.SYNTAX_ERROR), result.diagnostics.map { it.code }, header)
        }
    }

    /** A type listed twice is reported at the later one. */
    @Test
    fun `a type listed twice is reported`() {
        val text = machine("\"blinking\" ATTACHABLE TO \"Bulb\", \"Lamp\", \"Bulb\"")
        val result = read(text)

        assertEquals(listOf(DiagnosticCode.REPEATED_ATTACHABLE_TYPE), result.diagnostics.map { it.code })
        assertEquals(text.lastIndexOf("Bulb"), result.diagnostics.single().range.start.offset)
        assertEquals("The type 'Bulb' is listed twice; each type may be listed once", result.diagnostics.single().message)
    }

    /** A name or a second type of an attached state machine is reported at itself. */
    @Test
    fun `an attached state machine has neither a name nor a second type`() {
        val text = machine("\"lamp\" ATTACHED TO \"Lamp\", \"Bulb\"")
        val result = read(text)

        assertEquals(listOf(DiagnosticCode.ATTACHED_HEADER, DiagnosticCode.ATTACHED_HEADER), result.diagnostics.map { it.code })
        assertEquals(listOf("lamp", "Bulb"), result.diagnostics.map { it.range.textIn(text) })
        assertFalse(result.model!!.isAttachable)
        assertEquals("Lamp", result.model.attachedToClass)
    }

    /** `ATTACHABLE` has no lower-case form, while `ATTACH` and `EXCHANGE` have one, like their neighbours. */
    @Test
    fun `keywords follow the case of their neighbours`() {
        val lowerHeader = KstatesReader().read(machine("\"b\" attachable TO \"Bulb\""))
        assertTrue(DiagnosticCode.SYNTAX_ERROR in lowerHeader.diagnostics.map { it.code })
        assertFalse(lowerHeader.model!!.isAttachable)
        val text = machine(
            "ATTACHED TO \"Room\"",
            "appendobj(\"self\", \"spares\", MACRO(\"new bulb\")) attach \"blinking\" exchange(\"lamp->bulb\", \"steady\")"
        )
        val result = read(text)
        assertEquals(emptyList(), result.diagnostics.filter { it.code != DiagnosticCode.UNKNOWN_MACRO })
        val rules = rulesOf(result.model!!)
        assertEquals(listOf(ActionOperationType.APPENDOBJ, ActionOperationType.EXCHANGE), rules.map { it.operationType })
        assertEquals(ValueActionRightSide("blinking"), rules.first().attach!!.stateMachineName)
        val lexer = KstatesLexer(CharStreams.fromString("attach exchange attachable"))
        assertEquals(
            listOf(KstatesLexer.ATTACH, KstatesLexer.EXCHANGE, KstatesLexer.IDENT),
            lexer.allTokens.filter { it.channel == 0 }.map { it.type }
        )
    }

    /** `SETOBJ` and `APPENDOBJ` take an `ATTACH` clause with a value in each of the three forms. */
    @Test
    fun `the clause follows SETOBJ and APPENDOBJ with any value`() {
        val text = machine(
            "ATTACHED TO \"Room\"",
            """
            APPENDOBJ("self", "spares", MACRO("new bulb", "40")) ATTACH "blinking"
            SETOBJ("lamp", "bulb", MACRO("new bulb", "60")) ATTACH EVAL { return $(lamp->mode) }
            SETOBJ("lamp", "bulb", MACRO("new bulb", "60")) ATTACH MACRO("pick", "lamp->mode")
            SETOBJ("lamp", "control", "switch")
            """.trimIndent()
        )
        val result = read(text)
        val rules = rulesOf(result.model!!)

        assertEquals(emptyList(), result.diagnostics.filter { it.code != DiagnosticCode.UNKNOWN_MACRO })
        assertEquals("blinking", (rules[0].attach!!.stateMachineName as ValueActionRightSide).value)
        assertEquals("return $(lamp->mode)", (rules[1].attach!!.stateMachineName as EvalActionRightSide).code)
        assertEquals(MacroActionRightSide("pick", listOf("lamp->mode")), rules[2].attach!!.stateMachineName)
        assertNull(rules[3].attach)
        assertEquals("ATTACH \"blinking\"", result.sourceMap.rangeOf(rules[0].attach!!)!!.textIn(text))
        assertEquals(
            listOf("blinking"),
            result.sourceMap.operandRangesOf(rules[0].attach!!.stateMachineName).map { it.textIn(text) }
        )
        assertEquals("return $(lamp->mode)", result.sourceMap.codeRangeOf(rules[1].attach!!.stateMachineName)!!.textIn(text))
    }

    /** `ATTACH` after any other operation is a syntax error, and the rule is left out. */
    @Test
    fun `the clause after any other operation is a syntax error`() {
        listOf(
            "SET(\"mode\", \"on\")", "APPEND(\"log\", \"x\")", "SETLIST(\"log\", EVAL { return [] })", "DROPLIST(\"log\")",
            "DROPOBJ(\"bulb\")", "EVENT(\"lamp\", \"on\")", "EXCHANGE(\"bulb\", \"steady\")"
        ).forEach { rule ->
            val result = KstatesReader().read(machine("ATTACHED TO \"Lamp\"", "$rule ATTACH \"blinking\""))
            assertEquals(listOf(DiagnosticCode.SYNTAX_ERROR), result.diagnostics.map { it.code }.distinct(), rule)
        }
    }

    /**
     * `ATTACH` after a rule of another operation that is written with three operands parses, since the grammar allows
     * the clause after any rule with three operands; the rule is reported for its number of operands and left out.
     */
    @Test
    fun `the clause after another operation with three operands is reported for the operands`() {
        listOf(
            "SET(\"self\", \"mode\", \"on\")", "APPEND(\"self\", \"log\", \"x\")", "EXCHANGE(\"self\", \"bulb\", \"steady\")",
            "EVENT(\"lamp\", \"on\", \"now\")"
        ).forEach { rule ->
            val result = KstatesReader().read(machine("ATTACHED TO \"Lamp\"", "$rule ATTACH \"blinking\""))
            assertEquals(listOf(DiagnosticCode.ACTION_ARITY), result.diagnostics.map { it.code }, rule)
        }
    }

    /** `EXCHANGE` takes a path and a value in each of the three forms, and other numbers of operands are reported. */
    @Test
    fun `EXCHANGE takes a path and a value`() {
        val text = machine(
            "ATTACHED TO \"Lamp\"",
            """
            EXCHANGE("bulb", "steady")
            EXCHANGE("bulb", EVAL { return $(mode) })
            WITH MACRO("pick") AS "choice" { EXCHANGE("bulb", MACRO("pick")) }
            """.trimIndent()
        )
        val result = read(text)
        val rules = rulesOf(result.model!!)

        assertEquals(emptyList(), result.diagnostics.filter { it.code != DiagnosticCode.UNKNOWN_MACRO })
        assertEquals(ActionRule(ActionOperationType.EXCHANGE, "bulb", ValueActionRightSide("steady")), rules[0])
        assertEquals("return $(mode)", (rules[1].rightSide as EvalActionRightSide).code)
        assertEquals(listOf("bulb", "steady"), result.sourceMap.operandRangesOf(rules[0]).map { it.textIn(text) } +
            result.sourceMap.operandRangesOf(rules[0].rightSide!!).map { it.textIn(text) })
        listOf("EXCHANGE(\"bulb\")", "EXCHANGE(\"self\", \"bulb\", \"steady\")").forEach { rule ->
            val wrong = KstatesReader().read(machine("ATTACHED TO \"Lamp\"", rule))
            assertEquals(listOf(DiagnosticCode.ACTION_ARITY), wrong.diagnostics.map { it.code }, rule)
        }
    }

    /** Dynamic attachment is part of the core language, so its constructs are allowed with every optional feature off. */
    @Test
    fun `the constructs are allowed with every optional feature off`() {
        val text = machine(
            "\"keeper\" ATTACHABLE TO \"Room\"",
            "APPENDOBJ(\"self\", \"spares\", EVAL { return {} }) ATTACH \"blinking\" EXCHANGE(\"lamp->bulb\", \"steady\") SET(\"x\", \"1\")"
        )
        val result = KstatesReader(emptySet()).read(text)

        assertEquals(emptyList(), result.diagnostics)
        assertEquals("keeper", result.model!!.name)
        assertEquals(3, rulesOf(result.model).size)
    }

    /** The code and the macro call of an `ATTACH` clause are checked like those of a rule. */
    @Test
    fun `the value of the clause is checked like a right side`() {
        val text = machine("ATTACHED TO \"Room\"", "SETOBJ(\"lamp\", \"bulb\", EVAL { return {} }) ATTACH MACRO(\"pick\")")
        val result = KstatesReader().read(text)

        assertEquals(listOf(DiagnosticCode.UNKNOWN_MACRO), result.diagnostics.map { it.code })
        val withoutReturn = KstatesReader().read(machine("ATTACHED TO \"Room\"", "SETOBJ(\"lamp\", \"bulb\", EVAL { return {} }) ATTACH EVAL { 1 }"))
        assertEquals(listOf(DiagnosticCode.EVAL_WITHOUT_RETURN), withoutReturn.diagnostics.map { it.code })
    }
}
