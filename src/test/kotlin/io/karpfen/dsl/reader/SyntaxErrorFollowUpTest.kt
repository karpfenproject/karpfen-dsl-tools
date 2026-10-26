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
package io.karpfen.dsl.reader

import io.karpfen.dsl.diagnostics.DiagnosticCode
import io.karpfen.dsl.dsl.textual.KmetaDSLConverter
import io.karpfen.dsl.reader.kmeta.KmetaReader
import io.karpfen.dsl.reader.kmodel.KmodelReader
import io.karpfen.dsl.reader.kstates.KstatesReader
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Tests that the checks a syntax error may mislead report nothing; the syntax error is reported instead. These are the
 * checks which look for something in the whole text, the initial state of a state machine, the types of a metamodel,
 * the target type of a relation and the object a `knows` names, where the parser may have lost what they look for,
 * and the checks of KModel statements against the class type of their object, where a syntax error in the braces
 * may have put the statements into another object.
 */
class SyntaxErrorFollowUpTest {

    /**
     * A typo in the header of the one initial state, or a state whose name is still being typed, makes the parser
     * drop the state; that the state machine then lacks an initial state is no problem of its own.
     */
    @Test
    fun `a state lost to a syntax error leaves the initial state count out`() {
        val texts = listOf(
            machine("INITIAL STAT \"a\" { }\nSTATE \"b\" { }"),
            machine("INITIAL STAT \"a\" { }"),
            machine("INITIAL STATE { }\nSTATE \"b\" { }"),
            machine("INITIAL STATE ")
        )
        texts.forEach { text ->
            val codes = KstatesReader().read(text).diagnostics.map { it.code }
            assertTrue(codes.isNotEmpty() && codes.all { it == DiagnosticCode.SYNTAX_ERROR }, "$codes for\n$text")
        }
    }

    /**
     * A syntax error outside the `STATES` block leaves the states as they are written, so their count is checked.
     */
    @Test
    fun `a syntax error outside the states keeps the initial state count`() {
        val text = machine("STATE \"a\" { }", transitions = "TRANSITION \"a\" -> { }")

        val codes = KstatesReader().read(text).diagnostics.map { it.code }

        assertTrue(DiagnosticCode.INITIAL_STATE_COUNT in codes && DiagnosticCode.SYNTAX_ERROR in codes, "$codes")
    }

    /**
     * A type whose name the parser lost leaves the relations to it unresolved without a report of their own.
     */
    @Test
    fun `a type lost to a syntax error leaves unknown target types unreported`() {
        val text = "typ \"Point\" \"a point\" { prop(\"x\", \"number\") }\ntype \"Box\" \"a box\" { has(\"p\", \"Point\") }"

        val codes = KmetaReader().read(text).diagnostics.map { it.code }.distinct()

        assertTrue(DiagnosticCode.UNKNOWN_TARGET_TYPE !in codes && codes.isNotEmpty(), "$codes")
    }

    /**
     * An object whose signature the parser lost leaves the references to it unresolved without a report of their
     * own; without the syntax error, a reference to a missing object is reported.
     */
    @Test
    fun `an object lost to a syntax error leaves its references unreported`() {
        val metamodel = KmetaDSLConverter.parseKmetaString(
            "type \"Point\" \"a point\" { }\ntype \"Room\" \"a room\" { has(\"p\", \"Point\") knows(\"q\", \"Point\") }"
        )
        val broken = "make object \"r\":\"Room\" {\n    has(\"p\") -> make object :\"Point\" { }\n    knows(\"q\") -> \"a\"\n}"
        val missing = "make object \"r\":\"Room\" {\n    knows(\"q\") -> \"a\"\n}"

        val brokenCodes = KmodelReader(metamodel).read(broken).diagnostics.map { it.code }
        val missingCodes = KmodelReader(metamodel).read(missing).diagnostics.map { it.code }

        assertTrue(DiagnosticCode.UNRESOLVED_REFERENCE !in brokenCodes && brokenCodes.isNotEmpty(), "$brokenCodes")
        assertEquals(listOf(DiagnosticCode.UNRESOLVED_REFERENCE), missingCodes)
    }

