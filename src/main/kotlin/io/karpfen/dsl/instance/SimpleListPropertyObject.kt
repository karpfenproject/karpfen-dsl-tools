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
 * A list-valued simple property of a [DataObject].
 *
 * @param ofType The simple property definition from the metamodel.
 * @param key The property name.
 * @param values The stored values: Strings, Doubles or Booleans.
 */
class SimpleListPropertyObject(
    ofType: SimpleProperty,
    key: String,
    private val values: MutableList<Any>
) : SimplePropertyObject(ofType, key) {

    override fun isList(): Boolean {
        return true
    }

    /**
     * Returns the values as booleans, unchecked.
     */
    fun getAsBooleanList(): List<Boolean> {
        return values as List<Boolean>
    }

    /**
     * Returns the values as strings, unchecked.
     */
    fun getAsStringList(): List<String> {
        return values as List<String>
    }

    /**
     * Returns the values as numbers, unchecked.
     */
    fun getAsNumberList(): List<Number> {
        return values as List<Number>
    }

    /**
     * Returns the live list of values.
     */
    fun getAsList(): List<Any> {
        return values
    }

    /**
     * Removes every occurrence of [value].
     */
    fun removeValue(value: Any) {
        values.removeIf { it == value }
    }

    /**
     * Removes all values, leaving an empty list.
     */
    fun clear() {
        values.clear()
    }

    /**
     * Returns the live list of values.
     */
    fun getValues(): List<Any> {
        return values
    }

    /**
     * Replaces all values with [newValues].
     *
     * @throws IllegalArgumentException If a value is not a Boolean, String or Double as the property type
     * requires; the values before it have been added by then.
     */
    fun setValues(newValues: List<Any>) {
        values.clear()
        for (value in newValues) {
            when (ofType.propertyType) {
                SimplePropertyType.BOOLEAN if value !is Boolean -> {
                    throw IllegalArgumentException("Expected a Boolean value")
                }
                SimplePropertyType.STRING if value !is String -> {
                    throw IllegalArgumentException("Expected a String value")
                }
                SimplePropertyType.NUMBER if value !is Double -> {
                    throw IllegalArgumentException("Expected a Number value")
                }
                else -> appendValue(value)
            }
        }
    }

    /**
     * Appends [value] to the list.
     *
     * @throws IllegalArgumentException If [value] is not a Boolean, String or Double as the property type requires.
     */
    fun appendValue(value: Any) {
        when (getValueType()) {
            SimplePropertyType.BOOLEAN -> {
                if (value !is Boolean) {
                    throw IllegalArgumentException("Expected a Boolean value")
                }
            }
            SimplePropertyType.STRING -> {
                if (value !is String) {
                    throw IllegalArgumentException("Expected a String value")
                }
            }
            SimplePropertyType.NUMBER -> {
                if (value !is Double) {
                    throw IllegalArgumentException("Expected a Number value")
                }
            }
        }
        values.add(value)
    }
}
