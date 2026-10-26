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

import io.karpfen.dsl.dsl.textual.JsonToModelConverter
import io.karpfen.dsl.dsl.textual.KmetaDSLConverter
import io.karpfen.dsl.dsl.textual.KmodelDSLConverter
import io.karpfen.dsl.dsl.textual.KstatesDSLConverter
import io.karpfen.dsl.fixtures.ExampleCatalog
import io.karpfen.dsl.fixtures.MetamodelExample
import io.karpfen.dsl.fixtures.ModelExample
import io.karpfen.dsl.meta.Metamodel
import io.karpfen.dsl.parser.KStatesFileParser
import io.karpfen.dsl.parser.KmetaFileParser
import io.karpfen.dsl.parser.KmodelFileParser
import java.io.File
import dsl.textual.JsonToModelConverter as LegacyJsonConverter
import dsl.textual.KmetaDSLConverter as LegacyKmetaConverter
import dsl.textual.KmodelDSLConverter as LegacyKmodelConverter
import dsl.textual.KstatesDSLConverter as LegacyKstatesConverter
import meta.Metamodel as LegacyMetamodel
import parser.KStatesFileParser as LegacyKstatesFileParser
import parser.KmetaFileParser as LegacyKmetaFileParser
import parser.KmodelFileParser as LegacyKmodelFileParser

/**
 * The entry points of both versions the compatibility tests compare, each paired with the examples of [catalog] it
 * reads: the throwing converters of the three languages for texts and for files, the conversion of a single KModel
 * object, the syntax-level file parsers and the conversion of JSON documents.
 *
 * @param catalog The examples.
 * @param documents Where the conversions of files find the texts they read.
 */
