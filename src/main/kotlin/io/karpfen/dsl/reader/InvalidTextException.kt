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

/**
 * Thrown for a text that has errors where a model was expected, by [ParseResult.modelOrThrow] and so by the
 * converters of `io.karpfen.dsl.dsl.textual`, which read through the readers.
 *
 * It carries every error the reader found in the text, not only the first, so that one failure shows all that is
 * wrong. Its message names each error on a line of its own with its position, its code and its message, in the form
 * `<source>:<line>:<column>: <code>: <message>`, so that a failing test or a log line is enough to find the problem.
 * Warnings are not part of it, since they do not make a text wrong.
 *
 * It is an [IllegalArgumentException], since the text is a wrong argument of the call, so that code that catches
 * those also catches this one.
 *
 * @property errors The errors of the text, of severity [Severity.ERROR], ordered by their position; never empty.
 * @property source What the text is, such as the path of the file it was read from; null for a text that is no file.
 */
class InvalidTextException(
    val errors: List<Diagnostic>,
    val source: String? = null
) : IllegalArgumentException(messageOf(errors, source)) {

    init {
        require(errors.isNotEmpty()) { "An invalid text has at least one error" }
        require(errors.all { it.severity == Severity.ERROR }) { "Only errors make a text invalid" }
    }

    /** Writes the message. */
    private companion object {

        /**
         * Returns the message for [errors] of the text [source]: a first line with their number, followed by one
         * line per error.
         */
        fun messageOf(errors: List<Diagnostic>, source: String?): String {
            val count = if (errors.size == 1) "1 error" else "${errors.size} errors"
            val heading = if (source == null) "The text has $count:" else "$source has $count:"
            val lines = errors.map { error ->
                val start = error.range.start
                val position = listOfNotNull(source, start.line, start.column).joinToString(":")
                "$position: ${error.code.id}: ${error.message}"
            }
            return (listOf(heading) + lines).joinToString("\n")
        }
    }
}
