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
package io.karpfen.dsl.printer.kmodel

import io.karpfen.dsl.instance.DataObject
import io.karpfen.dsl.parser.KmodelSemantics

/**
 * The objects that one KModel document or fragment writes: its root object and every object that one embeds,
 * directly or through others, in the order the text writes them.
 *
 * A `knows` statement refers to an object by its id, and reading resolves the id among the objects of the whole
 * document, where the last object of an id wins, see [KmodelSemantics.referableById]. A document therefore has to
 * write every object it links to, each as the last object of its id. A fragment stands in a document whose other
 * objects it does not know, so it may also link to objects it does not write.
 *
 * @property objects The objects, in the order the text writes them.
 * @property isDocument True for a whole document, false for a fragment.
 */
internal class WrittenObjects(val objects: Set<DataObject>, val isDocument: Boolean) {

    /** The objects of [objects] that a `knows` statement refers to by their ids. */
    private val referableById: Map<String, DataObject> = KmodelSemantics.referableById(objects)

    /**
     * Returns true if the text writes [obj].
     */
    operator fun contains(obj: DataObject): Boolean = obj in objects

    /**
     * Returns true if the id of [obj], an object the text writes, refers to [obj] and not to a later object of the same
     * id.
     */
    fun isReferableById(obj: DataObject): Boolean = referableById[obj.id] === obj
}
