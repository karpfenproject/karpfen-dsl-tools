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
 * Reports the types of `TAKES` and `RETURNS` directives of macros that name neither a primitive nor a type of the
 * domain metamodel or the event metamodel, or that use another function than `list` and `reference`, at the type.
 *
 * The check needs every class type of the project, so it stays silent while the event metamodel has errors.
 */
internal class MacroSignatureCheck : MachineCheck {

    /** Checks the types of the macros of the state machine of [context]. */
    override fun check(context: MachineContext) {
        val types = context.types?.takeIf { it.eventsKnown } ?: return
        val macroTypes = MacroTypes(types)
        for (macro in context.machine.macros) {
            macro.takes.forEach { report(context, macroTypes.read(it.paramType), it) }
            report(context, macroTypes.read(macro.returns.returnType), macro.returns)
        }
    }

    /** Reports [reading] at the type of [directive] if it is unknown. */
    private fun report(context: MachineContext, reading: MacroTypeReading, directive: Any) {
        if (reading is MacroTypeReading.Unknown) {
            context.report.report(DiagnosticCode.UNKNOWN_MACRO_TYPE, context.report.operandRangeOf(directive, 0), reading.message)
        }
    }
}
