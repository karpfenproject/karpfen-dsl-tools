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
import io.karpfen.dsl.meta.ClassType
import io.karpfen.dsl.states.StateMachine

/**
 * Reports the types of a state machine that the domain metamodel does not define, and a state machine whose object is
 * missing from the model or of a type the state machine may not run on.
 *
 * An attached state machine has one type, the one after `ATTACHED TO`, and an unknown type is reported at it. An
 * attachable state machine lists its types after `ATTACHABLE TO`, and each unknown one is reported at itself; it runs
 * on an object only if a project names one for it. The object is reported at the name of the state machine, the type of
 * an attached one or the name of an attachable one.
 *
 * The types are checked only against a domain metamodel without errors, the object only against a model without
 * errors and only if all types are known.
 */
internal class AttachmentCheck : MachineCheck {

    /** Checks the types and the object of the state machine of [context]. */
    override fun check(context: MachineContext) {
        val types = context.types ?: return
        val machine = context.machine
        val unknown = machine.types.withIndex().filter { types.domainType(it.value) == null }
        if (unknown.isNotEmpty()) {
            unknown.forEach { reportUnknown(context, machine, it.index, it.value) }
            return
        }
        checkObject(context, machine.types.mapNotNull { types.domainType(it) })
    }

    /** Reports that the metamodel lacks the type [name] at [index] of the types of [machine]. */
    private fun reportUnknown(context: MachineContext, machine: StateMachine, index: Int, name: String) {
        if (machine.isAttachable) {
            context.report.report(
                DiagnosticCode.UNKNOWN_ATTACHED_TYPE,
                context.report.operandRangeOf(machine, index),
                "The metamodel defines no type '$name'; make the state machine attachable to types of the metamodel"
            )
        } else {
            context.report.report(
                DiagnosticCode.UNKNOWN_ATTACHED_TYPE,
                context.report.rangeOf(machine),
                "The metamodel defines no type '$name'; attach the state machine to one of its types"
            )
        }
    }

    /** Checks the object the state machine of [context] runs on against [known], the types of the state machine. */
    private fun checkObject(context: MachineContext, known: List<ClassType>) {
        val id = context.attachedObjectId ?: return
        val objects = context.objects ?: return
        val range = context.report.rangeOf(context.machine)
        val attached = objects.firstOrNull { it.id == id }
        if (attached == null) {
            context.report.report(
                DiagnosticCode.ATTACHED_OBJECT_TYPE,
                range,
                "The model has no object '$id' to run this state machine on"
            )
        } else if (known.none { it.name == attached.ofType.name }) {
            val expected = known.joinToString(", ") { "'${it.name}'" }
            val verb = if (context.machine.isAttachable) "attachable" else "attached"
            context.report.report(
                DiagnosticCode.ATTACHED_OBJECT_TYPE,
                range,
                "Object '$id' is of type '${attached.ofType.name}', but the state machine is $verb to $expected"
            )
        }
    }
}
