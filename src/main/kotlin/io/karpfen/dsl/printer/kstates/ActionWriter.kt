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

import io.karpfen.dsl.parser.ActionRuleSyntax
import io.karpfen.dsl.printer.CodeWriter
import io.karpfen.dsl.states.actions.ActionBlock
import io.karpfen.dsl.states.actions.ActionItem
import io.karpfen.dsl.states.actions.ActionRightSide
import io.karpfen.dsl.states.actions.ActionRule
import io.karpfen.dsl.states.actions.EvalActionRightSide
import io.karpfen.dsl.states.actions.InScopeBlock
import io.karpfen.dsl.states.actions.MacroActionRightSide
import io.karpfen.dsl.states.actions.ValueActionRightSide
import io.karpfen.dsl.states.actions.WithBlock

/**
 * Writes the `ENTRY` and `DO` blocks of a state with their action rules, `IF IN SCOPE` blocks and `WITH`
 * blocks, in the order of [ActionBlock.items].
 *
 * @param syntax The shared pieces of KStates syntax.
 */
internal class ActionWriter(private val syntax: KstatesSyntax) {

    /**
     * Writes [block] as `keyword { ... }`, or nothing when it has no items, since KStates reads a missing
     * `ENTRY` or `DO` block as an empty one.
     *
     * @param keyword `ENTRY` or `DO`.
     */
    fun writeSection(keyword: String, block: ActionBlock, writer: CodeWriter) {
        if (block.isNotEmpty()) {
            writer.block(keyword) { writeItems(block, writer) }
        }
    }

    /** Writes the items of [block] in order. */
    private fun writeItems(block: ActionBlock, writer: CodeWriter) {
        block.items.forEach { writeItem(it, writer) }
    }

    /** Writes an action rule on a line of its own, or an `IF IN SCOPE` or `WITH` block. */
    private fun writeItem(item: ActionItem, writer: CodeWriter) {
        when (item) {
            is ActionRule -> writer.line(ruleText(item, writer))
            is InScopeBlock -> writeInScopeBlock(item, writer)
            is WithBlock -> writeWithBlock(item, writer)
        }
    }

    /**
     * Writes `IF IN SCOPE("path", ...) { ... }`.
     *
     * @throws IllegalArgumentException If the block names no path, which KStates cannot express.
     */
    private fun writeInScopeBlock(block: InScopeBlock, writer: CodeWriter) {
        require(block.paths.isNotEmpty()) { "An IF IN SCOPE block needs at least one path." }
        writer.block("IF IN SCOPE(${syntax.quoteAll(block.paths)})") { writeItems(block.body, writer) }
    }

    /** Writes `WITH MACRO("name", ...) AS "name" { ... }`. */
    private fun writeWithBlock(block: WithBlock, writer: CodeWriter) {
        val macroCall = syntax.macroCall(block.macro.macroName, block.macro.args)
        writer.block("WITH $macroCall AS ${syntax.quote(block.name)}") { writeItems(block.body, writer) }
    }

    /**
     * Returns an action rule with the operands it has, as [ActionRule] describes them: `OP("path")`,
     * `OP("path", value)` or `OP("source", "relation", target)`.
     *
     * @throws IllegalArgumentException If the rule has a relation but no right side, or another number of operands
     * than its operation expects, which KStates cannot read back.
     */
    private fun ruleText(rule: ActionRule, writer: CodeWriter): String {
        require(rule.rightSide != null || rule.secondSide == null) {
            "The ${rule.operationType} rule on '${rule.leftSide}' has a relation but no right side."
        }
        val stringCount = if (rule.secondSide == null) 1 else 2
        val written = ActionRuleSyntax.writtenArity(stringCount, rule.rightSide != null)
        val expected = ActionRuleSyntax.expectedArity(rule.operationType)
        require(written == expected) { ActionRuleSyntax.arityMismatch(rule.operationType, expected, written) }
        val operands = listOfNotNull(
            syntax.quote(rule.leftSide),
            rule.secondSide?.let { syntax.quote(it) },
            rule.rightSide?.let { rightSideText(it, writer) }
        )
        return "${ActionRuleSyntax.keywordOf(rule.operationType)}(${operands.joinToString(", ")})"
    }

    /**
     * Returns a literal value, a macro call or an `EVAL` block.
     *
     * @throws IllegalArgumentException If [rightSide] is of none of these kinds.
     */
    private fun rightSideText(rightSide: ActionRightSide, writer: CodeWriter): String = when (rightSide) {
        is ValueActionRightSide -> syntax.quote(rightSide.value)
        is MacroActionRightSide -> syntax.macroCall(rightSide.macroName, rightSide.args)
        is EvalActionRightSide -> syntax.eval(rightSide.code, writer)
        else -> throw IllegalArgumentException("Unsupported kind of right side: ${rightSide.actionValueType}")
    }
}
