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

import io.karpfen.dsl.instance.DataObject
import io.karpfen.dsl.instance.Model
import io.karpfen.dsl.meta.Metamodel
import io.karpfen.dsl.printer.kmodel.KmodelPrinter
import io.karpfen.dsl.reader.InvalidTextException
import io.karpfen.dsl.reader.kmodel.KmodelReader
import java.io.File

/**
 * Converts KModel text and files into [Model]s that conform to a [Metamodel], and models back into text.
 *
 * The converter is the throwing form of [KmodelReader], for a text that is expected to be right: it reads through the
 * reader, so every rule of KModel holds here as there, and returns the model of a text without errors or throws an
 * [InvalidTextException] with all errors of the text. Warnings do not make it throw. A tool that shows the problems
 * of a text, or that must go on with a text that has errors, uses the reader.
 */
object KmodelDSLConverter {

    /**
     * Reads KModel text into a [Model] whose `knows` references are resolved, see [KmodelReader].
     *
     * @param content The KModel content as a string.
     * @param meta The metamodel the model must conform to.
     * @return The model; its objects hold the root object.
     * @throws InvalidTextException If the text has at least one error, such as a syntax error, a class type,
     * property or relation the metamodel does not define, or a `knows` reference to an object that does not exist.
     */
    fun parseKmodelString(content: String, meta: Metamodel): Model = KmodelReader(meta).read(content).modelOrThrow()

    /**
     * Reads a KModel text that is a single object, one `make object` block, and returns that object.
     *
     * A KModel text is always one object with the objects embedded in it, so the text is read exactly as
     * [parseKmodelString] reads it, with the same rules, and the root object of the model is returned. This is the
     * KModel counterpart of [JsonToModelConverter.toDataObject], meant for small, self-contained snippets such as an
     * event payload written in KModel. The snippet cannot use `knows` references into a surrounding model, since a
     * reference to an object outside the snippet is an error like in any other text.
     *
     * @param content The KModel snippet as a string.
     * @param meta The metamodel the snippet must conform to.
     * @return The root object of the snippet.
     * @throws InvalidTextException If the snippet has at least one error, as for [parseKmodelString].
     */
    fun parseSingleObject(content: String, meta: Metamodel): DataObject = parseKmodelString(content, meta).objects.single()

    /**
     * Reads the KModel file at [filePath] into a [Model], see [parseKmodelString].
     *
     * @param filePath The path to the KModel file to read.
     * @param meta The metamodel the model must conform to.
     * @return The model.
     * @throws InvalidTextException If the text of the file has at least one error; its message names the file.
     */
    fun parseKmodelFile(filePath: String, meta: Metamodel): Model =
        KmodelReader(meta).read(File(filePath).readText()).modelOrThrow(filePath)

    /**
     * Converts a [Model] back into KModel text in the canonical layout, starting from its root object and
     * without comments; see [KmodelPrinter].
     *
     * @param model The model to convert.
     * @return The KModel text, ending with a line break.
     * @throws IllegalArgumentException If the model has no unique root object or a value cannot be written.
     */
    fun convertToKmodelString(model: Model): String = KmodelPrinter().print(model)
}
