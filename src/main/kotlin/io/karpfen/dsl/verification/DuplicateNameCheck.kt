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
 * Reports an attachable state machine that shares its name with another attachable state machine of the project, at
 * its name. Each state machine of the name is reported, since no file of a project comes before another.
 */
internal class DuplicateNameCheck : MachineCheck {

    /** Checks the name of the state machine of [context]. */
    override fun check(context: MachineContext) {
        val machine = context.machine
        if (machine.isAttachable && context.attachables.hasNamesake(machine)) {
            context.report.report(
                DiagnosticCode.DUPLICATE_STATE_MACHINE_NAME,
                context.report.rangeOf(machine),
                "Another attachable state machine of the project is named '${machine.name}' too; every attachable " +
                    "state machine needs a name of its own"
            )
        }
    }
}
