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

import io.karpfen.dsl.diagnostics.DiagnosticCode
import io.karpfen.dsl.reader.ReadingSession
import io.karpfen.dsl.reader.repetitionsBy
import io.karpfen.dsl.states.JoinTransition
import io.karpfen.dsl.states.SplitTransition
import io.karpfen.dsl.states.State
import io.karpfen.dsl.states.Transition
import io.karpfen.dsl.states.TransitionLike

/**
 * A state that a transition, split, join or timeout names.
 *
 * @property element The transition or the state with the timeout, whose operands hold the name.
 * @property name The name of the state.
 * @property operand The index of the name among the operands of [element], see
 * [io.karpfen.dsl.source.SourceLocation.operandRanges].
 */
internal class StateMention(val element: Any, val name: String, val operand: Int)

/**
 * Reports the states of a KStates state machine whose name an earlier state has, at any depth, at their name.
 *
 * The runtime finds a state by its name, the first one of a name, so a second state of the name can never become
 * active. The check only reports.
 *
 * @param session The read the states belong to; it holds their locations and receives the problems.
 */
internal class DuplicateStateCheck(private val session: ReadingSession) {

    /**
     * Checks [states], every state of the state machine, each before its inner states.
     */
    fun check(states: List<State>) {
        for (repetition in states.repetitionsBy { it.name }) {
            session.report(
                DiagnosticCode.DUPLICATE_STATE,
                session.problemRangeOf(repetition.element),
                "There is already a state named '${repetition.element.name}' in line " +
                    "${session.locationOf(repetition.first)?.range?.start?.line ?: 0}; every state needs a name of its own"
            )
        }
    }
}

/**
 * Reports the names of states that the transitions, splits, joins and timeouts of a KStates state machine use but no
 * state has, and the targets a split names twice and the sources a join names twice.
 *
 * Each problem is reported at the name in question. The check of unknown names is left out for a text with a syntax
 * error, since the parser may have lost the state that a name means. The check only reports.
 *
 * @param session The read the state machine belongs to; it holds the locations and receives the problems.
 */
internal class StateReferenceCheck(private val session: ReadingSession) {

    /**
     * Checks [transitions] and the timeouts of [states], every state of the state machine.
     */
    fun check(states: List<State>, transitions: List<TransitionLike>) {
        if (!session.hasSyntaxErrors()) {
            checkKnown(states.map { it.name }.toSet(), transitions.flatMap { mentionsOf(it) } + states.mapNotNull { timeoutOf(it) })
        }
        transitions.filterIsInstance<SplitTransition>().forEach { split ->
            reportRepeated(mentionsOf(split).drop(1), DiagnosticCode.REPEATED_SPLIT_TARGET, "split", "target")
        }
        transitions.filterIsInstance<JoinTransition>().forEach { join ->
            reportRepeated(mentionsOf(join).dropLast(1), DiagnosticCode.REPEATED_JOIN_SOURCE, "join", "source")
        }
    }

    /** Reports each of [mentions] whose name none of [known] is. */
    private fun checkKnown(known: Set<String>, mentions: List<StateMention>) {
        for (mention in mentions.filter { it.name !in known }) {
            session.report(
                DiagnosticCode.UNKNOWN_STATE,
                session.operandProblemRangeOf(mention.element, mention.operand),
                "No state is named '${mention.name}'; check the spelling or define the state"
            )
        }
    }

    /**
     * Reports each of [mentions], the targets of a split or the sources of a join, whose name an earlier one has.
     */
    private fun reportRepeated(mentions: List<StateMention>, code: DiagnosticCode, kind: String, role: String) {
        for (repetition in mentions.repetitionsBy { it.name }) {
            val mention = repetition.element
            session.report(
                code,
                session.operandProblemRangeOf(mention.element, mention.operand),
                "The $kind names the $role '${mention.name}' twice; each $role may be named once"
            )
        }
    }

    /** Returns the states [transition] names, in the order they are written. */
    private fun mentionsOf(transition: TransitionLike): List<StateMention> {
        val names = when (transition) {
            is Transition -> listOf(transition.fromState, transition.toState)
            is SplitTransition -> listOf(transition.fromState) + transition.toStates
            is JoinTransition -> transition.fromStates + transition.toState
        }
        return names.mapIndexed { index, name -> StateMention(transition, name, index) }
    }

    /**
     * Returns the state the `TIMEOUT` of [state] leads to, or null without a timeout; its target is the last operand
     * of the state, after the fragment a container state uses.
     */
    private fun timeoutOf(state: State): StateMention? = state.timeoutTo?.let {
        StateMention(state, it, session.locationOf(state)?.operandRanges?.lastIndex ?: 0)
    }
}
