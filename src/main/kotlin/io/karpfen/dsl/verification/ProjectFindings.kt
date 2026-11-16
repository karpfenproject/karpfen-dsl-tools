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
import io.karpfen.dsl.diagnostics.Severity

/**
 * The problems [ProjectVerifier] found in a project, each in the text it concerns: most in the text of a state machine,
 * those of the `behaves after` clauses in the text of the domain metamodel or of the event metamodel.
 *
 * The problems only add to the diagnostics of the reads; they never repeat them. Their ranges lie in the text they
 * belong to.
 *
 * @param stateMachines The state machines of the verified project, in the order of the input.
 * @param findings The problems of each state machine, sorted by their offset, in the same order.
 * @property metamodel The problems found in the text of the domain metamodel, sorted by their offset.
 * @property events The problems found in the text of the event metamodel, sorted by their offset; empty for a project
 * without one.
 */
class ProjectFindings internal constructor(
    private val stateMachines: List<StateMachineInput>,
    private val findings: List<List<Diagnostic>>,
    val metamodel: List<Diagnostic> = emptyList(),
    val events: List<Diagnostic> = emptyList()
) {

    /**
     * Every problem found: those of the state machines, the problems of the first state machine first, then those of the
     * domain metamodel, then those of the event metamodel.
     */
    val all: List<Diagnostic>
        get() = findings.flatten() + metamodel + events

    /** The problems of severity error among [all]. */
    val errors: List<Diagnostic>
        get() = all.filter { it.severity == Severity.ERROR }

    /** The problems of severity warning among [all]. */
    val warnings: List<Diagnostic>
        get() = all.filter { it.severity == Severity.WARNING }

    /**
     * Returns the problems found in the text of [stateMachine], sorted by their offset.
     *
     * @throws IllegalArgumentException If [stateMachine] is not one of the verified state machines.
     */
    fun of(stateMachine: StateMachineInput): List<Diagnostic> {
        val index = stateMachines.indexOfFirst { it === stateMachine }
        require(index >= 0) { "The state machine was not verified" }
        return findings[index]
    }
}