class ComparedExamples(
    private val catalog: ExampleCatalog = ExampleCatalog(),
    private val documents: DocumentFiles = DocumentFiles()
) {

    /**
     * Returns the KMeta examples with the converters; version 1.0 reads an event definition with the types of its
     * domain metamodel as read by version 1.0.
     */
    fun kmetaConversions(): List<ComparedExample> = catalog.metamodels().map { example ->
        val legacyBaseTypes = legacyBaseTypesOf(example)
        ComparedExample(
            example.toString(),
            example.text,
            { LegacyKmetaConverter.parseKmetaString(it, legacyBaseTypes) },
            { example.parse(it) }
        )
    }

    /**
     * Returns the KModel examples with the converters, each version with the domain metamodel as it reads it.
     */
    fun kmodelConversions(): List<ComparedExample> = catalog.models().map { example ->
        val legacyMetamodel = legacyDomainNextTo(example.file)
        ComparedExample(
            example.toString(),
            example.text,
            { LegacyKmodelConverter.parseKmodelString(it, legacyMetamodel) },
            { example.parse(it) }
        )
    }

    /**
     * Returns the KStates examples with the converters.
     */
    fun kstatesConversions(): List<ComparedExample> = catalog.stateMachines().map { example ->
        ComparedExample(
            example.toString(),
            example.text,
            { LegacyKstatesConverter.parseKstatesString(it) },
            { example.parse(it) }
        )
    }

    /**
     * Returns the examples of all three languages with the conversions of files, which read the text from a file;
     * the KMeta conversion of a file takes no base types.
     */
    fun fileConversions(): List<ComparedExample> =
        catalog.metamodels().map { example ->
            ComparedExample(
                "${example.file} as a file",
                example.text,
                { LegacyKmetaConverter.parseKmetaFile(documents.holding(it, "kmeta")) },
                { KmetaDSLConverter.parseKmetaFile(documents.holding(it, "kmeta")) }
            )
        } + catalog.models().map { example ->
            val legacyMetamodel = legacyDomainNextTo(example.file)
            ComparedExample(
                "${example.file} as a file",
                example.text,
                { LegacyKmodelConverter.parseKmodelFile(documents.holding(it, "kmodel"), legacyMetamodel) },
                { KmodelDSLConverter.parseKmodelFile(documents.holding(it, "kmodel"), example.metamodel) }
            )
        } + catalog.stateMachines().map { example ->
            ComparedExample(
                "${example.file} as a file",
                example.text,
                { LegacyKstatesConverter.parseKstatesFile(documents.holding(it, "kstates")) },
                { KstatesDSLConverter.parseKstatesFile(documents.holding(it, "kstates")) }
            )
        }

    /**
     * Returns the KModel examples with the conversion of a single object.
     */
    fun singleObjects(): List<ComparedExample> = catalog.models().map { example ->
        val legacyMetamodel = legacyDomainNextTo(example.file)
        ComparedExample(
            "${example.file} as a single object",
            example.text,
            { LegacyKmodelConverter.parseSingleObject(it, legacyMetamodel) },
            { KmodelDSLConverter.parseSingleObject(it, example.metamodel) }
        )
    }

    /**
     * Returns the examples of all three languages with the syntax-level file parsers.
     */
    fun syntaxLevelParses(): List<ComparedExample> =
        catalog.metamodels().map { example ->
            ComparedExample(
                "${example.file} parsed",
                example.text,
                { LegacyKmetaFileParser().parseString(it) },
                { KmetaFileParser().parseString(it) }
            )
        } + catalog.models().map { example ->
            ComparedExample(
                "${example.file} parsed",
                example.text,
                { LegacyKmodelFileParser().parseString(it) },
                { KmodelFileParser().parseString(it) }
            )
        } + catalog.stateMachines().map { example ->
            ComparedExample(
                "${example.file} parsed",
                example.text,
                { LegacyKstatesFileParser().parseString(it) },
                { KStatesFileParser().parseString(it) }
            )
        }

    /**
     * Returns the root object of every KModel example as a JSON document, see [JsonDocuments], with the conversions
     * of JSON documents into an object of the class type of the root.
     */
    fun jsonConversions(): List<ComparedExample> = catalog.models().map { example ->
        val legacyMetamodel = legacyDomainNextTo(example.file)
        val root = example.parse().objects.single()
        val typeName = root.ofType.name
        ComparedExample(
            "${example.file} as JSON",
            JsonDocuments.of(root),
            { LegacyJsonConverter(legacyMetamodel).toDataObject(it, typeName) },
            { JsonToModelConverter(example.metamodel).toDataObject(it, typeName) }
        )
    }

    /**
     * Returns JSON arrays with the conversion of JSON arrays into lists: the root object of every KModel example in
     * an array of its class type, and arrays of primitive values of each primitive type.
     */
    fun jsonListConversions(): List<ComparedExample> {
        val objectLists = catalog.models().map { example ->
            val legacyMetamodel = legacyDomainNextTo(example.file)
            val root = example.parse().objects.single()
            val array = "[${JsonDocuments.of(root)}]"
            val name = "${example.file} as a JSON array"
            jsonListExample(name, array, root.ofType.name, legacyMetamodel, example.metamodel)
        }
        val anyModel = catalog.models().first()
        val primitiveLists = PRIMITIVE_ARRAYS.map { (type, array) ->
            jsonListExample("an array of $type", array, type, legacyDomainNextTo(anyModel.file), anyModel.metamodel)
        }
        return objectLists + primitiveLists
    }

    private fun jsonListExample(
        name: String,
        array: String,
        elementType: String,
        legacyMetamodel: LegacyMetamodel,
        metamodel: Metamodel
    ) = ComparedExample(
        name,
        array,
        { LegacyJsonConverter(legacyMetamodel).toList(it, elementType) },
        { JsonToModelConverter(metamodel).toList(it, elementType) }
    )

    private fun legacyBaseTypesOf(example: MetamodelExample) =
        if (example.baseTypes.isEmpty()) emptyList() else legacyDomainNextTo(example.file).types

    /**
     * Reads the domain metamodel next to [file] with version 1.0: the KMeta file of its directory that is not an
     * event definition, the same the catalog takes for [MetamodelExample.baseTypes] and [ModelExample.metamodel].
     */
    private fun legacyDomainNextTo(file: File) = LegacyKmetaConverter.parseKmetaString(
        file.absoluteFile.parentFile.listFiles().orEmpty()
            .single { it.extension == "kmeta" && it.name != EVENTS_FILE_NAME }
            .readText()
    )

    private companion object {

        const val EVENTS_FILE_NAME = "EVENTS.kmeta"

        /** Arrays of primitive values by the name of their element type, which converts case-insensitively. */
        val PRIMITIVE_ARRAYS = listOf(
            "number" to "[1.5, 2, -3]",
            "Boolean" to "[true, false]",
            "string" to "[\"a\", 1, true]"
        )
    }
}
