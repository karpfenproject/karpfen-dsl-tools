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
package io.karpfen.dsl.states

import io.karpfen.dsl.states.actions.ActionBlock

/**
 * Represents a single state in a hierarchical state machine.
 *
 * States can be nested and can define entry and do action blocks. A state can also carry a [history], a tick limit
 * made of a [delay] and a [timeout], and breakpoints for a debugger.
 *
 * @property name The name identifier for this state.
 * @property onEntry Actions executed when entering this state.
 * @property onDo Actions executed while in this state.
 * @property innerStates Child states nested within this state.
 * @property outerState Reference to the parent state, if this state is nested.
 * @property isInitial True if this is the initial state of the machine or parent state.
 * @property entryBreakpoint True if a breakpoint is attached to the entry of this state.
 * @property exitBreakpoint True if a breakpoint is attached to the exit of this state.
 * @property history What the state remembers about its inner states while it is not active.
 * @property delay The number of ticks the state stays active at least, or null without a `DELAY`.
 * @property timeout The number of ticks after which the state is left for [timeoutTo], or null without a `TIMEOUT`.
 * @property timeoutTo The name of the state a `TIMEOUT` leads to, or null without a `TIMEOUT`.
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
        "State(name=$name, isInitial=$isInitial, entryBreakpoint=$entryBreakpoint, exitBreakpoint=$exitBreakpoint, " +
            "history=$history, delay=$delay, timeout=$timeout, timeoutTo=$timeoutTo, innerStates=${innerStates.size}, " +
            "entryActions=${onEntry.items.size}, doActions=${onDo.items.size})"
}
