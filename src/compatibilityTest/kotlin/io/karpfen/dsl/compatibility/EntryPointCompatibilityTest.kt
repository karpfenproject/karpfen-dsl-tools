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

/**
 * Compares the entry points of the library besides the conversion of texts with those of version 1.0, see
 * [Comparison]: the conversion of files, of a single KModel object and of JSON documents and arrays, and the
 * syntax-level file parsers, which report syntax errors to another listener than version 1.0 but must read the same.
 */
class EntryPointCompatibilityTest {

    private val examples = ComparedExamples()

    private val comparison = Comparison()

    /**
     * The examples in every layout convert from files as in version 1.0.
     */
    @Test
    fun `files convert as in version 1_0`() {
        comparison.inEveryLayout(examples.fileConversions())
    }

    /**
     * The KModel examples and mutations of them convert into a single object as in version 1.0.
     */
    @Test
    fun `single objects convert or fail as in version 1_0`() {
        val singleObjects = examples.singleObjects()
        comparison.inEveryLayout(singleObjects)
        comparison.onMutations(singleObjects, Mutator(Vocabularies.KMODEL), MUTATIONS_PER_FILE)
    }

    /**
     * The examples of the three languages and mutations of them parse on the syntax level as in version 1.0.
     */
    @Test
    fun `the file parsers read as in version 1_0`() {
        val parses = examples.syntaxLevelParses()
        val vocabulary = Vocabularies.KMETA + Vocabularies.KMODEL + Vocabularies.KSTATES
        comparison.inEveryLayout(parses)
        comparison.onMutations(parses, Mutator(vocabulary), MUTATIONS_PER_FILE)
    }

    /**
     * JSON documents of the KModel examples and mutations of them convert or fail as in version 1.0.
     */
    @Test
    fun `json documents convert or fail as in version 1_0`() {
        val conversions = examples.jsonConversions()
        comparison.inEveryLayout(conversions)
        comparison.onMutations(conversions, Mutator(JSON_VOCABULARY), MUTATIONS_PER_FILE)
    }

    /**
     * JSON arrays of objects and of primitive values and mutations of them convert into lists or fail as in
     * version 1.0.
     */
    @Test
    fun `json arrays convert or fail as in version 1_0`() {
        val conversions = examples.jsonListConversions()
        comparison.inEveryLayout(conversions)
        comparison.onMutations(conversions, Mutator(JSON_VOCABULARY), MUTATIONS_PER_FILE)
    }

    private companion object {

        /** The number of mutations of every example. */
        const val MUTATIONS_PER_FILE = 200

        /** Punctuation, values and keys of JSON documents for the mutations of JSON documents. */
        val JSON_VOCABULARY = listOf(
            "{", "}", "[", "]", ":", ",", "\"", "null", "true", "1.5", "-2", "\"x\"", "\"__id__\"", "\"__type__\"",
            "{}", "[]", "\"k\": 1", "\"k\": [1, 2]", "\"k\": {\"__id__\": \"o\"}", "\n", " "
        )
    }
}
