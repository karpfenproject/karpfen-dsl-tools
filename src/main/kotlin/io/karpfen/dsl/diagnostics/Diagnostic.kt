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
package io.karpfen.dsl.diagnostics

import io.karpfen.dsl.source.SourceRange

/**
 * A problem found in a text, at the place where it occurs.
 *
 * @property range The offending part of the text; empty where something is missing, for example at the end
 * of the text.
 * @property severity How serious the problem is.
 * @property code What kind of problem it is; stable across versions, unlike [message].
 * @property message A description of the problem for a human reader.
 */
data class Diagnostic(
    val range: SourceRange,
    val severity: Severity,
    val code: DiagnosticCode,
    val message: String
)
