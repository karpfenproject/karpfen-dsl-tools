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
package io.karpfen.dsl.meta

/**
 * A metamodel: the class type definitions and the root class type that models of it start from.
 *
 * @property types All class type definitions of the metamodel.
 * @property rootClass The class type of the root object of a model.
 */
class Metamodel(val types: MutableList<ClassType>, val rootClass: ClassType) {

    /**
     * Returns the class type named [name], or null when the metamodel has none of that name.
     */
    fun getTypeByName(name: String): ClassType? {
        return types.firstOrNull { it.name == name }
    }

    /**
     * Returns the names of all class types of the metamodel.
     */
    fun getClassTypes(): Set<String> {
        return types.map { it.name }.toSet()
    }

    /**
     * Returns all class types keyed by their name.
     */
    fun typesAsMap(): Map<String, ClassType> {
        return types.associateBy { it.name }
    }
}
