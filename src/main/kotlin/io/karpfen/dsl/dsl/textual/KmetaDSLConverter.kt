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
package io.karpfen.dsl.dsl.textual

import io.karpfen.dsl.meta.ClassType
import io.karpfen.dsl.meta.Metamodel
import io.karpfen.dsl.printer.kmeta.KmetaPrinter
import io.karpfen.dsl.reader.InvalidTextException
import io.karpfen.dsl.reader.kmeta.KmetaReader
import java.io.File

/**
 * Converts KMeta text and files into [Metamodel]s, and metamodels back into text.
 *
 * The converter is the throwing form of [KmetaReader], for a text that is expected to be right: it reads through the
 * reader, so every rule of KMeta holds here as there, and returns the metamodel of a text without errors or throws an
 * [InvalidTextException] with all errors of the text. Warnings do not make it throw. A tool that shows the problems
 * of a text, or that must go on with a text that has errors, uses the reader.
 */
object KmetaDSLConverter {

    /**
     * Reads KMeta text into a [Metamodel], see [KmetaReader].
     *
     * The last type defined in [content] becomes the root class type of the metamodel. [baseTypes] are class types
     * that are already known from another metamodel and may be referenced by the types in this file but are not
     * redefined here. This is what lets an EVENTS.kmeta describe event payloads that embed or link to domain types,
     * such as an event carrying a `Vector`, without duplicating those definitions. The base types are also included
     * in the resulting metamodel, after the types of the text, so that nested references resolve when the payload is
     * later turned into objects.
     *
     * @param content The KMeta content as a string.
     * @param baseTypes Already-resolved class types that may be referenced but are not defined here.
     * @return The metamodel.
     * @throws InvalidTextException If the text has at least one error, such as a syntax error, a property of an
     * unknown primitive type, a relation to an unknown class type, a type defined twice, or no type at all.
     */
    fun parseKmetaString(content: String, baseTypes: List<ClassType> = emptyList()): Metamodel =
        KmetaReader(baseTypes).read(content).modelOrThrow()

    /**
     * Reads the KMeta file at [filePath] into a [Metamodel] without base types, see [parseKmetaString].
     *
     * @param filePath The path to the KMeta file to read.
     * @return The metamodel.
     * @throws InvalidTextException If the text of the file has at least one error; its message names the file.
     */
    fun parseKmetaFile(filePath: String): Metamodel = KmetaReader().read(File(filePath).readText()).modelOrThrow(filePath)

    /**
     * Converts a [Metamodel] back into KMeta text in the canonical layout, with all its class types, the root
     * class type last, and without comments; see [KmetaPrinter], which can also leave out base types.
     *
     * @param metamodel The metamodel to convert.
     * @return The KMeta text, ending with a line break.
     * @throws IllegalArgumentException If a name or a description cannot be written as a KMeta string literal.
     */
    fun convertToKmetaString(metamodel: Metamodel): String = KmetaPrinter().print(metamodel)
}
