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
 * A side table from the elements of a model to where they are written in its text.
 *
 * The table is keyed by identity, not by equality: two equal data class instances, such as two identical
 * `SET` rules, have their own locations, and elements whose `hashCode` would not terminate, such as states
 * that point to their outer state, work as keys. The model classes themselves know nothing of their source.
 *
 * The readers of `io.karpfen.dsl.reader` record these elements; the name is the quoted string named here:
 * - KMeta: a [io.karpfen.dsl.meta.ClassType] (named by its type name), a
 *   [io.karpfen.dsl.meta.SimpleProperty] and a [io.karpfen.dsl.meta.ClassTypeProperty] (named by their key),
 *   and the [io.karpfen.dsl.meta.ClassTypeReference] of a relation (the `"T"` or `list("T")` part, named by
 *   the type name).
 * - KModel: a [io.karpfen.dsl.instance.DataObject] (named by its key), every `prop`, `has` and `knows`
 *   assignment as a [io.karpfen.dsl.instance.SimplePropertyObject] or
 *   [io.karpfen.dsl.instance.ClassTypePropertyObject] (named by its key; one location per statement when
 *   several statements form a list), and the [io.karpfen.dsl.instance.ObjectReference] of each `has` and
 *   `knows` statement (named by the id of the object it refers to).
 * - KStates: the [io.karpfen.dsl.states.StateMachine] (named by its class), every
 *   [io.karpfen.dsl.states.State], the `ENTRY` and `DO` blocks and the bodies of `IF IN SCOPE` and `WITH`
 *   blocks as [io.karpfen.dsl.states.actions.ActionBlock]s, every action item and the right side of every
 *   rule, every transition of the three kinds, every condition clause and the
 *   [io.karpfen.dsl.states.conditions.CompositeCondition] of a block with several clauses, and every
 *   [io.karpfen.dsl.states.Macro] with its takes and returns directives, its
 *   [io.karpfen.dsl.states.macros.Definition] and its [io.karpfen.dsl.states.macros.CodeBlock].
 *   A state, a macro and a takes directive are named by their name, a `WITH` block by the name it binds,
 *   a macro call by the macro, an `EVENT` clause by the event and a `VALUE` clause by its variable.
 *   Every `EVAL` body, held by an [io.karpfen.dsl.states.actions.EvalActionRightSide], an
 *   [io.karpfen.dsl.states.conditions.EvalCondition] or a [io.karpfen.dsl.states.macros.CodeBlock], has a
 *   [SourceLocation.codeRange].
 *
 * Elements the reader makes up instead of reading, such as the empty `ENTRY` block of a state without one or
 * the always-true condition of a transition without a `CONDITION` block, have no location. A source map is
 * immutable and may be shared between threads.
 */
class SourceMap internal constructor(locations: IdentityHashMap<Any, List<SourceLocation>>) {

    /** The locations of every element, keyed by identity. */
    private val locations = IdentityHashMap(locations)

    /** The number of elements that have a location. */
    val size: Int
        get() = locations.size

    /**
     * Returns the location of [element], the first one if several statements define it; null if the element
     * has no location in this map.
     */
    fun locationOf(element: Any): SourceLocation? = locations[element]?.first()

    /**
     * Returns every location of [element] in the order of the text: one for most elements, one per statement
     * for a KModel list property that several statements assign; empty if the element has no location.
     */
    fun locationsOf(element: Any): List<SourceLocation> = locations[element] ?: emptyList()

    /**
     * Returns the range of the construct that defines [element], see [SourceLocation.range].
     */
    fun rangeOf(element: Any): SourceRange? = locationOf(element)?.range

    /**
     * Returns the range of the name of [element], see [SourceLocation.nameRange].
     */
    fun nameRangeOf(element: Any): SourceRange? = locationOf(element)?.nameRange

    /**
     * Returns the inner range of the `EVAL` body [element] holds, see [SourceLocation.codeRange].
     */
    fun codeRangeOf(element: Any): SourceRange? = locationOf(element)?.codeRange

    /** Holds the map without any location. */
    companion object {

        /** A source map without any location. */
        val EMPTY: SourceMap = SourceMap(IdentityHashMap())
    }
}
