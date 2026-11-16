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

import io.karpfen.dsl.composition.FlatteningResult
import io.karpfen.dsl.diagnostics.DiagnosticCode
import io.karpfen.dsl.parser.EvalCodeReferences
import io.karpfen.dsl.reader.kstates.MacroArgumentCount
import io.karpfen.dsl.source.SourceRange
import io.karpfen.dsl.source.TextSpans
import io.karpfen.dsl.states.Macro
import io.karpfen.dsl.states.StateMachine
import io.karpfen.dsl.states.StateMachineContents

/**
 * Warns about the macro calls of a flattened composition that only another file of the composition resolves: a file
 * may call any macro of the composition, but each file should define the macros it calls, so that it reads on its own.
 * Such a call is checked for its number of arguments here, since the reader of its file cannot find the macro, with the
 * rule the reader applies to the calls of the macros of its own text, see [MacroArgumentCount].
 *
 * The problems are reported at the call: a `MACRO(...)` call at the macro name, an `@name(...)` call at the name in the
 * code.
 *
 * @param flattened The flattened composition.
 */
internal class ForeignMacroCheck(private val flattened: FlatteningResult.Flattened) {

    /** The macros of the composition by name. */
    private val macrosByName: Map<String, Macro> = flattened.machine.macros.associateBy { it.name }

    /** Checks the calls of each file of the composition and adds the problems to [findings]. */
    fun check(findings: MachineFindings) {
        for (source in flattened.sources) {
            val input = findings.inputOf(source)
            val report = MachineReport(input.read.sourceMap)
            val local = source.macros.map { it.name }.toSet()
            val contents = StateMachineContents(source)
            for (call in contents.macroCalls.filter { it.macroName !in local }) {
                checkCall(call.macroName, call.args.size, fallsBack = true, report.rangeOf(call), report)
            }
            for ((holder, code) in contents.evalBodies) {
                val codeRange = report.codeRangeOf(holder) ?: continue
                for (call in EvalCodeReferences.macroCallsIn(code).filter { it.name !in local }) {
                    val range = TextSpans.rangeWithin(codeRange, code, call.start, call.nameEnd)
                    checkCall(call.name, call.argumentCount, fallsBack = false, range, report)
                }
            }
            findings.add(input, report.diagnostics)
        }
    }

    /**
     * Warns about the call of the macro [name] with [argumentCount] arguments, null if they cannot be counted, written
     * at [range], and checks the count; if [fallsBack], the runtime reads a missing argument from the path named like
     * its parameter.
     */
    private fun checkCall(name: String, argumentCount: Int?, fallsBack: Boolean, range: SourceRange, report: MachineReport) {
        val macro = macrosByName[name] ?: return
        val definedIn = flattened.sources.first { source -> source.macros.any { it === macro } }
        report.report(
            DiagnosticCode.MACRO_FROM_OTHER_FILE,
            range,
            "Macro '$name' is defined in ${describe(definedIn)}, not in this file; the call works, but define the " +
                "macros a file calls in the file itself"
        )
        argumentCount?.let { count ->
            MacroArgumentCount.problemOf(macro, count, fallsBack)?.let { (code, message) -> report.report(code, range, message) }
        }
    }

    /** Returns how a message names [machine]. */
    private fun describe(machine: StateMachine): String = when {
        machine.isFragment -> "the fragment '${machine.name}'"
        machine.isAttachable -> "the state machine '${machine.name}'"
        else -> "the state machine attached to '${machine.attachedToClass}'"
    }
}
