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

import io.karpfen.dsl.instance.DataObject
import io.karpfen.dsl.meta.ClassType
import io.karpfen.dsl.states.StateMachine

/**
 * What the checks of one state machine work on: the machine, what the project tells about it, and the report that
 * receives the problems. A part the project cannot tell for sure is null, and the checks that need it stay silent.
 *
 * @property machine The state machine.
 * @property report The report of the problems found in its text.
 * @property types The class types of the project, or null if the domain metamodel has errors.
 * @property contextType The type the machine is attached to, or null if it is unknown.
 * @property attachedObjectId The id of the object the machine runs on, or null if it is not given.
 * @property objects Every object of the model, the embedded ones included, or null if there is no model or it has
 * errors.
 * @property graph The nesting, reach and event scopes of the states, or null if state names repeat or are unknown.
 * @property sites The rules of the machine and the paths it reads.
 * @property resolver Finds what the paths of the machine refer to.
 * @property writtenKeys The keys that some action of some state machine of the project writes.
 */
internal class MachineContext(
    val machine: StateMachine,
    val report: MachineReport,
    val types: ProjectTypes?,
    val contextType: ClassType?,
    val attachedObjectId: String?,
    val objects: List<DataObject>?,
    val graph: MachineGraph?,
    val sites: MachineSites,
    val resolver: ReadResolver,
    val writtenKeys: Set<String>
) {

    /** The object of the model the machine runs on, or null if it is not given, not found or the model has errors. */
    val attachedObject: DataObject?
        get() = attachedObjectId?.let { id -> objects?.firstOrNull { it.id == id } }
}

/**
 * The views of the states of a state machine whose state names are unique and whose transitions name known states.
 *
 * @property tree The nesting of the states.
 * @property reachability What the machine can reach.
 * @property eventScopes The events in scope across the machine.
 */
internal class MachineGraph(val tree: StateTree, val reachability: Reachability, val eventScopes: EventScopes)

/**
 * A check of a state machine against the rest of its project.
 */
internal fun interface MachineCheck {

    /** Checks the state machine of [context] and reports its problems to the report of [context]. */
    fun check(context: MachineContext)
}
