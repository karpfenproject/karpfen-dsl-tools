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
package io.karpfen.dsl.composition

import io.karpfen.dsl.parser.EvalCodeReferences
import io.karpfen.dsl.states.Macro
import io.karpfen.dsl.states.State
import io.karpfen.dsl.states.StateMachine
import io.karpfen.dsl.states.StateMachineContents
import io.karpfen.dsl.states.TransitionLike
import java.util.IdentityHashMap

/**
 * Flattens a composition of state machines into one state machine that has no container state left to resolve.
 *
 * A composition is a state machine together with every fragment its container states use, directly or through other
 * fragments; fragments are found by their name among the state machines a caller makes available, in a flat name
 * space without folders or paths. Flattening
 *
 * - gives each container state copies of the states of its fragment as inner states, named `<container>/<state>`;
 *   since the name of a container that came out of a fragment is qualified already, names nest, as in
 *   `self test/wiring/lit`. The container keeps its name, history, breakpoints, tick limit and [State.fragment], so a
 *   consumer still tells it is a container and finds the states it goes down to with
 *   [io.karpfen.dsl.states.InitialStates.enteredBelow];
 * - copies the transitions, splits and joins of each fragment, and the targets of their timeouts, with the names
 *   qualified the same way, after the transitions of the state machine it starts from;
 * - copies a fragment once per container state that uses it, so that two uses share nothing;
 * - keeps the final states of the fragments final, and the header of the state machine it starts from;
 * - puts the macros of all files of the composition into one set, the macros of the starting state machine first. A
 *   macro that a fragment used several times defines is in the set once. Two macros of one name are the same macro if
 *   they take the same parameters, return the same type and have the same code, apart from the indentation of the
 *   code and the whitespace around it; otherwise they cannot both be in the set.
 *
 * The flattening fails with every problem it finds, see [FlatteningProblem]: a container state that uses a name no state
 * machine has, or a state machine that is no fragment; a fragment that contains itself; a flattened name that two
 * states have; two different macros of one name; and a macro call that no file of the composition defines.
 *
 * The flattening is a pure function: it neither changes its input nor keeps anything between calls. It is the one
 * implementation of the composition that the verification of a project, the runtime and the IDE use.
 *
 * ```
 * val lamp = KstatesReader().read(lampText).model!!
 * val bulbTest = KstatesReader().read(bulbTestText).model!!
 * when (val result = StateMachineFlattener.flatten(lamp, listOf(bulbTest))) {
 *     is FlatteningResult.Flattened -> run(result.machine)
 *     is FlatteningResult.NotFlattened -> result.problems.forEach { println(it.message) }
 * }
 * ```
 */
object StateMachineFlattener {

    /** The separator between the name of a container state and the name of a state of its fragment. */
    const val NAME_SEPARATOR: String = "/"

    /**
     * Flattens the composition that starts from [machine], with the fragments among [available], the state machines
     * container states may name; [machine] may be among them. A fragment as [machine] is flattened like any other
     * state machine and stays a fragment.
     */
    fun flatten(machine: StateMachine, available: Collection<StateMachine>): FlatteningResult =
        Flattening(machine, available).result()

    /**
     * One flattening of the composition that starts from [root], with the state machines of [available] by name.
     */
    private class Flattening(private val root: StateMachine, available: Collection<StateMachine>) {

        /** The named state machines, the first one of each name. */
        private val byName: Map<String, StateMachine> =
            available.filter { !it.name.isNullOrEmpty() }.reversed().associateBy { it.name!! }

        /** The original of each copy. */
        private val sources = IdentityHashMap<Any, ElementSource>()

        /** Where each copied state was written, by its flattened name. */
        private val origins = LinkedHashMap<String, StateOrigin>()

        /** Every copied state with its origin, in the order of the states, each before its inner states. */
        private val copiedStates = mutableListOf<Pair<State, StateOrigin>>()

        /** The copied transitions, those of the starting state machine first. */
        private val transitions = mutableListOf<TransitionLike>()

        /** The state machines of the composition, each once. */
        private val compositionSources = mutableListOf<StateMachine>()

        /** The problems found so far. */
        private val problems = mutableListOf<FlatteningProblem>()

        /** Returns the flat state machine or the problems. */
        fun result(): FlatteningResult {
            compositionSources.add(root)
            val states = copyMachine(root, prefix = "", stack = listOf(root), containers = emptyList(), outer = null)
            checkNames()
            val macros = macroSet()
            checkMacroCalls(macros)
            val distinct = distinctProblems()
            if (distinct.isNotEmpty()) {
                return FlatteningResult.NotFlattened(distinct)
            }
            val flat = StateMachine(
                root.attachedToClass,
                states,
                transitions,
                macros.toMutableList(),
                root.name,
                root.attachableTo,
                root.isFragment
            )
            sources[flat] = ElementSource(root, root)
            return FlatteningResult.Flattened(flat, origins, compositionSources.toList(), sources)
        }

        /**
         * Copies the states and transitions of [machine], whose names get [prefix], below [outer], and returns the
         * copied top-level states; [stack] holds the state machines being copied, [machine] last, and [containers] the
         * flattened names of the container states above.
         */
        private fun copyMachine(
            machine: StateMachine,
            prefix: String,
            stack: List<StateMachine>,
            containers: List<String>,
            outer: State?
        ): MutableList<State> {
            val copier = ElementCopier(machine, { prefix + it }, sources)
            val fragmentName = machine.name.takeIf { machine !== root }
            machine.transitions.forEach { transitions.add(copier.transition(it)) }
            return machine.states.map { copyState(it, copier, outer, fragmentName, stack, containers) }.toMutableList()
        }

