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
import io.karpfen.dsl.states.State

/**
 * Writes a state with its `ENTRY` and `DO` blocks and, nested in it, its inner states in order.
 *
 * @param actions Writes the `ENTRY` and `DO` blocks.
 * @param syntax The shared pieces of KStates syntax.
 */
internal class StateWriter(
    private val actions: ActionWriter,
    private val syntax: KstatesSyntax
) {

    /**
     * Writes [state] as `STATE "name" { ... }`, with `INITIAL` in front for an initial state, or as
     * `STATE "name" { }` when it has neither actions nor inner states.
     *
     * @throws IllegalArgumentException If a name, path or value cannot be written as a string literal, or an
     * action is of a kind KStates cannot express.
     */
    fun write(state: State, writer: CodeWriter) {
        val initial = if (state.isInitial) "INITIAL " else ""
        writer.block("${initial}STATE ${syntax.quote(state.name)}") {
            actions.writeSection("ENTRY", state.onEntry, writer)
            actions.writeSection("DO", state.onDo, writer)
            state.innerStates.forEach { write(it, writer) }
        }
    }
}
