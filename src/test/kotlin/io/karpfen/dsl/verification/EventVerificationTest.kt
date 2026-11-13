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

import io.karpfen.dsl.diagnostics.DiagnosticCode.EVENT_NOT_IN_SCOPE
import io.karpfen.dsl.diagnostics.DiagnosticCode.UNKNOWN_EVENT_FIELD
import io.karpfen.dsl.verification.VerifiedProject.Companion.machine
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests the checks of the reads of the event in scope: the fields read must exist in the payload of the events that can
 * be in scope, and some event must be able to be in scope where it is read.
 */
class EventVerificationTest {

    /** Returns a machine whose state `moving` reads [read] on entry and is entered by the transition [into]. */
    private fun movingReads(read: String, into: String = "EVENT(\"ctl\", \"go\")"): String = machine(
        "INITIAL STATE \"idle\" { }\nSTATE \"moving\" { ENTRY { SET(\"speed\", EVAL { return $read }) } }",
        "TRANSITION \"idle\" -> \"moving\" { CONDITION { $into } }"
    )

    /** A field the payload of the event in scope has is read without a finding, also through a relation. */
    @Test
    fun `a field of the event in scope is read`() {
        VerifiedProject(movingReads("\$(event->speed) + \$(event->to->x) + \$(event)"), attachedObjectId = null).assertClean()
    }

    /** A field the payload lacks, or a field of an event the event metamodel does not declare, is reported. */
    @Test
    fun `a field the event in scope lacks is reported`() {
        val missing = VerifiedProject(movingReads("\$(event->sped) + \$(event->to->z)"), attachedObjectId = null)
        val undeclared = VerifiedProject(movingReads("\$(event->speed)", "EVENT(\"ctl\", \"jump\")"), attachedObjectId = null)
        val empty = VerifiedProject(movingReads("\$(event->speed)", "EVENT(\"ctl\", \"stop\")"), attachedObjectId = null)

        assertEquals(
            listOf(UNKNOWN_EVENT_FIELD to "\$(event->sped)", UNKNOWN_EVENT_FIELD to "\$(event->to->z)"),
            missing.marked
        )
        assertEquals(listOf(UNKNOWN_EVENT_FIELD), undeclared.codes)
        assertEquals(listOf(UNKNOWN_EVENT_FIELD), empty.codes)
    }

    /** A field that one of several events in scope has is not reported, since that event may be the one in scope. */
    @Test
    fun `a field one of several events has is not reported`() {
        val text = machine(
            "INITIAL STATE \"idle\" { }\nSTATE \"moving\" { ENTRY { SET(\"speed\", EVAL { return \$(event->speed) }) } }",
            """
            TRANSITION "idle" -> "moving" { CONDITION { EVENT("ctl", "stop") } }
            TRANSITION "idle" -> "moving" { CONDITION { EVENT("ctl", "go") } }
            """
        )

        VerifiedProject(text, attachedObjectId = null).assertClean()
    }

    /** Without an event metamodel, or inside an `IF IN SCOPE` block on the event, fields are not checked. */
    @Test
    fun `event fields are not checked without an event metamodel or inside a guard`() {
        VerifiedProject(movingReads("\$(event->sped)"), attachedObjectId = null, events = null).assertClean()
        val guarded = machine(
            "INITIAL STATE \"idle\" { }\nSTATE \"moving\" { ENTRY { IF IN SCOPE(\"event->speed\") { SET(\"speed\", EVAL { return \$(event->sped) }) } } }",
            "TRANSITION \"idle\" -> \"moving\" { CONDITION { EVENT(\"ctl\", \"go\") } }"
        )
        VerifiedProject(guarded, attachedObjectId = null).assertClean()
    }

    /** The conditions after an `EVENT` clause read its event, also in a state no event leads into. */
    @Test
    fun `the conditions of a transition read the event of its EVENT clause`() {
        val text = machine(
            "INITIAL STATE \"idle\" { }\nSTATE \"moving\" { }",
            """
            TRANSITION "idle" -> "moving" { CONDITION { EVENT("ctl", "go") EVAL { return ${'$'}(event->speed) > 1 } } }
            TRANSITION "idle" -> "moving" { CONDITION { EVENT("ctl", "go") EVAL { return ${'$'}(event->sped) > 1 } } }
            """
        )

        assertEquals(listOf(UNKNOWN_EVENT_FIELD to "\$(event->sped)"), VerifiedProject(text, attachedObjectId = null).marked)
    }

    /** A state that only a transition without an `EVENT` clause leads into has no event in scope. */
    @Test
    fun `a read of the event where none can be in scope is warned about`() {
        val project = VerifiedProject(movingReads("\$(event->speed)", "VALUE(\"true\")"), attachedObjectId = null)

        assertEquals(listOf(EVENT_NOT_IN_SCOPE to "\$(event->speed)"), project.marked)
    }

    /**
     * The event of a transition into an inner state is in scope in the actions of the state and its outer state, and
     * leaves the scope when a transition without an event leaves the inner state.
     */
    @Test
    fun `the event of a transition into an inner state is in scope in its outer state`() {
        val text = machine(
            """
            INITIAL STATE "idle" { }
            STATE "outer" {
                ENTRY { SET("speed", EVAL { return ${'$'}(event->speed) }) }
                STATE "inner" { DO { SET("speed", EVAL { return ${'$'}(event->speed) }) } }
                STATE "other" { DO { SET("speed", EVAL { return ${'$'}(event->to) }) } }
            }
            """,
            """
            TRANSITION "idle" -> "inner" { CONDITION { EVENT("ctl", "go") } }
            TRANSITION "inner" -> "other" { }
            """
        )

        assertEquals(listOf(EVENT_NOT_IN_SCOPE to "\$(event->to)"), VerifiedProject(text, attachedObjectId = null).marked)
    }

    /** The conditions of a join see no event unless it has an `EVENT` clause of its own. */
    @Test
    fun `the conditions of a join see no event`() {
        val text = machine(
            "INITIAL STATE \"a\" { }\nSTATE \"b\" { }\nSTATE \"c\" { }",
            """
            SPLIT "a" -> "b", "c" { CONDITION { EVENT("ctl", "go") } }
            JOIN "b", "c" -> "a" { CONDITION { VALUE("event->ready") } }
            JOIN "b", "c" -> "a" { CONDITION { EVENT("ctl", "stop") EVAL { return ${'$'}(event) } } }
            """
        )

        assertEquals(listOf(EVENT_NOT_IN_SCOPE to "event->ready"), VerifiedProject(text, attachedObjectId = null).marked)
    }
}
