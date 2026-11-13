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

import io.karpfen.dsl.diagnostics.DiagnosticCode.MACRO_ARGUMENT_TYPE
import io.karpfen.dsl.diagnostics.DiagnosticCode.OPERATION_MISMATCH
import io.karpfen.dsl.diagnostics.DiagnosticCode.UNKNOWN_MACRO_TYPE
import io.karpfen.dsl.diagnostics.DiagnosticCode.UNKNOWN_PATH
import io.karpfen.dsl.diagnostics.DiagnosticCode.UNRESOLVED_CODE_REFERENCE
import io.karpfen.dsl.diagnostics.DiagnosticCode.UNRESOLVED_MACRO_ARGUMENT
import io.karpfen.dsl.verification.VerifiedProject.Companion.doing
import io.karpfen.dsl.verification.VerifiedProject.Companion.machine
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests the checks of the paths a state machine reads: in `VALUE` conditions, in the code of `EVAL` bodies and as
 * arguments of macros, and the types of the macros. The machines run on no object, so the values of the model play no
 * part.
 */
class ReadVerificationTest {

    /** A `VALUE` condition needs a path to a boolean or a string; a path that leads nowhere is reported too. */
    @Test
    fun `a VALUE condition on a path that leads nowhere or to no boolean is reported`() {
        val conditions = listOf("ready", "name", "true", "FALSE", "speed", "log", "position", "nothing")
        val transitions = conditions.joinToString("\n") { "TRANSITION \"a\" -> \"b\" NOT LOOPING { CONDITION { VALUE(\"$it\") } }" }
        val project = VerifiedProject(machine("INITIAL STATE \"a\" { }\nSTATE \"b\" { }", transitions), attachedObjectId = null)

        assertEquals(
            listOf(
                OPERATION_MISMATCH to "speed",
                OPERATION_MISMATCH to "log",
                OPERATION_MISMATCH to "position",
                UNKNOWN_PATH to "nothing"
            ),
            project.marked
        )
    }

    /** A `$(path)` from the context is followed segment by segment and reported where it leads nowhere. */
    @Test
    fun `a code reference that leads nowhere is reported at the reference`() {
        val project = VerifiedProject(
            doing("SET(\"speed\", EVAL { return \$(speed) + \$(position->x) + \$(position->z) + \$(speed->x) + \$(waypoints->x) })"),
            attachedObjectId = null
        )

        assertEquals(
            listOf(UNRESOLVED_CODE_REFERENCE to "\$(position->z)", UNRESOLVED_CODE_REFERENCE to "\$(speed->x)"),
            project.marked
        )
    }

    /** A reference that an `IF IN SCOPE` block guards is not reported. */
    @Test
    fun `a guarded code reference is not reported`() {
        val text = doing("IF IN SCOPE(\"position->z\") { SET(\"speed\", EVAL { return \$(position->z->w) }) }")

        VerifiedProject(text, attachedObjectId = null).assertClean()
    }

    /** A reference to a binding of a `WITH` block follows the type its macro returns. */
    @Test
    fun `a code reference to a binding follows the type the macro returns`() {
        val macros = """
            MACRO "point" { RETURNS("Point") DEFINITION { EVAL { return {"x": 1, "y": 2} } } }
            MACRO "points" { RETURNS(list("Point")) DEFINITION { EVAL { return [] } } }
        """
        val actions = """
            WITH MACRO("point") AS "p" { SET("speed", EVAL { return ${'$'}(p->x) + ${'$'}(p->z) + ${'$'}(p) }) }
            WITH MACRO("points") AS "ps" { SET("speed", EVAL { return ${'$'}(ps) + ${'$'}(ps->x) }) }
        """
        val project = VerifiedProject(doing(actions, macros), attachedObjectId = null)

        assertEquals(listOf(UNRESOLVED_CODE_REFERENCE to "\$(p->z)", UNRESOLVED_CODE_REFERENCE to "\$(ps->x)"), project.marked)
    }

