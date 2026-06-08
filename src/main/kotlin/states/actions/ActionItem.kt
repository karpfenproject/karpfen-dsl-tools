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
 * One entry inside an [ActionBlock].
 *
 * An action block is no longer a flat list of rules: it can also contain conditional sub-blocks
 * ([InScopeBlock]) that only run when the access paths they name are available. Both a single
 * [ActionRule] and an [InScopeBlock] are therefore action items.
 */
sealed interface ActionItem
