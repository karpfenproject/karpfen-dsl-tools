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
package io.karpfen.dsl.source

/**
 * A contiguous part of a text, from [start] inclusive to [end] exclusive.
 *
 * @property start The position of the first character of the range.
 * @property end The position just behind the last character of the range; equal to [start] for an empty range.
 */
data class SourceRange(val start: SourcePosition, val end: SourcePosition) {

    init {
        require(end.offset >= start.offset) { "The end ${end.offset} lies before the start ${start.offset}" }
    }

    /** The number of chars in the range. */
    val length: Int
        get() = end.offset - start.offset

    /**
     * Returns true if the char at [offset] lies in this range.
     */
    fun contains(offset: Int): Boolean = offset >= start.offset && offset < end.offset

    /**
     * Returns true if [other] lies completely within this range.
     */
    fun encloses(other: SourceRange): Boolean =
        other.start.offset >= start.offset && other.end.offset <= end.offset

    /**
     * Returns the part of [text] this range covers; [text] must be the text the range was taken from.
     */
    fun textIn(text: CharSequence): String = text.subSequence(start.offset, end.offset).toString()
}