    /**
     * In the code of a macro, a reference follows the parameter it starts from through the dicts the macro receives,
     * which hold the id and the type name too; any other reference is a local variable.
     */
    @Test
    fun `a code reference in a macro follows its parameters`() {
        val macros = """
            MACRO "m" {
                TAKES("a", "Point")
                TAKES("ps", list("Point"))
                TAKES("r", "Robot")
                RETURNS("number")
                DEFINITION { EVAL {
                    for q in ${'$'}(ps) :
                        x = ${'$'}(q->anything)
                    return ${'$'}(a->x) + ${'$'}(a->__id__) + ${'$'}(a->z) + ${'$'}(ps->x) + ${'$'}(r->position->x) + ${'$'}(r->waypoints->x)
                } }
            }
        """
        val project = VerifiedProject(doing("", macros), attachedObjectId = null)

        assertEquals(
            listOf(
                UNRESOLVED_CODE_REFERENCE to "\$(a->z)",
                UNRESOLVED_CODE_REFERENCE to "\$(ps->x)",
                UNRESOLVED_CODE_REFERENCE to "\$(r->waypoints->x)"
            ),
            project.marked
        )
    }

    /** An argument of a macro call must lead somewhere, to a value of the type its parameter takes. */
    @Test
    fun `a macro argument that leads nowhere or to another type is reported`() {
        val macros = """
            MACRO "m" { TAKES("a", "Point") TAKES("n", "number") RETURNS("number") DEFINITION { EVAL { return 1 } } }
            MACRO "l" { TAKES("ps", list("Point")) RETURNS("number") DEFINITION { EVAL { return 1 } } }
        """
        val actions = """
            SET("speed", MACRO("m", "position", "speed"))
            SET("speed", MACRO("m", "target", "speed"))
            SET("speed", MACRO("m", "positon", "speed"))
            SET("speed", MACRO("m", "speed", "position"))
            SET("speed", MACRO("l", "waypoints"))
            SET("speed", MACRO("l", "visited"))
            SET("speed", MACRO("l", "position"))
            SET("speed", MACRO("l", "readings"))
        """
        val project = VerifiedProject(doing(actions, macros), attachedObjectId = null)

        assertEquals(
            listOf(
                UNRESOLVED_MACRO_ARGUMENT to "positon",
                MACRO_ARGUMENT_TYPE to "speed",
                MACRO_ARGUMENT_TYPE to "position",
                MACRO_ARGUMENT_TYPE to "position",
                MACRO_ARGUMENT_TYPE to "readings"
            ),
            project.marked
        )
    }

    /** An argument may start from a binding, whose type is the type its macro returns. */
    @Test
    fun `a macro argument from a binding has the type its macro returns`() {
        val macros = """
            MACRO "point" { RETURNS("Point") DEFINITION { EVAL { return {"x": 1, "y": 2} } } }
            MACRO "m" { TAKES("a", "Point") RETURNS("number") DEFINITION { EVAL { return 1 } } }
            MACRO "n" { TAKES("v", "number") RETURNS("number") DEFINITION { EVAL { return 1 } } }
        """
        val actions = """
            WITH MACRO("point") AS "p" {
                SET("speed", MACRO("m", "p"))
                SET("speed", MACRO("n", "p->x"))
                SET("speed", MACRO("n", "p"))
                SET("speed", MACRO("n", "p->z"))
            }
        """
        val project = VerifiedProject(doing(actions, macros), attachedObjectId = null)

        assertEquals(listOf(MACRO_ARGUMENT_TYPE to "p", UNRESOLVED_MACRO_ARGUMENT to "p->z"), project.marked)
    }

    /** The types of `TAKES` and `RETURNS` must name primitives, domain types or event types, in lists and references. */
    @Test
    fun `an unknown type of a macro is reported at the type`() {
        val macros = """
            MACRO "a" { TAKES("p", "Pointt") RETURNS(list("Nope")) DEFINITION { EVAL { return 1 } } }
            MACRO "b" { TAKES("p", map("Point")) RETURNS(reference("Robot")) DEFINITION { EVAL { return 1 } } }
            MACRO "c" { TAKES("e", "go") TAKES("l", list("number")) RETURNS("Number") DEFINITION { EVAL { return 1 } } }
        """
        val project = VerifiedProject(doing("", macros), attachedObjectId = null)

        assertEquals(
            listOf(UNKNOWN_MACRO_TYPE to "Pointt", UNKNOWN_MACRO_TYPE to "Nope", UNKNOWN_MACRO_TYPE to "Point"),
            project.marked
        )
    }

    /** While the event metamodel has errors, an unknown type might be an event, so nothing is reported. */
    @Test
    fun `macro types are not checked while the event metamodel has errors`() {
        val macros = "MACRO \"a\" { TAKES(\"p\", \"Pointt\") RETURNS(\"number\") DEFINITION { EVAL { return 1 } } }"

        VerifiedProject(doing("", macros), attachedObjectId = null, events = "type \"go\" \"go\" { prop(\"x\", \"nope\") }")
            .assertClean()
    }
}
