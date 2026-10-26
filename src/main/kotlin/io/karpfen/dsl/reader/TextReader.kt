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
 * Unlike the converters of `io.karpfen.dsl.dsl.textual`, which stop at the first problem, a reader reports
 * every problem as a [io.karpfen.dsl.diagnostics.Diagnostic] at the place where it occurs and keeps going, so
 * that text in the middle of being typed still yields a partial model. Everything a reader needs besides the
 * text, such as the metamodel of a KModel text, is passed to its constructor; a reader keeps no state between
 * reads, so one reader may serve several threads.
 *
 * The converters are not built on the readers: they keep the behaviour of version 1.0 down to the exceptions
 * they throw on broken text, null pointer exceptions included, which a reader that keeps going cannot reproduce.
 * Where a reader finds no error, it builds the same model as the converter, and where the converter throws, the
 * reader reports an error; the reader tests check both on thousands of mutated examples.
 *
 * @param M The type of the model the reader builds.
 */
fun interface TextReader<out M> {

    /**
     * Reads [text] into a model, the problems found in it and the locations of the model elements.
     */
    fun read(text: String): ParseResult<M>
}
