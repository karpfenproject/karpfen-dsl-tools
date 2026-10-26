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
package io.karpfen.dsl.reader.kstates

import io.karpfen.dsl.grammar.kstates.KstatesParser
import io.karpfen.dsl.reader.ReadingSession
import io.karpfen.dsl.reader.containsSyntaxError
import io.karpfen.dsl.reader.presentToken
import io.karpfen.dsl.reader.presentTokens
import io.karpfen.dsl.states.conditions.CompositeCondition
import io.karpfen.dsl.states.conditions.Condition
import io.karpfen.dsl.states.conditions.EvalCondition
import io.karpfen.dsl.states.conditions.EventCondition
import io.karpfen.dsl.states.conditions.ValueCondition

/**
 * Converts KStates `CONDITION` blocks into a single [Condition] the way the throwing converter does, and records
 * where each clause is written.
 *
 * No clause means an unconditional transition, one clause keeps its own condition, several clauses become a
 * [CompositeCondition]. A clause that contains a syntax error is left out.
 *
 * @param session The read the conversion belongs to.
 * @param evalBodies Reads the code of `EVAL` clauses.
 */
internal class ConditionConverter(
    private val session: ReadingSession,
    private val evalBodies: EvalBodies
) {

    /**
     * Converts [block], or the missing block of an unconditional transition for null.
     */
    fun convert(block: KstatesParser.Condition_blockContext?): Condition {
        if (block == null) {
            return alwaysTrue()
        }
        val clauses = block.condition_clause().mapNotNull { convertClause(it) }
        return when (clauses.size) {
            0 -> alwaysTrue()
            1 -> clauses.single()
            else -> CompositeCondition(clauses).also { session.record(it, block) }
        }
    }

    private fun convertClause(clause: KstatesParser.Condition_clauseContext): Condition? {
        if (clause.containsSyntaxError()) {
            return null
        }
        clause.eval_statement()?.let { statement ->
            val body = evalBodies.read(statement)
            return EvalCondition(body.code).also { session.record(it, clause, codeRange = body.codeRange) }
        }
        clause.event_condition()?.let { event ->
            val strings = event.presentTokens(KstatesParser.STRING)
            if (strings.size < 2) {
                return null
            }
            return EventCondition(unquoted(strings[0]), unquoted(strings[1])).also { session.record(it, clause, strings[1]) }
        }
        val value = clause.value_condition() ?: return null
        val keyToken = value.presentToken(KstatesParser.STRING) ?: return null
        return ValueCondition(unquoted(keyToken)).also { session.record(it, clause, keyToken) }
    }

    /**
     * Returns the condition of a transition without condition, which has no location.
     */
    private fun alwaysTrue(): Condition = ValueCondition("true")
}
