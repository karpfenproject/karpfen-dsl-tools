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
import io.karpfen.dsl.meta.SimplePropertyType
import io.karpfen.dsl.states.actions.ActionOperationType
import io.karpfen.dsl.states.actions.ValueActionRightSide

/**
 * Reports the literal values of `SET`, `APPEND` and `SETLIST` rules that the runtime cannot read as the type of the
 * property they write, at the value: a number must read as a decimal number, a boolean as `true` or `false` in any
 * case, and a string takes any value. A list property takes a literal as one element of its type.
 *
 * The check needs the type the state machine is attached to and leaves out the rules whose property it cannot find.
 */
internal class LiteralCheck : MachineCheck {

    /** The operations that write a literal into a property. */
    private val operations = setOf(ActionOperationType.SET, ActionOperationType.APPEND, ActionOperationType.SETLIST)

    /** Checks the literal values of the rules of the state machine of [context]. */
    override fun check(context: MachineContext) {
        val contextType = context.contextType ?: return
        for (site in context.sites.rules) {
            val rule = site.rule
            val value = rule.rightSide as? ValueActionRightSide ?: continue
            if (rule.operationType !in operations) {
                continue
            }
            val owner = (RuleTargets.ownerOf(contextType, rule.leftSide) as? RuleTarget.Found)?.type ?: continue
            val key = TypePaths.segmentsOf(rule.leftSide).last()
            val property = owner.simpleProperties.firstOrNull { it.key == key } ?: continue
            if (!fits(value.value, property.propertyType)) {
                context.report.report(
                    DiagnosticCode.LITERAL_MISMATCH,
                    context.report.operandRangeOf(value, 0),
                    "'${value.value}' is not a ${property.propertyType.keyword}, but '$key' holds ${property.propertyType.keyword} values"
                )
            }
        }
    }

    /** Returns true if the runtime reads [literal] as a value of [type]. */
    private fun fits(literal: String, type: SimplePropertyType): Boolean = when (type) {
        SimplePropertyType.NUMBER -> literal.toDoubleOrNull() != null
        SimplePropertyType.BOOLEAN -> literal.lowercase() in setOf("true", "false")
        SimplePropertyType.STRING -> true
    }
}
