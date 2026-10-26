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
package io.karpfen.dsl.printer

import io.karpfen.dsl.dsl.textual.KstatesDSLConverter
import io.karpfen.dsl.grammar.kstates.KstatesLexer
import io.karpfen.dsl.grammar.kstates.KstatesParser
import io.karpfen.dsl.printer.kstates.KstatesPrinter
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests that [ElementRange] covers the chars of an element also behind a character outside the Basic Multilingual
 * Plane, which ANTLR counts as one code point and a string as two chars.
 */
class ElementRangeTest {

    /** A state machine whose first state is named with an emoji, before the states and transitions that follow. */
    private val text = "STATEMACHINE ATTACHED TO \"R\" {\n    STATES {\n        INITIAL STATE \"smile 😀\" { }\n" +
        "        STATE \"after\" { }\n    }\n    TRANSITIONS {\n        TRANSITION \"smile 😀\" -> \"after\" { }\n    }\n}\n"

    /** The parse tree of [text]. */
    private val machine = KstatesParser(CommonTokenStream(KstatesLexer(CharStreams.fromString(text)))).kstates_file().statemachine()

    /**
     * The range of the elements after the emoji covers their text.
     */
    @Test
    fun `the range of an element after a supplementary character covers its text`() {
        val state = ElementRange.of(machine.states_block().state_definition(1), text)
        val transition = ElementRange.of(machine.transitions_block().transition_definition(0), text)

        assertEquals("STATE \"after\" { }", text.substring(state.start, state.stopInclusive + 1))
        assertEquals("TRANSITION \"smile 😀\" -> \"after\" { }", text.substring(transition.start, transition.stopInclusive + 1))
    }

    /**
     * A printed state spliced in at the range of its definition keeps the state machine.
     */
    @Test
    fun `a state spliced in after a supplementary character keeps the state machine`() {
        val parsed = KstatesDSLConverter.parseKstatesString(text)
        val range = ElementRange.of(machine.states_block().state_definition(1), text)

        val spliced = range.replaceIn(text, KstatesPrinter().printState(parsed.states[1], range.indentLevelIn(text)))

        assertEquals(text, spliced)
    }
}
