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
 * The primitive types a simple property of a metamodel can have.
 *
 * @property keyword the name by which KMeta writes the type in a `prop`, such as `number`.
 */
enum class SimplePropertyType(val keyword: String) {
    /** A boolean value, true or false. */
    BOOLEAN("boolean"),

    /** A numeric value, stored as a double. */
    NUMBER("number"),

    /** A text value. */
    STRING("string")
}
