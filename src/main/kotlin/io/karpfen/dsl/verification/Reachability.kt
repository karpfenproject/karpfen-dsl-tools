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

import io.karpfen.dsl.states.JoinTransition
import io.karpfen.dsl.states.SplitTransition
import io.karpfen.dsl.states.State
import io.karpfen.dsl.states.StateMachine
import io.karpfen.dsl.states.Transition

/**
 * The states a state machine can reach, as far as its transitions tell, with every condition taken as possibly true.
 *
 * The runtime runs a machine in a simple context, one stack of active states, or after a split in a parallel context,
 * one stack per target of the split. From a stack, every transition of a state on it may fire, and the timeout of a
 * state on it; a split may fire only in a simple context. A join fires in a parallel context with as many branches as
 * it has source states, each source state active in a branch of its own, and leads back to a simple context. A history
 * only returns to states that were active before, so it reaches nothing new.
 *
 * The branches of a split are followed each on its own, so a join counts as possible if each of its source states can
 * be active in a branch of its own at some time. This errs towards reaching too much, never too little.
 *
 * States are held by their names, which are unique, since a state holds its outer state and cannot be hashed.
 *
 * @param machine The state machine; its state names are unique and every transition names known states.
 * @param tree The nesting of its states.
 */
internal class Reachability(private val machine: StateMachine, private val tree: StateTree) {

    /** The names of the innermost states of the stacks reachable in a simple context. */
    val simple: Set<String>

    /** The splits that fire into a parallel context, with the names of the innermost states each branch can reach. */
    val regions: Map<SplitTransition, List<Set<String>>>

    /** The joins that can fire. */
    val firingJoins: Set<JoinTransition>

    init {
        val simpleStates = LinkedHashSet<String>()
        val reachedRegions = LinkedHashMap<SplitTransition, List<Set<String>>>()
        val joins = LinkedHashSet<JoinTransition>()
        val pending = ArrayDeque(listOfNotNull(tree.initial))
        while (pending.isNotEmpty()) {
            while (pending.isNotEmpty()) {
                val state = pending.removeFirst()
                if (simpleStates.add(state.name)) {
                    pending.addAll(successorsOf(state))
                    splitsFrom(state).filter { it !in reachedRegions }.forEach { split ->
                        val targets = split.toStates.mapNotNull { tree.stateNamed(it) }
                        if (targets.size == 1) pending.add(targets.single()) else reachedRegions[split] = targets.map { closureOf(it) }
                    }
                }
            }
            machine.joinTransitions().filter { it !in joins && canFire(it, reachedRegions.values) }.forEach { join ->
                joins.add(join)
                tree.stateNamed(join.toState)?.let { pending.add(it) }
            }
        }
        simple = simpleStates
        regions = reachedRegions
        firingJoins = joins
    }

    /** The names of the states active on some stack of a simple context. */
    val activeInSimple: Set<String> by lazy { stacksOf(simple) }

    /** The names of the states active on some stack of a branch of a parallel context. */
    val activeInParallel: Set<String> by lazy { stacksOf(regions.values.flatten().flatten().toSet()) }

    /** Returns true if [state] can be active at all. */
    fun isActive(state: State): Boolean = state.name in activeInSimple || state.name in activeInParallel

    /** Returns the names of the states on the stacks of the states named [names]. */
    private fun stacksOf(names: Set<String>): Set<String> =
        names.mapNotNull { tree.stateNamed(it) }.flatMap { tree.stackOf(it) }.map { it.name }.toSet()

    /** Returns the targets of the transitions and timeouts that may fire on the stack of [state], splits left out. */
    private fun successorsOf(state: State): List<State> = tree.stackOf(state).flatMap { active ->
        val byTransition = machine.transitions.filterIsInstance<Transition>()
            .filter { it.fromState == active.name }
            .mapNotNull { tree.stateNamed(it.toState) }
        val byTimeout = listOfNotNull(active.timeoutTo?.takeIf { active.timeout != null }?.let { tree.stateNamed(it) })
        byTransition + byTimeout
    }

    /** Returns the splits of the states on the stack of [state]. */
    private fun splitsFrom(state: State): List<SplitTransition> {
        val names = tree.stackOf(state).map { it.name }.toSet()
        return machine.transitions.filterIsInstance<SplitTransition>().filter { it.fromState in names }
    }

    /** Returns the names of the innermost states a branch that starts at [start] can reach. */
    private fun closureOf(start: State): Set<String> {
        val reached = LinkedHashSet<String>()
        val pending = ArrayDeque(listOf(start))
        while (pending.isNotEmpty()) {
            val state = pending.removeFirst()
            if (reached.add(state.name)) pending.addAll(successorsOf(state))
        }
        return reached
    }

    /** Returns true if [join] can fire in one of the parallel contexts [contexts], given as the reach of each branch. */
    private fun canFire(join: JoinTransition, contexts: Collection<List<Set<String>>>): Boolean {
        val sources = join.fromStates
        return contexts.any { branches -> branches.size == sources.size && matches(sources, branches.map { stacksOf(it) }) }
    }

    /**
     * Returns true if each of [sources] can be matched with a branch of its own among [branches], given as the states
     * each branch can have active, from the source at [index] on with the branches in [used] taken.
     */
    private fun matches(
        sources: List<String>,
        branches: List<Set<String>>,
        index: Int = 0,
        used: Set<Int> = emptySet()
    ): Boolean {
        if (index == sources.size) {
            return true
        }
        return branches.indices.any { branch ->
            branch !in used && sources[index] in branches[branch] && matches(sources, branches, index + 1, used + branch)
        }
    }
}
