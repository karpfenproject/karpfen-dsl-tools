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
package io.karpfen.dsl.composition

import io.karpfen.dsl.states.State
import io.karpfen.dsl.states.StateMachine
import java.util.IdentityHashMap

/**
 * What [StateMachineFlattener.flatten] made of a composition: the flat state machine, or the problems that prevent it.
 */
sealed interface FlatteningResult {

    /**
     * The composition flattened into [machine].
     *
     * @property machine The flat state machine. It has the header of the state machine the composition starts from,
     * the states of every fragment copied into the container states that use them, the transitions of all files with
     * the names of the copies, and the one set of macros of all files. Nothing in it is shared with the input, and
     * nothing is shared between two copies of a fragment, except the macros, which are in the set once.
     * @property origins Where each state of [machine] was written, by its flattened name, in the order of the states,
     * each before its inner states.
     * @property sources The state machines of the composition: the one it starts from first, then each fragment once,
     * in the order they are first used.
     */
    class Flattened internal constructor(
        val machine: StateMachine,
        val origins: Map<String, StateOrigin>,
        val sources: List<StateMachine>,
        internal val elementSources: IdentityHashMap<Any, ElementSource>
    ) : FlatteningResult {

        /** Returns where the state named [flattenedName] was written, or null if [machine] has no state of the name. */
        fun originOf(flattenedName: String): StateOrigin? = origins[flattenedName]

        /** Returns where [state], a state of [machine], was written, or null for a state of another machine. */
        fun originOf(state: State): StateOrigin? = origins[state.name]?.takeIf { elementSources.containsKey(state) }
    }

    /**
     * The composition could not be flattened.
     *
     * @property problems Every problem found, each once, in the order they were found; never empty.
     */
    class NotFlattened internal constructor(val problems: List<FlatteningProblem>) : FlatteningResult
}

/**
 * The element of an input state machine that an element of a flattened state machine copies.
 *
 * @property original The element as the input holds it, which the source map of its read knows.
 * @property machine The input state machine whose text holds [original].
 */
internal class ElementSource(val original: Any, val machine: StateMachine)
