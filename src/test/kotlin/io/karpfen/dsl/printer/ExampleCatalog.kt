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
package io.karpfen.dsl.printer

import io.karpfen.dsl.dsl.textual.KmetaDSLConverter
import io.karpfen.dsl.dsl.textual.KmodelDSLConverter
import io.karpfen.dsl.dsl.textual.KstatesDSLConverter
import io.karpfen.dsl.instance.Model
import io.karpfen.dsl.meta.ClassType
import io.karpfen.dsl.meta.Metamodel
import io.karpfen.dsl.states.StateMachine
import java.io.File

/**
 * The example documents the printer tests run on, found by walking [roots].
 *
 * The roots are the examples of this repository in `example/` and the copies of the karpfen runtime examples
 * in `src/test/resources/examples`. Each folder holds at most one domain metamodel; an `EVENTS.kmeta` next to
 * it is read with the domain types as base types, and every `.kmodel` conforms to the domain metamodel.
 *
 * @param roots The folders to walk, relative to the project folder the tests run in.
 */
class ExampleCatalog(private val roots: List<File> = listOf(File("example"), File("src/test/resources/examples"))) {

    /**
     * Returns every example file below [roots], with the extension `.kmeta`, `.kmodel` or `.kstates`.
     */
    fun allFiles(): List<File> = roots.flatMap { root ->
        root.walkTopDown().filter { it.isFile && it.extension in EXTENSIONS }.sortedBy { it.path }.toList()
    }

    /**
     * Returns every KMeta example, the event metamodels with the types of their domain metamodel as base types.
     */
    fun metamodels(): List<MetamodelExample> = folders().flatMap { folder ->
        val domain = folder.domainMetamodelFile?.let { MetamodelExample(it, emptyList()) }
        val events = folder.eventsFile?.let { MetamodelExample(it, folder.domainMetamodel().types) }
        listOfNotNull(domain, events)
    }

    /**
     * Returns every KModel example with the domain metamodel of its folder.
     */
    fun models(): List<ModelExample> = folders().flatMap { folder ->
        folder.modelFiles.map { ModelExample(it, folder.domainMetamodel()) }
    }

    /**
     * Returns every KStates example.
     */
    fun stateMachines(): List<StateMachineExample> =
        allFiles().filter { it.extension == "kstates" }.map { StateMachineExample(it) }

    private fun folders(): List<ExampleFolder> =
        allFiles().groupBy { it.parentFile }.map { (directory, files) -> ExampleFolder(directory, files) }

    /**
     * The example files of one folder.
     */
    private class ExampleFolder(private val directory: File, files: List<File>) {

        /** The KMeta file of the domain metamodel, if the folder has one. */
        val domainMetamodelFile: File? =
            files.filter { it.extension == "kmeta" && it.name != EVENTS_FILE_NAME }.let {
                check(it.size <= 1) { "The folder $directory holds more than one domain metamodel: $it" }
                it.singleOrNull()
            }

        /** The KMeta file of the event metamodel, if the folder has one. */
        val eventsFile: File? = files.singleOrNull { it.name == EVENTS_FILE_NAME }

        /** The KModel files of the folder. */
        val modelFiles: List<File> = files.filter { it.extension == "kmodel" }

        /**
         * Returns the parsed domain metamodel.
         */
        fun domainMetamodel(): Metamodel {
            val file = checkNotNull(domainMetamodelFile) { "The folder $directory has no domain metamodel." }
            return KmetaDSLConverter.parseKmetaFile(file.path)
        }
    }

    private companion object {
        val EXTENSIONS = setOf("kmeta", "kmodel", "kstates")
        const val EVENTS_FILE_NAME = "EVENTS.kmeta"
    }
}

/**
 * A KMeta example and the base types it is read with.
 *
 * @property file The example file.
 * @property baseTypes The types of the domain metamodel for an event metamodel, otherwise none.
 */
class MetamodelExample(val file: File, val baseTypes: List<ClassType>) {

    /** The text of the example file. */
    val text: String = file.readText()

    /**
     * Parses [content] with [baseTypes].
     */
    fun parse(content: String = text): Metamodel = KmetaDSLConverter.parseKmetaString(content, baseTypes)

    override fun toString(): String = file.path
}

/**
 * A KModel example and the metamodel it conforms to.
 *
 * @property file The example file.
 * @property metamodel The domain metamodel of its folder.
 */
class ModelExample(val file: File, val metamodel: Metamodel) {

    /** The text of the example file. */
    val text: String = file.readText()

    /**
     * Parses [content] against [metamodel].
     */
    fun parse(content: String = text): Model = KmodelDSLConverter.parseKmodelString(content, metamodel)

    override fun toString(): String = file.path
}

/**
 * A KStates example.
 *
 * @property file The example file.
 */
class StateMachineExample(val file: File) {

    /** The text of the example file. */
    val text: String = file.readText()

    /**
     * Parses [content].
     */
    fun parse(content: String = text): StateMachine = KstatesDSLConverter.parseKstatesString(content)

    override fun toString(): String = file.path
}
