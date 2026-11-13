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

import io.karpfen.dsl.diagnostics.DiagnosticCode
import io.karpfen.dsl.parser.ConditionClauses
import io.karpfen.dsl.states.JoinTransition
import io.karpfen.dsl.states.SplitTransition
import io.karpfen.dsl.states.Transition
import io.karpfen.dsl.states.TransitionLike
import io.karpfen.dsl.states.conditions.Condition
import io.karpfen.dsl.states.conditions.ValueCondition

/**
 * Warns about the states that can never be active, see [Reachability], each at its name. Only the outermost of them is
 * reported, since its inner states can never be active either.
 *
 * The check needs state names that are unique and known.
 */
internal class UnreachableStateCheck : MachineCheck {

    /** Checks the states of the state machine of [context]. */
    override fun check(context: MachineContext) {
        val graph = context.graph ?: return
        for (state in graph.tree.states) {
            val outer = graph.tree.outerOf(state)
            val outerActive = outer == null || graph.reachability.isActive(outer)
            if (outerActive && !graph.reachability.isActive(state)) {
                context.report.report(
                    DiagnosticCode.UNREACHABLE_STATE,
                    context.report.rangeOf(state),
                    "State '${state.name}' can never be active: no transition, split, join or timeout leads into it " +
                        "or into one of its inner states from the initial state"
                )
            }
        }
    }
}

/**
 * Warns about the transitions and splits that never fire because an earlier transition of the same state always fires
 * first: one whose condition is always true and that may fire again and again, as it is not `NOT LOOPING`. The runtime
 * tries the transitions of a state in the order they are written and fires the first one whose condition holds.
 *
 * A condition is always true if it has no clauses or only `VALUE("true")` clauses.
 */
internal class ShadowedTransitionCheck : MachineCheck {

    /** Checks the transitions of the state machine of [context]. */
    override fun check(context: MachineContext) {
        val bySource = context.machine.transitions.mapNotNull { transition -> sourceOf(transition)?.let { it to transition } }
            .groupBy({ it.first }, { it.second })
        for ((source, transitions) in bySource) {
            val first = transitions.indexOfFirst { it is Transition && it.allowLoops && isAlwaysTrue(it.condition) }
            if (first < 0) {
                continue
            }
            val shadowing = transitions[first] as Transition
            transitions.drop(first + 1).forEach { shadowed ->
                context.report.report(
                    DiagnosticCode.SHADOWED_TRANSITION,
                    context.report.headRangeOf(shadowed),
                    "This never fires, since the transition from '$source' to '${shadowing.toState}' before it always " +
                        "fires first; give that one a condition, make it NOT LOOPING or move it after this one"
                )
            }
        }
    }

    /** Returns the state [transition] starts in, or null for a join. */
    private fun sourceOf(transition: TransitionLike): String? = when (transition) {
        is Transition -> transition.fromState
        is SplitTransition -> transition.fromState
        is JoinTransition -> null
    }

    /** Returns true if [condition] holds whatever the values. */
    private fun isAlwaysTrue(condition: Condition): Boolean = ConditionClauses.clausesOf(condition).all {
        it is ValueCondition && it.boolVariable.trim().lowercase() == "true"
    }
}

/**
 * Warns about the joins that can never fire, since no split leads to branches in which each of their source states is
 * active in a branch of its own, and about the splits whose source state can be active only inside a branch of a
 * parallel context, where the runtime takes no split; each at the head of the join or split, see [Reachability].
 *
 * The check needs state names that are unique and known.
 */
internal class ParallelRegionCheck : MachineCheck {

    /** Checks the joins and splits of the state machine of [context]. */
    override fun check(context: MachineContext) {
        val graph = context.graph ?: return
        val reachability = graph.reachability
        for (transition in context.machine.transitions) {
            when (transition) {
                is JoinTransition -> if (transition !in reachability.firingJoins) {
                    context.report.report(
                        DiagnosticCode.JOIN_NEVER_FIRES,
                        context.report.headRangeOf(transition),
                        "This join never fires: no split leads to ${transition.fromStates.size} branches in which " +
                            "each of its source states is active in a branch of its own"
                    )
                }
                is SplitTransition -> {
                    val source = graph.tree.stateNamed(transition.fromState) ?: continue
                    if (source.name !in reachability.activeInSimple && source.name in reachability.activeInParallel) {
                        context.report.report(
                            DiagnosticCode.SPLIT_INSIDE_PARALLEL,
                            context.report.headRangeOf(transition),
                            "State '${source.name}' is active only inside a branch of a split, where no split fires, " +
                                "so this split never fires"
                        )
                    }
                }
                is Transition -> Unit
            }
        }
    }
}
