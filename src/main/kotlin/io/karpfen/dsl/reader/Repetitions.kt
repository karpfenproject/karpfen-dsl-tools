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
package io.karpfen.dsl.reader

/**
 * An element whose name an earlier element of the same list already has.
 *
 * @param T The type of the elements.
 * @property element The later element.
 * @property first The first element of the name.
 */
internal class Repetition<T>(val element: T, val first: T)

/**
 * Returns the elements of this list whose name, as [nameOf] gives it, an earlier element has, each with the first
 * element of that name, in the order of the list.
 */
internal fun <T> List<T>.repetitionsBy(nameOf: (T) -> String): List<Repetition<T>> {
    val firstByName = mutableMapOf<String, T>()
    return mapNotNull { element ->
        val name = nameOf(element)
        val first = firstByName[name]
        if (first == null) {
            firstByName[name] = element
            null
        } else {
            Repetition(element, first)
        }
    }
}
