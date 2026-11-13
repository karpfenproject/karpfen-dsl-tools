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
package io.karpfen.dsl.parser

import io.karpfen.dsl.states.History

/**
 * The syntax of what a KStates state carries besides its actions and inner states, shared by the parse visitor,
 * the reader and the printer: its history, its tick limit and its breakpoints.
 *
 * A history is written in front of `STATE` as `SHALLOW HISTORY` or `DEEP HISTORY`. A tick limit is the block
 * `TICK LIMIT { DELAY: 3 TICKS TIMEOUT: AFTER 10 TICKS TRANSITION TO "state" }` with either part or both, in any
 * order. A number of ticks is a whole number, and the grammar reads `TICK` and `TICKS` alike. The breakpoints of a
 * state are the block `BREAKPOINT { ENTRY EXIT }`, those of a transition `BREAKPOINT { START END }`, each with either
 * word or both, in any order.
 */
internal object StateOptionSyntax {

    /**
     * Returns the number of ticks written as [text], or null if [text] is no whole number that fits a [Long].
     */
    fun ticksOf(text: String): Long? = text.toLongOrNull()

    /**
     * Returns the message for the number [text] of the `DELAY` or `TIMEOUT` named [part] that is no whole number.
     */
    fun ticksMessage(part: String, text: String): String =
        "The $part of a TICK LIMIT counts whole ticks, but was written as $text."

    /**
     * Returns [count] ticks as they are written, `1 TICK` or for example `3 TICKS`.
     *
     * @throws IllegalArgumentException If [count] is negative, which KStates cannot express.
     */
    fun ticks(count: Long): String {
        require(count >= 0) { "KStates cannot write a negative number of ticks: $count." }
        return "$count ${if (count == 1L) "TICK" else "TICKS"}"
    }

    /**
     * Returns the words in front of `STATE` that give a state [history], ending with a space, or the empty string
     * for a state without one.
     */
    fun historyPrefix(history: History): String = when (history) {
        History.NONE -> ""
        History.SHALLOW -> "SHALLOW HISTORY "
        History.DEEP -> "DEEP HISTORY "
    }

    /**
     * Returns the `BREAKPOINT` block that names [first] and [second], each only when its flag is set, or null when
     * neither is set.
     *
     * @param first The word of the first breakpoint, `ENTRY` or `START`, and whether it is set.
     * @param second The word of the second breakpoint, `EXIT` or `END`, and whether it is set.
     */
    fun breakpointBlock(first: Pair<String, Boolean>, second: Pair<String, Boolean>): String? {
        val words = listOf(first, second).filter { it.second }.map { it.first }
        return if (words.isEmpty()) null else "BREAKPOINT { ${words.joinToString(" ")} }"
    }
}
