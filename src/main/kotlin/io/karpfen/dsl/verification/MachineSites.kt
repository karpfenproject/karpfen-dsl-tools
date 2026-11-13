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

import io.karpfen.dsl.parser.ConditionClauses
import io.karpfen.dsl.parser.EvalCodeReferences
import io.karpfen.dsl.source.SourceRange
import io.karpfen.dsl.source.TextSpans
import io.karpfen.dsl.states.Macro
import io.karpfen.dsl.states.StateMachine
import io.karpfen.dsl.states.StateMachineContents
import io.karpfen.dsl.states.actions.ActionRule
import io.karpfen.dsl.states.actions.EvalActionRightSide
import io.karpfen.dsl.states.actions.InScopeBlock
import io.karpfen.dsl.states.actions.MacroActionRightSide
import io.karpfen.dsl.states.actions.WithBlock
import io.karpfen.dsl.states.conditions.EvalCondition
import io.karpfen.dsl.states.conditions.ValueCondition

/**
 * An action rule of a state and what its paths can start from.
 *
 * @property rule The rule.
 * @property scope What the paths read by the rule can start from.
 */
internal class RuleSite(val rule: ActionRule, val scope: ReadScope)

/**
 * Where a path is read.
 */
internal enum class ReadKind {

    /** A `$(path)` in the code of an `EVAL`. */
    CODE,

    /** The path of a `VALUE` condition. */
    VALUE_CONDITION,

    /** An argument of a `MACRO(...)` call, which the runtime resolves to the value it passes. */
    MACRO_ARGUMENT
}

/**
 * A path read at some place of a state machine.
 *
 * @property path The path as written.
 * @property range Where the path is written.
 * @property scope What the path can start from.
 * @property kind Where the path is read.
 * @property parameter For a macro argument, the parameter it is passed to with its type where known, otherwise null.
 */
internal class ReadSite(
    val path: String,
    val range: SourceRange,
    val scope: ReadScope,
    val kind: ReadKind,
    val parameter: Pair<String, MacroType?>? = null
)

/**
 * Finds the places of a state machine where the runtime follows paths: the action rules of its states, and every path
 * it reads, in the code of `EVAL` bodies, in `VALUE` conditions and as arguments of `MACRO(...)` calls.
 *
 * A path in an `IF IN SCOPE` block is only tested by the runtime, so it is no read; the paths inside the block carry the
 * block as a guard. The arguments of a call are read only up to the number of parameters of the macro the call names,
 * and not at all for a macro that is not defined, since the runtime then fails before it reads them.
 *
 * @param machine The state machine.
 * @param report The report of the machine, which knows where its elements are written.
 * @param eventScopes The events in scope across the machine, or null if they cannot be told.
 * @param macroTypes The reader of the types of macros, or null if the types of the project are unknown.
 */
internal class MachineSites(
    private val machine: StateMachine,
    private val report: MachineReport,
    private val eventScopes: EventScopes?,
    private val macroTypes: MacroTypes?
) {

    /** The macros by name; the first of a name wins, as in the runtime. */
    private val macrosByName: Map<String, Macro> = machine.macros.reversed().associateBy { it.name }

    /** The action rules of the states, in text order. */
    val rules: List<RuleSite>

    /** The paths read, the paths of the states first, then those of the transitions, then those of the macros. */
    val reads: List<ReadSite>

    init {
        val foundRules = mutableListOf<RuleSite>()
        val foundReads = mutableListOf<ReadSite>()
        collectStates(foundRules, foundReads)
        collectTransitions(foundReads)
        collectMacros(foundReads)
        rules = foundRules
        reads = foundReads
    }

    /** Returns the macro the runtime calls for the name [name], or null. */
    fun macroNamed(name: String): Macro? = macrosByName[name]

    /** Adds the rules of the states to [rules] and the paths they read to [reads]. */
    private fun collectStates(rules: MutableList<RuleSite>, reads: MutableList<ReadSite>) {
        for (placed in StateMachineContents(machine).actions) {
            val scope = ReadScope(
                events = eventScopes?.ofState(placed.state),
                bindings = placed.surroundings.withBlocks.map { Binding(it.name, returnTypeOf(it.macro.macroName)) },
                guards = placed.surroundings.inScopeBlocks.flatMap { it.paths }
            )
            when (val item = placed.item) {
                is ActionRule -> {
                    rules.add(RuleSite(item, scope))
                    when (val rightSide = item.rightSide) {
                        is EvalActionRightSide -> reads.addAll(codeReads(rightSide, rightSide.code, scope))
                        is MacroActionRightSide -> reads.addAll(argumentReads(rightSide, scope))
                        else -> Unit
                    }
                }
                is WithBlock -> reads.addAll(argumentReads(item.macro, scope))
                is InScopeBlock -> Unit
            }
        }
    }

    /** Adds the paths the conditions of the transitions read to [reads]. */
    private fun collectTransitions(reads: MutableList<ReadSite>) {
        for (transition in machine.transitions) {
            val scope = ReadScope(events = eventScopes?.ofCondition(transition))
            for (clause in ConditionClauses.clausesOf(transition.condition)) {
                when (clause) {
                    is ValueCondition -> if (!isLiteral(clause.boolVariable)) {
                        reads.add(ReadSite(clause.boolVariable, report.rangeOf(clause), scope, ReadKind.VALUE_CONDITION))
                    }
                    is EvalCondition -> reads.addAll(codeReads(clause, clause.code, scope))
                }
            }
        }
    }

    /** Adds the paths the code of the macros reads to [reads]. */
    private fun collectMacros(reads: MutableList<ReadSite>) {
        for (macro in machine.macros) {
            val parameters = LinkedHashMap<String, MacroType?>()
            macro.takes.forEach { parameters.putIfAbsent(it.paramName, macroTypes?.typeOf(it.paramType)) }
            val scope = ReadScope(events = emptySet(), parameters = parameters)
            val codeBlock = macro.definition.codeBlock
            reads.addAll(codeReads(codeBlock, codeBlock.code, scope))
        }
    }

    /** Returns the `$(path)` reads of [code], held by [holder], with [scope]. */
    private fun codeReads(holder: Any, code: String, scope: ReadScope): List<ReadSite> {
        val codeRange = report.codeRangeOf(holder)
        return EvalCodeReferences.referencesIn(code).map { reference ->
            val range = codeRange?.let { TextSpans.rangeWithin(it, code, reference.start, reference.end) }
                ?: report.rangeOf(holder)
            ReadSite(reference.path, range, scope, ReadKind.CODE)
        }
    }

    /** Returns the arguments [call] passes, read with [scope]. */
    private fun argumentReads(call: MacroActionRightSide, scope: ReadScope): List<ReadSite> {
        val macro = macroNamed(call.macroName) ?: return emptyList()
        return call.args.take(macro.takes.size).mapIndexed { index, argument ->
            val parameter = macro.takes[index]
            ReadSite(
                argument,
                report.operandRangeOf(call, index),
                scope,
                ReadKind.MACRO_ARGUMENT,
                parameter.paramName to macroTypes?.typeOf(parameter.paramType)
            )
        }
    }

    /** Returns the type the macro [name] returns, or null if the macro or its type is unknown. */
    private fun returnTypeOf(name: String): MacroType? =
        macroNamed(name)?.let { macroTypes?.typeOf(it.returns.returnType) }

    /** Returns true if [variable] is one of the literals a `VALUE` condition reads as a constant. */
    private fun isLiteral(variable: String): Boolean = variable.trim().lowercase() in setOf("true", "false")
}
