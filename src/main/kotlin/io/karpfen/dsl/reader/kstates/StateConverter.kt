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
import io.karpfen.dsl.grammar.kstates.KstatesParser
import io.karpfen.dsl.parser.StateOptionSyntax
import io.karpfen.dsl.reader.ReadingSession
import io.karpfen.dsl.reader.presentToken
import io.karpfen.dsl.states.History
import io.karpfen.dsl.states.State
import io.karpfen.dsl.states.actions.ActionBlock
import org.antlr.v4.runtime.ParserRuleContext

/**
 * Converts the state definitions of a KStates parse tree into [State]s with their nested states and records
 * where each state and each of its `ENTRY` and `DO` blocks is written.
 *
 * A state without a name is left out together with its nested states. A `DELAY` or `TIMEOUT` whose number of ticks
 * is missing or not whole is left out and, when the number is written, reported; so is a `TIMEOUT` without its
 * target state, which the parser already reported.
 *
 * @param session The read the conversion belongs to.
 * @param actions Converts the `ENTRY` and `DO` blocks.
 */
internal class StateConverter(
    private val session: ReadingSession,
    private val actions: ActionConverter
) {

    /**
     * Converts [definitions] in text order into states nested in [outerState], or top-level states for null.
     */
    fun convert(definitions: List<KstatesParser.State_definitionContext>, outerState: State?): MutableList<State> =
        definitions.mapNotNull { convertState(it, outerState) }.toMutableList()

    /**
     * Converts a state definition with its history, breakpoints and tick limit, its `ENTRY` and `DO` blocks and its
     * nested states, inside [outerState]; null without a name.
     */
    private fun convertState(definition: KstatesParser.State_definitionContext, outerState: State?): State? {
        val nameToken = definition.presentToken(KstatesParser.STRING) ?: return null
        val breakpoints = definition.state_breakpoint_block()?.state_breakpoint_definition()
        val tickLimit = definition.tick_limit_block()?.tick_limit_definition()
        val timeout = tickLimit?.timeout_definition()?.let { timeoutOf(it) }
        val state = State(
            name = unquoted(nameToken),
            onEntry = definition.entry_block()?.let { entry -> actionBlockOf(entry.action_block(), entry) } ?: emptyBlock(),
            onDo = definition.do_block()?.let { doBlock -> actionBlockOf(doBlock.action_block(), doBlock) } ?: emptyBlock(),
            innerStates = mutableListOf(),
            outerState = outerState,
            isInitial = definition.presentToken(KstatesParser.INITIAL) != null,
            entryBreakpoint = breakpoints?.presentToken(KstatesParser.ENTRY) != null,
            exitBreakpoint = breakpoints?.presentToken(KstatesParser.EXIT) != null,
            history = historyOf(definition.history_definition()),
            delay = tickLimit?.delay_definition()?.let { ticksOf("DELAY", it) },
            timeout = timeout?.first,
            timeoutTo = timeout?.second
        )
        state.innerStates.addAll(convert(definition.state_definition(), state))
        session.record(state, definition, nameToken)
        return state
    }

    /** Returns the history written as [definition] in front of `STATE`, [History.NONE] for null or half a history. */
    private fun historyOf(definition: KstatesParser.History_definitionContext?): History {
        val shallow = definition?.shallow_history_definition()
        val deep = definition?.deep_history_definition()
        return when {
            shallow?.presentToken(KstatesParser.HISTORY) != null -> History.SHALLOW
            deep?.presentToken(KstatesParser.HISTORY) != null -> History.DEEP
            else -> History.NONE
        }
    }

    /**
     * Returns the ticks and the target state of the `TIMEOUT` [definition], or null if either is missing or the
     * number of ticks is not whole.
     */
    private fun timeoutOf(definition: KstatesParser.Timeout_definitionContext): Pair<Long, String>? {
        val ticks = ticksOf("TIMEOUT", definition) ?: return null
        val target = definition.presentToken(KstatesParser.STRING) ?: return null
        return ticks to unquoted(target)
    }

    /**
     * Returns the number of ticks of the `DELAY` or `TIMEOUT` [definition] named [part]; null without a number, and
     * null with a report for a number that is not whole.
     */
    private fun ticksOf(part: String, definition: ParserRuleContext): Long? {
        val number = definition.presentToken(KstatesParser.NUMBER) ?: return null
        val ticks = StateOptionSyntax.ticksOf(number.text)
        if (ticks == null) {
            session.report(DiagnosticCode.TICK_COUNT, session.locator.rangeOf(number), StateOptionSyntax.ticksMessage(part, number.text))
        }
        return ticks
    }

    /**
     * Converts the action block of an `ENTRY` or `DO` [section], recorded at the whole section.
     */
    private fun actionBlockOf(
        block: KstatesParser.Action_blockContext?,
        section: ParserRuleContext
    ): ActionBlock = block?.let { actions.convertBlock(it, section) } ?: emptyBlock()

    /** Returns the empty action block of a state without an `ENTRY` or `DO` block, which has no location. */
    private fun emptyBlock(): ActionBlock = ActionBlock(mutableListOf())
}
