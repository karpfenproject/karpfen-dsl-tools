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
import io.karpfen.dsl.grammar.kstates.KstatesLexer
import io.karpfen.dsl.grammar.kstates.KstatesParser
import io.karpfen.dsl.printer.ElementRange
import io.karpfen.dsl.printer.ExampleCatalog
import io.karpfen.dsl.printer.StateMachineExample
import io.karpfen.dsl.states.State
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.ParserRuleContext
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.TestFactory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Prints the KStates examples with [KstatesPrinter] and reads the text back, as a whole and element by element.
 */
class KstatesPrinterTest {

    private val printer = KstatesPrinter()
    private val examples = ExampleCatalog().stateMachines()

    /**
     * Parsing an example, printing it and parsing the text again yields an equal state machine.
     */
    @TestFactory
    fun `printed state machines parse back to equal state machines`(): List<DynamicTest> = examples.map { example ->
        dynamicTest(example.toString()) {
            val parsed = example.parse()
            assertEquals(parsed.structure(), example.parse(printer.print(parsed)).structure())
        }
    }

    /**
     * Printing the state machine read from printed text yields the same text again.
     */
    @TestFactory
    fun `printing is idempotent`(): List<DynamicTest> = examples.map { example ->
        dynamicTest(example.toString()) {
            val printed = printer.print(example.parse())
            assertEquals(printed, printer.print(example.parse(printed)))
        }
    }

    /**
     * Replacing the text of each state of an example, nested ones included, with the printed state keeps the
     * state machine.
     */
    @TestFactory
    fun `a printed state replaces its definition in the document`(): List<DynamicTest> = examples.map { example ->
        dynamicTest(example.toString()) {
            val parsed = example.parse()
            val statesByName = parsed.states.flatMap { statesBelow(it) }.groupBy { it.name }
            val definitions = syntaxTreeOf(example).states_block().state_definition().flatMap { definitionsBelow(it) }
            definitions.forEach { definition ->
                val state = statesByName.getValue(definition.STRING().text.trim('"')).single()
                assertSplicedKeepsMachine(example, definition) { printer.printState(state, it) }
            }
        }
    }

    /**
     * Replacing the text of each transition of an example with the printed transition keeps the state machine.
     */
    @TestFactory
    fun `a printed transition replaces its definition in the document`(): List<DynamicTest> = examples.map { example ->
        dynamicTest(example.toString()) {
            val parsed = example.parse()
            syntaxTreeOf(example).transitions_block().transition_definition().forEachIndexed { index, definition ->
                assertSplicedKeepsMachine(example, definition) { printer.printTransition(parsed.transitions[index], it) }
            }
        }
    }

    /**
     * Replacing the text of each macro of an example with the printed macro keeps the state machine.
     */
    @TestFactory
    fun `a printed macro replaces its definition in the document`(): List<DynamicTest> = examples.map { example ->
        dynamicTest(example.toString()) {
            val parsed = example.parse()
            syntaxTreeOf(example).macros_block()?.macro_definition().orEmpty().forEachIndexed { index, definition ->
                assertSplicedKeepsMachine(example, definition) { printer.printMacro(parsed.macros[index], it) }
            }
        }
    }

    /**
     * Each printed state, transition and macro stands in the printed document, so documents and fragments
     * share one layout.
     */
    @TestFactory
    fun `printed elements appear in the printed document`(): List<DynamicTest> = examples.map { example ->
        dynamicTest(example.toString()) {
            val parsed = example.parse()
            val document = printer.print(parsed)
            val fragments = parsed.states.flatMap { statesWithDepth(it, SECTION_CONTENT_LEVEL) }
                .map { (state, depth) -> printer.printState(state, depth) } +
                parsed.transitions.map { printer.printTransition(it, SECTION_CONTENT_LEVEL) } +
                parsed.macros.map { printer.printMacro(it, SECTION_CONTENT_LEVEL) }
            fragments.forEach { assertTrue(document.contains(it), it) }
        }
    }

    /**
     * The converter writes state machines with the printer.
     */
    @Test
    fun `the converter delegates to the printer`() {
        val machine = examples.first().parse()
        assertEquals(printer.print(machine), KstatesDSLConverter.convertToKstatesString(machine))
    }

    /**
     * Replaces the text of [element] in the example with the fragment [print] returns for the indentation
     * level of the element, and checks that the result parses to the state machine of the example.
     */
    private fun assertSplicedKeepsMachine(example: StateMachineExample, element: ParserRuleContext, print: (Int) -> String) {
        val range = ElementRange.of(element)
        val spliced = range.replaceIn(example.text, print(range.indentLevelIn(example.text)))
        assertEquals(example.parse().structure(), example.parse(spliced).structure(), spliced)
    }

    private fun syntaxTreeOf(example: StateMachineExample): KstatesParser.StatemachineContext =
        KstatesParser(CommonTokenStream(KstatesLexer(CharStreams.fromString(example.text)))).kstates_file().statemachine()

    private fun definitionsBelow(definition: KstatesParser.State_definitionContext): List<KstatesParser.State_definitionContext> =
        listOf(definition) + definition.state_definition().flatMap { definitionsBelow(it) }

    private fun statesBelow(state: State): List<State> = listOf(state) + state.innerStates.flatMap { statesBelow(it) }

    private fun statesWithDepth(state: State, depth: Int): List<Pair<State, Int>> =
        listOf(state to depth) + state.innerStates.flatMap { statesWithDepth(it, depth + 1) }

    private companion object {

        /** The indentation level of the states, transitions and macros inside their sections. */
        const val SECTION_CONTENT_LEVEL = 2
    }
}
