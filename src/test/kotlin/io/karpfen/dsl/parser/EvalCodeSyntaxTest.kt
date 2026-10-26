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
package io.karpfen.dsl.parser

import io.karpfen.dsl.dsl.textual.KstatesDSLConverter
import io.karpfen.dsl.reader.kstates.KstatesReader
import io.karpfen.dsl.states.actions.ActionRule
import io.karpfen.dsl.states.actions.EvalActionRightSide
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Checks which text [EvalCodeSyntax] takes for the code of an `EVAL` body, and that the converter and the reader take
 * the same code.
 */
class EvalCodeSyntaxTest {

    /**
     * The code leaves out the whitespace at the edges of a body and keeps everything between them.
     */
    @Test
    fun `the code is the body without the whitespace at its edges`() {
        assertEquals("x = 1\n  // note\nreturn x", EvalCodeSyntax.codeOf(" \n x = 1\n  // note\nreturn x \t\n"))
        assertEquals("", EvalCodeSyntax.codeOf(" \r\n "))
        assertEquals(4 to 6, EvalCodeSyntax.codeBoundsIn("ab  cd  ef", 2, 8))
    }

    /**
     * The converter and the reader read the same code from a body, comments between its tokens included.
     */
    @Test
    fun `the converter and the reader read the same code`() {
        val text = "STATEMACHINE ATTACHED TO \"R\" { STATES { INITIAL STATE \"s\" { DO { SET(\"x\", EVAL {  \n" +
            "    a = 1 // one\n    return a\n  }) } } } TRANSITIONS { } }"

        val converted = KstatesDSLConverter.parseKstatesString(text).states.single().onDo.items.single()
        val read = KstatesReader().read(text).model!!.states.single().onDo.items.single()

        val expected = "a = 1 // one\n    return a"
        assertEquals(expected, ((converted as ActionRule).rightSide as EvalActionRightSide).code)
        assertEquals(expected, ((read as ActionRule).rightSide as EvalActionRightSide).code)
    }
}
