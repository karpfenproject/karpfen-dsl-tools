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

/**
 * What a state remembers about its inner states while it is not active, written in front of `STATE`.
 *
 * A state with a history is entered again where it was left: the next time the state is entered, the state machine
 * continues in the remembered inner state instead of staying in the state itself.
 */
enum class History {

    /** The state remembers nothing. */
    NONE,

    /** `SHALLOW HISTORY`: the state remembers which of its directly nested states was active last. */
    SHALLOW,

    /** `DEEP HISTORY`: the state remembers the innermost state that was active last, at any depth inside it. */
    DEEP
}
