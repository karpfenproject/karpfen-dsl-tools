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

import io.karpfen.dsl.diagnostics.DiagnosticCode.ATTACHED_OBJECT_TYPE
import io.karpfen.dsl.diagnostics.DiagnosticCode.LITERAL_MISMATCH
import io.karpfen.dsl.diagnostics.DiagnosticCode.OPERATION_MISMATCH
import io.karpfen.dsl.diagnostics.DiagnosticCode.UNKNOWN_ATTACHED_TYPE
import io.karpfen.dsl.diagnostics.DiagnosticCode.UNKNOWN_PATH
import io.karpfen.dsl.verification.VerifiedProject.Companion.doing
import io.karpfen.dsl.verification.VerifiedProject.Companion.machine
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests the checks of the attachment of a state machine and of its action rules: paths that lead nowhere, operations
 * that do not fit what their path leads to, and literals that do not fit the property they write.
 */
class ActionVerificationTest {

    /** A state machine attached to a type the metamodel lacks is reported at the type name. */
    @Test
    fun `an unknown attached type is reported`() {
        val project = VerifiedProject(machine("INITIAL STATE \"s\" { }", attachedTo = "Robo"))

        assertEquals(listOf(UNKNOWN_ATTACHED_TYPE to "Robo"), project.marked)
        VerifiedProject(machine("INITIAL STATE \"s\" { }")).assertClean()
    }

    /** An object of another type, or one the model lacks, is reported; without an object or a model nothing is. */
    @Test
    fun `an attached object of another type or a missing one is reported`() {
        val text = machine("INITIAL STATE \"s\" { }")

        assertEquals(listOf(ATTACHED_OBJECT_TYPE to "Robot"), VerifiedProject(text, attachedObjectId = "p0").marked)
        assertEquals(listOf(ATTACHED_OBJECT_TYPE), VerifiedProject(text, attachedObjectId = "nobody").codes)
        VerifiedProject(text, attachedObjectId = null).assertClean()
        VerifiedProject(text, model = null).assertClean()
    }

    /** A path of a rule that leads nowhere is reported at the path; one that leads to a property is not. */
    @Test
    fun `a path that leads nowhere is reported`() {
        val project = VerifiedProject(
            doing(
                """
                SET("spead", "1")
                SET("position->z", "1")
                SET("speed->x", "1")
                SET("waypoints->x", "1")
                SET("position->x", "1")
                SET("target->x", "2")
                """
            )
        )

        assertEquals(
            listOf(
                UNKNOWN_PATH to "spead",
                UNKNOWN_PATH to "position->z",
                UNKNOWN_PATH to "speed->x",
                UNKNOWN_PATH to "waypoints->x"
            ),
            project.marked
        )
    }

    /** A path that an `IF IN SCOPE` block guards is not reported, even if the types know it not. */
    @Test
    fun `a guarded path is not reported`() {
        VerifiedProject(doing("IF IN SCOPE(\"position->z\") { SET(\"position->z\", \"1\") }")).assertClean()
    }

    /** The operations on properties must fit whether the property is a list, and a relation takes none of them. */
    @Test
    fun `an operation on a property that does not fit is reported`() {
        val project = VerifiedProject(
            doing(
                """
                SET("log", "a")
                APPEND("speed", "1")
                DROPLIST("name")
                DROPLIST("log")
                SETLIST("readings", "2")
                APPEND("log", "a")
                """
            )
        )

        assertEquals(
            listOf(OPERATION_MISMATCH to "log", OPERATION_MISMATCH to "speed", OPERATION_MISMATCH to "name"),
            project.marked
        )
    }

    /** A property operation on a relation is reported with the relation operation the runtime asks for instead. */
    @Test
    fun `a property operation on a relation is reported with the operation to use`() {
        val project = VerifiedProject(
            doing(
                """
                SET("target", "p1")
                SET("visited", "p1")
                APPEND("position", "1")
                SETLIST("waypoints", "1")
                DROPLIST("visited")
                """
            )
        )

        assertEquals(
            listOf("SETOBJ", "APPENDOBJ", "SETOBJ", "APPENDOBJ", "DROPOBJ"),
            project.findings.map { finding -> listOf("SETOBJ", "APPENDOBJ", "DROPOBJ").single { it in finding.message } }
        )
        assertEquals(List(5) { OPERATION_MISMATCH }, project.codes)
    }

    /** `SETOBJ` and `APPENDOBJ` need a relation of the right kind, and an object of its type that a `knows` may link. */
    @Test
    fun `an operation on a relation that does not fit is reported`() {
        val project = VerifiedProject(
            doing(
                """
                SETOBJ("self", "visited", "target")
                APPENDOBJ("self", "target", "target")
                SETOBJ("self", "position", "target")
                SETOBJ("self", "target", "name")
                SETOBJ("self", "speed", "target")
                SETOBJ("self", "nothing", "target")
                SETOBJ("self", "partner", "target")
                SETOBJ("waypoints", "x", "target")
                SETOBJ("self", "target", "position")
                SETOBJ("", "target", "waypoints->x")
                APPENDOBJ("self", "visited", "target")
                SETOBJ("position", "x", "target")
                """
            )
        )

        assertEquals(
            listOf(
                OPERATION_MISMATCH to "visited",
                OPERATION_MISMATCH to "target",
                OPERATION_MISMATCH to "target",
                OPERATION_MISMATCH to "name",
                OPERATION_MISMATCH to "speed",
                UNKNOWN_PATH to "nothing",
                OPERATION_MISMATCH to "target",
                OPERATION_MISMATCH to "waypoints",
                OPERATION_MISMATCH to "waypoints->x",
                OPERATION_MISMATCH to "x"
            ),
            project.marked
        )
    }

    /** `DROPOBJ` needs a path to one object. */
    @Test
    fun `a DROPOBJ that leads to no single object is reported`() {
        val project = VerifiedProject(doing("DROPOBJ(\"visited\")\nDROPOBJ(\"speed\")\nDROPOBJ(\"nothing\")\nDROPOBJ(\"target\")"))

        assertEquals(
            listOf(OPERATION_MISMATCH to "visited", OPERATION_MISMATCH to "speed", UNKNOWN_PATH to "nothing"),
            project.marked
        )
    }

    /** A literal must read as the type of the property it is written to, a list property taking one element. */
    @Test
    fun `a literal of the wrong type is reported`() {
        val project = VerifiedProject(
            doing(
                """
                SET("speed", "fast")
                SET("ready", "yes")
                APPEND("readings", "x")
                SET("speed", "2.5")
                SET("ready", "TRUE")
                SET("name", "anything")
                SETLIST("readings", "3")
                EVENT("public", "anything")
                """
            )
        )

        assertEquals(
            listOf(LITERAL_MISMATCH to "fast", LITERAL_MISMATCH to "yes", LITERAL_MISMATCH to "x"),
            project.marked
        )
    }
}
