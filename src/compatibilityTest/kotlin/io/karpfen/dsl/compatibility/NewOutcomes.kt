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
package io.karpfen.dsl.compatibility

import io.karpfen.dsl.fixtures.TextLayout

/**
 * Every example in a layout that version 1.0 converts and the current version rejects, see [NewOutcome]. Each entry
 * names the errors the reader finds and why version 1.0 converted the text nevertheless. Every entry point that reads
 * an example of the language as a whole text, from a string or from a file and as a single KModel object, has these
 * outcomes.
 */
object NewOutcomes {

    /** The cleaning robot examples, whose macros call each other from `EVAL` code. */
    private val cleaningRobotMachines = setOf("cleaning_robot.kstates")

    /** The new outcomes of all examples. */
    val ALL: List<NewOutcome> = listOf(
        NewOutcome(
            "A KMeta text that starts with a byte order mark is rejected: the lexer does not know the mark, where " +
                "version 1.0 logged the error and read on after it",
            TextLayout.BYTE_ORDER_MARK.name, "kmeta", null, listOf("syntax.unrecognized-input")
        ),
        NewOutcome(
            "A KModel text that starts with a byte order mark is rejected: the lexer does not know the mark, where " +
                "version 1.0 logged the error and read on after it",
            TextLayout.BYTE_ORDER_MARK.name, "kmodel", null, listOf("syntax.unrecognized-input")
        ),
        NewOutcome(
            "A KStates text that starts with a byte order mark is rejected: the parser does not expect the mark before " +
                "STATEMACHINE, where version 1.0 logged the error and read on after it",
            TextLayout.BYTE_ORDER_MARK.name, "kstates", null, listOf("syntax.error")
        ),
        NewOutcome(
            "A state machine whose macro names an escaped line break continues is rejected: the names of the macro " +
                "definitions take up the line break, the calls with @ in EVAL code do not, and version 1.0 did not " +
                "check the names of these calls",
            TextLayout.ESCAPED_LINE_BREAKS.name, "kstates", cleaningRobotMachines, listOf("kstates.unknown-macro")
        ),
        NewOutcome(
            "As with escaped line breaks, in an indented text",
            TextLayout.INDENTED_ESCAPED_LINE_BREAKS.name, "kstates", cleaningRobotMachines, listOf("kstates.unknown-macro")
        )
    )

    /**
     * Returns the new outcomes of the examples of the languages with [extensions].
     */
    fun of(vararg extensions: String): List<NewOutcome> = ALL.filter { it.extension in extensions }
}
