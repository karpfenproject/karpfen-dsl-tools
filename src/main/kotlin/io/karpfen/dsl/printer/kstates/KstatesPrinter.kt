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
package io.karpfen.dsl.printer.kstates

import io.karpfen.dsl.printer.CodeWriter
import io.karpfen.dsl.printer.DocumentPrinter
import io.karpfen.dsl.states.Macro
import io.karpfen.dsl.states.State
import io.karpfen.dsl.states.StateMachine
import io.karpfen.dsl.states.TransitionLike

/**
 * Writes [StateMachine]s, and single states, transitions and macros of them, as KStates text in the
 * canonical layout.
 *
 * The layout indents by four spaces, writes one statement per line and separates the `STATES`,
 * `TRANSITIONS` and `MACROS` sections by a blank line. States, transitions, macros and actions follow the
 * order of the state machine; the `MACROS` section is left out when there are no macros, and so are empty
 * `ENTRY` and `DO` blocks and the `CONDITION` block of an unconditional transition. The code of an `EVAL` block
 * keeps its tokens and comments but is re-indented: code on one line stays on the line of the block, other code goes
 * onto lines of its own between the braces, its first line one level deeper than the block and the lines after it
 * with their indentation relative to each other. Their indentation relative to the first line is not kept, since
 * the code holds its first line without the indentation it had. A line that continues a string literal after an
 * escaped line break stays exactly as it is; Python's triple-quoted strings are no KStates literals, so their lines
 * are re-indented like the others. Comments outside `EVAL` blocks are not part of a state machine and are not
 * written, so reading a KStates text and writing it again drops them. A single state, transition or macro is written as a fragment, see
 * [DocumentPrinter].
 *
 * The printer holds no state; one instance serves any number of calls, also concurrent ones on state
 * machines that are not changed meanwhile.
 *
 * @param states Writes states.
 * @param transitions Writes transitions.
 * @param macros Writes macros.
 * @param syntax The shared pieces of KStates syntax.
 */
class KstatesPrinter internal constructor(
    private val states: StateWriter,
    private val transitions: TransitionWriter,
    private val macros: MacroWriter,
    private val syntax: KstatesSyntax
) : DocumentPrinter<StateMachine> {

    /**
     * Creates a printer.
     */
    constructor() : this(KstatesSyntax())

    private constructor(syntax: KstatesSyntax) : this(
        StateWriter(ActionWriter(syntax), syntax),
        TransitionWriter(ConditionWriter(syntax), syntax),
        MacroWriter(syntax),
        syntax
    )

    /**
     * Returns [document] as a KStates document.
     *
     * @throws IllegalArgumentException If a name, path or value cannot be written as a KStates string
     * literal, or the state machine holds something KStates cannot express, such as a split with a single
     * target, an action rule with another number of operands than its operation expects, `EVAL` code whose braces
     * do not balance, or a typed call that a macro directive cannot hold, or if its states or actions nest deeper than
     * the printer can recurse.
     */
    override fun print(document: StateMachine): String = CodeWriter.document { writer ->
        writer.block("STATEMACHINE ATTACHED TO ${syntax.quote(document.attachedToClass)}") {
            writer.block("STATES") { document.states.forEach { states.write(it, writer) } }
            writer.blankLine()
            writer.block("TRANSITIONS") { document.transitions.forEach { transitions.write(it, writer) } }
            if (document.macros.isNotEmpty()) {
                writer.blankLine()
                writer.block("MACROS") { document.macros.forEach { macros.write(it, writer) } }
            }
        }
    }

    /**
     * Returns [state] with its `ENTRY` and `DO` blocks and its inner states, as a fragment whose first line stands
     * at [indentLevel].
     *
     * @throws IllegalArgumentException If the state cannot be written, see [print], or [indentLevel] is
     * negative.
     */
    fun printState(state: State, indentLevel: Int = 0): String =
        CodeWriter.fragment(indentLevel) { states.write(state, it) }

    /**
     * Returns [transition], a `TRANSITION`, `SPLIT` or `JOIN` with its condition, as a fragment whose first line
     * stands at [indentLevel].
     *
     * @throws IllegalArgumentException If the transition cannot be written, see [print], or [indentLevel] is
     * negative.
     */
    fun printTransition(transition: TransitionLike, indentLevel: Int = 0): String =
        CodeWriter.fragment(indentLevel) { transitions.write(transition, it) }

    /**
     * Returns [macro] with its directives and its definition, as a fragment whose first line stands at
     * [indentLevel].
     *
     * @throws IllegalArgumentException If the macro cannot be written, see [print], or [indentLevel] is
     * negative.
     */
    fun printMacro(macro: Macro, indentLevel: Int = 0): String =
        CodeWriter.fragment(indentLevel) { macros.write(macro, it) }
}
