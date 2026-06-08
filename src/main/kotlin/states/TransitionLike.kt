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
 * Common supertype of all transition kinds.
 *
 * - [Transition]: an ordinary 1-to-1 transition.
 * - [SplitTransition]: 1-to-N — leaves one state and enters several states in parallel (fan out).
 * - [JoinTransition]: N-to-1 — leaves a set of parallel states and enters a single state (fan in).
 *
 * All kinds share an [allowLoops] flag (NOT LOOPING) and a [condition]; only their source/target
 * multiplicity differs. Keeping them in one ordered list preserves definition order as the priority
 * mechanism between, say, a normal transition and a split out of the same state.
 */
sealed interface TransitionLike {
    val allowLoops: Boolean
    val condition: Condition
}
