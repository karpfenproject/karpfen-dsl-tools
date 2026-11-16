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
import io.karpfen.dsl.grammar.kstates.KstatesParser
import io.karpfen.dsl.parser.ConditionClauses
import io.karpfen.dsl.reader.ReadingSession
import io.karpfen.dsl.reader.containsSyntaxError
import io.karpfen.dsl.reader.presentToken
import io.karpfen.dsl.states.JoinTransition
import io.karpfen.dsl.states.SplitTransition
import io.karpfen.dsl.states.State
import io.karpfen.dsl.states.StateMachine
import io.karpfen.dsl.states.Transition
import io.karpfen.dsl.states.TransitionLike
import io.karpfen.dsl.states.conditions.DoneCondition
import org.antlr.v4.runtime.tree.TerminalNode

/**
 * Checks the rules of pages in a single KStates text: container states, final states, fragments and `DONE`.
 *
 * - A `CONTAINER STATE` names its fragment with `USE "fragment"` and has no `ENTRY` or `DO` block and no inner states;
 *   a breakpoint block and a tick limit are fine. Only a container state has `USE`.
 * - A `FINAL STATE` stands at the top level of a fragment and holds a breakpoint block at most: no tick limit, no
 *   actions, no inner states, no `INITIAL` and no history. No transition, split or join leaves it.
 * - A fragment has at least one final state, and none of its container states uses the fragment itself.
 * - `DONE` stands on a `TRANSITION` or `SPLIT` whose source is a container state, or on a `JOIN` of which at least one
 *   source is a container state.
 *
 * Each problem is an error, reported at the word or the name that breaks the rule. The parts of a state are found in
 * the parse tree, since the model does not keep, for example, an empty `ENTRY` block. The count of final states is
 * left out when the `STATES` block contains a syntax error, and the states a transition names are looked up by name
 * only where a state of the name exists; [StateReferenceCheck] reports the others. The check only reports.
 *
 * @param session The read the state machine belongs to; it holds the locations and receives the problems.
 */
internal class PageChecks(private val session: ReadingSession) {

    /**
     * Checks [machine], whose states, each before its inner states, are [states], read with the `STATES` block
     * [statesBlock].
     */
    fun check(machine: StateMachine, states: List<State>, statesBlock: KstatesParser.States_blockContext?) {
        statesBlock?.state_definition()?.forEach { checkDefinition(it, machine.isFragment, isTopLevel = true) }
        checkFinalStateCount(machine, statesBlock)
        val byName = states.reversed().associateBy { it.name }
        machine.transitions.forEach { checkSources(it, byName) }
        machine.transitions.forEach { checkDone(it, byName) }
        checkSelfUse(machine, states)
    }

    /**
     * Checks the state [definition] and the states nested in it; [inFragment] tells whether the text is a fragment and
     * [isTopLevel] whether the state stands directly in the `STATES` block.
     */
    private fun checkDefinition(definition: KstatesParser.State_definitionContext, inFragment: Boolean, isTopLevel: Boolean) {
        val kind = definition.state_kind()
        val use = definition.fragment_use()?.presentToken(KstatesParser.USE)
        val container = kind?.presentToken(KstatesParser.CONTAINER)
        when {
            container != null -> checkContainer(definition, container, use)
            kind?.presentToken(KstatesParser.FINAL) != null -> checkFinal(definition, use, inFragment, isTopLevel)
            use != null -> report(
                DiagnosticCode.CONTAINER_USE,
                use,
                "Only a container state uses a fragment; write CONTAINER STATE to run the fragment inside this state"
            )
        }
        definition.state_definition().forEach { checkDefinition(it, inFragment, isTopLevel = false) }
    }

    /** Checks the container state [definition], marked by [keyword], whose `USE` is [use] or null. */
    private fun checkContainer(definition: KstatesParser.State_definitionContext, keyword: TerminalNode, use: TerminalNode?) {
        if (use == null && !definition.containsSyntaxError()) {
            report(
                DiagnosticCode.CONTAINER_USE,
                keyword,
                "A container state names the fragment it runs: write USE \"fragment\" after the name of the state"
            )
        }
        val actions = listOfNotNull(
            definition.entry_block()?.presentToken(KstatesParser.ENTRY),
            definition.do_block()?.presentToken(KstatesParser.DO)
        )
        for (block in actions) {
            report(
                DiagnosticCode.CONTAINER_STATE_CONTENT,
                block,
                "A container state has no actions of its own; its fragment does the work, so move the ${block.text} " +
                    "block into a state of the fragment"
            )
        }
        for (inner in definition.state_definition()) {
            val name = inner.presentToken(KstatesParser.STRING) ?: continue
            session.report(
                DiagnosticCode.CONTAINER_STATE_CONTENT,
                session.valueProblemRangeOf(name),
                "A container state has no inner states of its own; its inner states are those of its fragment"
            )
        }
    }

