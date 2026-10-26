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
 * Where a model element is written in its text.
 *
 * @property range The whole construct that defines the element, from its first to its last token.
 * @property nameRange The name of the element between its quotes, for elements that have one; see [SourceMap]
 * for what counts as the name of each kind of element.
 * @property codeRange The inner range of an `EVAL` body: exactly the code the model holds for it, so that the
 * char at index `i` of the code is the char at `codeRange.start.offset + i` of the text; null for elements
 * without code.
 */
data class SourceLocation(
    val range: SourceRange,
    val nameRange: SourceRange? = null,
    val codeRange: SourceRange? = null
)
