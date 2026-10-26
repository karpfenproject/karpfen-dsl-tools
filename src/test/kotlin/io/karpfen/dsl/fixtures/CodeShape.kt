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

/**
 * The shape of `EVAL` code, which a round trip through the KStates printer and a parser keeps: the code without the
 * whitespace at its edges, with the indentation all its lines after the first share removed and with blank lines
 * emptied.
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
        val lines = code.trim().lines()
        val following = lines.drop(1)
        val shared = following.filter { it.isNotBlank() }.minOfOrNull { indentationWidthOf(it) } ?: 0
        return (listOf(lines.first()) + following.map { if (it.isBlank()) "" else it.drop(shared) }).joinToString("\n")
    }

    private fun indentationWidthOf(line: String): Int = line.takeWhile { it.isWhitespace() }.length
}
