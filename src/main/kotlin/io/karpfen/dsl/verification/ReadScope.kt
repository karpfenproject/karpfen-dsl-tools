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
package io.karpfen.dsl.verification

import io.karpfen.dsl.meta.ClassType

/**
 * The name a `WITH` block binds to the result of its macro.
 *
 * @property name The name.
 * @property type The type the macro returns, or null if it is unknown.
 */
internal class Binding(val name: String, val type: MacroType?)

/**
 * What a path read at some place of a state machine can start from, besides the object the machine is attached to.
 *
 * @property events The names of the events that can be in scope there, or null if they cannot be told.
 * @property bindings The bindings of the `WITH` blocks around the place, the outermost first.
 * @property parameters The parameters of the macro whose code holds the place, with their types where known, or null
 * outside the code of a macro.
 * @property guards The paths of the `IF IN SCOPE` blocks around the place.
 */
internal class ReadScope(
    val events: Set<String>?,
    val bindings: List<Binding> = emptyList(),
    val parameters: Map<String, MacroType?>? = null,
    val guards: List<String> = emptyList()
) {

    /**
     * Returns true if an `IF IN SCOPE` block around the place makes sure that [path] can be read: a guard that the
     * path starts with, or a guard on the event for a path on the event.
     */
    fun isGuarded(path: String): Boolean {
        val segments = TypePaths.segmentsOf(path)
        return guards.map { TypePaths.segmentsOf(it) }.any { guard ->
            (guard.first() == EVENT_ROOT && segments.first() == EVENT_ROOT) ||
                (guard.size <= segments.size && segments.subList(0, guard.size) == guard)
        }
    }

    companion object {

        /** The name under which code reads the event in scope. */
        const val EVENT_ROOT = "event"
    }
}

/**
 * What a path read at some place refers to.
 */
internal sealed interface Read {

    /** A path from the object the state machine is attached to, followed to [resolution]. */
    class OfContext(val resolution: Resolution) : Read

    /** A path from a binding of a `WITH` block, followed to [resolution]. */
    class OfBinding(val resolution: Resolution) : Read

    /** A path from a parameter of a macro, followed to [resolution]. */
    class OfParameter(val resolution: Resolution) : Read

    /** A path from the event in scope, with the segments [rest] after `event`. */
    class OfEvent(val rest: List<String>) : Read

    /** A local variable of the code of a macro, which the runtime leaves to Python. */
    object Local : Read
}

/**
 * Finds what a path read at some place refers to, the way the runtime resolves it: in the code of a macro, a path
 * starts from a parameter or names a local variable; elsewhere it starts from the innermost binding of its first
 * segment, from the event in scope if the first segment is `event`, or from the object the state machine is attached
 * to.
 *
 * @param contextType The type of the object the state machine is attached to, or null if it is unknown.
 */
internal class ReadResolver(private val contextType: ClassType?) {

    /** Returns what [path], read with [scope], refers to. */
    fun resolve(path: String, scope: ReadScope): Read {
        val segments = TypePaths.segmentsOf(path)
        val root = segments.first()
        val rest = segments.drop(1)
        scope.parameters?.let { parameters ->
            if (root !in parameters) {
                return Read.Local
            }
            val type = parameters[root] ?: return Read.OfParameter(Resolution.Undecided)
            return Read.OfParameter(TypePaths.resolveFrom(root, type, rest, Navigation.DICT))
        }
        scope.bindings.lastOrNull { it.name == root }?.let { binding ->
            val type = binding.type ?: return Read.OfBinding(Resolution.Undecided)
            return Read.OfBinding(TypePaths.resolveFrom(root, type, rest, Navigation.MODEL))
        }
        if (root == ReadScope.EVENT_ROOT) {
            return Read.OfEvent(rest)
        }
        val type = contextType ?: return Read.OfContext(Resolution.Undecided)
        return Read.OfContext(TypePaths.resolve(type, segments, Navigation.MODEL))
    }
}
