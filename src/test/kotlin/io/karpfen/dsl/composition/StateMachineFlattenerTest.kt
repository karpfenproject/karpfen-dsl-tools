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
package io.karpfen.dsl.composition

import io.karpfen.dsl.fixtures.StructureDump
import io.karpfen.dsl.reader.kstates.KstatesReader
import io.karpfen.dsl.states.History
import io.karpfen.dsl.states.JoinTransition
import io.karpfen.dsl.states.SplitTransition
import io.karpfen.dsl.states.State
import io.karpfen.dsl.states.StateMachine
import io.karpfen.dsl.states.Transition
import io.karpfen.dsl.states.actions.ActionRule
import io.karpfen.dsl.states.conditions.DoneCondition
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotSame
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

/**
 * Tests [StateMachineFlattener]: the names, copies, transitions, macros and origins of a flattened composition, and the
 * problems that prevent the flattening.
 */
class StateMachineFlattenerTest {

    /** Reads [text], which must read without problems. */
    private fun read(text: String): StateMachine {
        val result = KstatesReader().read(text)
        assertEquals(emptyList(), result.diagnostics, text)
        return result.model!!
    }

    /** Returns a fragment named [name] with [states], [transitions] and [macros]. */
    private fun fragment(name: String, states: String, transitions: String = "", macros: String = ""): String =
        "STATEMACHINE \"$name\" FRAGMENT {\nSTATES {\n$states\n}\nTRANSITIONS {\n$transitions\n}\n" +
            (if (macros.isEmpty()) "" else "MACROS {\n$macros\n}\n") + "}\n"

    /** Returns a state machine attached to the lamp with [states], [transitions] and [macros]. */
    private fun lamp(states: String, transitions: String = "", macros: String = ""): String =
        "STATEMACHINE ATTACHED TO \"Lamp\" {\nSTATES {\n$states\n}\nTRANSITIONS {\n$transitions\n}\n" +
            (if (macros.isEmpty()) "" else "MACROS {\n$macros\n}\n") + "}\n"

    /** Returns a macro named [name] whose code returns [value]. */
    private fun macro(name: String, value: String = "1"): String =
        "MACRO \"$name\" { TAKES(\"w\", \"number\") RETURNS(\"number\") DEFINITION { EVAL { return $value } } }"

    /** The fragment `bulb test` with an initial state, a timeout, two final states and a macro. */
    private val bulbTest = read(
        fragment(
            "bulb test",
            """
            INITIAL STATE "lit" { TICK LIMIT { TIMEOUT: AFTER 3 TICKS TRANSITION TO "failed" } ENTRY { SET("bulb->watts", MACRO("half", "bulb->maxWatts")) } }
            STATE "dark" { }
            FINAL STATE "passed" { }
            FINAL STATE "failed" { }
            """,
            """
            TRANSITION "lit" -> "dark" { CONDITION { EVENT("service", "check") } }
            SPLIT "dark" -> "passed", "failed" { }
            JOIN "lit", "dark" -> "passed" { }
            """,
            macro("half")
        )
    )

    /** Returns the flattened state machine of [machine] with [available], which must flatten. */
    private fun flattened(machine: StateMachine, available: List<StateMachine>): FlatteningResult.Flattened {
        val result = StateMachineFlattener.flatten(machine, available)
        return assertIs<FlatteningResult.Flattened>(result, (result as? FlatteningResult.NotFlattened)?.problems?.map { it.message }.toString())
    }

    /** Returns the problems of flattening [machine] with [available], which must fail. */
    private fun problems(machine: StateMachine, available: List<StateMachine>): List<FlatteningProblem> =
        assertIs<FlatteningResult.NotFlattened>(StateMachineFlattener.flatten(machine, available)).problems

    /** Returns every state of [states] and their inner states, each before its inner states. */
    private fun allStates(states: List<State>): List<State> = states.flatMap { listOf(it) + allStates(it.innerStates) }

