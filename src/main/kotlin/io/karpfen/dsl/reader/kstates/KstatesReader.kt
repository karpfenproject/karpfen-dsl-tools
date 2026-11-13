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

import io.karpfen.dsl.grammar.kstates.KstatesLexer
import io.karpfen.dsl.grammar.kstates.KstatesParser
import io.karpfen.dsl.parser.StringLiteralSyntax
import io.karpfen.dsl.reader.LexerInput
import io.karpfen.dsl.reader.ParseResult
import io.karpfen.dsl.reader.ReadingSession
import io.karpfen.dsl.reader.TextReader
import io.karpfen.dsl.reader.containsSyntaxError
import io.karpfen.dsl.reader.presentToken
import io.karpfen.dsl.states.KstatesFeature
import io.karpfen.dsl.states.State
import io.karpfen.dsl.states.StateMachine
import org.antlr.v4.runtime.CommonTokenStream
import org.slf4j.LoggerFactory

/**
 * Reads KStates text into a [StateMachine] without throwing, see [TextReader].
 *
 * A text without errors yields the state machine that
 * [io.karpfen.dsl.dsl.textual.KstatesDSLConverter.parseKstatesString] builds from it; like the converter, the
 * reader reads the text as written, with one exception: a `DELAY` or `TIMEOUT` whose number of ticks is not whole
 * is reported and left out, where the converter keeps the state without the ticks. The reader reports syntax errors,
 * action rules written with another number of operands than their operation expects, numbers of ticks that are not
 * whole, a state machine that does not mark exactly one state as `INITIAL`, every construct of a
 * [KstatesFeature] that is not among the switched-on features, see [FeatureCheck], which is still part of the state
 * machine, and what [ConsistencyChecks] finds: names defined twice, names of states and macros that are not defined,
 * macro calls with the wrong number of arguments, misplaced `EVENT` clauses, and the warnings about options without
 * effect and `EVAL` code that does not run as meant.
 * The count of initial states is left out when the `STATES` block is missing or contains a syntax error, since
 * the parser may have lost a state or its `INITIAL` while it recovered.
 *
 * A partial state machine leaves out states, macros and `WITH` blocks without a name, transitions without
 * their states, and every action rule, condition clause and takes or returns directive that contains a syntax
 * error or has the wrong number of operands. A transition whose condition clauses are all left out becomes
 * unconditional; a macro without a readable returns directive or definition gets an empty one.
 *
 * @param switchedOn The optional features the text may use. Every feature is on by default, so the reader then
 * reports what the converter rejects and nothing more.
 */
class KstatesReader(private val switchedOn: Set<KstatesFeature> = KstatesFeature.ALL) : TextReader<StateMachine> {

    /** Receives the failures of reads, see [TextReader]. */
    private val logger = LoggerFactory.getLogger(KstatesReader::class.java)

    /** Reads [text] into a state machine, as described for the class. */
    override fun read(text: String): ParseResult<StateMachine> =
        ReadingSession.read(LexerInput.asWritten(text), StringLiteralSyntax.KSTATES, logger) { session ->
            val tokens = tokensOf(session)
            val machine = parse(session, tokens).statemachine()
            val evalBodies = EvalBodies(session)
            val states = StateConverter(session, ActionConverter(session, evalBodies))
                .convert(machine?.states_block()?.state_definition().orEmpty(), null)
            val transitions = TransitionConverter(session, ConditionConverter(session, evalBodies))
                .convert(machine?.transitions_block()?.transition_definition().orEmpty())
            val macros = MacroConverter(session, evalBodies).convert(machine?.macros_block()?.macro_definition().orEmpty())
            checkInitialStates(machine?.states_block(), states, session)
            FeatureCheck(session, switchedOn).check(machine)
            val classToken = machine?.presentToken(KstatesParser.STRING)
            val stateMachine = StateMachine(classToken?.let { unquoted(it) } ?: "", states, transitions, macros)
            machine?.let { session.record(stateMachine, it, classToken) }
            ConsistencyChecks(session).check(stateMachine, machine, tokens)
            stateMachine
        }

    /**
     * Checks the count of initial states among [states], read from [statesBlock], unless the block is missing or
     * contains a syntax error; a missing initial state is marked at the `STATES` keyword.
     */
    private fun checkInitialStates(statesBlock: KstatesParser.States_blockContext?, states: List<State>, session: ReadingSession) {
        if (statesBlock != null && !statesBlock.containsSyntaxError()) {
            InitialStateCheck(session).check(states, session.locator.rangeOf(statesBlock.STATES()))
        }
    }

    /** Returns the tokens of the text of [session], with the comments; the lexer reports its errors to the session. */
    private fun tokensOf(session: ReadingSession): CommonTokenStream =
        CommonTokenStream(session.reportingSyntaxErrors(KstatesLexer(session.charStream())))

    /** Parses [tokens], the tokens of the text of [session], whose parser reports its syntax errors to the session. */
    private fun parse(session: ReadingSession, tokens: CommonTokenStream): KstatesParser.Kstates_fileContext =
        session.reportingSyntaxErrors(KstatesParser(tokens)).kstates_file()
}
