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
package io.karpfen.dsl.reader

import io.karpfen.dsl.states.JoinTransition
import io.karpfen.dsl.states.SplitTransition
import io.karpfen.dsl.states.State
import io.karpfen.dsl.states.StateMachine
import io.karpfen.dsl.states.Transition
import io.karpfen.dsl.states.TransitionLike
import io.karpfen.dsl.states.actions.ActionBlock
import io.karpfen.dsl.states.actions.ActionRightSide
import io.karpfen.dsl.states.actions.ActionRule
import io.karpfen.dsl.states.actions.EvalActionRightSide
import io.karpfen.dsl.states.actions.InScopeBlock
import io.karpfen.dsl.states.actions.MacroActionRightSide
import io.karpfen.dsl.states.actions.ValueActionRightSide
import io.karpfen.dsl.states.actions.WithBlock
import io.karpfen.dsl.states.conditions.CompositeCondition
import io.karpfen.dsl.states.conditions.Condition
import io.karpfen.dsl.states.conditions.EvalCondition
import io.karpfen.dsl.states.conditions.EventCondition
import io.karpfen.dsl.states.conditions.ValueCondition

/**
 * Walks a [StateMachine] read from an error-free text and lets [checker] check the location of every element:
 * the keyword its construct starts with, its name and the code of its `EVAL` bodies.
 *
 * @param checker Checks the locations against the text.
 */
class StateMachineLocations(private val checker: LocationChecker) {

    /**
     * Checks [stateMachine] and everything in it.
     */
    fun check(stateMachine: StateMachine) {
        checker.expect(stateMachine, "the state machine", "STATEMACHINE", stateMachine.attachedToClass)
        stateMachine.states.forEach { checkState(it) }
        stateMachine.transitions.forEach { checkTransition(it) }
        for (macro in stateMachine.macros) {
            checker.expect(macro, "macro ${macro.name}", "MACRO", macro.name)
            macro.takes.forEach { checker.expect(it, "takes ${it.paramName}", "TAKES", it.paramName) }
            checker.expect(macro.returns, "returns of ${macro.name}", "RETURNS")
            checker.expect(macro.definition, "definition of ${macro.name}", "DEFINITION")
            val code = macro.definition.codeBlock
            checker.expect(code, "code of ${macro.name}", "EVAL", code = code.code)
            checker.expectWithin(macro, code, "code of ${macro.name}")
        }
    }

    private fun checkState(state: State) {
        val keyword = if (state.isInitial) "INITIAL" else "STATE"
        checker.expect(state, "state ${state.name}", keyword, state.name)
        state.outerState?.let { checker.expectWithin(it, state, "state ${state.name}") }
        checkSection(state, state.onEntry, "ENTRY")
        checkSection(state, state.onDo, "DO")
        state.innerStates.forEach { checkState(it) }
    }

    private fun checkSection(state: State, block: ActionBlock, keyword: String) {
        if (block.isNotEmpty()) {
            checker.expect(block, "$keyword of ${state.name}", keyword)
            checker.expectWithin(state, block, "$keyword of ${state.name}")
            checkItems(block, "$keyword of ${state.name}")
        }
    }

    private fun checkItems(block: ActionBlock, where: String) {
        for (item in block.items) {
            when (item) {
                is ActionRule -> {
                    checker.expect(item, "${item.operationType} in $where", item.operationType.name)
                    item.rightSide?.let { checkRightSide(it, "right side of ${item.operationType} in $where") }
                }
                is InScopeBlock -> {
                    checker.expect(item, "IF IN SCOPE in $where", "IF IN SCOPE")
                    checker.expect(item.body, "body of IF IN SCOPE in $where", "{")
                    checkItems(item.body, "IF IN SCOPE in $where")
                }
                is WithBlock -> {
                    checker.expect(item, "WITH ${item.name} in $where", "WITH", item.name)
                    checkRightSide(item.macro, "macro of WITH ${item.name}")
                    checker.expect(item.body, "body of WITH ${item.name}", "{")
                    checkItems(item.body, "WITH ${item.name} in $where")
                }
            }
        }
    }

    private fun checkRightSide(rightSide: ActionRightSide, what: String) {
        when (rightSide) {
            is ValueActionRightSide -> checker.expect(rightSide, what, "\"${rightSide.value}\"")
            is MacroActionRightSide -> checker.expect(rightSide, what, "MACRO", rightSide.macroName)
            is EvalActionRightSide -> checker.expect(rightSide, what, "EVAL", code = rightSide.code)
        }
    }

    private fun checkTransition(transition: TransitionLike) {
        val (keyword, what) = when (transition) {
            is Transition -> "TRANSITION" to "transition ${transition.fromState} -> ${transition.toState}"
            is SplitTransition -> "SPLIT" to "split from ${transition.fromState}"
            is JoinTransition -> "JOIN" to "join to ${transition.toState}"
        }
        val location = checker.expect(transition, what, keyword)
        val hasConditionBlock = location != null && checker.textOf(location.range).contains("CONDITION")
        if (hasConditionBlock) {
            checkCondition(transition.condition, "condition of $what")
            checker.expectWithin(transition, transition.condition, "condition of $what")
        }
    }

    private fun checkCondition(condition: Condition, what: String) {
        when (condition) {
            is CompositeCondition -> {
                checker.expect(condition, what, "CONDITION")
                condition.clauses.forEach { checkCondition(it, "clause of $what") }
            }
            is EvalCondition -> checker.expect(condition, what, "EVAL", code = condition.code)
            is EventCondition -> checker.expect(condition, what, "EVENT", condition.eventValue)
            is ValueCondition -> checker.expect(condition, what, "VALUE", condition.boolVariable)
        }
    }
}
