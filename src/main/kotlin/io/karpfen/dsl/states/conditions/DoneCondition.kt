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
package io.karpfen.dsl.states.conditions

/**
 * The clause `DONE` of a `CONDITION` block, which speaks about the container states a transition leaves.
 *
 * On a `TRANSITION` or a `SPLIT` that leaves a container state it holds while the active state directly inside the
 * container is a final state of its fragment. On a `JOIN` it speaks about every source that is a container state and
 * holds while each of them has reached a final state; the sources that are no container states do not count for it.
 * The clause takes no arguments. Its instances are told apart by identity, so that each clause of a text has a
 * location of its own.
 */
class DoneCondition : Condition(ConditionType.DONE) {
    override fun toString(): String = "DoneCondition"
}
