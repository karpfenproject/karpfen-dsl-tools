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
package io.karpfen.dsl.printer.kstates

import io.karpfen.dsl.printer.CodeWriter
import io.karpfen.dsl.states.conditions.CompositeCondition
import io.karpfen.dsl.states.conditions.Condition
import io.karpfen.dsl.states.conditions.EvalCondition
import io.karpfen.dsl.states.conditions.EventCondition
import io.karpfen.dsl.states.conditions.ValueCondition

/**
 * Writes the `CONDITION` block of a transition with one clause per line.
 *
 * A [CompositeCondition] contributes its clauses in order. A transition without a `CONDITION` block reads as
 * the condition `VALUE("true")`, so that condition on its own is written as no block at all.
 *
 * @param syntax The shared pieces of KStates syntax.
 */
internal class ConditionWriter(private val syntax: KstatesSyntax) {

    /**
     * Writes `CONDITION { ... }` for [condition], or nothing for an unconditional transition.
     *
     * @throws IllegalArgumentException If a clause is of a kind KStates cannot express.
     */
    fun write(condition: Condition, writer: CodeWriter) {
        val clauses = if (isUnconditional(condition)) emptyList() else clausesOf(condition)
        if (clauses.isNotEmpty()) {
            writer.block("CONDITION") { clauses.forEach { writer.line(clauseText(it, writer)) } }
        }
    }

    private fun isUnconditional(condition: Condition): Boolean =
        condition is ValueCondition && condition.boolVariable == UNCONDITIONAL_VARIABLE

    private fun clausesOf(condition: Condition): List<Condition> =
        if (condition is CompositeCondition) condition.clauses.flatMap { clausesOf(it) } else listOf(condition)

    /**
     * Returns one clause: `EVENT("domain", "event")`, `VALUE("variable")` or an `EVAL` block.
     *
     * @throws IllegalArgumentException If [clause] is of none of these kinds.
     */
    private fun clauseText(clause: Condition, writer: CodeWriter): String = when (clause) {
        is EventCondition -> "EVENT(${syntax.quoteAll(listOf(clause.eventDomain, clause.eventValue))})"
        is ValueCondition -> "VALUE(${syntax.quote(clause.boolVariable)})"
        is EvalCondition -> syntax.eval(clause.code, writer)
        else -> throw IllegalArgumentException("Unsupported kind of condition: ${clause.conditionType}")
    }

    private companion object {

        /** The variable of the condition that KStates reads for a transition without a `CONDITION` block. */
        const val UNCONDITIONAL_VARIABLE = "true"
    }
}
