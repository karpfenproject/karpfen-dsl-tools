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
 * Tests the checks of pages across the files of a project: the compositions of fragments, the problems that prevent
 * their flattening, the checks of a fragment where it runs, and the name space shared by fragments and attachable state
 * machines.
 */
class PagesVerificationTest {

    /** The lamp metamodel. */
    private val metamodel = """
        type "Bulb" "A light bulb" {
            prop("watts", "number")
            prop("maxWatts", "number")
            prop("lit", "boolean")
        }
        type "Lamp" "A lamp" {
            prop("mode", "string")
            has("bulb", "Bulb")
        }
    """.trimIndent()

    /** Returns a fragment named [name] with [states], [transitions] and [macros]. */
    private fun fragment(name: String, states: String, transitions: String = "", macros: String = ""): String =
        "STATEMACHINE \"$name\" FRAGMENT {\n    STATES {\n$states\n    }\n    TRANSITIONS {\n$transitions\n    }\n" +
            (if (macros.isEmpty()) "" else "    MACROS {\n$macros\n    }\n") + "}\n"

    /** Returns a state machine attached to the lamp with [states], [transitions] and [macros]. */
    private fun lamp(states: String, transitions: String = "", macros: String = ""): String =
        "STATEMACHINE ATTACHED TO \"Lamp\" {\n    STATES {\n$states\n    }\n    TRANSITIONS {\n$transitions\n    }\n" +
            (if (macros.isEmpty()) "" else "    MACROS {\n$macros\n    }\n") + "}\n"

    /** Returns a macro named [name] that takes watts and returns [value]. */
    private fun macro(name: String, value: String = "w / 2"): String =
        "MACRO \"$name\" { TAKES(\"w\", \"number\") RETURNS(\"number\") DEFINITION { EVAL { return $value } } }"

    /** The fragment `bulb test`, which lights the bulb and passes. */
    private val bulbTest = fragment(
        "bulb test",
        "INITIAL STATE \"lit\" { ENTRY { SET(\"bulb->lit\", \"true\") } }\nFINAL STATE \"passed\" { }",
        "TRANSITION \"lit\" -> \"passed\" { }"
    )

    /** Returns the lamp that runs the container states [containers], each written `name` to `fragment`, one after the other. */
    private fun lampUsing(vararg containers: Pair<String, String>, macros: String = ""): String {
        val names = listOf("off") + containers.map { it.first } + listOf("on")
        val states = "INITIAL STATE \"off\" { }\n" +
            containers.joinToString("\n") { (name, used) -> "CONTAINER STATE \"$name\" USE \"$used\" { }" } + "\nSTATE \"on\" { }"
        val transitions = names.zipWithNext().mapIndexed { index, (from, to) ->
            "TRANSITION \"$from\" -> \"$to\" { ${if (index == 0) "" else "CONDITION { DONE }"} }"
        }.joinToString("\n")
        return lamp(states, transitions, macros)
    }

    /**
     * The verified project of the state machines [texts] and the metamodel [kmeta].
     *
     * @property findings What the verification found.
     * @property inputs The state machines in the order of [texts].
     */
    private inner class Project(private vararg val texts: String, kmeta: String = metamodel) {
        val inputs = texts.map { StateMachineInput(KstatesReader().read(it)) }
        val metamodelRead = KmetaReader().read(kmeta)
        val findings: ProjectFindings = ProjectVerifier().verify(ProjectInput(metamodelRead, null, null, inputs))

        /** Returns the codes and the marked text of the problems of the state machine at [index]. */
        fun marked(index: Int): List<Pair<DiagnosticCode, String>> = marks(findings.of(inputs[index]), texts[index])

        /** Returns the problems of the state machine at [index]. */
        fun of(index: Int): List<Diagnostic> = findings.of(inputs[index])
    }

    /** Returns the codes of [diagnostics] with the parts of [text] they mark. */
    private fun marks(diagnostics: List<Diagnostic>, text: String): List<Pair<DiagnosticCode, String>> =
        diagnostics.map { it.code to it.range.textIn(text) }

    /** A lamp that runs a fragment twice is clean: no state inside a container is unreachable, and `DONE` is fine. */
    @Test
    fun `a fitting composition is clean`() {
        val project = Project(lampUsing("first" to "bulb test", "second" to "bulb test"), bulbTest)

        assertEquals(emptyList(), project.findings.all)
    }

