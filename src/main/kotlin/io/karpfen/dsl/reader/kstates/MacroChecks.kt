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

import io.karpfen.dsl.diagnostics.DiagnosticCode
import io.karpfen.dsl.parser.CodeMacroCall
import io.karpfen.dsl.parser.EvalCodeReferences
import io.karpfen.dsl.reader.ReadingSession
import io.karpfen.dsl.reader.repetitionsBy
import io.karpfen.dsl.source.SourceRange
import io.karpfen.dsl.source.TextSpans
import io.karpfen.dsl.states.Macro
import io.karpfen.dsl.states.actions.MacroActionRightSide

/**
 * Reports the macros of a KStates text whose name an earlier macro has, and the `TAKES` parameters of a macro whose
 * name an earlier parameter of the same macro has, each at its name.
 *
 * The runtime calls the first macro of a name, and a Python function cannot take two parameters of the same name. The
 * check only reports.
 *
 * @param session The read the macros belong to; it holds their locations and receives the problems.
 */
internal class MacroDefinitionCheck(private val session: ReadingSession) {

    /**
     * Checks [macros], the macros of the text in text order.
     */
    fun check(macros: List<Macro>) {
        for (repetition in macros.repetitionsBy { it.name }) {
            session.report(
                DiagnosticCode.DUPLICATE_MACRO,
                session.problemRangeOf(repetition.element),
                "There is already a macro named '${repetition.element.name}' in line " +
                    "${session.locationOf(repetition.first)?.range?.start?.line ?: 0}; the runtime only ever calls that one"
            )
        }
        for (macro in macros) {
            for (repetition in macro.takes.repetitionsBy { it.paramName }) {
                session.report(
                    DiagnosticCode.DUPLICATE_PARAMETER,
                    session.problemRangeOf(repetition.element),
                    "Macro '${macro.name}' already takes a parameter named '${repetition.element.paramName}'; " +
                        "every parameter needs a name of its own"
                )
            }
        }
    }
}

/**
 * Reports the calls of macros that the KStates text does not define, and the calls that pass another number of
 * arguments than the macro takes: the `MACRO(...)` calls of actions and `WITH` blocks, and the `@name(...)` calls in the
 * code of `EVAL` bodies, which the runtime turns into calls of Python functions.
 *
 * Another number of arguments is an error for an `@name(...)` call, which Python rejects, and for a `MACRO(...)` call
 * that passes more arguments than the macro takes. A `MACRO(...)` call that passes fewer is only warned about, since the
 * runtime reads each missing argument from the path named like its parameter.
 *
 * A call is matched with the first macro of its name, as the runtime does. The check is left out for a text with a
 * syntax error, since the parser may have lost a macro or one of its `TAKES`. It only reports.
 *
 * A fragment, and a state machine with a container state, is part of a composition whose macros form one set, see
 * [io.karpfen.dsl.composition.StateMachineFlattener]. A call in such a text may name a macro that another file of the
 * composition defines, so a call of a macro the text does not define is not reported there, neither as unknown nor for
 * its number of arguments; the verification of the project checks it against the whole composition.
 *
 * @param session The read the calls belong to; it holds their locations and receives the problems.
 * @param foreignMacrosAllowed True if calls may name macros other files define, see above.
 */
internal class MacroCallCheck(private val session: ReadingSession, private val foreignMacrosAllowed: Boolean = false) {

    /**
     * Checks [calls], the `MACRO(...)` calls of the text, and the `@name(...)` calls in [evalBodies], the elements
     * that hold the code of an `EVAL` with that code, against [macros].
     */
    fun check(macros: List<Macro>, calls: List<MacroActionRightSide>, evalBodies: List<Pair<Any, String>>) {
        if (session.hasSyntaxErrors()) {
            return
        }
        val macrosByName = macros.reversed().associateBy { it.name }
        calls.forEach { call ->
            checkCall(call.macroName, call.args.size, macrosByName, session.problemRangeOf(call), fallsBack = true)
        }
        for ((holder, code) in evalBodies) {
            val codeRange = session.locationOf(holder)?.codeRange ?: continue
            EvalCodeReferences.macroCallsIn(code).forEach { checkCodeCall(it, code, codeRange, macrosByName) }
        }
    }

    /**
     * Checks the call `@name(...)` [call] in [code], which stands at [codeRange].
     */
    private fun checkCodeCall(call: CodeMacroCall, code: String, codeRange: SourceRange, macrosByName: Map<String, Macro>) {
        val range = TextSpans.rangeWithin(codeRange, code, call.start, call.nameEnd)
        val count = call.argumentCount
        if (count == null) {
            if (call.name !in macrosByName && !foreignMacrosAllowed) reportUnknown(call.name, range)
            return
        }
        checkCall(call.name, count, macrosByName, range, fallsBack = false)
    }

    /**
     * Checks a call of the macro [name] with [argumentCount] arguments, written at [range]; if [fallsBack], the runtime
     * reads a missing argument from the path named like its parameter.
     */
    private fun checkCall(
        name: String,
        argumentCount: Int,
        macrosByName: Map<String, Macro>,
        range: SourceRange,
        fallsBack: Boolean
    ) {
        val macro = macrosByName[name]
        if (macro == null) {
            if (!foreignMacrosAllowed) reportUnknown(name, range)
            return
        }
        MacroArgumentCount.problemOf(macro, argumentCount, fallsBack)?.let { (code, message) -> session.report(code, range, message) }
    }

    /** Reports a call of the macro [name], which the text does not define, at [range]. */
    private fun reportUnknown(name: String, range: SourceRange) {
        session.report(
            DiagnosticCode.UNKNOWN_MACRO,
            range,
            "No macro is named '$name'; define it in the MACROS block or check the spelling"
        )
    }
}

/**
 * The rule for the number of arguments of a macro call, which the reader checks for the calls of the macros of a text
 * and the verification of a project for the calls of macros that another file of a composition defines.
 */
internal object MacroArgumentCount {

    /**
     * Returns the kind and the message of the problem of a call of [macro] with [argumentCount] arguments, or null if
     * the number fits; if [fallsBack], the runtime reads a missing argument from the path named like its parameter.
     */
    fun problemOf(macro: Macro, argumentCount: Int, fallsBack: Boolean): Pair<DiagnosticCode, String>? {
        val name = macro.name
        val takes = macro.takes.size
        return when {
            fallsBack && argumentCount < takes -> {
                val missing = macro.takes.drop(argumentCount).joinToString(", ") { "'${it.paramName}'" }
                DiagnosticCode.MISSING_MACRO_ARGUMENTS to "Macro '$name' takes $takes ${plural(takes)}, but the call " +
                    "passes $argumentCount; the runtime reads each missing one from the path named like its parameter, " +
                    "here $missing"
            }
            takes != argumentCount -> DiagnosticCode.MACRO_ARGUMENT_COUNT to
                "Macro '$name' takes $takes ${plural(takes)}, but the call passes $argumentCount; pass one argument per TAKES"
            else -> null
        }
    }

    /** Returns the word for [count] arguments. */
    private fun plural(count: Int): String = if (count == 1) "argument" else "arguments"
}
