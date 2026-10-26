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

import io.karpfen.dsl.printer.ExampleCatalog
import io.karpfen.dsl.printer.MetamodelExample
import io.karpfen.dsl.printer.ModelExample
import java.io.File
import dsl.textual.KmetaDSLConverter as LegacyKmetaConverter
import dsl.textual.KmodelDSLConverter as LegacyKmodelConverter
import dsl.textual.KstatesDSLConverter as LegacyKstatesConverter

/**
 * An example file with its throwing conversion, once by version 1.0 and once by the current version, each set
 * up with base types or a metamodel of its own version.
 *
 * @property path The path of the example file.
 * @property text The content of the example file.
 * @property legacy Converts a text with version 1.0.
 * @property current Converts a text with the current version.
 */
class ComparedExample(
    val path: String,
    val text: String,
    private val legacy: (String) -> Any?,
    private val current: (String) -> Any?
) {

    /**
     * Converts [content] with both versions.
     *
     * @return The outcome of version 1.0 and the outcome of the current version.
     */
    fun outcomesOf(content: String): Pair<Outcome, Outcome> = Outcome.of { legacy(content) } to Outcome.of { current(content) }

    override fun toString(): String = path

    /** Pairs the examples of the [ExampleCatalog] with the converters of both versions. */
    companion object {

        private const val EVENTS_FILE_NAME = "EVENTS.kmeta"

        /**
         * Returns the KMeta examples; version 1.0 reads an event definition with the types of its domain
         * metamodel as read by version 1.0.
         */
        fun kmeta(catalog: ExampleCatalog = ExampleCatalog()): List<ComparedExample> = catalog.metamodels().map { example ->
            val legacyBaseTypes = if (example.baseTypes.isEmpty()) emptyList() else legacyDomainNextTo(example.file).types
            ComparedExample(
                example.toString(),
                example.text,
                { LegacyKmetaConverter.parseKmetaString(it, legacyBaseTypes) },
                { example.parse(it) }
            )
        }

        /**
         * Returns the KModel examples, each version with the domain metamodel as it reads it.
         */
        fun kmodel(catalog: ExampleCatalog = ExampleCatalog()): List<ComparedExample> = catalog.models().map { example ->
            val legacyMetamodel = legacyDomainNextTo(example.file)
            ComparedExample(
                example.toString(),
                example.text,
                { LegacyKmodelConverter.parseKmodelString(it, legacyMetamodel) },
                { example.parse(it) }
            )
        }

        /**
         * Returns the KStates examples.
         */
        fun kstates(catalog: ExampleCatalog = ExampleCatalog()): List<ComparedExample> = catalog.stateMachines().map { example ->
            ComparedExample(
                example.toString(),
                example.text,
                { LegacyKstatesConverter.parseKstatesString(it) },
                { example.parse(it) }
            )
        }

        /**
         * Reads the domain metamodel next to [file] with version 1.0: the KMeta file of its directory that is not
         * an event definition, the same the catalog takes for [MetamodelExample.baseTypes] and
         * [ModelExample.metamodel].
         */
        private fun legacyDomainNextTo(file: File) = LegacyKmetaConverter.parseKmetaString(
            file.absoluteFile.parentFile.listFiles().orEmpty()
                .single { it.extension == "kmeta" && it.name != EVENTS_FILE_NAME }
                .readText()
        )    }
}
