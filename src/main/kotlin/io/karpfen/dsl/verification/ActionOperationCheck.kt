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
import io.karpfen.dsl.source.SourceRange
import io.karpfen.dsl.states.actions.ActionOperationType
import io.karpfen.dsl.states.actions.ValueActionRightSide

/**
 * Reports the action rules whose paths lead nowhere or whose operation does not fit what they lead to, as the runtime
 * finds them from the object the state machine is attached to.
 *
 * `SET` needs a property that is not a list; `APPEND`, `SETLIST` and `DROPLIST` need a list property. `SETOBJ` needs a
 * relation to one object, `APPENDOBJ` a relation to a list of objects, and the object a literal path leads to must be
 * of the type of the relation and may only be linked by a `knows` relation, since a `has` relation takes a new object.
 * `DROPOBJ` and `EXCHANGE` need a path to one object. A property operation on a relation is reported with the relation operation to
 * use instead. A path that an `IF IN SCOPE` block around
 * the rule guards is not reported as leading nowhere.
 *
 * The check needs the type the state machine is attached to.
 */
internal class ActionOperationCheck : MachineCheck {

    /** Checks the rules of the state machine of [context]. */
    override fun check(context: MachineContext) {
        val type = context.contextType ?: return
        for (site in context.sites.rules) {
            val rule = site.rule
            when (rule.operationType) {
                ActionOperationType.SET, ActionOperationType.APPEND,
                ActionOperationType.SETLIST, ActionOperationType.DROPLIST -> checkPropertyRule(context, site, type)
                ActionOperationType.SETOBJ, ActionOperationType.APPENDOBJ -> checkRelationRule(context, site, type)
                ActionOperationType.DROPOBJ, ActionOperationType.EXCHANGE -> checkObjectPath(context, site, type)
                ActionOperationType.EVENT -> Unit
            }
        }
    }

    /** Checks a rule of [site] that writes a property. */
    private fun checkPropertyRule(context: MachineContext, site: RuleSite, contextType: ClassType) {
        val rule = site.rule
        val range = context.report.operandRangeOf(rule, 0)
        val owner = found(context, site, RuleTargets.ownerOf(contextType, rule.leftSide), rule.leftSide, range) ?: return
        val key = TypePaths.segmentsOf(rule.leftSide).last()
        val relation = owner.objectProperties.firstOrNull { it.key == key }
        val property = owner.simpleProperties.firstOrNull { it.key == key }
        when {
            relation != null -> mismatch(context, range, relationMessage(rule.operationType, key, relation.isList))
            property == null -> if (!site.scope.isGuarded(rule.leftSide)) {
                context.report.report(
                    DiagnosticCode.UNKNOWN_PATH,
                    range,
                    "Type '${owner.name}' has no property or relation '$key'"
                )
            }
            rule.operationType == ActionOperationType.SET && property.isList ->
                mismatch(context, range, "'$key' is a list property; write it with SETLIST or APPEND instead of SET")
            rule.operationType != ActionOperationType.SET && !property.isList ->
                mismatch(context, range, "'$key' is not a list property; write it with SET instead of ${rule.operationType}")
        }
    }

    /**
     * Returns the message for the property operation [operation] on the relation [key], a list relation if [isList],
     * naming the operation for relations to use instead.
     */
    private fun relationMessage(operation: ActionOperationType, key: String, isList: Boolean): String {
        val instead = when {
            operation == ActionOperationType.DROPLIST -> "remove its objects with DROPOBJ"
            isList -> "add to it with APPENDOBJ"
            else -> "set it with SETOBJ"
        }
        return "'$key' is a relation, not a property; $instead instead of $operation"
    }

    /** Checks a `SETOBJ` or `APPENDOBJ` rule of [site]. */
    private fun checkRelationRule(context: MachineContext, site: RuleSite, contextType: ClassType) {
        val rule = site.rule
        val sourceRange = context.report.operandRangeOf(rule, 0)
        val owner = found(context, site, RuleTargets.sourceOf(contextType, rule.leftSide), rule.leftSide, sourceRange)
            ?: return
        val key = rule.secondSide ?: return
        val range = context.report.operandRangeOf(rule, 1)
        val relation = owner.objectProperties.firstOrNull { it.key == key }
        if (relation == null) {
            if (owner.simpleProperties.any { it.key == key }) {
                mismatch(context, range, "'$key' is a property; write properties with SET, APPEND and SETLIST")
            } else {
                context.report.report(DiagnosticCode.UNKNOWN_PATH, range, "Type '${owner.name}' has no relation '$key'")
            }
            return
        }
        if (rule.operationType == ActionOperationType.SETOBJ && relation.isList) {
            mismatch(context, range, "'$key' is a relation to a list of objects; add to it with APPENDOBJ instead of SETOBJ")
        } else if (rule.operationType == ActionOperationType.APPENDOBJ && !relation.isList) {
            mismatch(context, range, "'$key' is a relation to one object; set it with SETOBJ instead of APPENDOBJ")
        }
        (rule.rightSide as? ValueActionRightSide)?.let { checkLinkedObject(context, site, it, relation, contextType) }
    }

    /** Checks the object the literal path [value] of a `SETOBJ` or `APPENDOBJ` rule of [site] links by [relation]. */
    private fun checkLinkedObject(
        context: MachineContext,
        site: RuleSite,
        value: ValueActionRightSide,
        relation: ClassTypeProperty,
        contextType: ClassType
    ) {
        val range = context.report.operandRangeOf(value, 0)
        val target = found(context, site, RuleTargets.objectAt(contextType, value.value), value.value, range) ?: return
        val expected = relation.reference.classTypeName
        if (target.name != expected) {
            mismatch(context, range, "'${value.value}' leads to an object of type '${target.name}', but '${relation.key}' holds '$expected'")
        } else if (relation.associationType == AssociationType.EMBEDDED) {
            mismatch(
                context,
                range,
                "'${relation.key}' is a has relation, which takes a new object, but '${value.value}' leads to an object " +
                    "of the model; build the object with a MACRO or an EVAL, or make the relation a knows relation"
            )
        }
    }

    /** Checks the path of a `DROPOBJ` or `EXCHANGE` rule of [site], which must lead to one object. */
    private fun checkObjectPath(context: MachineContext, site: RuleSite, contextType: ClassType) {
        val rule = site.rule
        val range = context.report.operandRangeOf(rule, 0)
        found(context, site, RuleTargets.objectAt(contextType, rule.leftSide), rule.leftSide, range)
    }

    /**
     * Returns the type of the object [target] found for [path] of the rule of [site], or reports at [range] why there is
     * none and returns null; a path the rule is guarded for is not reported as leading nowhere.
     */
    private fun found(context: MachineContext, site: RuleSite, target: RuleTarget, path: String, range: SourceRange): ClassType? =
        when (target) {
            is RuleTarget.Found -> target.type
            is RuleTarget.Problem -> {
                if (target.code != DiagnosticCode.UNKNOWN_PATH || !site.scope.isGuarded(path)) {
                    context.report.report(target.code, range, target.message)
                }
                null
            }
            RuleTarget.Undecided -> null
        }

    /** Reports an operation that does not fit, explained by [message], at [range]. */
    private fun mismatch(context: MachineContext, range: SourceRange, message: String) {
        context.report.report(DiagnosticCode.OPERATION_MISMATCH, range, message)
    }
}
