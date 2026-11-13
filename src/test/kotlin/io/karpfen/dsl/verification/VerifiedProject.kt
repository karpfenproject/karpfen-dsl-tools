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
import io.karpfen.dsl.reader.kmodel.KmodelReader
import io.karpfen.dsl.reader.kstates.KstatesReader
import kotlin.test.assertTrue

/**
 * A small project around a robot whose state machine a test writes, verified with [ProjectVerifier].
 *
 * The domain has a robot with properties of every primitive type, single and list properties, `has` and `knows`
 * relations to one point and to lists of points, and a `knows` relation to another robot. The model leaves the robot's `ready` and `log` and the `y` of the
 * point `p1` without a value. The events are `go`, with a speed and a point, and `stop`, without a payload.
 *
 * @param kstates The text of the state machine.
 * @param attachedObjectId The object the state machine runs on.
 * @param metamodel The text of the domain metamodel.
 * @param events The text of the event metamodel, or null for a project without one.
 * @param model The text of the model, or null for a project without one.
 */
class VerifiedProject(
    val kstates: String,
    attachedObjectId: String? = "r1",
    metamodel: String = METAMODEL,
    events: String? = EVENTS,
    model: String? = MODEL
) {

    /** The problems the verification finds in the state machine. */
    val findings: List<Diagnostic>

    init {
        val metamodelRead = KmetaReader().read(metamodel)
        val eventsRead = events?.let { text -> KmetaReader(metamodelRead.model?.types.orEmpty()).read(text) }
        val modelRead = model?.let { text -> metamodelRead.model?.let { KmodelReader(it).read(text) } }
        val machine = StateMachineInput(KstatesReader().read(kstates), attachedObjectId)
        findings = ProjectVerifier().verify(ProjectInput(metamodelRead, eventsRead, modelRead, listOf(machine))).of(machine)
    }

    /** The codes of [findings]. */
    val codes: List<DiagnosticCode>
        get() = findings.map { it.code }

    /** The parts of [kstates] that [findings] mark, each with its code. */
    val marked: List<Pair<DiagnosticCode, String>>
        get() = findings.map { it.code to it.range.textIn(kstates) }

    /** Asserts that the verification finds nothing. */
    fun assertClean() {
        assertTrue(findings.isEmpty(), findings.joinToString("\n"))
    }

    companion object {

        /** The domain metamodel. */
        const val METAMODEL = """
type "Point" "a point" { prop("x", "number") prop("y", "number") }
type "Robot" "a robot" {
    prop("name", "string")
    prop("speed", "number")
    prop("ready", "boolean")
    prop("log", list("string"))
    prop("readings", list("number"))
    has("position", "Point")
    has("waypoints", list("Point"))
    knows("target", "Point")
    knows("visited", list("Point"))
    knows("partner", "Robot")
}
type "World" "the world" { has("robot", "Robot") has("points", list("Point")) }
"""

        /** The event metamodel. */
        const val EVENTS = """
type "go" "go somewhere" { prop("speed", "number") has("to", "Point") }
type "stop" "stop" { }
"""

        /** The model. */
        const val MODEL = """
make object "w":"World" {
    has("robot") -> make object "r1":"Robot" {
        prop("name") -> "r"
        prop("speed") -> "1.0"
        prop("readings") -> "1.0"
        has("position") -> make object "p0":"Point" { prop("x") -> "0.0" prop("y") -> "0.0" }
        knows("target") -> "p1"
    }
    has("points") -> make object "p1":"Point" { prop("x") -> "1.0" }
}
"""

        /**
         * Returns a state machine attached to the robot with the [states], [transitions] and [macros] given as text.
         */
        fun machine(states: String, transitions: String = "", macros: String = "", attachedTo: String = "Robot"): String =
            "STATEMACHINE ATTACHED TO \"$attachedTo\" {\n    STATES {\n$states\n    }\n    TRANSITIONS {\n$transitions\n    }\n" +
                (if (macros.isEmpty()) "" else "    MACROS {\n$macros\n    }\n") + "}\n"

        /** Returns a state machine with a single initial state `s` whose `DO` block holds [actions]. */
        fun doing(actions: String, macros: String = ""): String =
            machine("INITIAL STATE \"s\" { DO {\n$actions\n} }", macros = macros)
    }
}
