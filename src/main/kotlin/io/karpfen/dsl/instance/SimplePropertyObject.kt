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

import io.karpfen.dsl.meta.SimpleProperty
import io.karpfen.dsl.meta.SimplePropertyType

/**
 * Base class of the simple (primitive) property values of a [DataObject], either a single value or a list.
 *
 * @property ofType The simple property definition from the metamodel.
 * @property key The property name.
 */
abstract class SimplePropertyObject(
    val ofType: SimpleProperty,
    val key: String
) {

    /**
     * Returns true if this property holds a list of values.
     */
    abstract fun isList(): Boolean

    /**
     * Returns the primitive type of this property.
     */
    fun getValueType(): SimplePropertyType {
        return ofType.propertyType
    }

    /**
     * Returns true if this property is of type [SimplePropertyType.BOOLEAN].
     */
    fun isBoolean(): Boolean {
        return getValueType() == SimplePropertyType.BOOLEAN
    }

    /**
     * Returns true if this property is of type [SimplePropertyType.STRING].
     */
    fun isString(): Boolean {
        return getValueType() == SimplePropertyType.STRING
    }

    /**
     * Returns true if this property is of type [SimplePropertyType.NUMBER].
     */
    fun isNumber(): Boolean {
        return getValueType() == SimplePropertyType.NUMBER
    }
}
