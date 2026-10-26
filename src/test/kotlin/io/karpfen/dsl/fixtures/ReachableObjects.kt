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
package io.karpfen.dsl.fixtures

import java.lang.reflect.Modifier
import java.util.IdentityHashMap

/**
 * Finds the objects of the library reachable from a root object through their fields, collections, maps and arrays,
 * such as every element of a model, which a source map may hold a location for.
 */
object ReachableObjects {

    /** The package of the classes whose objects are followed. */
    private const val LIBRARY_PACKAGE = "io.karpfen.dsl."

    /**
     * Returns every object of the library reachable from [root], each once, compared by identity.
     */
    fun from(root: Any?): List<Any> {
        val found = IdentityHashMap<Any, Unit>()
        val pending = ArrayDeque<Any?>(listOf(root))
        while (pending.isNotEmpty()) {
            when (val value = pending.removeLast()) {
                null, is String, is Number, is Boolean, is Char, is Enum<*> -> Unit
                is Map<*, *> -> pending.addAll(value.keys + value.values)
                is Iterable<*> -> pending.addAll(value)
                is Array<*> -> pending.addAll(value)
                else -> if (value.javaClass.name.startsWith(LIBRARY_PACKAGE) && found.put(value, Unit) == null) {
                    pending.addAll(fieldValuesOf(value))
                }
            }
        }
        return found.keys.toList()
    }

    private fun fieldValuesOf(value: Any): List<Any?> =
        generateSequence(value.javaClass) { it.superclass }
            .takeWhile { it != Any::class.java }
            .flatMap { it.declaredFields.asSequence() }
            .filter { !Modifier.isStatic(it.modifiers) }
            .onEach { it.isAccessible = true }
            .map { it.get(value) }
            .toList()
}
