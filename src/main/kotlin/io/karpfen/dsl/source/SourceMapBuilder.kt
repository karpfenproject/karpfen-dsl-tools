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
package io.karpfen.dsl.source

import java.util.IdentityHashMap

/**
 * Collects the locations of the elements of one model while a reader builds it, and turns them into a
 * [SourceMap].
 *
 * A builder belongs to a single read and is not thread-safe.
 */
internal class SourceMapBuilder {

    private val locations = IdentityHashMap<Any, MutableList<SourceLocation>>()

    /**
     * Adds [location] to the locations of [element]; an element that several statements define gets one per
     * statement, in the order they are recorded.
     */
    fun record(element: Any, location: SourceLocation) {
        locations.getOrPut(element) { mutableListOf() }.add(location)
    }

    /**
     * Returns the first location recorded for [element], or null.
     */
    fun locationOf(element: Any): SourceLocation? = locations[element]?.first()

    /**
     * Removes [element] and its locations, for an element the reader drops from the model after recording it.
     */
    fun forget(element: Any) {
        locations.remove(element)
    }

    /**
     * Returns an immutable [SourceMap] of the locations recorded so far.
     */
    fun build(): SourceMap {
        val snapshot = IdentityHashMap<Any, List<SourceLocation>>()
        for ((element, elementLocations) in locations) {
            snapshot[element] = elementLocations.toList()
        }
        return SourceMap(snapshot)
    }
}
