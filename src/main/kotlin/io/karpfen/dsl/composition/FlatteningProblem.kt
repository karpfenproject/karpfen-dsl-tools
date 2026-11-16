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

import io.karpfen.dsl.states.Macro
import io.karpfen.dsl.states.State
import io.karpfen.dsl.states.StateMachine

/**
 * A problem that prevents [StateMachineFlattener] from flattening a composition.
 *
 * Each problem names the input state machine in whose text it stands, [machine], and the element of that state machine
 * to mark, [element], which the source map of the read of [machine] locates. Its [message] names the state machine and
 * the elements in question, so that a tool without the texts, such as the runtime, can show it as it is.
 */
sealed class FlatteningProblem {

    /** The input state machine in whose text the problem stands. */
    abstract val machine: StateMachine

    /** The element of [machine] at which the problem is marked. */
    abstract val element: Any

    /** A description of the problem for a modeler. */
    abstract val message: String

    /**
     * The container state [container] of [machine] uses the name [fragmentName], which no state machine has. It is
     * marked at the container state, at its `USE` name in a text.
     */
    class UnknownFragment(
        override val machine: StateMachine,
        val container: State,
        val fragmentName: String
    ) : FlatteningProblem() {
        override val element: Any get() = container
        override val message: String
            get() = "Container state '${container.name}' of ${describe(machine)} uses '$fragmentName', but no fragment " +
                "is named '$fragmentName'"
    }

    /**
     * The container state [container] of [machine] uses [used], a state machine named [fragmentName] that is no
     * fragment. It is marked at the container state.
     */
    class NotAFragment(
        override val machine: StateMachine,
        val container: State,
        val fragmentName: String,
        val used: StateMachine
    ) : FlatteningProblem() {
        override val element: Any get() = container
        override val message: String
            get() = "Container state '${container.name}' of ${describe(machine)} uses '$fragmentName', which is an " +
                "attachable state machine, not a fragment; only a FRAGMENT runs inside a container state"
    }

    /**
     * The container state [container] of [machine] uses a fragment that contains [container] itself: [cycle] lists
     * the fragments of the cycle, the one [container] uses first and [machine] last. It is marked at the container state
     * that closes the cycle.
     */
    class FragmentCycle(
        override val machine: StateMachine,
        val container: State,
        val cycle: List<String>
    ) : FlatteningProblem() {
        override val element: Any get() = container
        override val message: String
            get() = "Container state '${container.name}' of ${describe(machine)} uses '${cycle.first()}', which " +
                "contains this state again: ${cycle.joinToString(" uses ") { "'$it'" }}; a fragment cannot contain itself"
    }

    /**
     * The state [state] of [machine] gets the flattened name [flattenedName], which another state of the flattened
     * state machine has too. It is marked at the state.
     */
    class NameClash(
        override val machine: StateMachine,
        val state: State,
        val flattenedName: String
    ) : FlatteningProblem() {
        override val element: Any get() = state
        override val message: String
            get() = "State '${state.name}' of ${describe(machine)} becomes '$flattenedName' in the flattened state " +
                "machine, but another state has this name already; rename one of them"
    }

    /**
     * The macro [macro] of [machine] has the name of the macro [other] of [otherMachine], defined earlier in the
     * composition, but another definition. It is marked at [macro].
     */
    class ConflictingMacros(
        override val machine: StateMachine,
        val macro: Macro,
        val other: Macro,
        val otherMachine: StateMachine
    ) : FlatteningProblem() {
        override val element: Any get() = macro
        override val message: String
            get() = "Macro '${macro.name}' of ${describe(machine)} differs from the macro '${other.name}' of " +
                "${describe(otherMachine)}; the macros of a composition form one set, so macros of one name must be the same"
    }

    /**
     * A call in [machine] names the macro [macroName], which no file of the composition defines. [call] is the
     * `MACRO(...)` call, a [io.karpfen.dsl.states.actions.MacroActionRightSide], or, for an `@name(...)` call in the
     * code of an `EVAL`, the element that holds the code; then [codeSpan] is the part of the code from the `@` to the
     * end of the name, otherwise null.
     */
    class UnknownMacro(
        override val machine: StateMachine,
        val call: Any,
        val macroName: String,
        val codeSpan: IntRange?
    ) : FlatteningProblem() {
        override val element: Any get() = call
        override val message: String
            get() = "No file of the composition of ${describe(machine)} defines a macro named '$macroName'"
    }

    /** Describes state machines in messages. */
    internal companion object {

        /**
         * Returns how a message names [machine]: the fragment or attachable state machine by its name, an attached
         * state machine by its type.
         */
        fun describe(machine: StateMachine): String = when {
            machine.isFragment -> "fragment '${machine.name}'"
            machine.isAttachable -> "state machine '${machine.name}'"
            else -> "the state machine attached to '${machine.attachedToClass}'"
        }
    }
}
