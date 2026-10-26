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
package io.karpfen.dsl.printer.kstates

import io.karpfen.dsl.dsl.textual.KstatesDSLConverter
import io.karpfen.dsl.fixtures.CodeShape
import io.karpfen.dsl.reader.kstates.KstatesReader
import io.karpfen.dsl.states.Macro
import io.karpfen.dsl.states.SplitTransition
import io.karpfen.dsl.states.State
import io.karpfen.dsl.states.actions.ActionBlock
import io.karpfen.dsl.states.actions.ActionItem
import io.karpfen.dsl.states.actions.ActionOperationType.APPEND
import io.karpfen.dsl.states.actions.ActionOperationType.APPENDOBJ
import io.karpfen.dsl.states.actions.ActionOperationType.DROPLIST
import io.karpfen.dsl.states.actions.ActionOperationType.DROPOBJ
import io.karpfen.dsl.states.actions.ActionOperationType.EVENT
import io.karpfen.dsl.states.actions.ActionOperationType.SET
import io.karpfen.dsl.states.actions.ActionOperationType.SETLIST
import io.karpfen.dsl.states.actions.ActionOperationType.SETOBJ
import io.karpfen.dsl.states.actions.ActionRule
import io.karpfen.dsl.states.actions.EvalActionRightSide
import io.karpfen.dsl.states.actions.InScopeBlock
import io.karpfen.dsl.states.actions.ValueActionRightSide
import io.karpfen.dsl.states.conditions.ValueCondition
import io.karpfen.dsl.states.macros.CodeBlock
import io.karpfen.dsl.states.macros.Definition
import io.karpfen.dsl.states.macros.ReturnsDirective
import io.karpfen.dsl.states.macros.TakesDirective
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

/**
 * Checks that [KstatesPrinter] rejects what KStates cannot express, so that it never writes text that reads back
 * with errors or as another state machine, and that it writes the closely related things KStates can express.
 */
class KstatesExpressibilityTest {

    private val printer = KstatesPrinter()

    /**
     * A split with a single target cannot be written in KStates.
     */
    @Test
    fun `a split with one target is rejected`() {
        val split = SplitTransition("idle", listOf("idle"), true, ValueCondition("true"))
        assertFailsWith<IllegalArgumentException> { printer.printTransition(split) }
    }

    /**
     * An `IF IN SCOPE` block without paths and a rule with a relation but no right side cannot be written.
     */
    @Test
    fun `actions KStates cannot express are rejected`() {
        val emptyScope = InScopeBlock(emptyList(), ActionBlock(mutableListOf()))
        val relationWithoutTarget = ActionRule(SETOBJ, "self", null, "target")
        assertFailsWith<IllegalArgumentException> { printer.printState(stateDoing(emptyScope)) }
        assertFailsWith<IllegalArgumentException> { printer.printState(stateDoing(relationWithoutTarget)) }
    }

    /**
     * A rule with another number of operands than its operation expects would not read back, so it is rejected;
     * every operation is written with the operands it expects.
     */
    @Test
    fun `rules with the wrong number of operands are rejected`() {
        val value = ValueActionRightSide("v")
        val withoutRightSide = listOf(SET, APPEND, SETLIST, EVENT).map { ActionRule(it, "x") }
        val withRightSide = listOf(DROPLIST, DROPOBJ).map { ActionRule(it, "x", value) }
        val withoutRelation = ActionRule(SETOBJ, "x", value)
        val withRelation = ActionRule(SET, "x", value, "r")
        (withoutRightSide + withRightSide + withoutRelation + withRelation).forEach { rule ->
            assertFailsWith<IllegalArgumentException>("$rule") { printer.printState(stateDoing(rule)) }
        }

        val rightArities = listOf(
            ActionRule(SET, "x", value),
            ActionRule(DROPLIST, "x"),
            ActionRule(APPENDOBJ, "x", value, "r")
        )
        rightArities.forEach { rule -> assertEquals(rule, readBack(stateDoing(rule)).onDo.items.single()) }
    }

