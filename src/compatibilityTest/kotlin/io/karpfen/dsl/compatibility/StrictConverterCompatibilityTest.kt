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

/**
 * Compares the throwing converters with those of version 1.0, which the build compiles from the history of this
 * repository into the `reference` source set; its classes live in the old root packages.
 *
 * Both versions convert the example files, in every layout of the tests, and thousands of deterministic mutations of
 * them, see [Comparison]: a text the current version converts gives the model of version 1.0, and a text it rejects
 * has an error. Where version 1.0 converts an example in a layout that the current version rejects, [NewOutcomes]
 * names it.
 */
class StrictConverterCompatibilityTest {

    private val examples = ComparedExamples()

    private val comparison = Comparison()

    /**
     * Every example file converts with both versions to the same model, and so it does in every layout but those
     * of [NewOutcomes].
     */
    @Test
    fun `the examples convert as in version 1_0`() {
        val all = examples.kmetaConversions() + examples.kmodelConversions() + examples.kstatesConversions()
        val asWritten = comparison.assertAgree(all.map { Comparison.Case(it.path, it, it.text, null) })

        assertTrue(asWritten.all { it is Outcome.Converted }, "An example does not convert")
        assertEquals(24, all.size)
        comparison.inEveryLayout(all, NewOutcomes.ALL)
    }

    /**
     * Mutated KMeta texts, domain metamodels and event definitions, convert or fail as with version 1.0.
     */
    @Test
    fun `mutated kmeta texts convert or fail as in version 1_0`() {
        comparison.onMutations(examples.kmetaConversions(), Mutator(Vocabularies.KMETA), MUTATIONS_PER_FILE)
    }

    /**
     * Mutated KModel texts convert or fail as with version 1.0.
     */
    @Test
    fun `mutated kmodel texts convert or fail as in version 1_0`() {
        comparison.onMutations(examples.kmodelConversions(), Mutator(Vocabularies.KMODEL), MUTATIONS_PER_FILE)
    }

    /**
     * Mutated KStates texts convert or fail as with version 1.0.
     */
    @Test
    fun `mutated kstates texts convert or fail as in version 1_0`() {
        comparison.onMutations(examples.kstatesConversions(), Mutator(Vocabularies.KSTATES), MUTATIONS_PER_FILE)
    }

    private companion object {

        /** The number of mutations of every example file; with the 21 examples, several thousand in all. */
        const val MUTATIONS_PER_FILE = 500
    }
}
