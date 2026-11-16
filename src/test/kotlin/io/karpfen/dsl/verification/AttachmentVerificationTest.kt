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
package io.karpfen.dsl.verification

import io.karpfen.dsl.diagnostics.Diagnostic
import io.karpfen.dsl.diagnostics.DiagnosticCode
import io.karpfen.dsl.reader.kmeta.KmetaReader
import io.karpfen.dsl.reader.kstates.KstatesReader
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Tests the checks of dynamic attachment across the files of a project: the names of the attachable state machines,
 * the `behaves after` of the metamodels, `ATTACH` and `EXCHANGE`, and the paths of a state machine that runs on several
 * types.
 */
class AttachmentVerificationTest {

    /** The lamp metamodel, with a dimmer that has watts but no `lit`, and a bulb that behaves after `glowing`. */
    private val metamodel = """
        type "Bulb" "A light bulb" behaves after "glowing" {
            prop("watts", "number")
            prop("lit", "boolean")
        }
        type "Dimmer" "A dimmer" {
            prop("watts", "number")
        }
        type "Switch" "A wall switch" {
            prop("position", "string")
        }
        type "Lamp" "A lamp" {
            prop("mode", "string")
            has("bulb", "Bulb")
            knows("spare", "Bulb")
            knows("control", "Switch")
        }
        type "Room" "A room" {
            has("lamp", "Lamp")
            has("switch", "Switch")
            has("spares", list("Bulb"))
        }
    """.trimIndent()

    /** Returns a state machine with [header] and [entry] in the `ENTRY` block of its one state. */
    private fun machine(header: String, entry: String = ""): String =
        "STATEMACHINE $header {\n    STATES {\n        INITIAL STATE \"a\" { ENTRY {\n$entry\n} }\n    }\n    TRANSITIONS { }\n}\n"

    /** The state machine `glowing`, which the bulb behaves after. */
    private val glowing = machine("\"glowing\" ATTACHABLE TO \"Bulb\"", "SET(\"lit\", \"true\")")

    /** The state machine `blinking`, which bulbs and dimmers may run. */
    private val blinking = machine("\"blinking\" ATTACHABLE TO \"Bulb\", \"Dimmer\"", "SET(\"watts\", \"0\")")

    /**
     * The verified project of [metamodel] or [kmeta], the event metamodel [events] and the state machines [texts].
     *
     * @property findings What the verification found.
     * @property inputs The state machines in the order of [texts].
     */
    private inner class Project(vararg texts: String, kmeta: String = metamodel, events: String? = null) {
        val inputs = texts.map { StateMachineInput(KstatesReader().read(it)) }
        val metamodelRead = KmetaReader().read(kmeta)
        val findings: ProjectFindings = ProjectVerifier().verify(
            ProjectInput(metamodelRead, events?.let { KmetaReader(metamodelRead.model!!.types).read(it) }, null, inputs)
        )

        /** Returns the codes and the marked text of the problems of the state machine at [index], written as [text]. */
        fun marked(index: Int, text: String): List<Pair<DiagnosticCode, String>> = marks(findings.of(inputs[index]), text)
    }

    /** Returns the codes of [diagnostics] with the parts of [text] they mark. */
    private fun marks(diagnostics: List<Diagnostic>, text: String): List<Pair<DiagnosticCode, String>> =
        diagnostics.map { it.code to it.range.textIn(text) }

    /** A project whose names and types fit finds nothing. */
    @Test
    fun `a fitting project is clean`() {
        val room = machine(
            "ATTACHED TO \"Room\"",
            """
            APPENDOBJ("self", "spares", EVAL { return {"watts": 40} }) ATTACH "blinking"
            SETOBJ("lamp", "bulb", EVAL { return {"watts": 60} }) ATTACH "glowing"
            EXCHANGE("lamp->bulb", "blinking")
            EXCHANGE("lamp->spare", "glowing")
            """.trimIndent()
        )
        val project = Project(room, glowing, blinking)

        assertEquals(emptyList(), project.findings.all)
    }