    /** A fragment used twice is copied twice, with qualified names and transitions, and shares nothing. */
    @Test
    fun `a fragment used twice is copied twice`() {
        val lamp = read(
            lamp(
                """
                INITIAL STATE "off" { }
                CONTAINER STATE "first" USE "bulb test" { }
                CONTAINER STATE "second" USE "bulb test" { }
                """,
                """
                TRANSITION "off" -> "first" { }
                TRANSITION "first" -> "second" { CONDITION { DONE } }
                """
            )
        )
        val result = flattened(lamp, listOf(bulbTest))
        val flat = result.machine

        assertEquals(
            listOf(
                "off", "first", "first/lit", "first/dark", "first/passed", "first/failed",
                "second", "second/lit", "second/dark", "second/passed", "second/failed"
            ),
            allStates(flat.states).map { it.name }
        )
        val first = flat.states[1]
        val second = flat.states[2]
        assertTrue(first.isContainer)
        assertEquals("bulb test", first.fragment)
        assertSame(first, first.innerStates[0].outerState)
        assertEquals("first/lit", first.initialInnerState!!.name)
        assertEquals(listOf(false, false, true, true), first.innerStates.map { it.isFinal })
        assertEquals("first/failed", first.innerStates[0].timeoutTo)
        assertEquals(
            listOf("off->first", "first->second", "first/lit->first/dark", "first/dark->first/passed,first/failed",
                "first/lit,first/dark->first/passed", "second/lit->second/dark", "second/dark->second/passed,second/failed",
                "second/lit,second/dark->second/passed"),
            flat.transitions.map { describe(it) }
        )
        assertNotSame(first.innerStates[0].onEntry.items[0], second.innerStates[0].onEntry.items[0])
        assertNotSame(bulbTest.states[0].onEntry.items[0], first.innerStates[0].onEntry.items[0])
        assertNotSame(flat.transitions[2].condition, flat.transitions[5].condition)
        assertEquals((bulbTest.states[0].onEntry.items[0] as ActionRule), first.innerStates[0].onEntry.items[0])
        assertEquals(listOf("half"), flat.macros.map { it.name })
        assertSame(bulbTest.macros.single(), flat.macros.single())
        assertEquals(listOf(lamp, bulbTest), result.sources)
        assertEquals(StateOrigin("bulb test", "dark", listOf("second")), result.originOf("second/dark"))
        assertEquals(StateOrigin(null, "first", emptyList()), result.originOf(first))
        assertEquals("second/dark", result.originOf("second/dark")!!.flattenedName)
        assertNull(result.originOf(bulbTest.states[0]))
        assertEquals(11, result.origins.size)
    }

    /** Containers nest three levels deep, and the names and origins nest with them. */
    @Test
    fun `containers nest three levels deep`() {
        val outer = read(fragment("outer", "INITIAL CONTAINER STATE \"middle\" USE \"middle\" { }\nFINAL STATE \"done\" { }", "TRANSITION \"middle\" -> \"done\" { CONDITION { DONE } }"))
        val middle = read(fragment("middle", "INITIAL CONTAINER STATE \"inner\" USE \"inner\" { }\nFINAL STATE \"done\" { }", "TRANSITION \"inner\" -> \"done\" { CONDITION { DONE } }"))
        val inner = read(fragment("inner", "INITIAL STATE \"lit\" { }\nFINAL STATE \"done\" { }", "TRANSITION \"lit\" -> \"done\" { }"))
        val lamp = read(lamp("INITIAL CONTAINER STATE \"test\" USE \"outer\" { }"))
        val result = flattened(lamp, listOf(inner, middle, outer))

        assertEquals(
            listOf("test", "test/middle", "test/middle/inner", "test/middle/inner/lit", "test/middle/inner/done", "test/middle/done", "test/done"),
            allStates(result.machine.states).map { it.name }
        )
        assertEquals(
            StateOrigin("inner", "lit", listOf("test", "test/middle", "test/middle/inner")),
            result.originOf("test/middle/inner/lit")
        )
        assertEquals("test/middle/inner/lit->test/middle/inner/done", describe(result.machine.transitions.last()))
        var entered = result.machine.states.single()
        while (entered.isContainer) entered = entered.initialInnerState!!
        assertEquals("test/middle/inner/lit", entered.name)
        assertEquals(listOf(lamp, outer, middle, inner), result.sources)
    }

    /** A container inside a compound state, with a history, keeps its place, its history and its options. */
    @Test
    fun `a container inside a compound state keeps its history`() {
        val lamp = read(
            lamp(
                """
                INITIAL STATE "off" { }
                STATE "service" {
                    STATE "idle" { }
                    DEEP HISTORY CONTAINER STATE "self test" USE "bulb test" { BREAKPOINT { ENTRY } TICK LIMIT { DELAY: 2 TICKS } }
                }
                """,
                "TRANSITION \"off\" -> \"self test\" { }\nTRANSITION \"self test\" -> \"idle\" { CONDITION { DONE } }"
            )
        )
        val flat = flattened(lamp, listOf(bulbTest)).machine
        val container = flat.states[1].innerStates[1]

        assertEquals("self test", container.name)
        assertSame(flat.states[1], container.outerState)
        assertEquals(History.DEEP, container.history)
        assertTrue(container.entryBreakpoint)
        assertEquals(2L, container.delay)
        assertEquals("self test/lit", container.initialInnerState!!.name)
        assertIs<DoneCondition>(flat.transitions[1].condition)
        assertFalse(flat.isFragment)
        assertEquals("Lamp", flat.attachedToClass)
    }

