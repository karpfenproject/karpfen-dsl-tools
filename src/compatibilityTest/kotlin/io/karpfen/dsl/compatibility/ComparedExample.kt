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

/**
 * An example text with one entry point of each version that reads it, such as the throwing converter of its
 * language, once of version 1.0 and once of the current version, each set up with base types or a metamodel of its
 * own version.
 *
 * @property path The path of the example file, or a name for a text that is no file.
 * @property text The example text.
 * @property legacy Reads a text with version 1.0.
 * @property current Reads a text with the current version.
 */
class ComparedExample(
    val path: String,
    val text: String,
    private val legacy: (String) -> Any?,
    private val current: (String) -> Any?
) {

    /**
     * Reads [content] with both versions.
     *
     * @return The outcome of version 1.0 and the outcome of the current version.
     */
    fun outcomesOf(content: String): Pair<Outcome, Outcome> = Outcome.of { legacy(content) } to Outcome.of { current(content) }

    override fun toString(): String = path
}
