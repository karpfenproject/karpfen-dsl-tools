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

/**
 * Represents a hierarchical state machine definition in one of the two forms of KStates.
 *
 * An attached state machine, `STATEMACHINE ATTACHED TO "Lamp"`, has no name and one type; a run attaches it to one
 * object of that type by the key of the object. An attachable state machine, `STATEMACHINE "blinking" ATTACHABLE TO
 * "Bulb", "Lamp"`, is a definition with a name and one or more types; the runtime builds an instance of it for an
 * object whose type has `behaves after` with its name, for an object a rule creates with `ATTACH` and its name, and
 * for an object an `EXCHANGE` names it for. The form is told by [name]: null for the attached form, a string,
 * possibly empty in a text that lacks it, for the attachable form.
 *
 * @property attachedToClass The name of the type an attached state machine is attached to; the empty string for an
 * attachable state machine, whose types are [attachableTo].
 * @property states List of top-level states defined in this state machine.
 * @property transitions List of state transitions with conditions.
 * @property macros List of macro definitions for use in actions and conditions.
 * @property name The name of an attachable state machine, by which `behaves after`, `ATTACH` and `EXCHANGE` name it;
 * null for an attached state machine.
 * @property attachableTo The types an attachable state machine may be attached to, in the order they are written;
 * empty for an attached state machine.
 */
data class StateMachine(
    val attachedToClass: String,
    val states: MutableList<State>,
    val transitions: MutableList<TransitionLike>,
    val macros: MutableList<Macro>,
    val name: String? = null,
    val attachableTo: List<String> = emptyList()
    ) {

    /** True for the attachable form, `STATEMACHINE "name" ATTACHABLE TO ...`, false for the attached form. */
    val isAttachable: Boolean
        get() = name != null

    /**
     * The types the state machine may run on: [attachableTo] for an attachable state machine, the single
     * [attachedToClass] for an attached one.
     */
    val types: List<String>
        get() = if (isAttachable) attachableTo else listOf(attachedToClass)

    /**
     * The normal and split transitions that leave [stateName], in definition order. These are the
     * transitions a branch sitting in [stateName] may take; joins are handled separately at the
     * context level via [joinTransitions].
     */
    fun outgoingFrom(stateName: String): List<TransitionLike> =
        transitions.filter {
            (it is Transition && it.fromState == stateName) ||
            (it is SplitTransition && it.fromState == stateName)
        }

    /** All join transitions, in definition order. */
    fun joinTransitions(): List<JoinTransition> = transitions.filterIsInstance<JoinTransition>()

    override fun toString(): String {
        val header = if (isAttachable) "name=$name, attachableTo=$attachableTo" else "attachedToClass=$attachedToClass"
        return "StateMachine($header, states=${states.size}, transitions=${transitions.size}, macros=${macros.size})"
    }

    /** Builds state machines of the attachable form. */
    companion object {

        /**
         * Returns an attachable state machine named [name] that may be attached to objects of the types [attachableTo].
         */
        fun attachable(
            name: String,
            attachableTo: List<String>,
            states: MutableList<State>,
            transitions: MutableList<TransitionLike>,
            macros: MutableList<Macro>
        ): StateMachine = StateMachine("", states, transitions, macros, name, attachableTo)
    }
}