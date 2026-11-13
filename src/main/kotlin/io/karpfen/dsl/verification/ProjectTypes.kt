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

import io.karpfen.dsl.meta.ClassType
import io.karpfen.dsl.meta.Metamodel

/**
 * The class types of a project: the types of the domain metamodel and the event types, the types the event metamodel
 * defines beyond the domain types it embeds.
 *
 * An event type has the name of the events whose payload it describes. The event types are known only if the project
 * has an event metamodel without errors or none at all; otherwise [eventsKnown] is false and the checks that need them
 * stay silent.
 *
 * @param domain The domain metamodel, read without errors.
 * @param events The event metamodel, or null if the project has none.
 * @property eventsKnown True if [eventType] knows every event type of the project.
 */
internal class ProjectTypes(domain: Metamodel, events: Metamodel?, val eventsKnown: Boolean) {

    /** The domain types by name; the first of a name wins, as in the runtime. */
    private val domainTypes: Map<String, ClassType> = domain.types.reversed().associateBy { it.name }

    /** The event types by name, without the domain types the event metamodel holds as base types. */
    private val eventTypes: Map<String, ClassType> = events?.types.orEmpty()
        .filter { type -> domain.types.none { it === type } }
        .reversed()
        .associateBy { it.name }

    /** Returns the domain type named [name], or null. */
    fun domainType(name: String): ClassType? = domainTypes[name]

    /** Returns the event type named [name], the payload of the events of that name, or null. */
    fun eventType(name: String): ClassType? = eventTypes[name]

    /** Returns the domain or event type named [name], the domain type first, or null. */
    fun anyType(name: String): ClassType? = domainTypes[name] ?: eventTypes[name]
}
