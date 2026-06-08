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
 * A conditional block inside an ENTRY or DO action block, written as `IF IN SCOPE(...) { ... }`.
 *
 * The [body] runs only when every path in [paths] is currently available. A path is available when it
 * resolves to a value: for a path rooted at `event` this means an event is in scope and carries the
 * named field; for any other path it means the path resolves against the state machine's context
 * object. This is what lets one ENTRY/DO block behave differently depending on whether the state was
 * reached through an event transition (with a scoped event) or through a plain transition.
 *
 * @property paths The access paths that must all be available for the [body] to run.
 * @property body The actions to run when the paths are available.
 */
data class InScopeBlock(
    val paths: List<String>,
    val body: ActionBlock
) : ActionItem {
    override fun toString(): String =
        "InScopeBlock(paths=$paths, body=$body)"
}
