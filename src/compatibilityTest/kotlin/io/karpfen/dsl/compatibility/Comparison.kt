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
 * Compares what both versions make of texts and fails with the first texts on which they differ.
 *
 * The converters of the current version read through the readers and reject every text with an error, see
 * [Outcome.Rejected], where version 1.0 went on with what its parser recovered and checked less. The claim of the
 * comparison is therefore: a text that the current version converts gives the same model as with version 1.0, and a
 * text that the current version rejects is one that has an error. In detail, for each text:
 *
 * - If the current version returns, version 1.0 returns the same structure.
 * - If the current version throws anything but a rejection, version 1.0 throws the same exception type with the same
 *   message, apart from the package names.
 * - If the current version rejects the text and version 1.0 throws, both reject it.
 * - If the current version rejects a text that version 1.0 converts, the text is a mutation, whose outcome only has
 *   to stay on the side of the rule above, or the text is an example in a layout, for which a [NewOutcome] must name
 *   the rejection with the codes of its errors. An example does not change its outcome silently.
 */
class Comparison {

    /**
     * Compares both versions on every example of [examples] in every [TextLayout] and on the texts without content.
     *
     * @param newOutcomes The examples in a layout that version 1.0 converts and the current version rejects; each must
     * match at least one such text, and every such text must match one of them.
     */
    fun inEveryLayout(examples: List<ComparedExample>, newOutcomes: List<NewOutcome> = emptyList()) {
        val texts = examples.flatMap { example ->
            TextLayout.entries.map { Case("$example in $it", example, it.apply(example.text), it.name) } +
                TextLayout.DEGENERATE_TEXTS.map { Case("$example on <${it.escaped()}>", example, it, "<${it.escaped()}>") }
        }
        val outcomes = texts.map { it to it.example.outcomesOf(it.text) }
        val mismatches = outcomes.mapNotNull { (case, outcome) -> mismatchOf(case, outcome, newOutcomes) }
        val unmatched = newOutcomes.filter { expectation ->
            outcomes.none { (case, outcome) -> expectation.matches(case, outcome.first, outcome.second) }
        }
        failOn(mismatches + unmatched.map { "The new outcome '${it.description}' matches no text" }, texts.size)
    }

    /**
     * Compares both versions on [count] variants of every example of [examples] that [mutator] makes, and requires
     * that some variants are read and others fail, so that neither side of the comparison goes untested.
     */
    fun onMutations(examples: List<ComparedExample>, mutator: Mutator, count: Int) {
        val texts = examples.flatMap { example ->
            mutator.variantsOf(example.path, example.text, count)
                .map { Case("$example, variant ${it.index}", example, it.text, null) }
                .toList()
        }
        val outcomes = assertAgree(texts)
        assertTrue(outcomes.any { it is Outcome.Converted }, "No variant was read, the comparison is one-sided")
        assertTrue(outcomes.any { it !is Outcome.Converted }, "No variant failed, the comparison is one-sided")
    }

    /**
     * Compares both versions on [cases], which are mutations or texts without a new outcome, and returns the outcomes
     * of the current version.
     */
    fun assertAgree(cases: List<Case>): List<Outcome> {
        val outcomes = cases.map { it to it.example.outcomesOf(it.text) }
        failOn(outcomes.mapNotNull { (case, outcome) -> mismatchOf(case, outcome, emptyList()) }, cases.size)
        return outcomes.map { it.second.second }
    }

    /**
     * Fails with the first [mismatches] of [total] texts, if there are any.
     */
    private fun failOn(mismatches: List<String>, total: Int) {
        if (mismatches.isNotEmpty()) {
            val firstOnes = mismatches.take(3).joinToString("\n\n")
            fail("${mismatches.size} of $total texts differ, the first ones:\n\n$firstOnes")
        }
    }

    /**
     * Describes how the [outcome] of both versions on the text of [case] breaks the claim of the comparison, or
     * returns null if it does not.
     */
    private fun mismatchOf(case: Case, outcome: Pair<Outcome, Outcome>, newOutcomes: List<NewOutcome>): String? {
        val (legacy, current) = outcome
        val agrees = when {
            current !is Outcome.Rejected -> legacy == current
            legacy !is Outcome.Converted -> true
            case.layout == null -> true
            else -> newOutcomes.any { it.matches(case, legacy, current) }
        }
        return if (agrees) {
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
     * @property layout The name of the [TextLayout] the example is written in, or the escaped text without content
     * between angle brackets; null for a mutation.
     */
    class Case(val name: String, val example: ComparedExample, val text: String, val layout: String?)
}

/**
 * Examples in a layout that version 1.0 converts and the current version rejects, since the reader finds errors in
 * them that version 1.0 did not check or read past.
 *
 * @property description What changed and why, for a failure.
 * @property layout The name of the [TextLayout], or the escaped text without content between angle brackets, as
 * [Comparison.Case.layout] holds it.
 * @property extension The extension of the example files it concerns, which names their language.
 * @property fileNames The names of the example files it concerns, null for every example of the language.
 * @property codes The ids of the codes of the errors that reject the text, each once, in the order of their first
 * position.
 */
class NewOutcome(
    val description: String,
    private val layout: String,
    val extension: String,
    private val fileNames: Set<String>?,
    private val codes: List<String>
) {

    /**
     * Returns true if [case] is a text of this new outcome: version 1.0 converted it to [legacy], and [current] is
     * the rejection this new outcome names.
     */
    fun matches(case: Comparison.Case, legacy: Outcome, current: Outcome): Boolean {
        val file = case.example.file ?: return false
        return case.layout == layout && file.extension == extension && (fileNames == null || file.name in fileNames) &&
            legacy is Outcome.Converted && current == Outcome.Rejected(codes)
    }
}