    /**
     * The macros of all files form one set: the macros of the starting state machine first, a macro of a fragment used
     * twice once, and an equal macro of another file once.
     */
    @Test
    fun `macros from several files form one set`() {
        val switchTest = read(
            fragment(
                "switch test",
                "INITIAL STATE \"pressed\" { ENTRY { SET(\"bulb->watts\", MACRO(\"half\", \"bulb->maxWatts\")) } }\nFINAL STATE \"done\" { }",
                macros = "MACRO \"half\" { TAKES(\"w\", \"number\") RETURNS(\"number\") DEFINITION { EVAL {\n        return 1\n    } } }\n" +
                    macro("quarter")
            )
        )
        val lamp = read(
            lamp(
                "INITIAL STATE \"off\" { ENTRY { SET(\"bulb->watts\", MACRO(\"double\", \"bulb->watts\")) } }\n" +
                    "CONTAINER STATE \"a\" USE \"bulb test\" { }\nCONTAINER STATE \"b\" USE \"bulb test\" { }\n" +
                    "CONTAINER STATE \"c\" USE \"switch test\" { }",
                macros = macro("double")
            )
        )
        val flat = flattened(lamp, listOf(bulbTest, switchTest)).machine

        assertEquals(listOf("double", "half", "quarter"), flat.macros.map { it.name })
        assertSame(bulbTest.macros.single(), flat.macros[1])
    }

    /** Flattening neither changes its input nor depends on an earlier call. */
    @Test
    fun `flattening is pure`() {
        val lamp = read(lamp("INITIAL STATE \"off\" { }\nCONTAINER STATE \"self test\" USE \"bulb test\" { }"))
        val before = StructureDump.of(lamp) + StructureDump.of(bulbTest)
        val first = flattened(lamp, listOf(bulbTest)).machine
        val second = flattened(lamp, listOf(bulbTest)).machine

        assertEquals(before, StructureDump.of(lamp) + StructureDump.of(bulbTest))
        assertEquals(StructureDump.of(first), StructureDump.of(second))
        assertNotSame(first.states[1], second.states[1])
        assertTrue(lamp.states[1].innerStates.isEmpty())
    }

    /** A fragment flattens as a fragment, an attachable state machine keeps its header, and a machine without containers stays as it is. */
    @Test
    fun `the header of the starting state machine is kept`() {
        val fragment = flattened(bulbTest, listOf(bulbTest)).machine
        assertTrue(fragment.isFragment)
        assertEquals("bulb test", fragment.name)
        val attachable = read(
            "STATEMACHINE \"tester\" ATTACHABLE TO \"Lamp\", \"Bulb\" {\nSTATES {\nINITIAL CONTAINER STATE \"t\" USE \"bulb test\" { }\n}\nTRANSITIONS { }\n}"
        )
        val flat = flattened(attachable, listOf(attachable, bulbTest)).machine
        assertTrue(flat.isAttachable)
        assertEquals("tester", flat.name)
        assertEquals(listOf("Lamp", "Bulb"), flat.attachableTo)
        val plain = read(lamp("INITIAL STATE \"off\" { }", "TRANSITION \"off\" -> \"off\" { }"))
        assertEquals(StructureDump.of(plain), StructureDump.of(flattened(plain, emptyList()).machine))
    }

