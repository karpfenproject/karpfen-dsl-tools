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

import java.lang.reflect.Field
import java.lang.reflect.Modifier
import java.util.IdentityHashMap

/**
 * Writes the structure of an object graph as indented text, by reflection over its fields.
 *
 * Objects are written with their simple class name and their fields in a fixed order, so that two graphs made
 * of different classes with the same simple names and fields, such as the models of version 1.0 and of the
 * current version, which differ only in their packages, yield the same text if their structure is the same.
 * An object met a second time is written as a reference to its first occurrence, which makes cycles, such as
 * a state and its outer state, and shared objects, such as resolved class types, part of the comparison.
 */
class StructureDump private constructor() {

    private val ids = IdentityHashMap<Any, Int>()

    private val text = StringBuilder()

    private fun write(value: Any?, depth: Int) {
        when (value) {
            null -> text.append("null")
            is String -> text.append('"').append(escaped(value)).append('"')
            is Number, is Boolean, is Char -> text.append(value.javaClass.simpleName).append(':').append(value)
            is Enum<*> -> text.append(value.declaringJavaClass.simpleName).append('.').append(value.name)
            is Map<*, *> -> writeElements(value.entries.map { listOf(it.key, it.value) }, depth)
            is Iterable<*> -> writeElements(value.toList(), depth)
            is Array<*> -> writeElements(value.toList(), depth)
            else -> writeObject(value, depth)
        }
    }

    private fun writeElements(elements: List<Any?>, depth: Int) {
        text.append('[')
        for (element in elements) {
            newLine(depth + 1)
            write(element, depth + 1)
        }
        if (elements.isNotEmpty()) {
            newLine(depth)
        }
        text.append(']')
    }

    private fun writeObject(value: Any, depth: Int) {
        val knownId = ids[value]
        if (knownId != null) {
            text.append('@').append(knownId)
            return
        }
        val id = ids.size
        ids[value] = id
        text.append(value.javaClass.simpleName).append('#').append(id).append(" {")
        for (field in fieldsOf(value.javaClass)) {
            newLine(depth + 1)
            text.append(field.name).append(" = ")
            field.isAccessible = true
            write(field.get(value), depth + 1)
        }
        newLine(depth)
        text.append('}')
    }

    private fun newLine(depth: Int) {
        text.append('\n').append("  ".repeat(depth))
    }

    /** Writes structures. */
    companion object {

        /**
         * Returns the structure of the graph reachable from [root].
         */
        fun of(root: Any?): String = StructureDump().apply { write(root, 0) }.text.toString()

        /**
         * Returns the instance fields of [type] and its superclasses, the fields of a superclass first and the
         * fields of one class sorted by name.
         */
        private fun fieldsOf(type: Class<*>): List<Field> =
            generateSequence(type) { it.superclass }
                .takeWhile { it != Any::class.java }
                .toList()
                .reversed()
                .flatMap { declaring ->
                    declaring.declaredFields
                        .filter { !Modifier.isStatic(it.modifiers) && !it.isSynthetic }
                        .sortedBy { it.name }
                }

        private fun escaped(value: String): String =
            value.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r")
    }
}
