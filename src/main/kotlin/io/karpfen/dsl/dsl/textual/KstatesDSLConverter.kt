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
package io.karpfen.dsl.dsl.textual

import io.karpfen.dsl.printer.kstates.KstatesPrinter
import io.karpfen.dsl.reader.InvalidTextException
import io.karpfen.dsl.reader.kstates.KstatesReader
import io.karpfen.dsl.states.KstatesFeature
import io.karpfen.dsl.states.StateMachine
import java.io.File

/**
 * Converts KStates text and files into [StateMachine]s, and state machines back into text.
 *
 * The converter is the throwing form of [KstatesReader] with every optional feature switched on, see
 * [KstatesFeature.ALL], for a text that is expected to be right: it reads through the reader, so every rule of KStates
 * holds here as there, the rules of fragments, container states, final states and `DONE` included, and returns the
 * state machine of a text without errors or throws an [InvalidTextException] with all errors of the text. Warnings do
 * not make it throw. A tool that shows the problems of a text, that must go on with a text that has errors, or that
 * switches optional features off, uses the reader.
 */
object KstatesDSLConverter {

    /**
     * Reads KStates text into a [StateMachine], in each of the three forms: attached, attachable and fragment, see
     * [KstatesReader].
     *
     * @param content The KStates content as a string.
     * @return The state machine.
     * @throws InvalidTextException If the text has at least one error, such as a syntax error, a state machine without
     * exactly one initial state, an action rule written with the wrong number of operands, a name defined twice, or a
     * final state outside a fragment.
     */
    fun parseKstatesString(content: String): StateMachine = KstatesReader().read(content).modelOrThrow()

    /**
     * Reads the KStates file at [filePath] into a [StateMachine], see [parseKstatesString].
     *
     * @param filePath The path to the KStates file to read.
     * @return The state machine.
     * @throws InvalidTextException If the text of the file has at least one error; its message names the file.
     */
    fun parseKstatesFile(filePath: String): StateMachine = KstatesReader().read(File(filePath).readText()).modelOrThrow(filePath)

    /**
     * Converts a [StateMachine] back into KStates text in the canonical layout, with the code of its `EVAL`
     * blocks re-indented and without the comments outside them; see [KstatesPrinter].
     *
     * @param stateMachine The state machine to convert.
     * @return The KStates text, ending with a line break.
     * @throws IllegalArgumentException If a name, path or value cannot be written as a KStates string literal,
     * or the state machine holds something KStates cannot express.
     */
    fun convertToKstatesString(stateMachine: StateMachine): String = KstatesPrinter().print(stateMachine)
}