    /** A container state that uses an unknown name or a state machine that is no fragment cannot flatten. */
    @Test
    fun `an unknown fragment and an attachable one are problems`() {
        val attachable = read("STATEMACHINE \"blinking\" ATTACHABLE TO \"Bulb\" {\nSTATES {\nINITIAL STATE \"on\" { }\n}\nTRANSITIONS { }\n}")
        val lamp = read(
            lamp("INITIAL CONTAINER STATE \"a\" USE \"missing\" { }\nCONTAINER STATE \"b\" USE \"blinking\" { }\nCONTAINER STATE \"c\" USE \"bulb test\" { }")
        )
        val problems = problems(lamp, listOf(attachable, bulbTest))

        assertEquals(2, problems.size)
        val unknown = assertIs<FlatteningProblem.UnknownFragment>(problems[0])
        assertSame(lamp, unknown.machine)
        assertSame(lamp.states[0], unknown.element)
        assertEquals("missing", unknown.fragmentName)
        assertEquals(
            "Container state 'a' of the state machine attached to 'Lamp' uses 'missing', but no fragment is named 'missing'",
            unknown.message
        )
        val notFragment = assertIs<FlatteningProblem.NotAFragment>(problems[1])
        assertSame(attachable, notFragment.used)
        assertSame(lamp.states[1], notFragment.container)
    }

    /** A fragment that contains itself through another fragment is a cycle at the container state that closes it. */
    @Test
    fun `a cycle of fragments is a problem`() {
        val first = read(fragment("first", "INITIAL CONTAINER STATE \"x\" USE \"second\" { }\nFINAL STATE \"done\" { }"))
        val second = read(fragment("second", "INITIAL CONTAINER STATE \"y\" USE \"first\" { }\nFINAL STATE \"done\" { }"))
        val lamp = read(lamp("INITIAL CONTAINER STATE \"t\" USE \"first\" { }"))
        val cycle = assertIs<FlatteningProblem.FragmentCycle>(problems(lamp, listOf(first, second)).single())

        assertSame(second, cycle.machine)
        assertSame(second.states[0], cycle.container)
        assertEquals(listOf("first", "second"), cycle.cycle)
        assertEquals(
            "Container state 'y' of fragment 'second' uses 'first', which contains this state again: 'first' uses 'second'; " +
                "a fragment cannot contain itself",
            cycle.message
        )
    }

    /** A written name with a slash that a flattened name repeats is a clash, reported at the later state. */
    @Test
    fun `a clash of flattened names is a problem`() {
        val lamp = read(lamp("INITIAL STATE \"t/lit\" { }\nCONTAINER STATE \"t\" USE \"bulb test\" { }"))
        val clash = assertIs<FlatteningProblem.NameClash>(problems(lamp, listOf(bulbTest)).single())

        assertSame(bulbTest, clash.machine)
        assertSame(bulbTest.states[0], clash.state)
        assertEquals("t/lit", clash.flattenedName)
    }

    /** Two macros of one name with different definitions cannot both be in the set; the later one is the problem. */
    @Test
    fun `two different macros of one name are a problem`() {
        val lamp = read(lamp("INITIAL CONTAINER STATE \"t\" USE \"bulb test\" { }", macros = macro("half", "2")))
        val conflict = assertIs<FlatteningProblem.ConflictingMacros>(problems(lamp, listOf(bulbTest)).single())

        assertSame(bulbTest, conflict.machine)
        assertSame(bulbTest.macros.single(), conflict.macro)
        assertSame(lamp, conflict.otherMachine)
    }

    /** A call that no file of the composition resolves is a problem at the call, once even for a fragment used twice. */
    @Test
    fun `a macro no file defines is a problem`() {
        val calling = read(
            fragment(
                "calling",
                "INITIAL STATE \"a\" { ENTRY { SET(\"bulb->watts\", MACRO(\"missing\", \"x\")) SET(\"bulb->watts\", EVAL { return @gone(1) + @half(2) }) } }\nFINAL STATE \"done\" { }"
            )
        )
        val lamp = read(
            lamp("INITIAL CONTAINER STATE \"a\" USE \"calling\" { }\nCONTAINER STATE \"b\" USE \"calling\" { }\nCONTAINER STATE \"c\" USE \"bulb test\" { }")
        )
        val problems = problems(lamp, listOf(calling, bulbTest)).map { assertIs<FlatteningProblem.UnknownMacro>(it) }

        assertEquals(listOf("missing", "gone"), problems.map { it.macroName })
        assertTrue(problems.all { it.machine === calling })
        assertNull(problems[0].codeSpan)
        assertEquals(7 until 12, problems[1].codeSpan)
    }

    /** Returns a short form of [transition]: its sources, an arrow and its targets. */
    private fun describe(transition: Any): String = when (transition) {
        is Transition -> "${transition.fromState}->${transition.toState}"
        is SplitTransition -> "${transition.fromState}->${transition.toStates.joinToString(",")}"
        is JoinTransition -> "${transition.fromStates.joinToString(",")}->${transition.toState}"
        else -> transition.toString()
    }
}
