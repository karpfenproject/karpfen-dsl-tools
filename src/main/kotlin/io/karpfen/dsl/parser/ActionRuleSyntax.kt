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
package io.karpfen.dsl.parser

import io.karpfen.dsl.grammar.kstates.KstatesParser
import io.karpfen.dsl.states.actions.ActionOperationType

/**
 * The syntax of KStates action rules that the reader and the printer share: which operation a rule names, the keyword
 * an operation is written with, and the [Arity] each operation expects.
 *
 * An operation expects the arity of its [ActionOperationType.operandCount]. A rule is written unary without a right
 * side, binary with one string before its right side and ternary with two.
 */
internal object ActionRuleSyntax {

    /**
     * Returns the operation [rule] names; a rule whose operation keyword is none of the others names `EVENT`.
     *
     * The rule must hold its operation, which a rule without syntax errors does.
     */
    fun operationOf(rule: KstatesParser.Action_ruleContext): ActionOperationType {
        val op = rule.action_operation()
        return when {
            op.SET() != null -> ActionOperationType.SET
            op.APPEND() != null -> ActionOperationType.APPEND
            op.SETLIST() != null -> ActionOperationType.SETLIST
            op.DROPLIST() != null -> ActionOperationType.DROPLIST
            op.SETOBJ() != null -> ActionOperationType.SETOBJ
            op.APPENDOBJ() != null -> ActionOperationType.APPENDOBJ
            op.DROPOBJ() != null -> ActionOperationType.DROPOBJ
            op.EXCHANGE() != null -> ActionOperationType.EXCHANGE
            else -> ActionOperationType.EVENT
        }
    }

    /**
     * Returns the keyword [operation] is written with: its name, in the capitals the grammar spells it in.
     */
    fun keywordOf(operation: ActionOperationType): String = operation.name

    /**
     * Returns the arity a rule of [operation] must be written with, the one with its operand count.
     */
    fun expectedArity(operation: ActionOperationType): Arity =
        Arity.entries.single { it.operandCount == operation.operandCount }

    /**
     * Returns the arity of a rule written with [stringCount] strings before its right side, if [hasRightSide].
     */
    fun writtenArity(stringCount: Int, hasRightSide: Boolean): Arity = when {
        !hasRightSide -> Arity.UNARY
        stringCount == 1 -> Arity.BINARY
        else -> Arity.TERNARY
    }

    /**
     * Returns the message for a rule of [operation] that is written with the arity [written] instead of
     * [expected].
     */
    fun arityMismatch(operation: ActionOperationType, expected: Arity, written: Arity): String =
        "Operation $operation expects ${expected.usage}, but was called as ${written.usage}."
}

/**
 * Number of operands an action operation is written with.
 *
 * @property usage The written form, used in error messages.
 * @property operandCount The number of operands, as [ActionOperationType.operandCount] counts them.
 */
enum class Arity(val usage: String, val operandCount: Int) {
    /** One operand, the target path. */
    UNARY("OP(\"path\")", 1),

    /** A target path and a value, or a path and the name of a state machine. */
    BINARY("OP(\"path\", value)", 2),

    /** A source path, a relation and a target. */
    TERNARY("OP(\"source\", \"relation\", target)", 3)
}
