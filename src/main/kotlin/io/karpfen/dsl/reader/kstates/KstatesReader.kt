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
import io.karpfen.dsl.reader.ParseResult
import io.karpfen.dsl.reader.ReadingSession
import io.karpfen.dsl.reader.TextReader
import io.karpfen.dsl.reader.presentToken
import io.karpfen.dsl.source.SourceRange
import io.karpfen.dsl.states.StateMachine
import org.antlr.v4.runtime.CommonTokenStream

/**
 * Reads KStates text into a [StateMachine] without throwing, see [TextReader].
 *
 * A text without errors yields the state machine that
 * [io.karpfen.dsl.dsl.textual.KstatesDSLConverter.parseKstatesString] builds from it. The reader reports
 * syntax errors, action rules written with another number of operands than their operation expects, and a
 * state machine that does not mark exactly one state as `INITIAL`.
 *
 * A partial state machine leaves out states, macros and `WITH` blocks without a name, transitions without
 * their states, and every action rule, condition clause and takes or returns directive that contains a syntax
 * error or has the wrong number of operands. A transition whose condition clauses are all left out becomes
 * unconditional; a macro without a readable returns directive or definition gets an empty one.
 */
class KstatesReader : TextReader<StateMachine> {

    override fun read(text: String): ParseResult<StateMachine> = ReadingSession.read(text) { session ->
        val machine = parse(session).statemachine()
        val evalBodies = EvalBodies(session)
        val states = StateConverter(session, ActionConverter(session, evalBodies))
            .convert(machine?.states_block()?.state_definition().orEmpty(), null)
        val transitions = TransitionConverter(session, ConditionConverter(session, evalBodies))
            .convert(machine?.transitions_block()?.transition_definition().orEmpty())
        val macros = MacroConverter(session, evalBodies).convert(machine?.macros_block()?.macro_definition().orEmpty())
        InitialStateCheck(session).check(states, statesRangeOf(machine, session))
        val classToken = machine?.presentToken(KstatesParser.STRING)
        val stateMachine = StateMachine(classToken?.let { unquoted(it) } ?: "", states, transitions, macros)
        machine?.let { session.record(stateMachine, it, classToken) }
        stateMachine
    }

    private fun parse(session: ReadingSession): KstatesParser.Kstates_fileContext {
        val lexer = session.reportingSyntaxErrors(KstatesLexer(session.charStream()))
        val parser = session.reportingSyntaxErrors(KstatesParser(CommonTokenStream(lexer)))
        return parser.kstates_file()
    }

    /**
     * Returns where a problem with the states as a whole is marked: the `STATES` keyword, or the state machine
     * if that is missing.
     */
    private fun statesRangeOf(machine: KstatesParser.StatemachineContext?, session: ReadingSession): SourceRange {
        val keyword = machine?.states_block()?.presentToken(KstatesParser.STATES)
        return keyword?.let { session.locator.rangeOf(it) }
            ?: machine?.let { session.locator.rangeOf(it) }
            ?: session.locator.startOfText()
    }
}
