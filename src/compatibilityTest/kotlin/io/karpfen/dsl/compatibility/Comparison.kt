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
import io.karpfen.dsl.fixtures.TextLayout
import kotlin.test.assertTrue
import kotlin.test.fail

/**
 * Compares what both versions make of texts and fails with the first texts on which they differ: on the structure
 * of what they return if both return, on the exception type and message, apart from the package names, if both
 * throw.
 */
class Comparison {

    /**
     * Compares both versions on every example of [examples] in every [TextLayout] and on the texts without content.
     */
    fun inEveryLayout(examples: List<ComparedExample>) {
        val texts = examples.flatMap { example ->
            TextLayout.entries.map { Case("$example in $it", example, it.apply(example.text)) } +
                TextLayout.DEGENERATE_TEXTS.map { Case("$example on <${it.escaped()}>", example, it) }
        }
        assertAgree(texts)
    }

    /**
     * Compares both versions on [count] variants of every example of [examples] that [mutator] makes, and requires
     * that some variants are read and others fail, so that neither side of the comparison goes untested.
     */
    fun onMutations(examples: List<ComparedExample>, mutator: Mutator, count: Int) {
        val texts = examples.flatMap { example ->
            mutator.variantsOf(example.path, example.text, count)
                .map { Case("$example, variant ${it.index}", example, it.text) }
                .toList()
        }
        val outcomes = assertAgree(texts)
        assertTrue(outcomes.any { it is Outcome.Converted }, "No variant was read, the comparison is one-sided")
        assertTrue(outcomes.any { it is Outcome.Failed }, "No variant failed, the comparison is one-sided")
    }

    /**
     * Compares both versions on [cases] and returns the outcomes of the current version.
     */
    fun assertAgree(cases: List<Case>): List<Outcome> {
        val outcomes = cases.map { it to it.example.outcomesOf(it.text) }
        val mismatches = outcomes.mapNotNull { (case, outcome) -> mismatchOf(case, outcome) }
        if (mismatches.isNotEmpty()) {
            val firstOnes = mismatches.take(3).joinToString("\n\n")
            fail("${mismatches.size} of ${cases.size} texts differ, the first ones:\n\n$firstOnes")
        }
        return outcomes.map { it.second.second }
    }

    /**
     * Describes how the [outcome] of both versions on the text of [case] differs, or returns null if it does not.
     */
    private fun mismatchOf(case: Case, outcome: Pair<Outcome, Outcome>): String? {
        val (legacy, current) = outcome
        return if (legacy == current) {
            null
        } else {
            "${case.name}\n--- text ---\n${case.text}\n--- version 1.0 ---\n$legacy\n--- current version ---\n$current"
        }
    }

    private fun String.escaped(): String = replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t")

    /**
     * One text both versions read through the entry points of [example].
     *
     * @property name The description of the text in a failure.
     * @property example The example and its entry points.
     * @property text The text.
     */
    class Case(val name: String, val example: ComparedExample, val text: String)
}
