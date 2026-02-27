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

class SimpleAtomicPropertyObject(
    ofType: SimpleProperty,
    key: String,
    private var value: Any
) : SimplePropertyObject(ofType, key) {

    override fun isList(): Boolean {
        return false
    }

    fun getAsBoolean(): Boolean {
        if (!isBoolean()) {
            throw IllegalStateException("Property is not of type BOOLEAN")
        }
        return value as Boolean
    }

    fun getAsString(): String {
        if (!isString()) {
            throw IllegalStateException("Property is not of type STRING")
        }
        return value as String
    }

    fun getAsNumber(): Number {
        if (!isNumber()) {
            throw IllegalStateException("Property is not of type NUMBER")
        }
        return value as Double
    }

    fun getValue(): Any {
        return value
    }

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