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
package io.karpfen.dsl.verification

import io.karpfen.dsl.diagnostics.Diagnostic
import io.karpfen.dsl.diagnostics.DiagnosticCode
import io.karpfen.dsl.source.SourceLocation
import io.karpfen.dsl.source.SourceMap
import io.karpfen.dsl.source.SourcePosition
import io.karpfen.dsl.source.SourceRange

/**
 * Collects the problems found in the text of one state machine, or of one metamodel, and finds where to mark them,
 * from the source map of its read.
 *
 * The report of a flattened composition finds the elements of all its files with one function, see
 * [CompositionText], whose ranges stand for the texts laid out one after the other.
 *
 * @param locationOf Returns the location of an element, or null for an element without one.
 */
internal class MachineReport(private val locationOf: (Any) -> SourceLocation?) {

    /** Creates the report of the text whose read has the source map [sourceMap]. */
    constructor(sourceMap: SourceMap) : this(sourceMap::locationOf)

    /** The problems reported so far, in the order of their reports. */
    private val found = mutableListOf<Diagnostic>()

    /** The problems reported, sorted by their offset; a problem reported twice at the same place appears once. */
    val diagnostics: List<Diagnostic>
        get() = found.distinct().sortedBy { it.range.start.offset }

    /** Reports a problem of the kind [code] at [range], described by [message]. */
    fun report(code: DiagnosticCode, range: SourceRange, message: String) {
        found.add(Diagnostic(range, code.severity, code, message))
    }

    /** Returns the range to mark when [element] is at fault: its name if it has a non-empty one, otherwise all of it. */
    fun rangeOf(element: Any): SourceRange {
        val location = locationOf(element) ?: return START_OF_TEXT
        return location.nameRange?.takeIf { it.length > 0 } ?: location.range
    }

    /** Returns the range of the operand at [index] of [element] if it is not empty, otherwise [rangeOf] the element. */
    fun operandRangeOf(element: Any, index: Int): SourceRange =
        locationOf(element)?.operandRanges.orEmpty().getOrNull(index)?.takeIf { it.length > 0 } ?: rangeOf(element)

    /**
     * Returns the range from the start of [element] to the end of its last operand with the closing quote of that
     * KStates string, the head of a transition, or [rangeOf] the element without operands.
     */
    fun headRangeOf(element: Any): SourceRange {
        val location = locationOf(element) ?: return START_OF_TEXT
        val lastValueEnd = location.operandRanges.lastOrNull()?.end ?: return rangeOf(element)
        val end = SourcePosition(lastValueEnd.offset + 1, lastValueEnd.line, lastValueEnd.column + 1)
        return SourceRange(location.range.start, minOf(end, location.range.end, compareBy { it.offset }))
    }

    /** Returns the range of the code of [element], an element that holds the code of an `EVAL`, or null. */
    fun codeRangeOf(element: Any): SourceRange? = locationOf(element)?.codeRange

    private companion object {

        /** The place to mark a problem of an element without a location: the start of the text. */
        val START_OF_TEXT = SourceRange(SourcePosition(0, 1, 1), SourcePosition(0, 1, 1))
    }
}
