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
package io.karpfen.dsl.compatibility

import io.karpfen.dsl.fixtures.Mutator
import io.karpfen.dsl.fixtures.Vocabularies
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.fail

/**
 * Compares the throwing converters with those of version 1.0, which the build compiles from the history of this
 * repository into the `reference` source set; its classes live in the old root packages.
 *
 * Both versions convert the example files and thousands of deterministic mutations of them. They must agree on
 * every text: on the structure of the model if both convert it, on the exception type and message, apart from
 * the package names, if both fail.
 *
 * Many of these exceptions are null pointer exceptions the JVM raises itself, whose messages it would drop once
 * the throwing code is compiled as hot; the `compatibilityTest` task therefore runs with
 * `-XX:-OmitStackTraceInFastThrow`, which keeps them.
 */
class StrictConverterCompatibilityTest {

    /**
     * Every example file converts to the same model with both versions.
     */
    @Test
    fun `the examples convert as in version 1_0`() {
        val examples = ComparedExample.kmeta() + ComparedExample.kmodel() + ComparedExample.kstates()
        val outcomes = examples.map { it to it.outcomesOf(it.text) }

        assertNoMismatches(outcomes.mapNotNull { (example, outcome) -> mismatchOf(example.path, example.text, outcome) }, examples.size)
        outcomes.forEach { (example, outcome) ->
            assertTrue(outcome.first is Outcome.Converted, "$example does not convert with version 1.0: ${outcome.first}")
        }
        assertEquals(21, examples.size)
    }

    /**
     * Mutated KMeta texts, domain metamodels and event definitions, convert or fail as with version 1.0.
     */
    @Test
    fun `mutated kmeta texts convert or fail as in version 1_0`() {
        compareMutations(ComparedExample.kmeta(), Mutator(Vocabularies.KMETA))
    }

    /**
     * Mutated KModel texts convert or fail as with version 1.0.
     */
    @Test
    fun `mutated kmodel texts convert or fail as in version 1_0`() {
        compareMutations(ComparedExample.kmodel(), Mutator(Vocabularies.KMODEL))
    }

    /**
     * Mutated KStates texts convert or fail as with version 1.0.
     */
    @Test
    fun `mutated kstates texts convert or fail as in version 1_0`() {
        compareMutations(ComparedExample.kstates(), Mutator(Vocabularies.KSTATES))
    }

    private fun compareMutations(examples: List<ComparedExample>, mutator: Mutator) {
        val outcomes = mutableListOf<Outcome>()
        val mismatches = mutableListOf<String>()
        for (example in examples) {
            for (variant in mutator.variantsOf(example.path, example.text, MUTATIONS_PER_FILE)) {
                val outcome = example.outcomesOf(variant.text)
                outcomes.add(outcome.second)
                mismatchOf("$example, variant ${variant.index}", variant.text, outcome)?.let { mismatches.add(it) }
            }
        }
        assertNoMismatches(mismatches, outcomes.size)
        assertTrue(outcomes.any { it is Outcome.Converted }, "No mutation converted, the comparison is one-sided")
        assertTrue(outcomes.any { it is Outcome.Failed }, "No mutation failed, the comparison is one-sided")
    }

    /**
     * Describes how the [outcome] of both versions on [text] differs, or returns null if it does not.
     */
    private fun mismatchOf(case: String, text: String, outcome: Pair<Outcome, Outcome>): String? {
        val (legacy, current) = outcome
        return if (legacy == current) {
            null
        } else {
            "$case\n--- text ---\n$text\n--- version 1.0 ---\n$legacy\n--- current version ---\n$current"
        }
    }

    private fun assertNoMismatches(mismatches: List<String>, total: Int) {
        if (mismatches.isNotEmpty()) {
            fail("${mismatches.size} of $total texts differ, the first ones:\n\n" + mismatches.take(3).joinToString("\n\n"))
        }
    }

    private companion object {

        /** The number of mutations of every example file; with the 21 examples, several thousand in all. */
        const val MUTATIONS_PER_FILE = 500
    }
}