    /** A container state that uses an unknown name is reported at the name, but not while a file cannot be read. */
    @Test
    fun `an unknown fragment is reported at its name`() {
        val text = lampUsing("self test" to "bulb tset")
        val project = Project(text, bulbTest)

        assertEquals(listOf(DiagnosticCode.UNKNOWN_FRAGMENT to "bulb tset"), project.marked(0))
        assertEquals(
            "Container state 'self test' of the state machine attached to 'Lamp' uses 'bulb tset', but no fragment is " +
                "named 'bulb tset'",
            project.of(0).single().message
        )
        val broken = Project(text, "STATEMACHINE \"bulb tset\" FRAGMENT { STATES { FINAL STATE }")
        assertEquals(emptyList(), broken.of(0))
    }

    /** A container state that uses an attachable state machine is reported at the name. */
    @Test
    fun `a container state must use a fragment`() {
        val glowing = "STATEMACHINE \"glowing\" ATTACHABLE TO \"Bulb\" {\n    STATES { INITIAL STATE \"on\" { } }\n    TRANSITIONS { }\n}\n"
        val project = Project(lampUsing("self test" to "glowing"), glowing)

        assertEquals(listOf(DiagnosticCode.NOT_A_FRAGMENT to "glowing"), project.marked(0))
        assertEquals(emptyList(), project.of(1))
    }

    /** Two fragments that use each other are a cycle, reported once at the container state that closes it. */
    @Test
    fun `a cycle of fragments is reported where it closes`() {
        val first = fragment("first", "INITIAL CONTAINER STATE \"x\" USE \"second\" { }\nFINAL STATE \"done\" { }", "TRANSITION \"x\" -> \"done\" { CONDITION { DONE } }")
        val second = fragment("second", "INITIAL CONTAINER STATE \"y\" USE \"first\" { }\nFINAL STATE \"done\" { }", "TRANSITION \"y\" -> \"done\" { CONDITION { DONE } }")
        val project = Project(lampUsing("t" to "first"), first, second)

        assertEquals(emptyList(), project.of(0))
        assertEquals(listOf(DiagnosticCode.FRAGMENT_CYCLE to "first"), project.marked(2))
        assertEquals(emptyList(), project.of(1))
    }

    /** A written name that a flattened name repeats is reported at the state that comes later. */
    @Test
    fun `a clash of flattened names is reported at the later state`() {
        val text = lamp(
            "INITIAL STATE \"t/lit\" { }\nCONTAINER STATE \"t\" USE \"bulb test\" { }",
            "TRANSITION \"t/lit\" -> \"t\" { }"
        )
        val project = Project(text, bulbTest)

        assertEquals(emptyList(), project.of(0))
        assertEquals(listOf(DiagnosticCode.QUALIFIED_NAME_CLASH to "lit"), project.marked(1))
    }

    /** Two different macros of one name in two files are reported at the later definition. */
    @Test
    fun `two different macros of one name are reported`() {
        val halving = fragment("halving", "INITIAL STATE \"a\" { }\nFINAL STATE \"done\" { }", "TRANSITION \"a\" -> \"done\" { }", macro("half"))
        val project = Project(lampUsing("t" to "halving", macros = macro("half", "w / 3")), halving)

        assertEquals(emptyList(), project.of(0))
        assertEquals(listOf(DiagnosticCode.CONFLICTING_MACROS to "half"), project.marked(1))
    }

    /**
     * A macro that only another file of the composition defines is a warning at the call in a fragment, with its
     * arguments counted, once however often the fragment is used.
     */
    @Test
    fun `macros of other files are warnings`() {
        val calling = fragment(
            "calling",
            "INITIAL STATE \"a\" { ENTRY {\nSET(\"bulb->watts\", MACRO(\"half\", \"bulb->maxWatts\"))\n" +
                "SET(\"bulb->watts\", MACRO(\"half\", \"bulb->maxWatts\", \"bulb->watts\"))\n" +
                "SET(\"bulb->watts\", EVAL { return @half(1) })\n} }\nFINAL STATE \"done\" { }",
            "TRANSITION \"a\" -> \"done\" { }"
        )
        val project = Project(lampUsing("first" to "calling", "second" to "calling", macros = macro("half")), calling)

        assertEquals(emptyList(), project.of(0))
        assertEquals(
            listOf(
                DiagnosticCode.MACRO_FROM_OTHER_FILE to "half",
                DiagnosticCode.MACRO_FROM_OTHER_FILE to "half",
                DiagnosticCode.MACRO_ARGUMENT_COUNT to "half",
                DiagnosticCode.MACRO_FROM_OTHER_FILE to "@half"
            ),
            project.marked(1)
        )
        assertEquals(
            "Macro 'half' is defined in the state machine attached to 'Lamp', not in this file; the call works, but " +
                "define the macros a file calls in the file itself",
            project.of(1).first().message
        )
        assertEquals(3, project.findings.warnings.size)
    }