    /**
     * A path that is valid for one listed type but not for another is reported with the type it fails for; a problem
     * that is the same for every type is reported once, as it is.
     */
    @Test
    fun `a path must be valid for every listed type`() {
        val text = machine(
            "\"blinking\" ATTACHABLE TO \"Bulb\", \"Dimmer\"",
            "SET(\"watts\", \"0\")\nSET(\"lit\", \"false\")\nSET(\"color\", \"red\")"
        )
        val project = Project(text, glowing)
        val findings = project.findings.of(project.inputs[0])

        assertEquals(
            listOf(
                "For type 'Dimmer': Type 'Dimmer' has no property or relation 'lit'",
                "For type 'Bulb': Type 'Bulb' has no property or relation 'color'",
                "For type 'Dimmer': Type 'Dimmer' has no property or relation 'color'"
            ),
            findings.map { it.message }
        )
        assertEquals(listOf("lit", "color", "color"), findings.map { it.range.textIn(text) })
        val unreachable = "STATEMACHINE \"blinking\" ATTACHABLE TO \"Bulb\", \"Dimmer\" {\n" +
            "    STATES { INITIAL STATE \"a\" { } STATE \"b\" { } }\n    TRANSITIONS { }\n}\n"
        val once = Project(unreachable, glowing)
        assertEquals(listOf(DiagnosticCode.UNREACHABLE_STATE to "b"), once.marked(0, unreachable))
        assertTrue(once.findings.of(once.inputs[0]).single().message.startsWith("State 'b' can never be active"))
    }

    /** Two attachable state machines of the same name are both reported at their names. */
    @Test
    fun `names of attachable state machines are unique across files`() {
        val other = machine("\"glowing\" ATTACHABLE TO \"Bulb\"")
        val project = Project(glowing, other)

        assertEquals(listOf(DiagnosticCode.DUPLICATE_STATE_MACHINE_NAME to "glowing"), project.marked(0, glowing))
        assertEquals(listOf(DiagnosticCode.DUPLICATE_STATE_MACHINE_NAME to "glowing"), project.marked(1, other))
    }

    /** A `behaves after` with an unknown name is reported in the metamodel, at the name. */
    @Test
    fun `behaves after needs an attachable state machine of the project`() {
        val project = Project(blinking)

        assertEquals(listOf(DiagnosticCode.UNKNOWN_STATE_MACHINE to "glowing"), marks(project.findings.metamodel, metamodel))
        assertEquals("No attachable state machine of the project is named 'glowing'", project.findings.metamodel.single().message)
        assertEquals(project.findings.metamodel, project.findings.errors)
    }

    /** A `behaves after` that names a state machine the type may not run is reported in the metamodel. */
    @Test
    fun `behaves after needs a state machine attachable to the type`() {
        val lampOnly = machine("\"glowing\" ATTACHABLE TO \"Lamp\"")
        val project = Project(lampOnly, blinking)

        assertEquals(listOf(DiagnosticCode.ATTACHABLE_TYPE_MISMATCH to "glowing"), marks(project.findings.metamodel, metamodel))
        assertEquals("The state machine 'glowing' is attachable to 'Lamp', not to 'Bulb'", project.findings.metamodel.single().message)
    }

    /** A `behaves after` in the event metamodel is reported there, at the whole clause. */
    @Test
    fun `behaves after in an event metamodel is an error`() {
        val events = "type \"press\" \"A press\" behaves after \"glowing\" { }"
        val project = Project(glowing, events = events)

        assertEquals(emptyList(), project.findings.metamodel)
        assertEquals(listOf(DiagnosticCode.BEHAVES_AFTER_IN_EVENTS to "behaves after \"glowing\""), marks(project.findings.events, events))
    }

