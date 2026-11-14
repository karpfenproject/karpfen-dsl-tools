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
import io.karpfen.dsl.meta.AssociationType
import io.karpfen.dsl.meta.ClassType
import io.karpfen.dsl.meta.ClassTypeProperty
import io.karpfen.dsl.states.actions.ActionOperationType
import io.karpfen.dsl.states.actions.ActionRule
import io.karpfen.dsl.states.actions.AttachClause
import io.karpfen.dsl.states.actions.ValueActionRightSide

/**
 * Reports the `ATTACH` clauses and `EXCHANGE` rules that name a state machine the project lacks or one that may not
 * run on the object in question, and the `ATTACH` clauses on a `knows` relation.
 *
 * An `ATTACH` clause names the state machine of the object its `SETOBJ` or `APPENDOBJ` creates, which is of the type
 * of the relation; a `knows` relation creates no object, so the clause is reported there, at the whole clause. An
 * `EXCHANGE` names the state machine of the object its path leads to. Only a name written as a string is checked; a
 * name that an `EVAL` or a `MACRO` computes is known only at run time. The names are checked even where the types are
 * unknown, the types only where the type the state machine runs on is known; [ActionOperationCheck] reports paths that
 * lead nowhere.
 */
internal class AttachmentNameCheck : MachineCheck {

    /** Checks the rules of the state machine of [context]. */
    override fun check(context: MachineContext) {
        for (site in context.sites.rules) {
            val rule = site.rule
            val attach = rule.attach
            when {
                rule.operationType == ActionOperationType.EXCHANGE -> checkExchange(context, rule)
                attach != null -> checkAttach(context, rule, attach)
            }
        }
    }

    /** Checks the [attach] clause of [rule], a `SETOBJ` or `APPENDOBJ`. */
    private fun checkAttach(context: MachineContext, rule: ActionRule, attach: AttachClause) {
        val relation = context.contextType?.let { relationOf(it, rule) }
        if (relation?.associationType == AssociationType.LINK) {
            context.report.report(
                DiagnosticCode.ATTACH_ON_KNOWS,
                context.report.rangeOf(attach),
                "'${relation.key}' is a knows relation, which links an object of the model instead of creating one; " +
                    "ATTACH only follows a has relation, replace the state machine of an existing object with EXCHANGE"
            )
            return
        }
        val name = attach.stateMachineName as? ValueActionRightSide ?: return
        context.attachables.checkReference(name.value, relation?.reference?.classTypeName, context.report.operandRangeOf(name, 0), context.report)
    }

    /** Checks the name of the state machine an `EXCHANGE` [rule] attaches. */
    private fun checkExchange(context: MachineContext, rule: ActionRule) {
        val name = rule.rightSide as? ValueActionRightSide ?: return
        val target = context.contextType?.let { RuleTargets.objectAt(it, rule.leftSide) as? RuleTarget.Found }
        context.attachables.checkReference(name.value, target?.type?.name, context.report.operandRangeOf(name, 0), context.report)
    }

    /** Returns the relation a `SETOBJ` or `APPENDOBJ` [rule] sets, seen from [contextType], or null if it is not found. */
    private fun relationOf(contextType: ClassType, rule: ActionRule): ClassTypeProperty? {
        val owner = (RuleTargets.sourceOf(contextType, rule.leftSide) as? RuleTarget.Found)?.type ?: return null
        return owner.objectProperties.firstOrNull { it.key == rule.secondSide }
    }
}