    /**
     * A macro that no file of the composition defines is an error at the call in a fragment; a fragment that a state
     * machine uses leaves this to the composition of that state machine, and an unused fragment reports it itself.
     */
    @Test
    fun `macros no file defines are errors`() {
        val calling = fragment(
            "calling",
            "INITIAL STATE \"a\" { ENTRY {\nSET(\"bulb->watts\", MACRO(\"half\", \"bulb->maxWatts\"))\n" +
                "SET(\"bulb->watts\", MACRO(\"missing\", \"bulb->maxWatts\"))\n" +
                "SET(\"bulb->watts\", EVAL { return @gone(2) })\n} }\nFINAL STATE \"done\" { }",
            "TRANSITION \"a\" -> \"done\" { }"
        )
        val used = Project(lampUsing("first" to "calling", "second" to "calling", macros = macro("half")), calling)

        assertEquals(emptyList(), used.of(0))
        assertEquals(
            listOf(DiagnosticCode.UNKNOWN_COMPOSED_MACRO to "missing", DiagnosticCode.UNKNOWN_COMPOSED_MACRO to "@gone"),
            used.marked(1)
        )
        val unused = Project(calling)
        assertEquals(
            listOf(
                DiagnosticCode.UNKNOWN_COMPOSED_MACRO to "half",
                DiagnosticCode.UNKNOWN_COMPOSED_MACRO to "missing",
                DiagnosticCode.UNKNOWN_COMPOSED_MACRO to "@gone"
            ),
            unused.marked(0)
        )
    }

    /**
     * A finding inside a fragment is reported in the text of the fragment, with the type of the state machine that uses
     * it, and once however often the fragment is used.
     */
    @Test
    fun `a finding inside a fragment is reported once in the fragment`() {
        val coloring = fragment(
            "coloring",
            "INITIAL STATE \"a\" { ENTRY { SET(\"bulb->color\", \"red\") } }\nFINAL STATE \"done\" { }",
            "TRANSITION \"a\" -> \"done\" { }"
        )
        val project = Project(lampUsing("first" to "coloring", "second" to "coloring"), coloring)

        assertEquals(emptyList(), project.of(0))
        assertEquals(listOf(DiagnosticCode.UNKNOWN_PATH to "bulb->color"), project.marked(1))
        assertEquals("Type 'Bulb' has no property or relation 'color'", project.of(1).single().message)
    }

    /** A fragment that nothing uses is still checked as far as it can be without a host, here for an unreachable state. */
    @Test
    fun `an unused fragment is checked on its own`() {
        val unused = fragment(
            "unused",
            "INITIAL STATE \"a\" { ENTRY { SET(\"bulb->color\", \"red\") } }\nSTATE \"lost\" { }\nFINAL STATE \"done\" { }",
            "TRANSITION \"a\" -> \"done\" { }"
        )
        val project = Project(unused)

        assertEquals(listOf(DiagnosticCode.UNREACHABLE_STATE to "lost"), project.marked(0))
    }

    /** A fragment and an attachable state machine share one name space. */
    @Test
    fun `fragments and attachable state machines share their names`() {
        val glowing = "STATEMACHINE \"bulb test\" ATTACHABLE TO \"Bulb\" {\n    STATES { INITIAL STATE \"on\" { } }\n    TRANSITIONS { }\n}\n"
        val project = Project(bulbTest, glowing)

        assertEquals(listOf(DiagnosticCode.DUPLICATE_STATE_MACHINE_NAME to "bulb test"), project.marked(0))
        assertEquals(listOf(DiagnosticCode.DUPLICATE_STATE_MACHINE_NAME to "bulb test"), project.marked(1))
    }

    /** `ATTACH`, `EXCHANGE` and `behaves after` cannot name a fragment. */
    @Test
    fun `a fragment cannot be attached`() {
        val kmeta = metamodel.replace("type \"Bulb\" \"A light bulb\"", "type \"Bulb\" \"A light bulb\" behaves after \"bulb test\"")
        val text = lamp(
            "INITIAL STATE \"off\" { ENTRY {\nSETOBJ(\"self\", \"bulb\", EVAL { return {\"watts\": 40} }) ATTACH \"bulb test\"\n" +
                "EXCHANGE(\"bulb\", \"bulb test\")\n} }"
        )
        val project = Project(text, bulbTest, kmeta = kmeta)

        assertEquals(
            listOf(DiagnosticCode.ATTACHED_FRAGMENT to "bulb test", DiagnosticCode.ATTACHED_FRAGMENT to "bulb test"),
            project.marked(0)
        )
        assertEquals(listOf(DiagnosticCode.ATTACHED_FRAGMENT to "bulb test"), marks(project.findings.metamodel, kmeta))
        assertEquals(emptyList(), project.of(1))
    }
}
