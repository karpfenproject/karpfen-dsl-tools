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
 * The optional features of KStates. The grammar always accepts their constructs, but a tool may switch a feature
 * off; the [io.karpfen.dsl.reader.kstates.KstatesReader] then reports every construct of it as a problem, see
 * [io.karpfen.dsl.diagnostics.DiagnosticCode.FEATURE_OFF].
 *
 * @property id A stable identifier of the feature for configuration files, such as `history`.
 * @property displayName The name of the feature in messages and menus, in the plural, such as `Histories`.
 * @property constructName The name of one construct of the feature, such as `history`, for messages that point
 * at it.
 */
enum class KstatesFeature(val id: String, val displayName: String, val constructName: String) {

    /** The histories of states, `SHALLOW HISTORY` and `DEEP HISTORY`. */
    HISTORY("history", "Histories", "history"),

    /** The tick limit of a state, the block `TICK LIMIT` with a delay and a timeout. */
    TICK_LIMIT("tickLimit", "Tick limits", "tick limit"),

    /** The breakpoints of states and transitions, the blocks `BREAKPOINT`. */
    BREAKPOINTS("breakpoints", "Breakpoints", "breakpoint block"),

    /** The macros of a state machine: the block `MACROS` with their definitions, and every call of a macro. */
    MACROS("macros", "Macros", "macro"),

    /** The transitions into and out of parallel states, `SPLIT` and `JOIN`. */
    PARALLEL("parallel", "Splits and joins", "split or join");

    companion object {
        /** Every feature; the default of a KStates reader, and the features the KStates converter reads with. */
        val ALL: Set<KstatesFeature> = entries.toSet()

        /** Returns the feature whose [id] is [id], or null if no feature has it. */
        fun byId(id: String): KstatesFeature? = entries.firstOrNull { it.id == id }
    }
}
