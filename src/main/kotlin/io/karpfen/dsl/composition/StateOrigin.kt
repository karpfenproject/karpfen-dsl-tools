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

/**
 * Where a state of a flattened state machine was written: in the state machine the composition starts from, or in a
 * fragment that a chain of container states uses.
 *
 * The state `self test/wiring/lit` was written as `lit` in the fragment that the container state `self test/wiring`
 * uses, which stands inside the container state `self test` of the starting state machine; its origin has the
 * fragment of `self test/wiring`, the local name `lit` and the containers `self test` and `self test/wiring`.
 *
 * @property fragment The name of the fragment the state was written in, or null for a state of the state machine the
 * composition starts from.
 * @property localName The name of the state in the text it was written in.
 * @property containers The flattened names of the container states above the state, the outermost first; empty for a
 * state of the starting state machine. The last one is the container whose fragment holds the state.
 */
data class StateOrigin(
    val fragment: String?,
    val localName: String,
    val containers: List<String>
) {

    /** The name of the state in the flattened state machine: the last container, a slash and the local name. */
    val flattenedName: String
        get() = containers.lastOrNull()?.let { it + StateMachineFlattener.NAME_SEPARATOR + localName } ?: localName
}