    /** `ATTACH` on a `knows` relation is reported at the clause, `ATTACH` with an unknown or unfitting name at the name. */
    @Test
    fun `ATTACH names an attachable state machine of the type of a has relation`() {
        val text = machine(
            "ATTACHED TO \"Room\"",
            """
            SETOBJ("lamp", "spare", "lamp->bulb") ATTACH "glowing"
            SETOBJ("lamp", "bulb", EVAL { return {} }) ATTACH "flashing"
            APPENDOBJ("self", "spares", EVAL { return {} }) ATTACH "keeper"
            APPENDOBJ("self", "spares", EVAL { return {} }) ATTACH EVAL { return "flashing" }
            """.trimIndent()
        )
        val keeper = machine("\"keeper\" ATTACHABLE TO \"Room\"")
        val project = Project(text, glowing, keeper)

        assertEquals(
            listOf(
                DiagnosticCode.ATTACH_ON_KNOWS to "ATTACH \"glowing\"",
                DiagnosticCode.UNKNOWN_STATE_MACHINE to "flashing",
                DiagnosticCode.ATTACHABLE_TYPE_MISMATCH to "keeper"
            ),
            project.marked(0, text)
        )
    }

    /** `EXCHANGE` needs a path to one object and a state machine attachable to its type. */
    @Test
    fun `EXCHANGE names an attachable state machine of the type at its path`() {
        val text = machine(
            "ATTACHED TO \"Room\"",
            """
            EXCHANGE("lamp->mode", "glowing")
            EXCHANGE("spares", "glowing")
            EXCHANGE("lamp", "glowing")
            EXCHANGE("lamp->bulb", "flashing")
            EXCHANGE("lamp->bulb", MACRO("pick"))
            """.trimIndent()
        )
        val withMacro = text.replace("TRANSITIONS { }", "TRANSITIONS { }\n    MACROS { MACRO \"pick\" { RETURNS(\"string\") DEFINITION { EVAL { return \"glowing\" } } } }")
        val project = Project(withMacro, glowing)

        assertEquals(
            listOf(
                DiagnosticCode.OPERATION_MISMATCH to "lamp->mode",
                DiagnosticCode.OPERATION_MISMATCH to "spares",
                DiagnosticCode.ATTACHABLE_TYPE_MISMATCH to "glowing",
                DiagnosticCode.UNKNOWN_STATE_MACHINE to "flashing"
            ),
            project.marked(0, withMacro)
        )
    }

    /** The paths an `ATTACH` value reads are verified like those of any right side; a computed name is not checked. */
    @Test
    fun `the code of an ATTACH clause is verified`() {
        val text = machine(
            "ATTACHED TO \"Room\"",
            "SETOBJ(\"lamp\", \"bulb\", EVAL { return {} }) ATTACH EVAL { return $(lamp->colour) }\n" +
                "SETOBJ(\"lamp\", \"bulb\", EVAL { return {} }) ATTACH EVAL { return $(lamp->mode) }"
        )
        val project = Project(text, glowing)

        assertEquals(listOf(DiagnosticCode.UNRESOLVED_CODE_REFERENCE to "$(lamp->colour)"), project.marked(0, text))
    }

    /** A type of an attachable state machine that the metamodel lacks is reported at the type. */
    @Test
    fun `the types of an attachable state machine must exist`() {
        val text = machine("\"glowing\" ATTACHABLE TO \"Bulb\", \"Lantern\"")
        val project = Project(text)

        assertEquals(listOf(DiagnosticCode.UNKNOWN_ATTACHED_TYPE to "Lantern"), project.marked(0, text))
    }

    /** An unknown name is not reported while a state machine of the project cannot be read, as it might be the one. */
    @Test
    fun `an unreadable state machine leaves unknown names unreported`() {
        val broken = "STATEMACHINE \"glowing\" ATTACHABLE TO \"Bulb\" { STATES { INITIAL STATE \"a\" { } "
        val project = Project(blinking, broken)

        assertEquals(emptyList(), project.findings.metamodel)
    }
}
