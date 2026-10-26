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
 * Kind of association between a class type and the class type one of its object properties points to.
 *
 * @property keyword the word by which KMeta declares a relation of this kind, and KModel writes it, such as `has`.
 */
enum class AssociationType(val keyword: String) {
    /** A reference to an object that lives elsewhere in the model and is found by its id, written as `knows`. */
    LINK("knows"),

    /** An object contained in the owning object, written as `has`. */
    EMBEDDED("has")
}
