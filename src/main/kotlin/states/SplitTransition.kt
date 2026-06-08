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
 * A 1-to-N transition that fans a single state out into several parallel states.
 *
 * When it fires, the execution context leaves [fromState] and enters one parallel branch per entry in
 * [toStates] (so [toStates] must hold at least two targets). A split may only fire while the context is
 * not already parallel.
 *
 * @property fromState The source state.
 * @property toStates The target states; one parallel branch is created for each.
 * @property allowLoops False if this transition must not fire twice in a row.
 * @property condition The condition that must hold for this transition to fire.
 */
data class SplitTransition(
    val fromState: String,
    val toStates: List<String>,
    override val allowLoops: Boolean,
    override val condition: Condition,
) : TransitionLike {
    override fun toString(): String =
        "SplitTransition(fromState=$fromState, toStates=$toStates, allowLoops=$allowLoops, condition=${condition.conditionType})"
}
