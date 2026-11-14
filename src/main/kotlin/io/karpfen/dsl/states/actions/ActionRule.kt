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
package io.karpfen.dsl.states.actions

/**
 * Represents a single action rule that modifies the model or sends events.
 *
 * The operands depend on the operation's arity:
 * - unary   (DROPLIST, DROPOBJ):   [leftSide] = target path; [secondSide] and [rightSide] are null.
 * - binary  (SET, APPEND, SETLIST, EVENT): [leftSide] = target path / event domain; [rightSide] = value.
 * - binary  (EXCHANGE): [leftSide] = path to the object; [rightSide] = the name of the attachable state machine.
 * - ternary (SETOBJ, APPENDOBJ):   [leftSide] = source path, [secondSide] = relation key, [rightSide] = target.
 *
 * A `SETOBJ` or `APPENDOBJ` may carry an [attach] clause that names the attachable state machine of the object the
 * rule creates; KStates writes it after the parentheses, as in `APPENDOBJ("self", "spares", MACRO("new bulb")) ATTACH
 * "blinking"`.
 *
 * @property operationType The type of operation.
 * @property leftSide The target property path, source path, or event domain name.
 * @property secondSide The relation key (ternary operations only); null otherwise.
 * @property rightSide The value, macro call, or code expression; null for unary operations.
 * @property attach The `ATTACH` clause of a `SETOBJ` or `APPENDOBJ`; null for a rule without one.
 */
data class ActionRule(
    val operationType: ActionOperationType,
    val leftSide: String,
    val rightSide: ActionRightSide? = null,
    val secondSide: String? = null,
    val attach: AttachClause? = null
) : ActionItem {
    override fun toString(): String =
        "ActionRule(operationType=$operationType, leftSide=$leftSide, secondSide=$secondSide, " +
            "rightSide=${rightSide?.actionValueType}" + (attach?.let { ", attach=${it.stateMachineName.actionValueType}" } ?: "") + ")"
}
