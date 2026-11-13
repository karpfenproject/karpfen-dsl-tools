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

import io.karpfen.dsl.diagnostics.DiagnosticCode.UNSET_PROPERTY
import io.karpfen.dsl.verification.VerifiedProject.Companion.doing
import io.karpfen.dsl.verification.VerifiedProject.Companion.machine
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests the check of the values the model gives the object a state machine runs on: a property read without a value
 * on the way is warned about, unless an action writes it or a guard protects the read.
 */
class ModelValueVerificationTest {

    /** A read of a property without a value, or through a relation without an object, is warned about. */
    @Test
    fun `a read of a property the model gives no value is warned about`() {
        val project = VerifiedProject(
            doing("SET(\"name\", EVAL { return \$(speed) + \$(target->x) + \$(target->y) + \$(position->y) + \$(partner->speed) })")
        )

        assertEquals(
            listOf(UNSET_PROPERTY to "\$(target->y)", UNSET_PROPERTY to "\$(partner->speed)"),
            project.marked
        )
    }

    /** A `VALUE` condition on a property without a value is warned about too. */
    @Test
    fun `a VALUE condition on a property the model gives no value is warned about`() {
        val text = machine(
            "INITIAL STATE \"a\" { }\nSTATE \"b\" { }",
            "TRANSITION \"a\" -> \"b\" { CONDITION { VALUE(\"ready\") } }\nTRANSITION \"b\" -> \"a\" { CONDITION { VALUE(\"name\") } }"
        )

        assertEquals(listOf(UNSET_PROPERTY to "ready"), VerifiedProject(text).marked)
    }

    /** A key that an action writes, here in another state, may have its value by the time it is read. */
    @Test
    fun `a read of a key some action writes is not warned about`() {
        val text = machine(
            """
            INITIAL STATE "a" { ENTRY { SET("ready", "true") SETOBJ("self", "partner", "partner") } }
            STATE "b" { DO { SET("speed", EVAL { return ${'$'}(ready) + ${'$'}(partner->speed) }) } }
            """,
            "TRANSITION \"a\" -> \"b\" { }"
        )

        VerifiedProject(text).assertClean()
    }

    /** A guarded read, a machine on no object and a model with errors give no warning. */
    @Test
    fun `values are not checked where they cannot be told`() {
        val guarded = doing("IF IN SCOPE(\"ready\") { SET(\"speed\", EVAL { return \$(ready) }) }")
        val unguarded = doing("SET(\"speed\", EVAL { return \$(ready) })")

        VerifiedProject(guarded).assertClean()
        VerifiedProject(unguarded, attachedObjectId = null).assertClean()
        VerifiedProject(unguarded, model = VerifiedProject.MODEL.replace("\"1.0\"", "\"fast\"")).assertClean()
    }
}
