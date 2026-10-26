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
import io.karpfen.dsl.states.conditions.EventCondition
import io.karpfen.dsl.states.conditions.ValueCondition
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame
import kotlin.test.assertTrue

/**
 * Checks that [ConditionClauses] takes a condition apart into the clauses it was made up of, so that the converter,
 * the reader and the printer agree on the condition of a transition.
 */
class ConditionClausesTest {

    /**
     * No clause makes the always-true condition, which has no clauses to write; one clause is the condition itself;
     * several clauses make a composite condition, which yields them again in order.
     */
    @Test
    fun `the clauses of a condition are the clauses it was made of`() {
        val event = EventCondition("robot", "bumped")
        val value = ValueCondition("ready")

        val unconditional = ConditionClauses.conditionOf(emptyList())
        val single = ConditionClauses.conditionOf(listOf(value))
        val composite = ConditionClauses.conditionOf(listOf(event, value))

        assertEquals("true", (unconditional as ValueCondition).boolVariable)
        assertEquals(emptyList(), ConditionClauses.clausesOf(unconditional))
        assertSame(value, single)
        assertEquals(listOf<Condition>(value), ConditionClauses.clausesOf(single))
        assertTrue(composite is CompositeCondition)
        assertEquals(listOf<Condition>(event, value), ConditionClauses.clausesOf(composite))
    }

    /**
     * Only a whole condition that is always true stands for no clause; written as a clause among others, `VALUE("true")`
     * stays, and the clauses of nested composite conditions stand in their place.
     */
    @Test
    fun `an always-true clause among others is kept and nested clauses are flattened`() {
        val alwaysTrue = ConditionClauses.alwaysTrue()
        val value = ValueCondition("ready")
        val nested = CompositeCondition(listOf(alwaysTrue, CompositeCondition(listOf(value, alwaysTrue))))

        assertEquals(listOf(alwaysTrue, value, alwaysTrue), ConditionClauses.clausesOf(nested))
    }
}
