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

import io.karpfen.dsl.diagnostics.Diagnostic
import io.karpfen.dsl.diagnostics.DiagnosticCode
import io.karpfen.dsl.meta.Metamodel
import io.karpfen.dsl.reader.ParseResult

/**
 * Checks the `behaves after` clauses of the metamodels of a project: in the domain metamodel, that the state machine
 * a clause names is an attachable state machine of the project and may be attached to the type of the clause; in the
 * event metamodel, that no type of its own has the clause, since events run no state machine.
 *
 * The problems are reported in the text of the metamodel: an unknown name or a type that does not fit at the name, a
 * clause in the event metamodel at the whole clause.
 *
 * @param attachables The attachable state machines of the project.
 */
internal class BehaviourCheck(private val attachables: AttachableMachines) {

    /** Returns the problems of the clauses of [metamodel], the domain metamodel. */
    fun checkDomain(metamodel: ParseResult<Metamodel>): List<Diagnostic> {
        val model = metamodel.model ?: return emptyList()
        val report = MachineReport(metamodel.sourceMap)
        for (type in model.types) {
            val clause = type.behavesAfter ?: continue
            attachables.checkReference(clause.stateMachineName, type.name, report.rangeOf(clause), report)
        }
        return report.diagnostics
    }

    /**
     * Returns the problems of the clauses of [events], the event metamodel; the domain types it holds as base types
     * have no location in its source map and are left out.
     */
    fun checkEvents(events: ParseResult<Metamodel>): List<Diagnostic> {
        val model = events.model ?: return emptyList()
        val report = MachineReport(events.sourceMap)
        for (type in model.types.filter { events.sourceMap.locationOf(it) != null }) {
            val clause = type.behavesAfter ?: continue
            report.report(
                DiagnosticCode.BEHAVES_AFTER_IN_EVENTS,
                events.sourceMap.rangeOf(clause) ?: report.rangeOf(type),
                "The event '${type.name}' cannot behave after a state machine; behaves after belongs to the types of " +
                    "the domain metamodel"
            )
        }
        return report.diagnostics
    }
}
