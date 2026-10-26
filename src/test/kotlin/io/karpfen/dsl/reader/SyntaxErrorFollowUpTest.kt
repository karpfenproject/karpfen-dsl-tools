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
 * Tests that the checks which look for something in the whole text, the initial state of a state machine, the
 * target type of a relation and the object a `knows` names, report nothing where a syntax error may have made the
 * parser lose what they look for; the syntax error is reported instead.
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

    private fun machine(states: String, transitions: String = ""): String =
        "STATEMACHINE ATTACHED TO \"Robot\" {\n    STATES {\n" + states.prependIndent("        ") +
            "\n    }\n    TRANSITIONS {\n" + transitions.prependIndent("        ") + "\n    }\n}\n"
}
