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
 * Reports a state machine attached to a type the domain metamodel does not define, and a state machine whose object is
 * missing from the model or of another type than the one it is attached to; each at the type name after
 * `ATTACHED TO`.
 *
 * The type is checked only against a domain metamodel without errors, the object only against a model without errors.
 */
internal class AttachmentCheck : MachineCheck {

    /** Checks the type and the object the state machine of [context] is attached to. */
    override fun check(context: MachineContext) {
        context.types ?: return
        val machine = context.machine
        val range = context.report.rangeOf(machine)
        val type = context.contextType
        if (type == null) {
            context.report.report(
                DiagnosticCode.UNKNOWN_ATTACHED_TYPE,
                range,
                "The metamodel defines no type '${machine.attachedToClass}'; attach the state machine to one of its types"
            )
            return
        }
        val id = context.attachedObjectId ?: return
        val objects = context.objects ?: return
        val attached = objects.firstOrNull { it.id == id }
        if (attached == null) {
            context.report.report(
                DiagnosticCode.ATTACHED_OBJECT_TYPE,
                range,
                "The model has no object '$id' to run this state machine on"
            )
        } else if (attached.ofType.name != type.name) {
            context.report.report(
                DiagnosticCode.ATTACHED_OBJECT_TYPE,
                range,
                "Object '$id' is of type '${attached.ofType.name}', but the state machine is attached to '${type.name}'"
            )
        }
    }
}
