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
package io.karpfen.dsl.printer.kstates

import io.karpfen.dsl.dsl.textual.KstatesDSLConverter
import io.karpfen.dsl.fixtures.CodeShape
import io.karpfen.dsl.states.State
import io.karpfen.dsl.states.StateMachine
import io.karpfen.dsl.states.Transition
import io.karpfen.dsl.states.actions.ActionBlock
import io.karpfen.dsl.states.actions.ActionItem
import io.karpfen.dsl.states.actions.ActionOperationType
import io.karpfen.dsl.states.actions.ActionRule
import io.karpfen.dsl.states.conditions.EvalCondition
import io.karpfen.dsl.states.conditions.ValueCondition
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Checks the canonical KStates layout of [KstatesPrinter] on every construct of the language, and what it
 * rejects.
 */
class KstatesLayoutTest {

    private val printer = KstatesPrinter()

    /**
     * Every construct is written canonically: uppercase keywords, one statement per line, `EVAL` code verbatim,
     * no block for an unconditional transition, and no comments.
     */
    @Test
    fun `the canonical layout`() {
        val text = """
            STATEMACHINE ATTACHED TO "Robot" {
                STATES {
                    // a comment
                    INITIAL STATE "idle" {}
                    STATE "work" {
                        ENTRY { set("speed", "1.0") EVENT("public", "started") }
                        DO {
                            IF IN SCOPE("event->factor", "speed") { SET("speed", EVAL {return $(event->factor)}) }
                            WITH MACRO("pick", "obstacles") AS "o" { SETOBJ("self", "target", MACRO("pick", "obstacles")) }
                            DROPLIST("log")
                            DROPOBJ("target")
                            APPENDOBJ("self", "visited", EVAL { return $(o) })
                            SETLIST("log", MACRO("fresh_log"))
                            APPEND("log", EVAL {
                                import math
                                return str(math.pi)
                            })
                        }
                        STATE "inner" { DO { APPEND("log", "inner") } }
                    }
                    STATE "a" { } STATE "b" { } STATE "done" { ENTRY { } }
                }
                TRANSITIONS {
                    TRANSITION "idle" -> "work" { CONDITION { EVENT("public", "go") EVAL { return 1 > 0 } VALUE("ready") } }
                    TRANSITION "work" -> "work" NOT LOOPING { CONDITION { VALUE("true") } }
                    SPLIT "work" -> "a", "b" { }
                    JOIN "a", "b" -> "done" NOT LOOPING {
                        CONDITION {
                            EVAL {
                                x = 1
                                return x > 0
                            }
                        }
                    }
                }
                MACROS {
                    MACRO "pick" {
                        TAKES("obstacles", list("Obstacle"))
                        RETURNS(reference("Obstacle"))
                        DEFINITION { EVAL { return $(obstacles)[0] } }
                    }
                    MACRO "fresh_log" {
                        RETURNS(list("string"))
                        DEFINITION {
                            EVAL {
                                result = []
                                return result
                            }
                        }
                    }
                }
            }
        """.trimIndent()
        val expected = """
            |STATEMACHINE ATTACHED TO "Robot" {
            |    STATES {
            |        INITIAL STATE "idle" { }
            |        STATE "work" {
            |            ENTRY {
            |                SET("speed", "1.0")
            |                EVENT("public", "started")
            |            }
            |            DO {
            |                IF IN SCOPE("event->factor", "speed") {
            |                    SET("speed", EVAL { return $(event->factor) })
            |                }
            |                WITH MACRO("pick", "obstacles") AS "o" {
            |                    SETOBJ("self", "target", MACRO("pick", "obstacles"))
            |                }
            |                DROPLIST("log")
            |                DROPOBJ("target")
            |                APPENDOBJ("self", "visited", EVAL { return $(o) })
            |                SETLIST("log", MACRO("fresh_log"))
            |                APPEND("log", EVAL {
            |                    import math
            |                    return str(math.pi)
            |                })
            |            }
            |            STATE "inner" {
            |                DO {
            |                    APPEND("log", "inner")
            |                }
            |            }
            |        }
            |        STATE "a" { }
            |        STATE "b" { }
            |        STATE "done" { }
            |    }
            |
            |    TRANSITIONS {
            |        TRANSITION "idle" -> "work" {
            |            CONDITION {
            |                EVENT("public", "go")
            |                EVAL { return 1 > 0 }
            |                VALUE("ready")
            |            }
            |        }
            |        TRANSITION "work" -> "work" NOT LOOPING { }
            |        SPLIT "work" -> "a", "b" { }
            |        JOIN "a", "b" -> "done" NOT LOOPING {
            |            CONDITION {
            |                EVAL {
            |                    x = 1
            |                    return x > 0
            |                }
            |            }
            |        }
            |    }
            |
            |    MACROS {
            |        MACRO "pick" {
            |            TAKES("obstacles", list("Obstacle"))
            |            RETURNS(reference("Obstacle"))
            |            DEFINITION {
            |                EVAL { return $(obstacles)[0] }
            |            }
            |        }
            |        MACRO "fresh_log" {
            |            RETURNS(list("string"))
            |            DEFINITION {
            |                EVAL {
            |                    result = []
            |                    return result
            |                }
            |            }
            |        }
            |    }
            |}
            |""".trimMargin()
        assertEquals(expected, printer.print(KstatesDSLConverter.parseKstatesString(text)))
    }

