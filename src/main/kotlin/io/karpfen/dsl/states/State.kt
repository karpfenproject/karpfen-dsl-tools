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
 * Two kinds of states serve the composition of a state machine from fragments. A container state, `CONTAINER STATE
 * "name" USE "fragment"`, names in [fragment] the fragment whose states run inside it; it has no actions of its own.
 * In the text it has no inner states. After [io.karpfen.dsl.composition.StateMachineFlattener] resolved it, it holds
 * copies of the states of the fragment as its inner states and keeps [fragment], so that a runtime can still tell that
 * entering it goes down to [initialInnerState]. A final state, `FINAL STATE "name"`, ends a fragment: once the active
 * state inside a container is final, the condition `DONE` of a transition that leaves the container holds.
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
 * @property fragment The name of the fragment a container state uses, the empty string for a container state whose
 * text lacks it; null for every state that is no container state.
 * @property isFinal True for a final state, `FINAL STATE`.
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
    val fragment: String? = null,
    val isFinal: Boolean = false,
) {

    /** True for a container state, `CONTAINER STATE "name" USE "fragment"`, see [fragment]. */
    val isContainer: Boolean
        get() = fragment != null

    /**
     * The inner state that is entered first when this state is entered and goes down, as the runtime builds a start
     * stack: the inner state marked `INITIAL`, otherwise the first inner state; null for a state without inner states.
     * For a flattened container state this is the copy of the state its fragment starts in, when the fragment marks a
     * state at its top level as `INITIAL`.
     */
    val initialInnerState: State?
        get() = innerStates.firstOrNull { it.isInitial } ?: innerStates.firstOrNull()
    override fun toString(): String =
        "State(name=$name, isInitial=$isInitial, entryBreakpoint=$entryBreakpoint, exitBreakpoint=$exitBreakpoint, " +
            "history=$history, delay=$delay, timeout=$timeout, timeoutTo=$timeoutTo, " +
            (fragment?.let { "fragment=$it, " } ?: "") + (if (isFinal) "isFinal=true, " else "") +
            "innerStates=${innerStates.size}, " +
            "entryActions=${onEntry.items.size}, doActions=${onDo.items.size})"
}
