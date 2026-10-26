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
import io.karpfen.dsl.states.JoinTransition
import io.karpfen.dsl.states.SplitTransition
import io.karpfen.dsl.states.Transition
import io.karpfen.dsl.states.TransitionLike

/**
 * Writes transitions of all three kinds, `TRANSITION`, `SPLIT` and `JOIN`, with `NOT LOOPING` and their
 * `CONDITION` block.
 *
 * @param conditions Writes the `CONDITION` block.
 * @param syntax The shared pieces of KStates syntax.
 */
internal class TransitionWriter(
    private val conditions: ConditionWriter,
    private val syntax: KstatesSyntax
) {

    /**
     * Writes [transition] as a block with its condition, or as `header { }` when it has none.
     *
     * @throws IllegalArgumentException If a split has fewer than two targets or a join fewer than two
     * sources, which KStates cannot express, or a name cannot be written as a string literal.
     */
    fun write(transition: TransitionLike, writer: CodeWriter) {
        val looping = if (transition.allowLoops) "" else " NOT LOOPING"
        writer.block(headerOf(transition) + looping) { conditions.write(transition.condition, writer) }
    }

    private fun headerOf(transition: TransitionLike): String = when (transition) {
        is Transition -> "TRANSITION ${syntax.quote(transition.fromState)} -> ${syntax.quote(transition.toState)}"
        is SplitTransition -> {
            require(transition.toStates.size >= 2) { "A SPLIT needs at least two target states: $transition" }
            "SPLIT ${syntax.quote(transition.fromState)} -> ${syntax.quoteAll(transition.toStates)}"
        }
        is JoinTransition -> {
            require(transition.fromStates.size >= 2) { "A JOIN needs at least two source states: $transition" }
            "JOIN ${syntax.quoteAll(transition.fromStates)} -> ${syntax.quote(transition.toState)}"
        }
    }
}
