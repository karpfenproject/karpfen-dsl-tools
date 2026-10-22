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
 * A single-valued simple property of a [DataObject].
 *
 * @param ofType The simple property definition from the metamodel.
 * @param key The property name.
 * @param value The stored value: a String, a Double or a Boolean.
 */
class SimpleAtomicPropertyObject(
    ofType: SimpleProperty,
    key: String,
    private var value: Any
) : SimplePropertyObject(ofType, key) {

    override fun isList(): Boolean {
        return false
    }

    /**
     * Returns the value of a boolean property.
     *
     * @throws IllegalStateException If the property is not of type [SimplePropertyType.BOOLEAN].
     */
    fun getAsBoolean(): Boolean {
        if (!isBoolean()) {
            throw IllegalStateException("Property is not of type BOOLEAN")
        }
        return value as Boolean
    }

    /**
     * Returns the value of a string property.
     *
     * @throws IllegalStateException If the property is not of type [SimplePropertyType.STRING].
     */
    fun getAsString(): String {
        if (!isString()) {
            throw IllegalStateException("Property is not of type STRING")
        }
        return value as String
    }

    /**
     * Returns the value of a number property.
     *
     * @throws IllegalStateException If the property is not of type [SimplePropertyType.NUMBER].
     */
    fun getAsNumber(): Number {
        if (!isNumber()) {
            throw IllegalStateException("Property is not of type NUMBER")
        }
        return value as Double
    }

    /**
     * Returns the stored value.
     */
    fun getValue(): Any {
        return value
    }

    /**
     * Replaces the stored value with [newValue].
     *
     * @throws IllegalArgumentException If [newValue] is not a Boolean, String or Double as the property type requires.
     */
    fun setValue(newValue: Any) {
        when (getValueType()) {
            SimplePropertyType.BOOLEAN -> {
                if (newValue !is Boolean) {
                    throw IllegalArgumentException("Expected a Boolean value")
                }
            }
            SimplePropertyType.STRING -> {
                if (newValue !is String) {
                    throw IllegalArgumentException("Expected a String value")
                }
            }
            SimplePropertyType.NUMBER -> {
                if (newValue !is Double) {
                    throw IllegalArgumentException("Expected a Number value")
                }
            }
        }
        this.value = newValue
    }
}
