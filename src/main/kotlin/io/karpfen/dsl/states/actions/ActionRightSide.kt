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
package io.karpfen.dsl.states.actions

/**
 * Base class for different types of right-side expressions in action rules.
 *
 * @property actionValueType The type of value this right-side represents.
 */
open class ActionRightSide(val actionValueType: ActionValueType) {
    override fun toString(): String =
        "ActionRightSide(actionValueType=$actionValueType)"
}
