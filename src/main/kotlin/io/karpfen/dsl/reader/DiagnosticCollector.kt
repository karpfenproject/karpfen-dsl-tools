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
import io.karpfen.dsl.diagnostics.DiagnosticCode
import io.karpfen.dsl.source.SourceRange

/**
 * Collects the [Diagnostic]s of one read.
 *
 * A collector belongs to a single read and is not thread-safe.
 */
internal class DiagnosticCollector {

    private val collected = mutableListOf<Diagnostic>()

    /** The diagnostics reported so far, in reporting order. */
    val diagnostics: List<Diagnostic>
        get() = collected.toList()

    /**
     * Reports a problem of kind [code] at [range], with the severity of its kind.
     */
    fun report(code: DiagnosticCode, range: SourceRange, message: String) {
        collected.add(Diagnostic(range, code.severity, code, message))
    }
}
