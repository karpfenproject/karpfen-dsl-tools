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

import io.karpfen.dsl.composition.FlatteningProblem
import io.karpfen.dsl.diagnostics.DiagnosticCode
import io.karpfen.dsl.source.SourceRange
import io.karpfen.dsl.source.TextSpans
import io.karpfen.dsl.states.StateMachine
import io.karpfen.dsl.states.StateMachineContents
import io.karpfen.dsl.states.actions.EvalActionRightSide
import io.karpfen.dsl.states.conditions.EvalCondition
import io.karpfen.dsl.states.macros.CodeBlock

/**
 * Reports the problems that prevent a composition from flattening, each in the text of the state machine where it
 * stands: an unknown fragment, a state machine that is no fragment and a cycle at the name after `USE` of the container
 * state, a name clash at the state, two different macros of one name at the later definition, and an unknown macro and
 * a call whose arguments do not fit its macro at the call, for an `@name(...)` call at the name in the code. A call
 * whose arguments do not fit is reported with the code and the message the reader gives a call of a macro of its own
 * text.
 *
 * A problem the reader of that text reports already is left out: a cycle of a fragment that uses itself directly, two
 * different macros of one name in the same text, an unknown macro in a text that is neither a fragment nor has a
 * container state, a call whose arguments do not fit a macro of its own text, and a container state without a fragment
 * name. An unknown fragment and an unknown macro are reported only if every state machine of the project could be
 * read, since the missing one might be in a state machine with a syntax error.
 *
 * One instance serves all compositions of a project, so that a cycle of fragments is reported once, at the place the
 * first composition that runs into it finds, although every composition that starts on the cycle closes it at another
 * container state.
 *
 * @param isAllRead True if every state machine of the project could be read.
 */
internal class FlatteningProblems(private val isAllRead: Boolean) {

    /** The names of the fragments of each cycle reported so far. */
    private val reportedCycles = mutableSetOf<Set<String>>()

    /**
     * Adds each of [problems] that is not left out to [findings]; an unknown macro is left out unless [withMacros],
     * which is false for a composition that starts from a fragment that a state machine of the project uses, since the
     * calls of that fragment may rely on the macros of the state machine that uses it.
     */
    fun report(problems: List<FlatteningProblem>, findings: MachineFindings, withMacros: Boolean) {
        for (problem in problems.filter { isReported(it, withMacros) }) {
            val input = findings.inputOf(problem.machine)
            val report = MachineReport(input.read.sourceMap)
            report.report(codeOf(problem), rangeOf(problem, report), messageOf(problem))
            findings.add(input, report.diagnostics)
        }
    }

    /** Returns true if [problem] is reported, see [FlatteningProblems] and [report]. */
    private fun isReported(problem: FlatteningProblem, withMacros: Boolean): Boolean = when (problem) {
        is FlatteningProblem.UnknownFragment -> isAllRead && problem.fragmentName.isNotEmpty()
        is FlatteningProblem.FragmentCycle -> problem.cycle.size > 1 && reportedCycles.add(problem.cycle.toSet())
        is FlatteningProblem.ConflictingMacros -> problem.machine !== problem.otherMachine
        is FlatteningProblem.UnknownMacro -> isAllRead && withMacros && isInComposition(problem.machine)
        is FlatteningProblem.MacroArgumentMismatch -> problem.machine.macros.none { it.name == problem.macro.name }
        is FlatteningProblem.NotAFragment, is FlatteningProblem.NameClash -> true
    }

    /** Returns the message of [problem]: for a call whose arguments do not fit, the one of the reader. */
    private fun messageOf(problem: FlatteningProblem): String = when (problem) {
        is FlatteningProblem.MacroArgumentMismatch -> problem.detail
        else -> problem.message
    }

    /** Returns true if [machine] is a fragment or has a container state, so that its reader lets unknown macros pass. */
    private fun isInComposition(machine: StateMachine): Boolean =
        machine.isFragment || StateMachineContents(machine).states.any { it.isContainer }

    /** Returns the kind of [problem]. */
    private fun codeOf(problem: FlatteningProblem): DiagnosticCode = when (problem) {
        is FlatteningProblem.UnknownFragment -> DiagnosticCode.UNKNOWN_FRAGMENT
        is FlatteningProblem.NotAFragment -> DiagnosticCode.NOT_A_FRAGMENT
        is FlatteningProblem.FragmentCycle -> DiagnosticCode.FRAGMENT_CYCLE
        is FlatteningProblem.NameClash -> DiagnosticCode.QUALIFIED_NAME_CLASH
        is FlatteningProblem.ConflictingMacros -> DiagnosticCode.CONFLICTING_MACROS
        is FlatteningProblem.UnknownMacro -> DiagnosticCode.UNKNOWN_COMPOSED_MACRO
        is FlatteningProblem.MacroArgumentMismatch -> DiagnosticCode.MACRO_ARGUMENT_COUNT
    }

    /** Returns where [problem] is marked, found with [report]: see [FlatteningProblems]. */
    private fun rangeOf(problem: FlatteningProblem, report: MachineReport): SourceRange = when (problem) {
        is FlatteningProblem.UnknownFragment, is FlatteningProblem.NotAFragment, is FlatteningProblem.FragmentCycle ->
            report.operandRangeOf(problem.element, 0)
        is FlatteningProblem.UnknownMacro ->
            codeRangeOf(problem.call, problem.codeSpan, report) ?: report.rangeOf(problem.call)
        is FlatteningProblem.MacroArgumentMismatch ->
            codeRangeOf(problem.call, problem.codeSpan, report) ?: report.rangeOf(problem.call)
        else -> report.rangeOf(problem.element)
    }

    /**
     * Returns the range of the part [codeSpan] of the code that [call] holds, the name of an `@name(...)` call, or null
     * for another call.
     */
    private fun codeRangeOf(call: Any, codeSpan: IntRange?, report: MachineReport): SourceRange? {
        val span = codeSpan ?: return null
        val code = when (val holder = call) {
            is EvalActionRightSide -> holder.code
            is EvalCondition -> holder.code
            is CodeBlock -> holder.code
            else -> return null
        }
        val codeRange = report.codeRangeOf(call) ?: return null
        return TextSpans.rangeWithin(codeRange, code, span.first, span.last + 1)
    }
}
