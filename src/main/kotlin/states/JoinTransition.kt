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
 * An N-to-1 transition that fans a set of parallel states back into a single state.
 *
 * When it fires, all parallel branches collapse and the context becomes simple again, entering
 * [toState]. A join is only checked while the context is parallel, and is given priority over the
 * branches' own transitions. [fromStates] must hold at least two source states.
 *
 * @property fromStates The set of source states; each must be active in a distinct branch for the join to fire.
 * @property toState The single target state entered after the join.
 * @property allowLoops False if this transition must not fire twice in a row.
 * @property condition The condition that must hold for this transition to fire.
 */
data class JoinTransition(
    val fromStates: List<String>,
    val toState: String,
    override val allowLoops: Boolean,
    override val condition: Condition,
) : TransitionLike {
    override fun toString(): String =
        "JoinTransition(fromStates=$fromStates, toState=$toState, allowLoops=$allowLoops, condition=${condition.conditionType})"
}
