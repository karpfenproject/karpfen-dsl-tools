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
package io.karpfen.dsl.conventions

import io.karpfen.dsl.diagnostics.Diagnostic
import io.karpfen.dsl.meta.Metamodel
import io.karpfen.dsl.reader.ParseResult
import io.karpfen.dsl.reader.kmeta.KmetaReader
import io.karpfen.dsl.reader.kmodel.KmodelReader
import io.karpfen.dsl.reader.kstates.KstatesReader
import io.karpfen.dsl.states.KstatesFeature
import io.karpfen.dsl.verification.ProjectInput
import io.karpfen.dsl.verification.ProjectVerifier
import io.karpfen.dsl.verification.StateMachineInput
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Keeps the examples of the reference documentation in `docs/reference` true: every whole text in a fenced code block
 * of a reference file reads with the readers of the library, and the whole state machines also pass the verification
 * of a project, without a single problem, error or warning.
 *
 * The language tag of a block decides what the block is:
 *
 * - A `kmeta` block is always a whole metamodel and is read with [KmetaReader].
 * - A `kmodel` block is always a whole model and is read with [KmodelReader] against the metamodel of the last `kmeta`
 *   block before it in the same file, which must exist.
 * - A `kstates` block whose first line that is neither blank nor a `//` comment starts with `STATEMACHINE` is a whole
 *   state machine. It is read with [KstatesReader] with every optional feature switched on. If a `kmeta` block stands
 *   before it in the same file, [ProjectVerifier] also checks it against the metamodel of the last such block, without
 *   an event metamodel, a model or the object it runs on. Any other `kstates` block is a fragment and is not read.
 * - A block of any other language, such as `text`, `kotlin` or `bash`, is not read. A text that is meant to show a
 *   problem is therefore written in a `text` block, or in a code span of a table.
 */
class ReferenceExamplesTest {

    /** The text of every reference file, by its path relative to the project folder, in the order of their names. */
    private val referenceFiles: Map<String, String> by lazy {
        SourceFiles.read(setOf("md"))
            .filterKeys { it.startsWith(REFERENCE_FOLDER) && it != "${REFERENCE_FOLDER}README.md" }
            .toSortedMap()
    }

    /**
     * The test reads the reference files of the chapter on the modeling languages, so it cannot pass on a list of files
     * that misses them.
     */
    @Test
    fun `the reference files of the modeling languages are read`() {
        val expected = listOf(
            "2-modeling-languages.md", "2.1-kmeta.md", "2.2-kmodel.md", "2.3-kstates.md", "2.4-checks.md", "2.5-library.md"
        ).map { REFERENCE_FOLDER + it }
        assertEquals(expected, referenceFiles.keys.toList())
    }

    /**
     * Every whole text of the reference files reads, and every whole state machine verifies, without a problem; each
     * language has whole texts, so the rule cannot pass because it reads nothing.
     */
    @Test
    fun `every whole text of the reference files reads without problems`() {
        val checked = referenceFiles.flatMap { (path, text) -> checkFile(path, text) }
        val failures = checked.filter { it.problems.isNotEmpty() }.map { example ->
            "${example.location}\n" + example.problems.joinToString("\n") { "    $it" }
        }
        assertEquals(emptyList(), failures, "The examples of the reference documentation must read without problems")
        listOf("kmeta", "kmodel", "kstates").forEach { language ->
            assertTrue(checked.any { it.language == language }, "no whole $language text was checked")
        }
    }

