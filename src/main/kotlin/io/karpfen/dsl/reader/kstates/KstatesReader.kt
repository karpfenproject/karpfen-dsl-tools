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
import io.karpfen.dsl.states.State
import io.karpfen.dsl.states.StateMachine
import org.antlr.v4.runtime.CommonTokenStream
import org.slf4j.LoggerFactory

/**
 * Reads KStates text into a [StateMachine] without throwing, see [TextReader].
 *
 * A text without errors yields the state machine that
 * [io.karpfen.dsl.dsl.textual.KstatesDSLConverter.parseKstatesString] builds from it; like the converter, the
 * reader reads the text as written. The reader reports syntax errors, action rules written with another number of
 * operands than their operation expects, and a state machine that does not mark exactly one state as `INITIAL`.
 * The count of initial states is left out when the `STATES` block is missing or contains a syntax error, since
 * the parser may have lost a state or its `INITIAL` while it recovered.
 *
 * A partial state machine leaves out states, macros and `WITH` blocks without a name, transitions without
 * their states, and every action rule, condition clause and takes or returns directive that contains a syntax
 * error or has the wrong number of operands. A transition whose condition clauses are all left out becomes
 * unconditional; a macro without a readable returns directive or definition gets an empty one.
 */
class KstatesReader : TextReader<StateMachine> {

    /** Receives the failures of reads, see [TextReader]. */
    private val logger = LoggerFactory.getLogger(KstatesReader::class.java)

    /** Reads [text] into a state machine, as described for the class. */
    override fun read(text: String): ParseResult<StateMachine> =
        ReadingSession.read(LexerInput.asWritten(text), StringLiteralSyntax.KSTATES, logger) { session ->
            val machine = parse(session).statemachine()
            val evalBodies = EvalBodies(session)
            val states = StateConverter(session, ActionConverter(session, evalBodies))
                .convert(machine?.states_block()?.state_definition().orEmpty(), null)
            val transitions = TransitionConverter(session, ConditionConverter(session, evalBodies))
                .convert(machine?.transitions_block()?.transition_definition().orEmpty())
            val macros = MacroConverter(session, evalBodies).convert(machine?.macros_block()?.macro_definition().orEmpty())
            checkInitialStates(machine?.states_block(), states, session)
            val classToken = machine?.presentToken(KstatesParser.STRING)
            val stateMachine = StateMachine(classToken?.let { unquoted(it) } ?: "", states, transitions, macros)
            machine?.let { session.record(stateMachine, it, classToken) }
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

    /** Parses the text of [session], whose lexer and parser report their syntax errors to it. */
    private fun parse(session: ReadingSession): KstatesParser.Kstates_fileContext {
        val lexer = session.reportingSyntaxErrors(KstatesLexer(session.charStream()))
        val parser = session.reportingSyntaxErrors(KstatesParser(CommonTokenStream(lexer)))
        return parser.kstates_file()
    }
}