    /**
     * Checks the final state [definition], whose `USE` is [use] or null, in a text that [inFragment] tells to be a
     * fragment or not, at the top level if [isTopLevel].
     */
    private fun checkFinal(
        definition: KstatesParser.State_definitionContext,
        use: TerminalNode?,
        inFragment: Boolean,
        isTopLevel: Boolean
    ) {
        val name = definition.presentToken(KstatesParser.STRING)
        val placement = when {
            !inFragment -> "A FINAL STATE ends a fragment, but this state machine is no fragment"
            !isTopLevel -> "A FINAL STATE stands at the top level of its fragment, not inside another state"
            else -> null
        }
        if (placement != null && name != null) {
            session.report(DiagnosticCode.MISPLACED_FINAL_STATE, session.valueProblemRangeOf(name), placement)
        }
        definition.presentToken(KstatesParser.INITIAL)?.let {
            report(DiagnosticCode.FINAL_STATE_CONTENT, it, "A final state ends the fragment, so it cannot be INITIAL")
        }
        definition.history_definition()?.let {
            session.report(
                DiagnosticCode.FINAL_STATE_CONTENT,
                session.locator.rangeOf(it),
                "A final state has no inner states, so it carries no history"
            )
        }
        use?.let { report(DiagnosticCode.CONTAINER_USE, it, "A final state uses no fragment; only a container state does") }
        val parts = listOfNotNull(
            definition.tick_limit_block()?.presentToken(KstatesParser.TICK)?.let { it to "a TICK LIMIT, since it is never left" },
            definition.entry_block()?.presentToken(KstatesParser.ENTRY)?.let { it to "no ENTRY block" },
            definition.do_block()?.presentToken(KstatesParser.DO)?.let { it to "no DO block" }
        )
        for ((keyword, what) in parts) {
            report(
                DiagnosticCode.FINAL_STATE_CONTENT,
                keyword,
                "A final state holds a BREAKPOINT block at most; it takes $what"
            )
        }
        for (inner in definition.state_definition()) {
            val innerName = inner.presentToken(KstatesParser.STRING) ?: continue
            session.report(
                DiagnosticCode.FINAL_STATE_CONTENT,
                session.valueProblemRangeOf(innerName),
                "A final state holds a BREAKPOINT block at most; it has no inner states"
            )
        }
    }

    /**
     * Reports a fragment without a final state at its top level, at the `STATES` keyword of [statesBlock], unless the
     * block is missing or contains a syntax error.
     */
    private fun checkFinalStateCount(machine: StateMachine, statesBlock: KstatesParser.States_blockContext?) {
        if (!machine.isFragment || statesBlock == null || statesBlock.containsSyntaxError()) {
            return
        }
        if (machine.states.none { it.isFinal }) {
            session.report(
                DiagnosticCode.FRAGMENT_WITHOUT_FINAL_STATE,
                session.locator.rangeOf(statesBlock.STATES()),
                "A fragment needs at least one FINAL STATE at its top level; reaching it makes the container state done"
            )
        }
    }

    /** Reports each source of [transition] that is a final state, at its name; [byName] holds the states by name. */
    private fun checkSources(transition: TransitionLike, byName: Map<String, State>) {
        val sources = when (transition) {
            is Transition -> listOf(transition.fromState)
            is SplitTransition -> listOf(transition.fromState)
            is JoinTransition -> transition.fromStates
        }
        sources.forEachIndexed { index, source ->
            if (byName[source]?.isFinal == true) {
                session.report(
                    DiagnosticCode.TRANSITION_FROM_FINAL_STATE,
                    session.operandProblemRangeOf(transition, index),
                    "'$source' is a final state, which nothing leaves; let a transition of the container state react " +
                        "with DONE instead"
                )
            }
        }
    }

    /**
     * Reports each `DONE` clause of [transition] that speaks about no container state, at the clause; [byName] holds
     * the states by name.
     */
    private fun checkDone(transition: TransitionLike, byName: Map<String, State>) {
        val clauses = ConditionClauses.clausesOf(transition.condition).filterIsInstance<DoneCondition>()
        if (clauses.isEmpty()) {
            return
        }
        val sources = when (transition) {
            is Transition -> listOf(transition.fromState)
            is SplitTransition -> listOf(transition.fromState)
            is JoinTransition -> transition.fromStates
        }
        val known = sources.mapNotNull { byName[it] }
        if (known.size < sources.size || known.any { it.isContainer }) {
            return
        }
        val message = when (transition) {
            is JoinTransition -> "DONE on a join speaks about its sources that are container states, but none of " +
                "${sources.joinToString(", ") { "'$it'" }} is one"
            else -> "DONE holds when the container state a transition leaves has reached a final state, but " +
                "'${sources.single()}' is no container state"
        }
        clauses.forEach { session.report(DiagnosticCode.MISPLACED_DONE, session.problemRangeOf(it), message) }
    }

    /** Reports each container state of the fragment [machine], among [states], that uses the fragment itself. */
    private fun checkSelfUse(machine: StateMachine, states: List<State>) {
        val name = machine.name?.takeIf { machine.isFragment && it.isNotEmpty() } ?: return
        for (state in states.filter { it.fragment == name }) {
            session.report(
                DiagnosticCode.FRAGMENT_USES_ITSELF,
                session.operandProblemRangeOf(state, 0),
                "The fragment '$name' cannot run inside itself; use another fragment"
            )
        }
    }

    /** Reports a problem of kind [code] at the word [token], described by [message]. */
    private fun report(code: DiagnosticCode, token: TerminalNode, message: String) {
        session.report(code, session.locator.rangeOf(token), message)
    }
}