    /**
     * Reads the fenced blocks of the file at [path] with the text [text] in order and returns every whole text checked,
     * with its problems.
     */
    private fun checkFile(path: String, text: String): List<CheckedExample> {
        var metamodel: ParseResult<Metamodel>? = null
        val checked = mutableListOf<CheckedExample>()
        for (block in FencedBlock.allIn(text)) {
            val location = "$path:${block.line} (${block.language})"
            when (block.language) {
                "kmeta" -> {
                    val read = KmetaReader().read(block.text)
                    metamodel = read
                    checked.add(CheckedExample(location, block.language, describe(read.diagnostics)))
                }
                "kmodel" -> {
                    val problems = metamodel?.model?.let { describe(KmodelReader(it).read(block.text).diagnostics) }
                        ?: listOf("no kmeta block that yields a metamodel stands before this model")
                    checked.add(CheckedExample(location, block.language, problems))
                }
                "kstates" -> if (isWholeStateMachine(block.text)) {
                    checked.add(CheckedExample(location, block.language, describe(problemsOfStateMachine(block.text, metamodel))))
                }
            }
        }
        return checked
    }

    /**
     * Returns the problems of the state machine [text]: those of its read, and those of its verification against
     * [metamodel] if there is one.
     */
    private fun problemsOfStateMachine(text: String, metamodel: ParseResult<Metamodel>?): List<Diagnostic> {
        val read = KstatesReader(KstatesFeature.ALL).read(text)
        if (metamodel == null || read.diagnostics.isNotEmpty()) {
            return read.diagnostics
        }
        val machine = StateMachineInput(read)
        return ProjectVerifier().verify(ProjectInput(metamodel, stateMachines = listOf(machine))).of(machine)
    }

    /**
     * Returns true if [text] is a whole state machine: its first line that is neither blank nor a comment starts with
     * the keyword `STATEMACHINE`.
     */
    private fun isWholeStateMachine(text: String): Boolean =
        text.lineSequence().map { it.trim() }.firstOrNull { it.isNotEmpty() && !it.startsWith("//") }
            ?.startsWith("STATEMACHINE") == true

    /**
     * Returns [diagnostics] as lines of a message: the code, the line and column in the text of the block, and the
     * message of each.
     */
    private fun describe(diagnostics: List<Diagnostic>): List<String> = diagnostics.map {
        "${it.code.id} ${it.range.start.line}:${it.range.start.column}: ${it.message}"
    }

    /**
     * A whole text of a reference file that was checked.
     *
     * @property location The file and line of the block, for the message of a failure.
     * @property language The language tag of the block.
     * @property problems The problems found in the text, each described in a line.
     */
    private class CheckedExample(val location: String, val language: String, val problems: List<String>)

    /**
     * A fenced code block of a markdown text: the lines between a line of three backticks followed by a language tag and
     * the next line of three backticks alone.
     *
     * @property language The language tag after the opening backticks.
     * @property text The lines of the block, joined by line breaks.
     * @property line The line of the opening backticks, counted from 1.
     */
    private class FencedBlock(val language: String, val text: String, val line: Int) {

        /** Finds fenced blocks. */
        companion object {

            /** The opening line of a fenced block, with its language tag. */
            private val OPENING = Regex("""^```(\w+)\s*$""")

            /** The closing line of a fenced block. */
            private val CLOSING = Regex("""^```\s*$""")

            /**
             * Returns the fenced blocks of the markdown [markdown] in the order they are written; a block without a
             * closing line runs to the end of the text.
             */
            fun allIn(markdown: String): List<FencedBlock> {
                val lines = markdown.lines()
                val blocks = mutableListOf<FencedBlock>()
                var index = 0
                while (index < lines.size) {
                    val opening = OPENING.matchEntire(lines[index])
                    if (opening == null) {
                        index += 1
                        continue
                    }
                    val start = index + 1
                    var end = start
                    while (end < lines.size && !CLOSING.matches(lines[end])) {
                        end += 1
                    }
                    blocks.add(FencedBlock(opening.groupValues[1], lines.subList(start, end).joinToString("\n"), index + 1))
                    index = end + 1
                }
                return blocks
            }
        }
    }

    private companion object {

        /** The folder of the reference files, relative to the project folder. */
        const val REFERENCE_FOLDER = "docs/reference/"
    }
}
