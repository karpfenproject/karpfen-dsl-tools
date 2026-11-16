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
import io.karpfen.dsl.source.SourceRange
import io.karpfen.dsl.states.StateMachine

/**
 * The named state machines of a project by their names: the attachable ones, which `behaves after`, `ATTACH` and
 * `EXCHANGE` refer to, and the fragments, which container states use. Both share one name space.
 *
 * Only state machines whose read can be trusted take part, see [ProjectVerifier]. A name that none of them has is
 * reported only if every state machine of the project could be read, since a state machine with a syntax error might
 * be the one of that name. A state machine without a name, which its reader reports, is never found.
 *
 * @param machines The state machines of the project whose reads can be trusted, attached and attachable.
 * @param isComplete True if every state machine of the project is among [machines].
 */
internal class AttachableMachines(machines: List<StateMachine>, private val isComplete: Boolean) {

    /**
     * The attachable state machines and fragments with a name, by name, each name with its state machines in project
     * order.
     */
    private val byName: Map<String, List<StateMachine>> =
        machines.filter { (it.isAttachable || it.isFragment) && !it.name.isNullOrEmpty() }.groupBy { it.name!! }

    /** True if every state machine of the project could be read, so that a name none of them has is truly unknown. */
    val isAllRead: Boolean
        get() = isComplete

    /** Returns true if another attachable state machine or fragment of the project has the name of [machine]. */
    fun hasNamesake(machine: StateMachine): Boolean = byName[machine.name].orEmpty().any { it !== machine }

    /**
     * Reports at [range] that no attachable state machine is named [name], that the first state machine of the name is a
     * fragment, which is never attached, or that it may not be attached to an object of the type [typeName]; the type
     * is not checked if [typeName] is null, since the type is unknown then.
     *
     * @param report The report of the text that names the state machine.
     */
    fun checkReference(name: String, typeName: String?, range: SourceRange, report: MachineReport) {
        val machine = byName[name]?.first()
        if (machine == null) {
            if (isComplete) {
                report.report(
                    DiagnosticCode.UNKNOWN_STATE_MACHINE,
                    range,
                    "No attachable state machine of the project is named '$name'"
                )
            }
            return
        }
        if (machine.isFragment) {
            report.report(
                DiagnosticCode.ATTACHED_FRAGMENT,
                range,
                "'$name' is a fragment, which runs only inside a container state that uses it; behaves after, ATTACH " +
                    "and EXCHANGE name attachable state machines"
            )
            return
        }
        if (typeName != null && typeName !in machine.attachableTo) {
            report.report(
                DiagnosticCode.ATTACHABLE_TYPE_MISMATCH,
                range,
                "The state machine '$name' is attachable to ${machine.attachableTo.joinToString(", ") { "'$it'" }}, " +
                    "not to '$typeName'"
            )
        }
    }
}
