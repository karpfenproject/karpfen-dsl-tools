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
package io.karpfen.dsl.reader

/**
 * Reads the text of one karpfen language into a model without ever throwing.
 *
 * A reader reports every problem as a [io.karpfen.dsl.diagnostics.Diagnostic] at the place where it occurs and keeps
 * going, so that text in the middle of being typed still yields a partial model. Everything a reader needs besides the
 * text, such as the metamodel of a KModel text, is passed to its constructor; a reader keeps no state between reads,
 * so one reader may serve several threads.
 *
 * The readers are the one implementation of reading in the library and hold every rule of their language. The
 * converters of `io.karpfen.dsl.dsl.textual` read through them and throw for a text with errors, see
 * [ParseResult.modelOrThrow]. The KMeta and KModel readers read the text with its common indentation trimmed, see
 * [LexerInput], while their ranges refer to the text as written. A text without errors gives the model that version
 * 1.0 of the library gave for it; the compatibility tests check that on thousands of mutated examples.
 *
 * Should a reader fail nevertheless, through a defect or on nesting deeper than its parser can recurse, it reports
 * a [io.karpfen.dsl.diagnostics.DiagnosticCode.READER_FAILURE] and logs the failure through SLF4J, below
 * `io.karpfen.dsl.reader`: a defect as an error with its stack trace, the nesting as a warning without one.
 *
 * @param M The type of the model the reader builds.
 */
fun interface TextReader<out M> {

    /**
     * Reads [text] into a model, the problems found in it and the locations of the model elements.
     */
    fun read(text: String): ParseResult<M>
}
