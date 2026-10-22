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
package io.karpfen.dsl.instance

import io.karpfen.dsl.meta.ClassTypeProperty

/**
 * Base class of the relation values of a [DataObject], either a single object or a list of objects.
 *
 * @property ofType The object property definition from the metamodel.
 * @property key The property name.
 */
abstract class ClassTypePropertyObject(val ofType: ClassTypeProperty, val key: String) {

    /**
     * Returns true if this relation holds a list of objects.
     */
    abstract fun isList(): Boolean

    /**
     * Returns the object property definition from the metamodel.
     */
    fun getPropertyType(): ClassTypeProperty {
        return ofType
    }

    /**
     * Returns the name of the class type the related objects must have.
     */
    fun getClassType(): String {
        return ofType.reference.classTypeName
    }
}
