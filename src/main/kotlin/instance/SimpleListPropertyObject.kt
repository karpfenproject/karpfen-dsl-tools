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
package instance

import meta.SimpleProperty
import meta.SimplePropertyType

class SimpleListPropertyObject(
    ofType: SimpleProperty,
    key: String,
    private val values: MutableList<Any>
) : SimplePropertyObject(ofType, key) {

    override fun isList(): Boolean {
        return true
    }

    fun getAsBooleanList(): List<Boolean> {
        return values as List<Boolean>
    }

    fun getAsStringList(): List<String> {
        return values as List<String>
    }

    fun getAsNumberList(): List<Number> {
        return values as List<Number>
    }

    fun getAsList(): List<Any> {
        return values
    }

    fun removeValue(value: Any) {
        values.removeIf { it == value }
    }

    fun getValues(): List<Any> {
        return values
    }

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

    fun appendValue(value: Any) {
        when (getValueType()) {
            meta.SimplePropertyType.BOOLEAN -> {
                if (value !is Boolean) {
                    throw IllegalArgumentException("Expected a Boolean value")
                }
            }
            meta.SimplePropertyType.STRING -> {
                if (value !is String) {
                    throw IllegalArgumentException("Expected a String value")
                }
            }
            meta.SimplePropertyType.NUMBER -> {
                if (value !is Double) {
                    throw IllegalArgumentException("Expected a Number value")
                }
            }
        }
        values.add(value)
    }

}