    /**
     * A typo in the keyword of the only type, or a type whose name is still being typed, makes the parser drop the
     * type; that the text then defines no type is no problem of its own. Without a syntax error it is one.
     */
    @Test
    fun `a type lost to a syntax error leaves the missing types unreported`() {
        val texts = listOf("typ \"A\" \"d\" { prop(\"x\", \"number\") }", "type", "type { }")
        texts.forEach { text ->
            val result = KmetaReader().read(text)
            val codes = result.diagnostics.map { it.code }
            assertTrue(codes.isNotEmpty() && DiagnosticCode.NO_TYPES !in codes, "$codes for $text")
            assertEquals(null, result.model, text)
        }
        assertEquals(listOf(DiagnosticCode.NO_TYPES), KmetaReader().read("// no type\n").diagnostics.map { it.code })
    }

    /**
     * An object block that is not closed yet takes in the statements that follow it in its parent; they are no
     * unknown properties or relations of the object they landed in. With the block closed, the text has no problem.
     */
    @Test
    fun `statements an unclosed object takes in are not checked against its class type`() {
        val unclosed = robotRoom(
            "has(\"robots\") -> make object \"r1\":\"Robot\" {\n    prop(\"name\") -> \"Robby\"",
            "prop(\"area\") -> \"12\"\nprop(\"label\") -> \"lab\"\nknows(\"door\") -> \"r1\""
        )
        val closed = unclosed.replace("\"Robby\"", "\"Robby\" }")

        val codes = KmodelReader(robotRoomMetamodel).read(unclosed).diagnostics.map { it.code }

        assertEquals(listOf(DiagnosticCode.SYNTAX_ERROR), codes, unclosed)
        assertEquals(emptyList(), KmodelReader(robotRoomMetamodel).read(closed).diagnostics, closed)
    }

    /**
     * An object closed before the block that is not closed keeps the checks of its statements, and so does the block
     * that lacks the closing brace.
     */
    @Test
    fun `objects closed before an unclosed one are still checked`() {
        val text = robotRoom(
            "prop(\"aera\") -> \"12\"\n" +
                "has(\"robots\") -> make object \"r0\":\"Robot\" { prop(\"nmae\") -> \"x\" }\n" +
                "has(\"robots\") -> make object \"r1\":\"Robot\" {",
            "prop(\"label\") -> \"lab\""
        )

        val diagnostics = KmodelReader(robotRoomMetamodel).read(text).diagnostics

        assertEquals(
            listOf("SYNTAX_ERROR", "UNKNOWN_PROPERTY aera", "UNKNOWN_PROPERTY nmae"),
            diagnostics.map { if (it.code == DiagnosticCode.SYNTAX_ERROR) "SYNTAX_ERROR" else "${it.code} ${it.range.textIn(text)}" }
                .sorted(),
            text
        )
    }

    /**
     * A closing brace too many ends an object early and hands its remaining statements to the objects around it,
     * whose class types do not define them.
     */
    @Test
    fun `statements handed out by a closing brace too many are not checked`() {
        val text = robotRoom(
            "has(\"robots\") -> make object \"r1\":\"Robot\" {\n    prop(\"on\") -> \"true\" }\n" +
                "    prop(\"name\") -> \"Robby\"\n}",
            "prop(\"area\") -> \"12\""
        )

        val codes = KmodelReader(robotRoomMetamodel).read(text).diagnostics.map { it.code }.distinct()

        assertEquals(listOf(DiagnosticCode.SYNTAX_ERROR), codes, text)
    }

    /**
     * Returns a state machine with the lines [states] in its `STATES` block and [transitions] in its `TRANSITIONS`
     * block.
     */
    private fun machine(states: String, transitions: String = ""): String =
        "STATEMACHINE ATTACHED TO \"Robot\" {\n    STATES {\n" + states.prependIndent("        ") +
            "\n    }\n    TRANSITIONS {\n" + transitions.prependIndent("        ") + "\n    }\n}\n"

    /**
     * Returns a room of [robotRoomMetamodel] with the lines [statements] followed by the lines [moreStatements].
     */
    private fun robotRoom(statements: String, moreStatements: String): String =
        "make object \"lab\":\"Room\" {\n" + "$statements\n$moreStatements".prependIndent("    ") + "\n}\n"

    /** The metamodel of [robotRoom]. */
    private val robotRoomMetamodel = KmetaDSLConverter.parseKmetaString(
        "type \"Robot\" \"a robot\" { prop(\"name\", \"string\") prop(\"on\", \"boolean\") }\n" +
            "type \"Room\" \"a room\" { prop(\"area\", \"number\") prop(\"label\", \"string\") " +
            "has(\"robots\", list(\"Robot\")) knows(\"door\", \"Robot\") }"
    )
}
