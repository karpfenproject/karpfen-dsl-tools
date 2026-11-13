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

import io.karpfen.dsl.diagnostics.DiagnosticCode

/**
 * Reports the reads of a field of the event in scope, `event->...`, that fail for every event that can be in scope
 * there, see [EventScopes]: the event metamodel does not declare the event, or its payload type has no such field.
 *
 * The payload of an event is read against the type of its name in the event metamodel, which holds the domain types
 * too. A read inside an `IF IN SCOPE` block on the event is left out, and so is a read where no event can be in scope,
 * which [EventScopeCheck] warns about. The check needs a domain metamodel and an event metamodel without errors and
 * state names that are unique and known.
 *
 * @param hasEventMetamodel True if the project has an event metamodel; without one the check stays silent.
 */
internal class EventFieldCheck(private val hasEventMetamodel: Boolean) : MachineCheck {

    /** Checks the event reads of the state machine of [context]. */
    override fun check(context: MachineContext) {
        val types = context.types?.takeIf { it.eventsKnown && hasEventMetamodel } ?: return
        for (site in context.sites.reads) {
            val read = context.resolver.resolve(site.path, site.scope) as? Read.OfEvent ?: continue
            val events = site.scope.events?.takeIf { it.isNotEmpty() } ?: continue
            if (read.rest.isEmpty() || site.scope.isGuarded(site.path)) {
                continue
            }
            val failures = events.sorted().map { failureFor(types, it, read.rest) }
            if (failures.all { it != null }) {
                context.report.report(DiagnosticCode.UNKNOWN_EVENT_FIELD, site.range, messageOf(site.path, failures.filterNotNull()))
            }
        }
    }

    /** Returns why [rest] cannot be read from the payload of the event [event], or null if it can or it cannot be told. */
    private fun failureFor(types: ProjectTypes, event: String, rest: List<String>): String? {
        val payload = types.eventType(event) ?: types.domainType(event)
            ?: return "the event metamodel declares no event '$event', so it carries no payload"
        val resolution = TypePaths.resolve(payload, rest, Navigation.MODEL, offset = 1)
        return (resolution as? Resolution.Failed)?.let { "for event '$event', ${it.message.replaceFirstChar(Char::lowercase)}" }
    }

    /** Returns the message for the read of [path], which fails for the reasons [failures], one per event. */
    private fun messageOf(path: String, failures: List<String>): String =
        "'$path' cannot be read from the event in scope: ${failures.joinToString("; ")}"
}

/**
 * Warns about the reads of the event in scope, `event` or `event->...`, where no event can be in scope, see
 * [EventScopes]: in a state that no transition or split with an `EVENT` clause leads into, directly or through an outer
 * or inner state, in the conditions of a transition without an `EVENT` clause that starts in such a state, and in the
 * conditions of a join without one. A read inside an `IF IN SCOPE` block on the event is left out.
 *
 * The check needs state names that are unique and known.
 */
internal class EventScopeCheck : MachineCheck {

    /** Checks the event reads of the state machine of [context]. */
    override fun check(context: MachineContext) {
        for (site in context.sites.reads) {
            if (context.resolver.resolve(site.path, site.scope) !is Read.OfEvent) {
                continue
            }
            if (site.scope.events?.isEmpty() == true && !site.scope.isGuarded(site.path)) {
                context.report.report(
                    DiagnosticCode.EVENT_NOT_IN_SCOPE,
                    site.range,
                    "No event is in scope here, since no transition with an EVENT clause leads here, so '${site.path}' " +
                        "cannot be read; guard the read with IF IN SCOPE or read it where an event is in scope"
                )
            }
        }
    }
}
