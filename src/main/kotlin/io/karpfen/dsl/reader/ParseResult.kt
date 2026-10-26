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

import io.karpfen.dsl.diagnostics.Diagnostic
import io.karpfen.dsl.diagnostics.Severity
import io.karpfen.dsl.source.SourceMap

/**
 * What a [TextReader] read from a text.
 *
 * Without errors, the model is the one the throwing converter builds from the same text. With errors, it is a
 * partial model: the reader leaves out what it cannot read, such as a state without a name or a property of
 * an unknown type, and keeps everything else.
 *
 * @param M The type of the model.
 * @property model The model, possibly partial; null only if the text does not yield any model, such as a
 * KMeta text without a type.
 * @property sourceMap Where the elements of [model] are written in the text.
 */
class ParseResult<out M>(
    val model: M?,
    diagnostics: List<Diagnostic>,
    val sourceMap: SourceMap
) {

    /** The problems found in the text, ordered by their position in the text. */
    val diagnostics: List<Diagnostic> = diagnostics.sortedBy { it.range.start.offset }

    /** The [diagnostics] of severity [Severity.ERROR]. */
    val errors: List<Diagnostic>
        get() = diagnostics.filter { it.severity == Severity.ERROR }

    /** True if the text contains at least one error. */
    val hasErrors: Boolean
        get() = diagnostics.any { it.severity == Severity.ERROR }
}
