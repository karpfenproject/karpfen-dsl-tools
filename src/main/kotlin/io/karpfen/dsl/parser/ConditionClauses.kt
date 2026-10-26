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

import io.karpfen.dsl.states.conditions.CompositeCondition
import io.karpfen.dsl.states.conditions.Condition
import io.karpfen.dsl.states.conditions.ValueCondition

/**
 * How the clauses of a KStates `CONDITION` block make up the condition of a transition, which the converter and the
 * reader build and the printer takes apart again.
 *
 * A transition without clauses, written without a `CONDITION` block, holds the always-true condition
 * `VALUE("true")`. A single clause is the condition itself, and several clauses form a [CompositeCondition] that
 * holds when all of them hold, in order.
 */
internal object ConditionClauses {

    /** The variable of the always-true condition. */
    private const val ALWAYS_TRUE_VARIABLE = "true"

    /**
     * Returns the condition that [clauses], in the order they are written, make up.
     */
    fun conditionOf(clauses: List<Condition>): Condition = when (clauses.size) {
        0 -> alwaysTrue()
        1 -> clauses.single()
        else -> CompositeCondition(clauses)
    }

    /**
     * Returns the condition of a transition without a `CONDITION` block.
     */
    fun alwaysTrue(): Condition = ValueCondition(ALWAYS_TRUE_VARIABLE)

    /**
     * Returns the clauses that a `CONDITION` block writes for [condition]: none for the always-true condition, the
     * clauses of a [CompositeCondition] in order with those of nested ones in their place, and otherwise the
     * condition itself.
     */
    fun clausesOf(condition: Condition): List<Condition> =
        if (isAlwaysTrue(condition)) emptyList() else flattened(condition)

    /** Returns true for the condition of a transition without a `CONDITION` block. */
    private fun isAlwaysTrue(condition: Condition): Boolean =
        condition is ValueCondition && condition.boolVariable == ALWAYS_TRUE_VARIABLE

    /** Returns the clauses of [condition], those of nested composite conditions in their place. */
    private fun flattened(condition: Condition): List<Condition> =
        if (condition is CompositeCondition) condition.clauses.flatMap { flattened(it) } else listOf(condition)
}
