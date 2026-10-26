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
 * A position in a text.
 *
 * Offsets and columns count UTF-16 chars, like the indices of a [String], so a character outside the Basic
 * Multilingual Plane takes two of them.
 *
 * @property offset The zero-based index of the character in the text; the length of the text for the end.
 * @property line The one-based line number.
 * @property column The one-based column: the offset from the start of the line plus one.
 */
data class SourcePosition(val offset: Int, val line: Int, val column: Int)
