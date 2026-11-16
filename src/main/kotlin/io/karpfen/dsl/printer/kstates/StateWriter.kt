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

import io.karpfen.dsl.parser.StateOptionSyntax
import io.karpfen.dsl.printer.CodeWriter
import io.karpfen.dsl.states.State

/**
 * Writes a state with its history, its breakpoints and tick limit, its `ENTRY` and `DO` blocks and, nested in it,
 * its inner states in order.
 *
 * @param actions Writes the `ENTRY` and `DO` blocks.
 * @param syntax The shared pieces of KStates syntax.
 */
internal class StateWriter(
    private val actions: ActionWriter,
    private val syntax: KstatesSyntax
) {

    /**
     * Writes [state] as `STATE "name" { ... }`, with `INITIAL` and its history in front, or as `STATE "name" { }`
     * when it has nothing inside. A container state is written as `CONTAINER STATE "name" USE "fragment"` and a final
     * state as `FINAL STATE "name"`, each with whatever it holds. Inside, the `BREAKPOINT` block comes first on one line, then the `TICK LIMIT`
     * block with its `DELAY` before its `TIMEOUT`, then the actions and the inner states.
     *
     * @throws IllegalArgumentException If a name, path or value cannot be written as a string literal, an action
     * is of a kind KStates cannot express, a number of ticks is negative, or the state has a timeout without the
     * state it leads to or the other way round.
     */
    fun write(state: State, writer: CodeWriter) {
        val initial = if (state.isInitial) "INITIAL " else ""
        val kind = when {
            state.isContainer -> "CONTAINER "
            state.isFinal -> "FINAL "
            else -> ""
        }
        val use = state.fragment?.let { " USE ${syntax.quote(it)}" } ?: ""
        writer.block("$initial${StateOptionSyntax.historyPrefix(state.history)}${kind}STATE ${syntax.quote(state.name)}$use") {
            StateOptionSyntax.breakpointBlock("ENTRY" to state.entryBreakpoint, "EXIT" to state.exitBreakpoint)?.let { writer.line(it) }
            writeTickLimit(state, writer)
            actions.writeSection("ENTRY", state.onEntry, writer)
            actions.writeSection("DO", state.onDo, writer)
            state.innerStates.forEach { write(it, writer) }
        }
    }

    /** Writes the `TICK LIMIT` block of [state], or nothing for a state with neither a delay nor a timeout. */
    private fun writeTickLimit(state: State, writer: CodeWriter) {
        val timeout = state.timeout
        val timeoutTo = state.timeoutTo
        require((timeout == null) == (timeoutTo == null)) {
            "A TIMEOUT needs both its ticks and the state it leads to: $state"
        }
        if (state.delay == null && timeout == null) return
        writer.block("TICK LIMIT") {
            state.delay?.let { writer.line("DELAY: ${StateOptionSyntax.ticks(it)}") }
            if (timeout != null && timeoutTo != null) {
                writer.line("TIMEOUT: AFTER ${StateOptionSyntax.ticks(timeout)} TRANSITION TO ${syntax.quote(timeoutTo)}")
            }
        }
    }
}
