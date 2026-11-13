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
package io.karpfen.dsl.verification

import io.karpfen.dsl.instance.Model
import io.karpfen.dsl.meta.Metamodel
import io.karpfen.dsl.reader.ParseResult
import io.karpfen.dsl.states.StateMachine

/**
 * The read texts of a karpfen project that [ProjectVerifier] checks against each other.
 *
 * Each text is given as its read result, the model with its diagnostics and its source map, as the readers return it.
 * A text whose read reports errors is used only as far as it can be trusted, see [ProjectVerifier].
 *
 * @property metamodel The domain metamodel, read with [io.karpfen.dsl.reader.kmeta.KmetaReader].
 * @property events The event metamodel, read with the types of [metamodel] as base types, or null if the project
 * declares no events.
 * @property model The model, read against [metamodel], or null if the project has none.
 * @property stateMachines The state machines, each with the object it is attached to.
 */
class ProjectInput(
    val metamodel: ParseResult<Metamodel>,
    val events: ParseResult<Metamodel>? = null,
    val model: ParseResult<Model>? = null,
    val stateMachines: List<StateMachineInput> = emptyList()
)

/**
 * A read state machine of a project and the object of the model it runs on.
 *
 * @property read The state machine as [io.karpfen.dsl.reader.kstates.KstatesReader] read it.
 * @property attachedObjectId The id of the object of the model the state machine is attached to, or null if it is not
 * attached to an object yet; without it, the checks against the values of the model are left out.
 */
class StateMachineInput(val read: ParseResult<StateMachine>, val attachedObjectId: String? = null)
