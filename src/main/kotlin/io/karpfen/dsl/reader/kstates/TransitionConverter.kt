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
import io.karpfen.dsl.reader.presentTokens
import io.karpfen.dsl.states.JoinTransition
import io.karpfen.dsl.states.SplitTransition
import io.karpfen.dsl.states.Transition
import io.karpfen.dsl.states.TransitionLike
import org.antlr.v4.runtime.ParserRuleContext

/**
 * Converts the transition definitions of a KStates parse tree into [Transition]s, [SplitTransition]s and
 * [JoinTransition]s and records where each is written.
 *
 * A transition is left out if fewer than two of its state names are present.
 *
 * @param session The read the conversion belongs to.
 * @param conditions Converts the `CONDITION` blocks.
 */
internal class TransitionConverter(
    private val session: ReadingSession,
    private val conditions: ConditionConverter
) {

    /**
     * Converts [definitions] in text order.
     */
    fun convert(definitions: List<KstatesParser.Transition_definitionContext>): MutableList<TransitionLike> =
        definitions.mapNotNull { convertDefinition(it) }.toMutableList()

    private fun convertDefinition(definition: KstatesParser.Transition_definitionContext): TransitionLike? {
        definition.normal_transition()?.let { return convertNormal(it) }
        definition.split_transition()?.let { return convertSplit(it) }
        definition.join_transition()?.let { return convertJoin(it) }
        return null
    }

    private fun convertNormal(transition: KstatesParser.Normal_transitionContext): Transition? {
        val states = stateNamesOf(transition) ?: return null
        return recorded(
            Transition(
                fromState = states[0],
                toState = states[1],
                allowLoops = transition.not_looping() == null,
                condition = conditions.convert(transition.condition_block())
            ),
            transition
        )
    }

    private fun convertSplit(transition: KstatesParser.Split_transitionContext): SplitTransition? {
        val states = stateNamesOf(transition) ?: return null
        return recorded(
            SplitTransition(
                fromState = states.first(),
                toStates = states.drop(1),
                allowLoops = transition.not_looping() == null,
                condition = conditions.convert(transition.condition_block())
            ),
            transition
        )
    }

    private fun convertJoin(transition: KstatesParser.Join_transitionContext): JoinTransition? {
        val states = stateNamesOf(transition) ?: return null
        return recorded(
            JoinTransition(
                fromStates = states.dropLast(1),
                toState = states.last(),
                allowLoops = transition.not_looping() == null,
                condition = conditions.convert(transition.condition_block())
            ),
            transition
        )
    }

    /**
     * Returns the state names of [transition] in text order, or null if fewer than two are present.
     */
    private fun stateNamesOf(transition: ParserRuleContext): List<String>? =
        transition.presentTokens(KstatesParser.STRING).map { unquoted(it) }.takeIf { it.size >= 2 }

    private fun <T : TransitionLike> recorded(transition: T, construct: ParserRuleContext): T {
        session.record(transition, construct)
        return transition
    }
}
