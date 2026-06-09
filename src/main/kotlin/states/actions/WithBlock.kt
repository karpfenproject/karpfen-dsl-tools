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
 * A `WITH macro_call AS "name" { ... }` binding block inside an ENTRY or DO action block.
 *
 * The macro is evaluated exactly once and its result is bound to [name] for the duration of [body].
 * Inside the body the result is readable with paths rooted at [name], e.g. `$(name->x)` for an object
 * result or `$(name)` for a scalar/list result. This lets a single macro evaluation feed several writes
 * (for example value-wise updates of an existing object's fields, preserving its id) without re-running
 * the macro per write. Blocks may be nested and combined with `IF IN SCOPE`.
 *
 * @property macro The macro call to evaluate once.
 * @property name The binding name the result is exposed under inside [body].
 * @property body The actions to run with the binding in scope.
 */
data class WithBlock(
    val macro: MacroActionRightSide,
    val name: String,
    val body: ActionBlock
) : ActionItem {
    override fun toString(): String =
        "WithBlock(macro=${macro.macroName}, name=$name, body=$body)"
}
