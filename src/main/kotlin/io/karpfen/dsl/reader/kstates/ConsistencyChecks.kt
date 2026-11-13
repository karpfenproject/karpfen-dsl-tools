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
import io.karpfen.dsl.states.StateMachine
import io.karpfen.dsl.states.StateMachineContents
import org.antlr.v4.runtime.TokenStream

/**
 * Runs the checks of a KStates text that need nothing but the text itself, once the state machine is read: names
 * defined twice ([DuplicateStateCheck], [MacroDefinitionCheck]), names of states and macros that are not defined and
 * repeated states of splits and joins ([StateReferenceCheck], [MacroCallCheck]), misplaced `EVENT` clauses
 * ([EventClauseCheck]), options without effect ([StateOptionCheck]) and `EVAL` code that does not run as meant
 * ([EvalBodyCheck]).
 *
 * The checks only report; the state machine stays as the reader built it.
 *
 * @param session The read the state machine belongs to; it holds the locations and receives the problems.
 */
internal class ConsistencyChecks(private val session: ReadingSession) {

    /**
     * Checks [machine], read from the parse tree [tree] of the tokens [tokens].
     */
    fun check(machine: StateMachine, tree: KstatesParser.StatemachineContext?, tokens: TokenStream) {
        val contents = StateMachineContents(machine)
        DuplicateStateCheck(session).check(contents.states)
        StateReferenceCheck(session).check(contents.states, machine.transitions)
        MacroDefinitionCheck(session).check(machine.macros)
        MacroCallCheck(session).check(machine.macros, contents.macroCalls, contents.evalBodies)
        EventClauseCheck(session).check(machine.transitions)
        StateOptionCheck(session).check(tree)
        EvalBodyCheck(session).check(tree, tokens, contents.evalBodies)
    }
}
