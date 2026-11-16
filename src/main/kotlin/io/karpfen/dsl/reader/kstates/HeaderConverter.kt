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
import io.karpfen.dsl.reader.ReadingSession
import io.karpfen.dsl.reader.presentToken
import io.karpfen.dsl.reader.presentTokens
import io.karpfen.dsl.reader.repetitionsBy
import io.karpfen.dsl.states.Macro
import io.karpfen.dsl.states.State
import io.karpfen.dsl.states.StateMachine
import io.karpfen.dsl.states.TransitionLike
import org.antlr.v4.runtime.ParserRuleContext
import org.antlr.v4.runtime.tree.TerminalNode

/**
 * Builds the [StateMachine] from the header of a KStates text, in the form the header has, checks the header and
 * records where the state machine is written.
 *
 * The grammar reads both forms with one rule: an optional name, `ATTACHED` or `ATTACHABLE`, `TO` and one or more
 * types. The attached form takes no name and one type; a name or a further type there is reported at itself. The
 * attachable form needs a name, which is reported at `ATTACHABLE` when it is missing, and lists each type once; a
 * repeated type is reported at the later one. A missing first type is a syntax error, which the parser reports.
 *
 * A fragment, `STATEMACHINE "name" FRAGMENT`, has a rule of its own, see [io.karpfen.dsl.parser.KstatesEntryRule]. It
 * needs a name, which is reported at `FRAGMENT` when it is missing, and has no type.
 *
 * An attached state machine is recorded named by its type, as in the texts before the attachable form. An attachable
 * state machine is recorded named by its name, with its types as its operands, see
 * [io.karpfen.dsl.source.SourceLocation.operandRanges]. A fragment is recorded named by its name, without operands.
 *
 * @param session The read the header belongs to; it receives the problems and the location.
 */
internal class HeaderConverter(private val session: ReadingSession) {

    /**
     * Returns the state machine with [states], [transitions] and [macros] in the form the header of [machine] has,
     * an attached one with an empty type if the text has no header at all; [machine] is the state machine of either
     * entry rule.
     */
    fun convert(
        machine: ParserRuleContext?,
        states: MutableList<State>,
        transitions: MutableList<TransitionLike>,
        macros: MutableList<Macro>
    ): StateMachine {
        if (machine is KstatesParser.Fragment_machineContext) {
            return convertFragment(machine, states, transitions, macros)
        }
        return convertAttachment(machine as? KstatesParser.StatemachineContext, states, transitions, macros)
    }

    /**
     * Returns the fragment [machine] with [states], [transitions] and [macros], named by its name or the empty string. A
     * missing name is reported at `FRAGMENT`, an empty one at the empty string.
     */
    private fun convertFragment(
        machine: KstatesParser.Fragment_machineContext,
        states: MutableList<State>,
        transitions: MutableList<TransitionLike>,
        macros: MutableList<Macro>
    ): StateMachine {
        val nameToken = machine.statemachine_name()?.presentToken(KstatesParser.STRING)
        val keyword = machine.presentToken(KstatesParser.FRAGMENT)
        if (nameToken == null && keyword != null) {
            session.report(
                DiagnosticCode.MISSING_STATE_MACHINE_NAME,
                session.locator.rangeOf(keyword),
                "A fragment needs a name, written as a string between STATEMACHINE and FRAGMENT; container states use " +
                    "the fragment by this name"
            )
        }
        reportIfEmpty(nameToken, "A fragment needs a name that is not empty; container states use the fragment by this name")
        val stateMachine = StateMachine.fragment(nameToken?.let { unquoted(it) } ?: "", states, transitions, macros)
        session.record(stateMachine, machine, nameToken)
        return stateMachine
    }

    /**
     * Returns the attached or attachable state machine [machine] with [states], [transitions] and [macros], see
     * [convert].
     */
    private fun convertAttachment(
        machine: KstatesParser.StatemachineContext?,
        states: MutableList<State>,
        transitions: MutableList<TransitionLike>,
        macros: MutableList<Macro>
    ): StateMachine {
        val nameToken = machine?.statemachine_name()?.presentToken(KstatesParser.STRING)
        val firstType = machine?.presentToken(KstatesParser.STRING)
        val furtherTypes = machine?.further_types()?.presentTokens(KstatesParser.STRING).orEmpty()
        val attachable = machine?.presentToken(KstatesParser.ATTACHABLE)
        if (machine == null || attachable == null) {
            checkAttached(nameToken, furtherTypes)
            val stateMachine = StateMachine(firstType?.let { unquoted(it) } ?: "", states, transitions, macros)
            machine?.let { session.record(stateMachine, it, firstType) }
            return stateMachine
        }
        val typeTokens = listOfNotNull(firstType) + furtherTypes
        checkAttachable(attachable, nameToken, typeTokens)
        val stateMachine = StateMachine.attachable(
            nameToken?.let { unquoted(it) } ?: "",
            typeTokens.map { unquoted(it) },
            states,
            transitions,
            macros
        )
        session.record(stateMachine, machine, nameToken, operands = typeTokens)
        return stateMachine
    }

    /** Reports the name [nameToken] and the [furtherTypes] of an attached state machine, which has neither. */
    private fun checkAttached(nameToken: TerminalNode?, furtherTypes: List<TerminalNode>) {
        nameToken?.let {
            session.report(
                DiagnosticCode.ATTACHED_HEADER,
                session.valueProblemRangeOf(it),
                "A state machine ATTACHED TO a type has no name; write ATTACHABLE TO for a state machine with a name"
            )
        }
        for (type in furtherTypes) {
            session.report(
                DiagnosticCode.ATTACHED_HEADER,
                session.valueProblemRangeOf(type),
                "A state machine is ATTACHED TO one type; write ATTACHABLE TO for a state machine that several types use"
            )
        }
    }

    /**
     * Reports an attachable state machine without the name [nameToken] at its keyword [attachable], one with an empty
     * name at the empty string, and each of [typeTokens] that repeats an earlier type.
     */
    private fun checkAttachable(attachable: TerminalNode, nameToken: TerminalNode?, typeTokens: List<TerminalNode>) {
        if (nameToken == null) {
            session.report(
                DiagnosticCode.MISSING_STATE_MACHINE_NAME,
                session.locator.rangeOf(attachable),
                "An attachable state machine needs a name, written as a string between STATEMACHINE and ATTACHABLE"
            )
        }
        reportIfEmpty(nameToken, "An attachable state machine needs a name that is not empty; ATTACH and EXCHANGE use it")
        for (repetition in typeTokens.repetitionsBy { unquoted(it) }) {
            session.report(
                DiagnosticCode.REPEATED_ATTACHABLE_TYPE,
                session.valueProblemRangeOf(repetition.element),
                "The type '${unquoted(repetition.element)}' is listed twice; each type may be listed once"
            )
        }
    }

    /**
     * Reports the name [nameToken] of a state machine as missing, described by [message], if it is written as an empty
     * string; an empty name names nothing.
     */
    private fun reportIfEmpty(nameToken: TerminalNode?, message: String) {
        if (nameToken != null && unquoted(nameToken).isEmpty()) {
            session.report(DiagnosticCode.MISSING_STATE_MACHINE_NAME, session.valueProblemRangeOf(nameToken), message)
        }
    }
}
