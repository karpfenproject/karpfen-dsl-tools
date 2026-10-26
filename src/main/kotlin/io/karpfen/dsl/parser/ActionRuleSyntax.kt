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
 * The syntax of KStates action rules that the parse visitor, the reader and the printer share: which operation a
 * rule names, the keyword an operation is written with, and the [Arity] each operation expects.
 *
 * An operation expects the arity of its [ActionOperationType.operandCount]. A rule is written unary without a right
 * side, binary with one string before its right side and ternary with two.
 */
internal object ActionRuleSyntax {

    /**
     * Returns the operation [rule] names; a rule whose operation keyword is none of the others names `EVENT`.
     *
     * The operation context stays in the local `op`: a rule the parser had to recover from may lack it, and the
     * resulting [NullPointerException] then names `op`, exactly as in version 1.0.
     */
    fun operationOf(rule: KstatesParser.Action_ruleContext): ParsedActionOperationType {
        val op = rule.action_operation()
        return when {
            op.SET() != null -> ParsedActionOperationType.SET
            op.APPEND() != null -> ParsedActionOperationType.APPEND
            op.SETLIST() != null -> ParsedActionOperationType.SETLIST
            op.DROPLIST() != null -> ParsedActionOperationType.DROPLIST
            op.SETOBJ() != null -> ParsedActionOperationType.SETOBJ
            op.APPENDOBJ() != null -> ParsedActionOperationType.APPENDOBJ
            op.DROPOBJ() != null -> ParsedActionOperationType.DROPOBJ
            else -> ParsedActionOperationType.EVENT
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
