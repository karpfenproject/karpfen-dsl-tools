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
import io.karpfen.dsl.reader.containsSyntaxError
import io.karpfen.dsl.reader.presentToken
import org.antlr.v4.runtime.ParserRuleContext

/**
 * Warns about the options of states and transitions that have no effect or hold each other back: a history on a state
 * without inner states other than a container state, whose inner states come from its fragment, and a final state,
 * which may carry no history at all, see [PageChecks], a `BREAKPOINT` block that names neither of its words, a `TICK LIMIT` block without a `DELAY`
 * and a `TIMEOUT`, and a `DELAY` longer than the `TIMEOUT` of the same block, which then fires only when the delay
 * has passed.
 *
 * The check reads the parse tree, since an empty block leaves no trace in the state machine, and leaves out every
 * construct that contains a syntax error. It only reports.
 *
 * @param session The read the parse tree belongs to; it receives the problems.
 */
internal class StateOptionCheck(private val session: ReadingSession) {

    /** Checks [tree] and every context nested in it. */
    fun check(tree: ParserRuleContext?) {
        tree?.let { visit(it) }
    }

    /** Checks [context] and visits the contexts nested in it. */
    private fun visit(context: ParserRuleContext) {
        when (context) {
            is KstatesParser.State_definitionContext -> checkHistory(context)
            is KstatesParser.State_breakpoint_blockContext -> checkBreakpoints(context, context.state_breakpoint_definition())
            is KstatesParser.Transition_breakpoint_blockContext ->
                checkBreakpoints(context, context.transition_breakpoint_definition())
            is KstatesParser.Tick_limit_blockContext -> checkTickLimit(context)
        }
        context.children.orEmpty().filterIsInstance<ParserRuleContext>().forEach(::visit)
    }

    /** Warns at the history of [state] if the state has no inner states. */
    private fun checkHistory(state: KstatesParser.State_definitionContext) {
        val history = state.history_definition() ?: return
        if (state.state_kind()?.children.orEmpty().isNotEmpty()) {
            return
        }
        if (state.state_definition().isEmpty() && !state.containsSyntaxError()) {
            session.report(
                DiagnosticCode.HISTORY_WITHOUT_INNER_STATES,
                session.locator.rangeOf(history),
                "A history remembers the inner state that was active last, but this state has no inner states"
            )
        }
    }

    /** Warns at the `BREAKPOINT` [block] if it names neither of its words, [definition] null. */
    private fun checkBreakpoints(block: ParserRuleContext, definition: ParserRuleContext?) {
        if (definition == null && !block.containsSyntaxError()) {
            session.report(
                DiagnosticCode.EMPTY_BREAKPOINT,
                session.locator.rangeOf(block),
                "This BREAKPOINT block marks nothing; name where to halt or remove the block"
            )
        }
    }

    /** Warns at the `TICK LIMIT` [block] if it is empty, and at its `DELAY` if that is longer than its `TIMEOUT`. */
    private fun checkTickLimit(block: KstatesParser.Tick_limit_blockContext) {
        if (block.containsSyntaxError()) {
            return
        }
        val definition = block.tick_limit_definition()
        if (definition == null) {
            session.report(
                DiagnosticCode.EMPTY_TICK_LIMIT,
                session.locator.rangeOf(block),
                "This TICK LIMIT block limits nothing; add a DELAY or a TIMEOUT or remove the block"
            )
            return
        }
        val delay = definition.delay_definition() ?: return
        val delayTicks = ticksOf(delay) ?: return
        val timeoutTicks = definition.timeout_definition()?.let { ticksOf(it) } ?: return
        if (delayTicks > timeoutTicks) {
            session.report(
                DiagnosticCode.DELAY_BEYOND_TIMEOUT,
                session.locator.rangeOf(delay),
                "The DELAY of $delayTicks ticks holds back the TIMEOUT after $timeoutTicks ticks, " +
                    "so the timeout fires only after the delay"
            )
        }
    }

    /** Returns the whole number of ticks [definition] counts, or null without one. */
    private fun ticksOf(definition: ParserRuleContext): Long? =
        definition.presentToken(KstatesParser.NUMBER)?.let { StateOptionSyntax.ticksOf(it.text) }
}
