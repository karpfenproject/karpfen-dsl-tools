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
import io.karpfen.dsl.fixtures.Mutator
import io.karpfen.dsl.fixtures.ReachableObjects
import io.karpfen.dsl.fixtures.StructureDump
import io.karpfen.dsl.fixtures.TextLayout
import io.karpfen.dsl.fixtures.Vocabularies
import io.karpfen.dsl.reader.kstates.KstatesReader
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.test.fail

/**
 * Tests that the readers never throw, whatever the text, and agree with the throwing converters.
 *
 * Thousands of deterministic mutations of the example files, every example in every [TextLayout] and every prefix of
 * an example stand in for text in the middle of being typed. On each, the reader must neither throw nor fail
 * internally, and every range it reports or records must be a proper range of the text. Where the reader finds no
 * error, the throwing converter must build a model of the same structure; where the converter throws, the reader
 * must report an error.
 */
class ReaderRobustnessTest {

    /**
     * Mutated KMeta texts, domain metamodels and event definitions.
     */
    @Test
    fun `the kmeta reader never throws and agrees with the converter`() {
        checkMutations(ReaderUnderTest.kmeta(), Mutator(Vocabularies.KMETA))
    }

    /**
     * Mutated KModel texts.
     */
    @Test
    fun `the kmodel reader never throws and agrees with the converter`() {
        checkMutations(ReaderUnderTest.kmodel(), Mutator(Vocabularies.KMODEL))
    }

    /**
     * Mutated KStates texts.
     */
    @Test
    fun `the kstates reader never throws and agrees with the converter`() {
        checkMutations(ReaderUnderTest.kstates(), Mutator(Vocabularies.KSTATES))
    }

    /**
     * Every example in every layout, and texts without any content, which the converters of KMeta and KModel read
     * with their common indentation trimmed and their line breaks made uniform.
     */
    @Test
    fun `the readers read every example in every layout as the converters do`() {
        val problems = ReaderUnderTest.all().flatMap { subject ->
            val layouts = TextLayout.entries.map { "$subject in $it" to it.apply(subject.text) }
            val degenerate = TextLayout.DEGENERATE_TEXTS.map { "<${StructureDump.of(it)}> as $subject" to it }
            (layouts + degenerate).mapNotNull { (case, text) -> readingOf(subject, text).problem?.let { "$case: $it" } }
        }

        assertTrue(problems.isEmpty(), problems.take(3).joinToString("\n"))
    }

    /**
     * Every prefix of a KStates example, as if the file were typed from start to end.
     */
    @Test
    fun `the kstates reader reads every prefix of an example`() {
        val example = ReaderUnderTest.kstates().first { it.path.endsWith("autonomous_car.kstates") }
        val problems = (0..example.text.length).mapNotNull { length ->
            val prefix = example.text.substring(0, length)
            problemOf(prefix) { example.reader.read(prefix) }?.let { "prefix of length $length: $it" }
        }

        assertTrue(problems.isEmpty(), problems.take(3).joinToString("\n"))
    }

    /**
     * Every prefix of a KMeta and of a KModel example.
     */
    @Test
    fun `the kmeta and kmodel readers read every prefix of an example`() {
        val examples = listOf(
            ReaderUnderTest.kmeta().first { it.path.endsWith("single_robot_example/cleaning_robot.kmeta") },
            ReaderUnderTest.kmodel().first { it.path.endsWith("single_robot_example/cleaning_robot.kmodel") }
        )
        val problems = examples.flatMap { example ->
            (0..example.text.length).mapNotNull { length ->
                val prefix = example.text.substring(0, length)
                problemOf(prefix) { example.reader.read(prefix) }?.let { "$example, prefix of length $length: $it" }
            }
        }

        assertTrue(problems.isEmpty(), problems.take(3).joinToString("\n"))
    }

    /**
     * Braces nested deeper than the parser's recursion can follow make the reader fail internally, which it reports
     * as a diagnostic instead of throwing.
     */
    @Test
    fun `nesting beyond the parser's reach is reported instead of thrown`() {
        val depth = 100_000
        val text = "STATEMACHINE ATTACHED TO \"R\" { STATES { INITIAL STATE \"a\" { DO { SET(\"x\", EVAL { " +
            "{".repeat(depth) + "}".repeat(depth) + " }) } } } TRANSITIONS { } }"

        val result = KstatesReader().read(text)

        assertEquals(listOf(DiagnosticCode.READER_FAILURE), result.diagnostics.map { it.code })
        assertNull(result.model)
    }

    private fun checkMutations(examples: List<ReaderUnderTest>, mutator: Mutator) {
        val problems = mutableListOf<String>()
        var readerErrors = 0
        var total = 0
        for (subject in examples) {
            for (variant in mutator.variantsOf(subject.path, subject.text, MUTATIONS_PER_FILE)) {
                total += 1
                val reading = readingOf(subject, variant.text)
                if (reading.result?.hasErrors == true) {
                    readerErrors += 1
                }
                reading.problem?.let {
                    problems.add("$subject, variant ${variant.index}: $it\n--- text ---\n${variant.text}")
                }
            }
        }
        if (problems.isNotEmpty()) {
            fail("${problems.size} of $total texts fail, the first ones:\n\n" + problems.take(3).joinToString("\n\n"))
        }
        assertTrue(readerErrors in 1 until total, "The mutations must yield texts with and without errors")
    }

    /**
     * Reads [text] with the reader of [subject] and finds why that is a problem, see [problemOf], or how it
     * disagrees with the converter, see [disagreementOf].
     */
    private fun readingOf(subject: ReaderUnderTest, text: String): Reading {
        var result: ParseResult<Any>? = null
        val readProblem = problemOf(text) { subject.reader.read(text).also { result = it } }
        return Reading(result, readProblem ?: result?.let { disagreementOf(it, subject.convertedStructureOf(text)) })
    }

    /**
     * One read of a text.
     *
     * @property result What the reader read, null if it threw.
     * @property problem Why the read is a problem or disagrees with the converter, null if it is fine.
     */
    private class Reading(val result: ParseResult<Any>?, val problem: String?)

    /**
     * Describes how the reader result and the structure the converter built, null if the converter threw,
     * disagree; null if they agree.
     */
    private fun disagreementOf(result: ParseResult<Any>, convertedStructure: String?): String? = when {
        convertedStructure == null && !result.hasErrors -> "the converter throws, but the reader reports no error"
        convertedStructure != null && !result.hasErrors && StructureDump.of(result.model) != convertedStructure ->
            "the reader reports no error, but its model differs from the converted one"
        else -> null
    }

    /**
     * Describes why reading [text] with [read] is a problem: it throws, fails internally, or a range of a diagnostic
     * or of a location in the source map is no proper range of the text, see [TextPositions]; null if reading is
     * fine.
     */
    private fun problemOf(text: String, read: () -> ParseResult<Any>): String? {
        val result = try {
            read()
        } catch (failure: Throwable) {
            return "the reader throws $failure"
        }
        result.diagnostics.firstOrNull { it.code == DiagnosticCode.READER_FAILURE }?.let { return it.message }
        val locations = ReachableObjects.from(result.model).flatMap { result.sourceMap.locationsOf(it) }
        val locatedRanges = locations.flatMap { listOfNotNull(it.range, it.nameRange, it.codeRange) }
        val ranges = result.diagnostics.map { it.range } + locatedRanges
        return ranges.firstNotNullOfOrNull { TextPositions.deviationOf(text, it) }
    }

    private companion object {

        /** The number of mutations of every example file; with the 21 examples, several thousand in all. */
        const val MUTATIONS_PER_FILE = 300
    }
}
