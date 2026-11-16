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
package io.karpfen.dsl.verification

import io.karpfen.dsl.diagnostics.Diagnostic
import io.karpfen.dsl.states.StateMachine
import java.util.IdentityHashMap

/**
 * Collects the problems the verification of a project finds in the text of each state machine, from whichever check
 * and composition they come.
 *
 * A text receives each problem once. A fragment receives the findings of every composition that uses it, so in its
 * text a problem of one kind at one place is kept once, the first one found, even if its message differs between two
 * uses, for example by the flattened name of a state.
 *
 * @param inputs The state machines of the project, in the order of the input.
 */
internal class MachineFindings(private val inputs: List<StateMachineInput>) {

    /** The state machines whose read can be trusted and yields a model, in the order of the input. */
    val readable: List<StateMachineInput> = inputs.filter { ProjectVerifier.isReadable(it.read) && it.read.model != null }

    /** The problems found so far in the text of each state machine. */
    private val found = IdentityHashMap<StateMachineInput, MutableList<Diagnostic>>()

    /**
     * Returns the input whose read yields [machine].
     *
     * @throws NoSuchElementException If [machine] is the model of no input.
     */
    fun inputOf(machine: StateMachine): StateMachineInput = inputs.first { it.read.model === machine }

    /** Adds [diagnostics], found in the text of [input]. */
    fun add(input: StateMachineInput, diagnostics: List<Diagnostic>) {
        found.getOrPut(input) { mutableListOf() }.addAll(diagnostics)
    }

    /** Returns the problems of each state machine, in the order of the input, each list sorted by offset. */
    fun all(): List<List<Diagnostic>> = inputs.map { input ->
        val diagnostics = found[input].orEmpty().distinct()
        val kept = if (input.read.model?.isFragment == true) diagnostics.distinctBy { it.code to it.range } else diagnostics
        kept.sortedBy { it.range.start.offset }
    }
}
