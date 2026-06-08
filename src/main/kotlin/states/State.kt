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
package states

import states.actions.ActionBlock

/**
 * Represents a single state in a hierarchical state machine.
 *
 * States can be nested and can define entry and do action blocks.
 *
 * @property name The name identifier for this state.
 * @property onEntry Actions executed when entering this state.
 * @property onDo Actions executed while in this state.
 * @property innerStates Child states nested within this state.
 * @property outerState Reference to the parent state, if this state is nested.
 * @property isInitial True if this is the initial state of the machine or parent state.
 */
data class State(
    val name: String,
    val onEntry: ActionBlock,
    val onDo: ActionBlock,
    val innerStates: MutableList<State>,
    val outerState: State? = null,
    val isInitial: Boolean = false
) {
    override fun toString(): String =
        "State(name=$name, isInitial=$isInitial, innerStates=${innerStates.size}, entryActions=${onEntry.items.size}, doActions=${onDo.items.size})"
}