    /**
     * Code whose braces do not balance outside string literals, or that ends in a comment, which reading drops,
     * cannot stand in an `EVAL` block; KStates reads `//` as a comment even where Python means floor division.
     */
    @Test
    fun `code an EVAL block cannot hold is rejected`() {
        val unbalanced = listOf("return {", "x = 1 # }\nreturn x", "}{")
        val endingInComment = listOf("return a // b", "return 7 // 2", "// only a comment")
        (unbalanced + endingInComment).forEach { code ->
            assertFailsWith<IllegalArgumentException>(code) { printer.printState(stateDoing(evalRule(code))) }
        }
    }

    /**
     * Code with braces inside string literals, with balanced braces and with a comment between its tokens reads back
     * as it was.
     */
    @Test
    fun `code an EVAL block can hold reads back unchanged`() {
        listOf("return \"}\"", "d = {'a': {}}\nreturn d['a']", "x = 7 // 2\nreturn x", "return 1").forEach { code ->
            val rule = readBack(stateDoing(evalRule(code))).onDo.items.single() as ActionRule
            assertEquals(code, CodeShape.of((rule.rightSide as EvalActionRightSide).code))
        }
    }

    /**
     * A line that continues a string literal after an escaped line break stays as it is, so the value of the literal
     * reads back unchanged, whether the line stands deeper or shallower than the code around it.
     */
    @Test
    fun `lines that continue a string literal are written as they are`() {
        val codes = listOf(
            "x = \"a\\\nb\"\nreturn x",
            "x = 'a\\\n      b'\nreturn x",
            "x = \"a\\\nb\"\n                    return x",
            "if a :\n    y = \"one\\\n  two\"\n    return y"
        )
        codes.forEach { code ->
            val rule = readBack(stateDoing(evalRule(code))).onDo.items.single() as ActionRule
            val reread = (rule.rightSide as EvalActionRightSide).code

            assertEquals(CodeShape.of(code), CodeShape.of(reread), reread)
            assertEquals(continuedLinesOf(code), continuedLinesOf(reread), reread)
        }
    }

    /**
     * Code read from a text with `\r\n` or `\r` line breaks is checked and written with `\n` line breaks, the form
     * that reads back. A backslash before `\r\n` escapes only the `\r`, so a literal continued that way is no
     * string literal of KStates until the line breaks are `\n`: then its line stays as it is, printing again changes
     * nothing, and braces inside it no longer count, so code whose braces then do not balance is rejected.
     */
    @Test
    fun `code with other line breaks is checked and written with the line breaks it reads back with`() {
        val continued = "s = \"a\\\n   b\"\nc()\nreturn s"
        listOf("\r\n", "\r").forEach { lineBreak ->
            val state = stateDoing(evalRule(continued.replace("\n", lineBreak)))
            val rule = readBack(state).onDo.items.single() as ActionRule
            val reread = (rule.rightSide as EvalActionRightSide).code

            assertEquals(CodeShape.of(continued), CodeShape.of(reread), reread)
            assertEquals(continuedLinesOf(continued), continuedLinesOf(reread), reread)
            assertEquals(printer.printState(state, 2), printer.printState(readBack(state), 2))
        }
        val braceInContinuedLiteral = "d = {\"k\": \"a\\\r\n{\"}  # }\r\nreturn d"
        assertFailsWith<IllegalArgumentException> { printer.printState(stateDoing(evalRule(braceInContinuedLiteral))) }
    }

