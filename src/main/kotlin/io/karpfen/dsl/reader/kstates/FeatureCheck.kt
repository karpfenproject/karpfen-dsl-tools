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
import io.karpfen.dsl.reader.ReadingSession
import io.karpfen.dsl.reader.presentToken
import io.karpfen.dsl.states.KstatesFeature
import org.antlr.v4.runtime.ParserRuleContext

/**
 * Reports every construct of a [KstatesFeature] that is not among [switchedOn], once, at the keyword that
 * introduces it: `SHALLOW` or `DEEP` of a history, `TICK` of a tick limit, `BREAKPOINT` of a breakpoint block,
 * `MACROS` of the macros block, which stands for the definitions in it, `MACRO` of every macro call, also the
 * one a `WITH` binds, and `SPLIT` or `JOIN` of such a transition. A construct whose keyword the text does not
 * contain, because the parser conjured it up, is not reported.
 *
 * The check only reports; the state machine is built from the text as before.
 *
 * @param session The read the parse tree belongs to; it receives the problems.
 * @param switchedOn The features that may be used.
 */
internal class FeatureCheck(private val session: ReadingSession, private val switchedOn: Set<KstatesFeature>) {

    /** Checks [tree] and every context nested in it, in text order. */
    fun check(tree: ParserRuleContext?) {
        if (tree == null || switchedOn.containsAll(KstatesFeature.entries)) return
        visit(tree)
    }

    /** Reports the construct [context] introduces, if any, and visits the contexts nested in it. */
    private fun visit(context: ParserRuleContext) {
        introducerOf(context)?.let { (feature, tokenType) -> report(feature, context, tokenType) }
        context.children.orEmpty().filterIsInstance<ParserRuleContext>().forEach(::visit)
    }

    /** The feature of the construct [context] introduces and the type of its keyword, or null for any other context. */
    private fun introducerOf(context: ParserRuleContext): Pair<KstatesFeature, Int>? = when (context) {
        is KstatesParser.Shallow_history_definitionContext -> KstatesFeature.HISTORY to KstatesParser.SHALLOW
        is KstatesParser.Deep_history_definitionContext -> KstatesFeature.HISTORY to KstatesParser.DEEP
        is KstatesParser.Tick_limit_blockContext -> KstatesFeature.TICK_LIMIT to KstatesParser.TICK
        is KstatesParser.State_breakpoint_blockContext -> KstatesFeature.BREAKPOINTS to KstatesParser.BREAKPOINT
        is KstatesParser.Transition_breakpoint_blockContext -> KstatesFeature.BREAKPOINTS to KstatesParser.BREAKPOINT
        is KstatesParser.Macros_blockContext -> KstatesFeature.MACROS to KstatesParser.MACROS
        is KstatesParser.Macro_callContext -> KstatesFeature.MACROS to KstatesParser.MACRO
        is KstatesParser.Split_transitionContext -> KstatesFeature.PARALLEL to KstatesParser.SPLIT
        is KstatesParser.Join_transitionContext -> KstatesFeature.PARALLEL to KstatesParser.JOIN
        else -> null
    }

    /** Reports the construct of [feature] in [context] at its keyword of type [tokenType], unless the feature is on. */
    private fun report(feature: KstatesFeature, context: ParserRuleContext, tokenType: Int) {
        if (feature in switchedOn) return
        val keyword = context.presentToken(tokenType) ?: return
        session.report(DiagnosticCode.FEATURE_OFF, session.locator.rangeOf(keyword), messageOf(feature))
    }

    companion object {
        /** The message of a construct of [feature] while the feature is off. */
        fun messageOf(feature: KstatesFeature): String =
            "${feature.displayName} are switched off, so this ${feature.constructName} is not allowed."
    }
}