        /**
         * Copies [state] of the state machine at the end of [stack] with [copier] into [outer], with its inner states
         * and, for a container state, the states of its fragment.
         */
        private fun copyState(
            state: State,
            copier: ElementCopier,
            outer: State?,
            fragmentName: String?,
            stack: List<StateMachine>,
            containers: List<String>
        ): State {
            val copy = copier.state(state, outer)
            val origin = StateOrigin(fragmentName, state.name, containers)
            origins.putIfAbsent(copy.name, origin)
            copiedStates.add(copy to origin)
            state.innerStates.forEach { copy.innerStates.add(copyState(it, copier, copy, fragmentName, stack, containers)) }
            state.fragment?.let { name -> resolve(state, name, copy, stack, containers) }
            return copy
        }

        /**
         * Puts copies of the states of the fragment named [name], which the container [state] of the state machine at
         * the end of [stack] uses, into [copy], or records why it cannot.
         */
        private fun resolve(state: State, name: String, copy: State, stack: List<StateMachine>, containers: List<String>) {
            val machine = stack.last()
            val fragment = byName[name]
            when {
                fragment == null -> problems.add(FlatteningProblem.UnknownFragment(machine, state, name))
                !fragment.isFragment -> problems.add(FlatteningProblem.NotAFragment(machine, state, name, fragment))
                stack.any { it === fragment } -> {
                    val cycle = stack.dropWhile { it !== fragment }.map { it.name.orEmpty() }
                    problems.add(FlatteningProblem.FragmentCycle(machine, state, cycle))
                }
                else -> {
                    if (compositionSources.none { it === fragment }) {
                        compositionSources.add(fragment)
                    }
                    val inner = copyMachine(fragment, copy.name + NAME_SEPARATOR, stack + fragment, containers + copy.name, copy)
                    copy.innerStates.addAll(inner)
                }
            }
        }

        /**
         * Records a [FlatteningProblem.NameClash] for each copied state whose name an earlier one has, unless both were
         * written in the same use of the same file, which the reader of that file reports.
         */
        private fun checkNames() {
            val firstByName = HashMap<String, StateOrigin>()
            for ((copy, origin) in copiedStates) {
                val first = firstByName.putIfAbsent(copy.name, origin) ?: continue
                if (first.containers != origin.containers) {
                    val source = sources.getValue(copy)
                    problems.add(FlatteningProblem.NameClash(source.machine, source.original as State, copy.name))
                }
            }
        }

        /** Returns the one set of macros of the composition and records the macros that conflict. */
        private fun macroSet(): List<Macro> {
            val set = LinkedHashMap<String, Pair<Macro, StateMachine>>()
            for (machine in compositionSources) {
                for (macro in machine.macros) {
                    val (first, firstMachine) = set[macro.name] ?: (null to null)
                    when {
                        first == null -> set[macro.name] = macro to machine
                        first === macro || sameMacro(first, macro) -> Unit
                        else -> problems.add(FlatteningProblem.ConflictingMacros(machine, macro, first, firstMachine!!))
                    }
                }
            }
            return set.values.map { it.first }
        }

        /** Records a [FlatteningProblem.UnknownMacro] for each call in a file of the composition that [macros] lack. */
        private fun checkMacroCalls(macros: List<Macro>) {
            val names = macros.map { it.name }.toSet()
            for (machine in compositionSources) {
                val contents = StateMachineContents(machine)
                contents.macroCalls.filter { it.macroName !in names }.forEach {
                    problems.add(FlatteningProblem.UnknownMacro(machine, it, it.macroName, null))
                }
                for ((holder, code) in contents.evalBodies) {
                    EvalCodeReferences.macroCallsIn(code).filter { it.name !in names }.forEach {
                        problems.add(FlatteningProblem.UnknownMacro(machine, holder, it.name, it.start until it.nameEnd))
                    }
                }
            }
        }

        /**
         * Returns the problems found, each once: a fragment used several times yields the same problem at the same
         * element once per use. Elements are compared by identity, since states refer to their outer states.
         */
        private fun distinctProblems(): List<FlatteningProblem> {
            val kept = mutableListOf<FlatteningProblem>()
            for (problem in problems) {
                val repeated = kept.any {
                    it.javaClass == problem.javaClass && it.machine === problem.machine && it.element === problem.element &&
                        it.message == problem.message && codeSpanOf(it) == codeSpanOf(problem)
                }
                if (!repeated) kept.add(problem)
            }
            return kept
        }

        /** Returns the part of the code [problem] marks, or null for a problem that marks no code. */
        private fun codeSpanOf(problem: FlatteningProblem): IntRange? = (problem as? FlatteningProblem.UnknownMacro)?.codeSpan

        /** Returns true if [first] and [second] define the same macro, see [StateMachineFlattener]. */
        private fun sameMacro(first: Macro, second: Macro): Boolean =
            first.takes == second.takes && first.returns == second.returns &&
                normalized(first.definition.codeBlock.code) == normalized(second.definition.codeBlock.code)

        /** Returns [code] without its common indentation and without whitespace at its edges. */
        private fun normalized(code: String): String = code.trimIndent().trim()
    }
}