    /**
     * A typed call names its function with an identifier, which no keyword of KStates is, and only a `RETURNS`
     * directive takes `reference(...)`; any other call is rejected.
     */
    @Test
    fun `typed calls a directive cannot hold are rejected`() {
        val takes = listOf("reference(\"T\")", "set(\"T\")", "with(\"T\")", "append(\"T\")", "STATE(\"T\")")
        takes.forEach { type ->
            val takingType = macro(listOf(TakesDirective("p", type)), "T")
            assertFailsWith<IllegalArgumentException>(type) { printer.printMacro(takingType) }
        }
        listOf("with(\"T\")", "as(\"T\")", "SET(\"T\")").forEach { type ->
            assertFailsWith<IllegalArgumentException>(type) { printer.printMacro(macro(emptyList(), type)) }
        }
    }

    /**
     * The typed calls KStates reads, `list(...)` in both directives and `reference(...)` in `RETURNS`, read back
     * without errors.
     */
    @Test
    fun `typed calls a directive can hold read back unchanged`() {
        val macro = macro(listOf(TakesDirective("p", "list(\"T\")"), TakesDirective("q", "number")), "reference(\"T\")")
        val text = documentWith(printer.printMacro(macro, 2))
        val result = KstatesReader().read(text)

        assertEquals(emptyList(), result.diagnostics)
        assertEquals(macro, result.model!!.macros.single())
    }

    /**
     * The type name of a typed call may hold a line break that a backslash escapes, as a literal continued on the
     * next line, or end in a backslash, as `'X\'` reads; such a call reads without errors and prints, and the printed
     * text reads back to the same macro.
     */
    @Test
    fun `typed calls whose type name holds an escaped line break or a trailing backslash print`() {
        val takenAndReturnedTypes = listOf(
            "list(\"a\\\nb\")" to "\"number\"",
            "list(\"O\\\n      bstacle\")" to "\"number\"",
            "list(\"'X\\'\")" to "\"number\"",
            "\"number\"" to "reference(\"'T\\'\")"
        )
        takenAndReturnedTypes.forEach { (taken, returned) ->
            val text = documentWith(
                "MACRO \"m\" {\n            TAKES(\"p\", $taken)\n            RETURNS($returned)\n" +
                    "            DEFINITION { EVAL { return 1 } }\n        }"
            )
            val read = KstatesReader().read(text)
            assertEquals(emptyList(), read.diagnostics, text)

            val printed = printer.print(read.model!!)
            val reread = KstatesReader().read(printed)

            assertEquals(emptyList(), reread.diagnostics, printed)
            assertEquals(read.model.macros.single(), reread.model!!.macros.single(), printed)
        }
    }

    private fun evalRule(code: String): ActionRule = ActionRule(SET, "x", EvalActionRightSide(code))

    /**
     * Returns the lines of [code] that continue a string literal, each with the line before it.
     */
    private fun continuedLinesOf(code: String): List<String> {
        val lines = code.lines()
        return StringContinuations().linesIn(code).sorted().map { lines[it - 1].trimStart() + "\n" + lines[it] }
    }

    private fun macro(takes: List<TakesDirective>, returns: String): Macro =
        Macro("m", takes, ReturnsDirective(returns), Definition(CodeBlock("return 1")))

    private fun stateDoing(item: ActionItem): State =
        State("busy", ActionBlock(mutableListOf()), ActionBlock(mutableListOf(item)), mutableListOf(), null, true)

    /**
     * Prints [state] into a document and reads it back with the converter, which must not throw.
     */
    private fun readBack(state: State): State {
        val text = "STATEMACHINE ATTACHED TO \"R\" {\n    STATES {\n        ${printer.printState(state, 2)}\n    }\n" +
            "    TRANSITIONS { }\n}\n"
        assertTrue(KstatesReader().read(text).diagnostics.isEmpty(), text)
        return KstatesDSLConverter.parseKstatesString(text).states.single()
    }

    private fun documentWith(macroText: String): String =
        "STATEMACHINE ATTACHED TO \"R\" {\n    STATES {\n        INITIAL STATE \"s\" { }\n    }\n" +
            "    TRANSITIONS { }\n    MACROS {\n        $macroText\n    }\n}\n"
}
