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

import io.karpfen.dsl.fixtures.ExampleCatalog
import io.karpfen.dsl.fixtures.Mutator
import io.karpfen.dsl.fixtures.TextLayout
import io.karpfen.dsl.fixtures.Vocabularies
import io.karpfen.dsl.printer.kmeta.KmetaPrinter
import io.karpfen.dsl.printer.kmeta.structure
import io.karpfen.dsl.printer.kmodel.KmodelPrinter
import io.karpfen.dsl.printer.kmodel.structure
import io.karpfen.dsl.printer.kstates.KstatesPrinter
import io.karpfen.dsl.printer.kstates.structure
import io.karpfen.dsl.reader.TextReader
import io.karpfen.dsl.reader.kmeta.KmetaReader
import io.karpfen.dsl.reader.kmodel.KmodelReader
import io.karpfen.dsl.reader.kstates.KstatesReader
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.test.fail

/**
 * Tests that the printers write whatever the readers read without errors, beyond the examples as they are written.
 *
 * Every example in every [TextLayout] and hundreds of deterministic [Mutator] variants of it stand in for documents
 * written by hand. Each text that its reader reads without errors yields a model, which the printer of its language
 * must write without throwing. The printed text must read back without errors to a model of the same structure, and
 * printing that model must yield the same text again.
 */
class PrinterRobustnessTest {

    private val catalog = ExampleCatalog()

    /**
     * Metamodels, written without their base types.
     */
    @Test
    fun `every metamodel a reader reads without errors prints and reads back`() {
        val printer = KmetaPrinter()
        checkPrinting(Mutator(Vocabularies.KMETA), catalog.metamodels().map { example ->
            PrintedExample(example.toString(), example.text, KmetaReader(example.baseTypes), { it.structure() }) {
                printer.print(it, example.baseTypes)
            }
        })
    }

    /**
     * Models.
     */
    @Test
    fun `every model a reader reads without errors prints and reads back`() {
        val printer = KmodelPrinter()
        checkPrinting(Mutator(Vocabularies.KMODEL), catalog.models().map { example ->
            PrintedExample(example.toString(), example.text, KmodelReader(example.metamodel), { it.structure() }) {
                printer.print(it)
            }
        })
    }

    /**
     * State machines.
     */
    @Test
    fun `every state machine a reader reads without errors prints and reads back`() {
        val printer = KstatesPrinter()
        checkPrinting(Mutator(Vocabularies.KSTATES), catalog.stateMachines().map { example ->
            PrintedExample(example.toString(), example.text, KstatesReader(), { it.structure() }) { printer.print(it) }
        })
    }

    /**
     * Prints every text of [examples] in every layout and every variant [mutator] makes of it that the reader reads
     * without errors, and fails with the first problems found.
     */
    private fun <T : Any> checkPrinting(mutator: Mutator, examples: List<PrintedExample<T>>) {
        val problems = mutableListOf<String>()
        var printed = 0
        for (example in examples) {
            val layouts = TextLayout.entries.map { "$it" to it.apply(example.text) }
            val variants = mutator.variantsOf(example.path, example.text, VARIANTS_PER_EXAMPLE).map { "variant ${it.index}" to it.text }
            for ((case, text) in layouts + variants) {
                val model = example.reader.read(text).takeUnless { it.hasErrors }?.model ?: continue
                printed += 1
                example.problemPrinting(model)?.let { problems.add("${example.path}, $case: $it\n--- text ---\n$text") }
            }
        }
        if (problems.isNotEmpty()) {
            fail("${problems.size} of $printed models do not print, the first ones:\n\n" + problems.take(3).joinToString("\n\n"))
        }
        assertTrue(printed >= MINIMUM_PRINTED_MODELS, "Only $printed texts read without errors")
    }

    /**
     * An example together with what reads, prints and compares the documents of its language.
     *
     * @property path The path of the example file.
     * @property text The content of the example file.
     * @property reader Reads a document of the language.
     * @property structureOf Returns the structure of a model, which compares with `equals`.
     * @property print Writes a model as a whole document.
     */
    private class PrintedExample<T : Any>(
        val path: String,
        val text: String,
        val reader: TextReader<T>,
        val structureOf: (T) -> Any,
        val print: (T) -> String
    ) {

        /**
         * Describes why printing [model] is a problem: the printer throws, the printed text reads back with errors or
         * to a model of another structure, or printing that model yields another text; null if printing is fine.
         */
        fun problemPrinting(model: T): String? {
            val printed = try {
                print(model)
            } catch (failure: IllegalArgumentException) {
                return "the printer rejects the model: ${failure.message}"
            }
            val reread = reader.read(printed)
            val rereadModel = reread.model
            return when {
                reread.hasErrors || rereadModel == null -> "the printed text reads back with ${reread.diagnostics}\n$printed"
                structureOf(rereadModel) != structureOf(model) -> "the printed text reads back to another model\n$printed"
                print(rereadModel) != printed -> "printing the model read back yields another text\n$printed"
                else -> null
            }
        }
    }

    private companion object {

        /** The number of mutated variants of every example. */
        const val VARIANTS_PER_EXAMPLE = 200

        /** The number of models below which the test would prove too little. */
        const val MINIMUM_PRINTED_MODELS = 100
    }
}
