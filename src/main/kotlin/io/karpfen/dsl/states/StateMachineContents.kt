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
package io.karpfen.dsl.states

import io.karpfen.dsl.states.actions.ActionBlock
import io.karpfen.dsl.states.actions.ActionItem
import io.karpfen.dsl.states.actions.ActionRightSide
import io.karpfen.dsl.states.actions.ActionRule
import io.karpfen.dsl.states.actions.EvalActionRightSide
import io.karpfen.dsl.states.actions.InScopeBlock
import io.karpfen.dsl.states.actions.MacroActionRightSide
import io.karpfen.dsl.states.actions.WithBlock
import io.karpfen.dsl.states.conditions.CompositeCondition
import io.karpfen.dsl.states.conditions.Condition
import io.karpfen.dsl.states.conditions.EvalCondition

/**
 * The blocks around an action item: the `WITH` and the `IF IN SCOPE` blocks that enclose it, outermost first.
 *
 * @property withBlocks The enclosing `WITH` blocks, whose bindings the item can read.
 * @property inScopeBlocks The enclosing `IF IN SCOPE` blocks, whose paths are available whenever the item runs.
 */
internal class ActionSurroundings(val withBlocks: List<WithBlock>, val inScopeBlocks: List<InScopeBlock>) {

    /**
     * Returns the surroundings of the items inside [item], a `WITH` or `IF IN SCOPE` block with these surroundings.
     */
    fun inside(item: ActionItem): ActionSurroundings = when (item) {
        is WithBlock -> ActionSurroundings(withBlocks + item, inScopeBlocks)
        is InScopeBlock -> ActionSurroundings(withBlocks, inScopeBlocks + item)
        is ActionRule -> this
    }

    /** Holds the surroundings of a top-level item. */
    companion object {

        /** The surroundings of an item directly in an `ENTRY` or `DO` block. */
        val NONE = ActionSurroundings(emptyList(), emptyList())
    }
}

/**
 * An action item of a state together with the blocks around it.
 *
 * @property item The item: a rule, a `WITH` block or an `IF IN SCOPE` block.
 * @property state The state whose `ENTRY` or `DO` block holds the item.
 * @property surroundings The blocks that enclose the item, the item itself not included.
 */
internal class PlacedAction(val item: ActionItem, val state: State, val surroundings: ActionSurroundings)

/**
 * The parts of a state machine that the checks of the readers and of the verification of a project go through: every
 * state, every action item with the blocks around it, every condition clause, every macro call and every `EVAL` body,
 * each in the order of the text.
 *
 * @param machine The state machine whose parts are listed.
 */
internal class StateMachineContents(private val machine: StateMachine) {

    /** Every state, each before its inner states, in the order of the text. */
    val states: List<State> by lazy { flattened(machine.states) }

    /** Every action item of every `ENTRY` and `DO` block, also those nested in `WITH` and `IF IN SCOPE` blocks. */
    val actions: List<PlacedAction> by lazy {
        states.flatMap { state -> listOf(state.onEntry, state.onDo).flatMap { placed(it, state, ActionSurroundings.NONE) } }
    }

    /** Every macro call of an action rule, of its `ATTACH` clause or of a `WITH` block. */
    val macroCalls: List<MacroActionRightSide> by lazy {
        actions.flatMap { placed ->
            when (val item = placed.item) {
                is ActionRule -> valuesOf(item).filterIsInstance<MacroActionRightSide>()
                is WithBlock -> listOf(item.macro)
                is InScopeBlock -> emptyList()
            }
        }
    }

    /**
     * Every `EVAL` body: the [EvalActionRightSide]s of the actions and their `ATTACH` clauses, the [EvalCondition]s
     * of the transitions and the code blocks of the macros, each with its code.
     */
    val evalBodies: List<Pair<Any, String>> by lazy {
        val ofActions = actions.flatMap { placed ->
            (placed.item as? ActionRule)?.let { valuesOf(it) }.orEmpty().filterIsInstance<EvalActionRightSide>().map { it to it.code }
        }
        val ofConditions = machine.transitions.flatMap { clausesOf(it.condition) }.filterIsInstance<EvalCondition>().map { it to it.code }
        val ofMacros = machine.macros.map { it.definition.codeBlock to it.definition.codeBlock.code }
        ofActions + ofConditions + ofMacros
    }

    /** Returns the right side of [rule] and the value of its `ATTACH` clause, those it has, in this order. */
    private fun valuesOf(rule: ActionRule): List<ActionRightSide> = listOfNotNull(rule.rightSide, rule.attach?.stateMachineName)

    /** Returns the simple conditions [condition] is made of, those of nested composite conditions in their place. */
    private fun clausesOf(condition: Condition): List<Condition> =
        if (condition is CompositeCondition) condition.clauses.flatMap { clausesOf(it) } else listOf(condition)

    /** Returns [states] and all their inner states, each before its inner states. */
    private fun flattened(states: List<State>): List<State> = states.flatMap { listOf(it) + flattened(it.innerStates) }

    /**
     * Returns the items of [block] of [state] with the blocks around them, each `WITH` and `IF IN SCOPE` block followed
     * by the items inside it.
     */
    private fun placed(block: ActionBlock, state: State, surroundings: ActionSurroundings): List<PlacedAction> =
        block.items.flatMap { item ->
            val nested = when (item) {
                is WithBlock -> placed(item.body, state, surroundings.inside(item))
                is InScopeBlock -> placed(item.body, state, surroundings.inside(item))
                is ActionRule -> emptyList()
            }
            listOf(PlacedAction(item, state, surroundings)) + nested
        }
}
