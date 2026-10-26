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
package io.karpfen.dsl.printer

/**
 * Writes the element a whole document of one karpfen language holds as text, the reverse of reading the
 * document with the matching converter.
 *
 * The text uses the canonical layout of the karpfen languages: four spaces per indentation level, one
 * statement per line, blocks that open on the line of their header, `{ }` for a block without content, and
 * an order that follows the element. Comments are not part of the elements, so a document that is read and
 * written again loses its comments.
 *
 * @param D The element a document holds: a metamodel, a model or a state machine.
 */
interface DocumentPrinter<in D> {

    /**
     * Returns [document] as the text of a whole document, ending with a line break.
     *
     * @throws IllegalArgumentException If [document] holds something its language cannot express, for
     * example a name that cannot be written as a string literal.
     */
    fun print(document: D): String
}