    /**
     * A state machine without macros has no `MACROS` section.
     */
    @Test
    fun `the macros section is left out when there are no macros`() {
        val machine = machineWith(Transition("idle", "idle", true, ValueCondition("true")))
        val expected = """
            |STATEMACHINE ATTACHED TO "Robot" {
            |    STATES {
            |        INITIAL STATE "idle" { }
            |    }
            |
            |    TRANSITIONS {
            |        TRANSITION "idle" -> "idle" { }
            |    }
            |}
            |""".trimMargin()
        assertEquals(expected, printer.print(machine))
    }

    /**
     * A comment between the tokens of the code stays part of it, and code with `//` goes onto lines of its own, so
     * the comment cannot swallow the closing brace.
     */
    @Test
    fun `code keeps a comment between its tokens`() {
        val code = "x = 7 // 2\nreturn x"
        val printed = printer.print(machineWith(Transition("idle", "idle", true, EvalCondition(code))))
        val reparsed = KstatesDSLConverter.parseKstatesString(printed).transitions.single().condition
        assertEquals(code, CodeShape.of((reparsed as EvalCondition).code))
    }

    /**
     * A state printed at another depth than it was written keeps the shape of its code: the lines after the first
     * keep their indentation relative to each other, one level deeper than the block.
     */
    @Test
    fun `code keeps its shape at another depth`() {
        val text = """
            |STATEMACHINE ATTACHED TO "Robot" {
            |    STATES {
            |        INITIAL STATE "idle" {
            |            DO {
            |                SET("x", EVAL {
            |                    if $(y) > 0 :
            |                        return 1
            |                    endif
            |                    return 0
            |                })
            |            }
            |        }
            |    }
            |    TRANSITIONS { }
            |}
            |""".trimMargin()
        val state = KstatesDSLConverter.parseKstatesString(text).states.single()
        val expected = """
            |INITIAL STATE "idle" {
            |                DO {
            |                    SET("x", EVAL {
            |                        if $(y) > 0 :
            |                            return 1
            |                        endif
            |                        return 0
            |                    })
            |                }
            |            }""".trimMargin()
        assertEquals(expected, printer.printState(state, 3))
    }

    /**
     * A fragment carries no indentation on its first line and the full indentation on the following ones.
     */
    @Test
    fun `fragments are indented for their level`() {
        val state = stateDoing(ActionRule(ActionOperationType.DROPLIST, "log"))
        val expected = "STATE \"busy\" {\n            DO {\n                DROPLIST(\"log\")\n            }\n        }"
        assertEquals(expected, printer.printState(state, 2))
    }

    private fun machineWith(transition: Transition): StateMachine = StateMachine(
        "Robot",
        mutableListOf(State("idle", ActionBlock(mutableListOf()), ActionBlock(mutableListOf()), mutableListOf(), null, true)),
        mutableListOf(transition),
        mutableListOf()
    )

    private fun stateDoing(item: ActionItem): State =
        State("busy", ActionBlock(mutableListOf()), ActionBlock(mutableListOf(item)), mutableListOf())
}
