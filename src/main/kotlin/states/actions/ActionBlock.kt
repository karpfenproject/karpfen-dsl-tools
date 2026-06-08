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
package states.actions

/**
 * Represents a sequence of action items to be executed in order.
 *
 * An item is either a plain [ActionRule] or a conditional [InScopeBlock] that only runs when the
 * access paths it names are available.
 *
 * @property items The ordered list of action items in this block.
 */
data class ActionBlock(val items: MutableList<ActionItem>) {

    /** True when the block has no items at all. */
    fun isEmpty(): Boolean = items.isEmpty()

    /** True when the block has at least one item. */
    fun isNotEmpty(): Boolean = items.isNotEmpty()

    override fun toString(): String =
        "ActionBlock(items=${items.size})"
}
