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
package io.karpfen.dsl.states

import io.karpfen.dsl.composition.FlatteningResult
import io.karpfen.dsl.composition.StateMachineFlattener
import io.karpfen.dsl.reader.kstates.KstatesReader
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

/**
 * Tests [InitialStates]: the start path of a state machine, the states entered below a container state, and
 * [State.initialInnerState], in plain state machines and in flattened compositions.
 */
class InitialStatesTest {

    /** Reads [text], which must read without problems. */
    private fun read(text: String): StateMachine {
        val result = KstatesReader().read(text)
        assertEquals(emptyList(), result.diagnostics, text)
        return result.model!!
    }

    /** Returns a state machine attached to the lamp with [states]. */
    private fun lamp(states: String): String = "STATEMACHINE ATTACHED TO \"Lamp\" {\nSTATES {\n$states\n}\nTRANSITIONS { }\n}\n"

    /** Returns a fragment named [name] with [states], which hold its final state. */
    private fun fragment(name: String, states: String): String =
        "STATEMACHINE \"$name\" FRAGMENT {\nSTATES {\n$states\n}\nTRANSITIONS { }\n}\n"

    /** Returns the flattened state machine of [machine] with the fragments [fragments], which must flatten. */
    private fun flat(machine: String, vararg fragments: String): StateMachine =
        assertIs<FlatteningResult.Flattened>(StateMachineFlattener.flatten(read(machine), fragments.map { read(it) })).machine

    /** Returns the names of [states]. */
    private fun names(states: List<State>): List<String> = states.map { it.name }

    /** Returns the state named [name] among the states of [machine] at any depth. */
    private fun stateNamed(machine: StateMachine, name: String): State =
        StateMachineContents(machine).states.single { it.name == name }

    /** A marked state inside a state that is not the first of its level starts the machine in it and around it. */
    @Test
    fun `a marked inner state of a later state starts the machine`() {
        val machine = read(lamp("STATE \"off\" { }\nSTATE \"on\" { STATE \"full\" { } INITIAL STATE \"dimmed\" { } }"))

        assertEquals(listOf("on", "dimmed"), names(InitialStates.startPath(machine)))
        assertEquals("dimmed", machine.states[1].initialInnerState!!.name)
        assertEquals(machine.states[1], InitialStates.initialAmong(machine.states))
    }

    /** Without a mark below it, the start path goes on to the first inner state of each level. */
    @Test
    fun `the path goes on through the first inner states`() {
        val machine = read(lamp("INITIAL STATE \"on\" { STATE \"full\" { STATE \"bright\" { } } STATE \"dimmed\" { } }\nSTATE \"off\" { }"))

        assertEquals(listOf("on", "full", "bright"), names(InitialStates.startPath(machine)))
        assertEquals(emptyList(), InitialStates.enteredBelow(machine.states[1]))
        assertEquals(emptyList(), InitialStates.startPath(StateMachine.fragment("empty", mutableListOf(), mutableListOf(), mutableListOf())))
    }

    /** In a fragment, a marked state inside a later state is where entering the container state goes. */
    @Test
    fun `a marked inner state of a fragment is entered with its container`() {
        val bulbTest = fragment(
            "bulb test",
            "STATE \"wait\" { }\nSTATE \"check\" { STATE \"dark\" { } INITIAL STATE \"lit\" { } }\nFINAL STATE \"done\" { }"
        )
        val machine = flat(lamp("INITIAL STATE \"off\" { }\nCONTAINER STATE \"t\" USE \"bulb test\" { }"), bulbTest)
        val container = stateNamed(machine, "t")

        assertEquals(listOf("t/check", "t/lit"), names(InitialStates.enteredBelow(container)))
        assertEquals("t/check", container.initialInnerState!!.name)
        assertEquals(listOf("off"), names(InitialStates.startPath(machine)))
    }

    /**
     * Each fragment of nested containers has its own mark, and the marks behind a container state are not seen from
     * above it: the machine starts at its own mark, and each container enters the path of its own fragment.
     */
    @Test
    fun `the marks of nested fragments stay behind their containers`() {
        val outer = fragment("outer", "STATE \"prepare\" { }\nINITIAL CONTAINER STATE \"inner\" USE \"inner\" { }\nFINAL STATE \"done\" { }")
        val inner = fragment("inner", "STATE \"p\" { }\nINITIAL STATE \"q\" { }\nFINAL STATE \"done\" { }")
        val machine = flat(lamp("STATE \"group\" { CONTAINER STATE \"test\" USE \"outer\" { } }\nINITIAL STATE \"idle\" { }"), outer, inner)
        val test = stateNamed(machine, "test")

        assertEquals(listOf("idle"), names(InitialStates.startPath(machine)))
        assertEquals(listOf("test/inner", "test/inner/q"), names(InitialStates.enteredBelow(test)))
        assertEquals(listOf("test/inner/q"), names(InitialStates.enteredBelow(stateNamed(machine, "test/inner"))))
        assertEquals("test", stateNamed(machine, "group").initialInnerState!!.name)
    }

    /** A marked state of a fragment that has inner states leads the path on to its first inner state. */
    @Test
    fun `a marked compound state of a fragment leads on to its first inner state`() {
        val bulbTest = fragment(
            "bulb test",
            "STATE \"wait\" { }\nINITIAL STATE \"check\" { STATE \"dark\" { } STATE \"lit\" { } }\nFINAL STATE \"done\" { }"
        )
        val machine = flat(lamp("INITIAL CONTAINER STATE \"t\" USE \"bulb test\" { }"), bulbTest)

        assertEquals(listOf("t/check", "t/dark"), names(InitialStates.enteredBelow(machine.states.single())))
    }

    /** A marked container state at the start of a machine starts the machine in the start path of its fragment. */
    @Test
    fun `a marked container state starts the machine in its fragment`() {
        val bulbTest = fragment("bulb test", "STATE \"wait\" { }\nINITIAL STATE \"lit\" { }\nFINAL STATE \"done\" { }")
        val machine = flat(lamp("STATE \"off\" { }\nINITIAL CONTAINER STATE \"t\" USE \"bulb test\" { }"), bulbTest)

        assertEquals(listOf("t", "t/lit"), names(InitialStates.startPath(machine)))
    }
}
