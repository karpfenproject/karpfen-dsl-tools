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

import states.conditions.Condition

/**
 * Represents a transition between two states in a state machine.
 *
 * Transitions are triggered when their associated condition evaluates to true.
 *
 * @property fromState The name of the source state.
 * @property toState The name of the target state.
 * @property allowLoops True if this transition is allowed to loop back to the same state.
 * @property startBreakpoint True if a breakpoint is attached to this transition's start.
 * @property endBreakpoint True if a breakpoint is attached to this transition's end.
 * @property condition The condition that must be satisfied for this transition to occur.
 */
data class Transition(
    val fromState: String,
    val toState: String,
    override val allowLoops: Boolean,
    override val startBreakpoint: Boolean = false,
    override val endBreakpoint: Boolean = false,
    override val condition: Condition,
) : TransitionLike {
    override fun toString(): String =
        "Transition(fromState=$fromState, toState=$toState, allowLoops=$allowLoops, startBreakpoint=$startBreakpoint, endBreakpoint=$endBreakpoint, condition=${condition.conditionType})"
}
