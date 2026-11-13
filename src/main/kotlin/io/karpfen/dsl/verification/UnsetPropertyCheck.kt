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
import io.karpfen.dsl.instance.DataObject

/**
 * Warns about the reads of a property, in a `VALUE` condition or as `$(path)` in the code of an `EVAL`, that the model
 * gives no value on the way from the object the state machine runs on: a relation without an object or a property
 * without a value, which the runtime then fails to read.
 *
 * A read is left out if some action of some state machine of the project writes one of the keys of its path, since the
 * value may be there by the time it is read, and so is a read that an `IF IN SCOPE` block around it guards. The check
 * needs the object the state machine runs on, from a model without errors, of the type the machine is attached to.
 */
internal class UnsetPropertyCheck : MachineCheck {

    /** The kinds of reads the check looks at. */
    private val kinds = setOf(ReadKind.CODE, ReadKind.VALUE_CONDITION)

    /** Checks the reads of the state machine of [context] against the values of its object. */
    override fun check(context: MachineContext) {
        val type = context.contextType ?: return
        val attached = context.attachedObject?.takeIf { it.ofType.name == type.name } ?: return
        for (site in context.sites.reads.filter { it.kind in kinds && !it.scope.isGuarded(it.path) }) {
            val read = context.resolver.resolve(site.path, site.scope) as? Read.OfContext ?: continue
            if ((read.resolution as? Resolution.Resolved)?.end !is PathEnd.Property) {
                continue
            }
            val segments = TypePaths.segmentsOf(site.path)
            if (segments.any { it in context.writtenKeys }) {
                continue
            }
            val missing = missingOn(attached, segments) ?: continue
            context.report.report(
                DiagnosticCode.UNSET_PROPERTY,
                site.range,
                "The model gives object '${missing.first.id}' no value for '${missing.second}', and no action writes " +
                    "it, so reading '${site.path}' fails; give it a value in the model"
            )
        }
    }

    /**
     * Follows [segments] from [start] through the values of the model; returns the object and the key that has no
     * value, or null if every segment has one.
     */
    private fun missingOn(start: DataObject, segments: List<String>): Pair<DataObject, String>? {
        var current = start
        for ((index, segment) in segments.withIndex()) {
            if (index == segments.lastIndex) {
                return if (current.properties.any { it.key == segment }) null else current to segment
            }
            current = current.getRel(segment).firstOrNull() ?: return current to segment
        }
        return null
    }
}
