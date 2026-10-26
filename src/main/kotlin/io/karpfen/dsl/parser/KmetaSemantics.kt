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
package io.karpfen.dsl.parser

import io.karpfen.dsl.meta.ClassType
import io.karpfen.dsl.meta.Metamodel

/**
 * How the types of a KMeta text make up a metamodel, which the converter and the reader share.
 */
internal object KmetaSemantics {

    /**
     * Returns the metamodel of [textTypes], the types a KMeta text defines in their order, and of [baseTypes], the
     * types defined elsewhere that the text may refer to: it holds the types of the text followed by the base types,
     * and the last type of the text is its root class type.
     *
     * @throws NoSuchElementException If [textTypes] is empty, as a metamodel needs a type of its own as root.
     */
    fun metamodelOf(textTypes: List<ClassType>, baseTypes: List<ClassType>): Metamodel =
        Metamodel((textTypes + baseTypes).toMutableList(), textTypes.last())
}
