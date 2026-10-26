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
package io.karpfen.dsl.reader

import io.karpfen.dsl.diagnostics.DiagnosticCode
import io.karpfen.dsl.reader.kmeta.KmetaReader
import io.karpfen.dsl.reader.kstates.KstatesReader
import io.karpfen.dsl.states.actions.ActionRule
import io.karpfen.dsl.states.actions.EvalActionRightSide
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests that ranges stay exact behind characters outside the Basic Multilingual Plane, which ANTLR counts as
 * one code point but a [String] as two chars.
 */
class SupplementaryCharacterTest {

    /**
     * Names and code behind emoji in comments, names and code are located exactly.
     */
    @Test
    fun `ranges behind emoji are exact`() {
        val text = """
            // a robot 😀 with feelings
            STATEMACHINE ATTACHED TO "Robot😀" {
                STATES {
                    INITIAL STATE "happy 😀" {
                        DO { SET("mood", EVAL { return "😀😀" }) }
                    }
                }
                TRANSITIONS { }
            }
        """.trimIndent()

        val result = KstatesReader().read(text)

        assertEquals(emptyList(), result.diagnostics)
        val stateMachine = result.model!!
        val state = stateMachine.states.single()
        val rightSide = (state.onDo.items.single() as ActionRule).rightSide as EvalActionRightSide
        assertEquals("Robot😀", result.sourceMap.nameRangeOf(stateMachine)?.textIn(text))
        assertEquals("happy 😀", result.sourceMap.nameRangeOf(state)?.textIn(text))
        assertEquals("return \"😀😀\"", result.sourceMap.codeRangeOf(rightSide)?.textIn(text))
        assertEquals(rightSide.code, result.sourceMap.codeRangeOf(rightSide)?.textIn(text))
        assertEquals(5 to 37, result.sourceMap.codeRangeOf(rightSide)!!.start.let { it.line to it.column })
    }

    /**
     * An emoji the KMeta lexer does not know is reported as one unrecognized character of two chars.
     */
    @Test
    fun `an unknown emoji is reported as a whole`() {
        val text = "type \"A\" \"first\" { 😀 }"

        val result = KmetaReader().read(text)

        val diagnostic = result.diagnostics.single()
        assertEquals(DiagnosticCode.UNRECOGNIZED_INPUT, diagnostic.code)
        assertEquals("😀", diagnostic.range.textIn(text))
        assertEquals(2, diagnostic.range.length)
    }
}
