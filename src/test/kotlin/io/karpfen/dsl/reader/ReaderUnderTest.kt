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

import io.karpfen.dsl.dsl.textual.captureConsole
import io.karpfen.dsl.fixtures.ExampleCatalog
import io.karpfen.dsl.fixtures.StructureDump
import io.karpfen.dsl.reader.kmeta.KmetaReader
import io.karpfen.dsl.reader.kmodel.KmodelReader
import io.karpfen.dsl.reader.kstates.KstatesReader

/**
 * An example file together with the tolerant reader and the throwing converter of its language, set up with the
 * same base types or metamodel.
 *
 * @property path The path of the example file.
 * @property text The content of the example file.
 * @property reader The reader under test.
 */
class ReaderUnderTest(
    val path: String,
    val text: String,
    val reader: TextReader<Any>,
    private val converter: (String) -> Any?
) {

    /**
     * Returns the structure of the model the throwing converter builds from [content], or null if it throws; the
     * converter's log output is discarded.
     */
    fun convertedStructureOf(content: String): String? {
        var structure: String? = null
        captureConsole {
            structure = try {
                StructureDump.of(converter(content))
            } catch (failure: Exception) {
                null
            }
        }
        return structure
    }

    override fun toString(): String = path

    /** Pairs the examples with their readers. */
    companion object {

        /**
         * Returns the KMeta examples, an event definition with the types of its domain metamodel as base types.
         */
        fun kmeta(catalog: ExampleCatalog = ExampleCatalog()): List<ReaderUnderTest> = catalog.metamodels().map {
            ReaderUnderTest(it.toString(), it.text, KmetaReader(it.baseTypes)) { content -> it.parse(content) }
        }

        /**
         * Returns the KModel examples with the domain metamodel of their directory.
         */
        fun kmodel(catalog: ExampleCatalog = ExampleCatalog()): List<ReaderUnderTest> = catalog.models().map {
            ReaderUnderTest(it.toString(), it.text, KmodelReader(it.metamodel)) { content -> it.parse(content) }
        }

        /**
         * Returns the KStates examples.
         */
        fun kstates(catalog: ExampleCatalog = ExampleCatalog()): List<ReaderUnderTest> = catalog.stateMachines().map {
            ReaderUnderTest(it.toString(), it.text, KstatesReader()) { content -> it.parse(content) }
        }

        /**
         * Returns every example.
         */
        fun all(): List<ReaderUnderTest> = kmeta() + kmodel() + kstates()
    }
}
