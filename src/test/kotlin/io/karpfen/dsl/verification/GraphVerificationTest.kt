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

import io.karpfen.dsl.diagnostics.DiagnosticCode.JOIN_NEVER_FIRES
import io.karpfen.dsl.diagnostics.DiagnosticCode.SHADOWED_TRANSITION
import io.karpfen.dsl.diagnostics.DiagnosticCode.SPLIT_INSIDE_PARALLEL
import io.karpfen.dsl.diagnostics.DiagnosticCode.UNREACHABLE_STATE
import io.karpfen.dsl.verification.VerifiedProject.Companion.machine
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests the checks of the states and transitions of a state machine: states that can never be active, transitions that
 * an earlier one shadows, and joins and splits that can never fire.
 */
class GraphVerificationTest {

    /** Returns the findings for a machine with [states] and [transitions], run on no object. */
    private fun marked(states: String, transitions: String) =
        VerifiedProject(machine(states, transitions), attachedObjectId = null).marked

    /** A state no transition leads into is warned about; one reached by a transition or a timeout is not. */
    @Test
    fun `a state that can never be active is warned about`() {
        val states = """
            INITIAL STATE "a" { }
            STATE "b" { TICK LIMIT { TIMEOUT: AFTER 2 TICKS TRANSITION TO "c" } }
            STATE "c" { }
            STATE "d" { STATE "e" { } }
        """

        assertEquals(listOf(UNREACHABLE_STATE to "d"), marked(states, "TRANSITION \"a\" -> \"b\" { }"))
    }

    /** Entering a state enters none of its inner states, so an inner state without a transition into it is warned about. */
    @Test
    fun `an inner state no transition leads into is warned about`() {
        val states = """
            INITIAL STATE "a" { }
            STATE "o" { STATE "i" { } STATE "j" { } }
        """

        assertEquals(
            listOf(UNREACHABLE_STATE to "i"),
            marked(states, "TRANSITION \"a\" -> \"o\" { }\nTRANSITION \"o\" -> \"j\" { }")
        )
    }

    /** The start enters the initial state of each level, here an inner one, and a join leads to its target. */
    @Test
    fun `the initial states and the target of a firing join are reached`() {
        val states = """
            STATE "a" { STATE "x" { } INITIAL STATE "y" { } }
            STATE "b" { }
            STATE "c" { }
            STATE "d" { }
        """
        val transitions = """
            SPLIT "y" -> "b", "c" { }
            JOIN "b", "c" -> "d" { }
            TRANSITION "d" -> "x" { }
        """

        assertEquals(emptyList(), marked(states, transitions))
    }

    /** A join with more source states than any split has branches never fires, and its target is never reached. */
    @Test
    fun `a join that can never fire is warned about`() {
        val states = "INITIAL STATE \"a\" { }\nSTATE \"b\" { }\nSTATE \"c\" { }\nSTATE \"d\" { }\nSTATE \"e\" { }"
        val transitions = """
            SPLIT "a" -> "b", "c" { }
            TRANSITION "c" -> "d" { }
            JOIN "b", "c", "d" -> "e" { }
            JOIN "b", "d" -> "a" { }
        """

        assertEquals(
            listOf(UNREACHABLE_STATE to "e", JOIN_NEVER_FIRES to "JOIN \"b\", \"c\", \"d\" -> \"e\""),
            marked(states, transitions)
        )
    }

    /** Two source states that only one branch can reach never stand side by side, so their join never fires. */
    @Test
    fun `a join whose sources share one branch is warned about`() {
        val states = "INITIAL STATE \"a\" { }\nSTATE \"b\" { }\nSTATE \"c\" { }\nSTATE \"d\" { }"
        val transitions = """
            SPLIT "a" -> "b", "c" { }
            TRANSITION "b" -> "d" { }
            JOIN "b", "d" -> "a" { }
            JOIN "d", "c" -> "a" { }
        """

        assertEquals(listOf(JOIN_NEVER_FIRES to "JOIN \"b\", \"d\" -> \"a\""), marked(states, transitions))
    }

    /** A split from a state that is active only inside a branch never fires. */
    @Test
    fun `a split inside a parallel region is warned about`() {
        val states = "INITIAL STATE \"a\" { }\nSTATE \"b\" { }\nSTATE \"c\" { }\nSTATE \"d\" { }\nSTATE \"e\" { }"
        val transitions = """
            SPLIT "a" -> "b", "c" { }
            SPLIT "b" -> "d", "e" { }
            JOIN "b", "c" -> "a" { }
        """

        assertEquals(
            listOf(UNREACHABLE_STATE to "d", UNREACHABLE_STATE to "e", SPLIT_INSIDE_PARALLEL to "SPLIT \"b\" -> \"d\", \"e\""),
            marked(states, transitions)
        )
    }

    /** A transition after one of the same state that always fires and may loop is warned about at its head. */
    @Test
    fun `a transition after one that always fires is warned about`() {
        val states = "INITIAL STATE \"a\" { }\nSTATE \"b\" { }\nSTATE \"c\" { }"
        val shadowing = """
            TRANSITION "a" -> "b" { CONDITION { VALUE("TRUE") } }
            TRANSITION "a" -> "c" { CONDITION { VALUE("ready") } }
            TRANSITION "b" -> "a" { }
        """

        assertEquals(listOf(SHADOWED_TRANSITION to "TRANSITION \"a\" -> \"c\""), marked(states, shadowing))
    }

    /** A transition that may not loop, or that has a real condition, shadows nothing. */
    @Test
    fun `a transition after one that may not fire is not warned about`() {
        val states = "INITIAL STATE \"a\" { }\nSTATE \"b\" { }\nSTATE \"c\" { }"
        val notLooping = """
            TRANSITION "a" -> "b" NOT LOOPING { }
            TRANSITION "a" -> "c" { }
            TRANSITION "b" -> "a" { }
            TRANSITION "c" -> "a" { CONDITION { VALUE("ready") } }
            TRANSITION "c" -> "b" { }
        """

        assertEquals(emptyList(), marked(states, notLooping))
    }
}
