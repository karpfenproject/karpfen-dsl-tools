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
 * @property entryBreakpoint True if a breakpoint is attached to this state's entry point.
 * @property exitBreakpoint True if a breakpoint is attached to this state's exit point.
 * @property history Specifies what kind of history is attached to this state.
 * @property delay The minimum tick amount a state has to stay entered.
 * @property timeout The maximum tick amount a state can stay entered.
 */
data class State(
    val name: String,
    val onEntry: ActionBlock,
    val onDo: ActionBlock,
    val innerStates: MutableList<State>,
    val outerState: State? = null,
    val isInitial: Boolean = false,
    val entryBreakpoint: Boolean = false,
    val exitBreakpoint: Boolean = false,
    val history: History = History.NONE,
    val delay: Long? = null,
    val timeout: Long? = null,
    val timeoutTo: String? = null,
) {
    override fun toString(): String =
        "State(name=$name, isInitial=$isInitial, entryBreakpoint=$entryBreakpoint, exitBreakpoint=$exitBreakpoint, historyState=${history}, delay=${delay}, timeout=${timeout}, timeoutTo=${timeoutTo} innerStates=${innerStates.size}, entryActions=${onEntry.items.size}, doActions=${onDo.items.size})"
}

enum class History {
    NONE, //No history attached
    SHALLOW, //Shallow history attached (only considers top level of substates)
    DEEP //Deep history attached (considers every state down to the deepest substate)
}