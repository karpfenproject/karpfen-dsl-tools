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
package io.karpfen.dsl.fixtures

import io.karpfen.dsl.parser.EvalCodeSyntax
import io.karpfen.dsl.printer.kstates.StringContinuations

/**
 * The shape of `EVAL` code, which a round trip through the KStates printer and a parser keeps: the code without the
 * whitespace at its edges, with the indentation that its lines after the first share removed and with blank lines
 * emptied. A line that continues a string literal stays as it is and shares no indentation with the others.
 *
 * The printer lays out the lines after the first one level deeper than their block, keeping their indentation
 * relative to each other, and the parser reads them with the indentation they then have; the runtime indents the
 * code itself, so the shape is what counts.
 */
object CodeShape {

    /**
     * Returns the shape of [code].
     */
    fun of(code: String): String {
        val lines = EvalCodeSyntax.codeOf(code).lines()
        val continuations = StringContinuations().linesIn(EvalCodeSyntax.codeOf(code))
        val rebased = lines.indices.drop(1).filter { it !in continuations }
        val shared = rebased.map { lines[it] }.filter { it.isNotBlank() }.minOfOrNull { indentationWidthOf(it) } ?: 0
        return lines.mapIndexed { index, line ->
            when {
                index == 0 || index in continuations -> line
                line.isBlank() -> ""
                else -> line.drop(shared)
            }
        }.joinToString("\n")
    }

    private fun indentationWidthOf(line: String): Int = line.takeWhile { it.isWhitespace() }.length
}
