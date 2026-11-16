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

import io.karpfen.dsl.composition.FlatteningResult
import io.karpfen.dsl.diagnostics.Diagnostic
import io.karpfen.dsl.source.SourceLocation
import io.karpfen.dsl.source.SourceMap
import io.karpfen.dsl.source.SourcePosition
import io.karpfen.dsl.source.SourceRange
import io.karpfen.dsl.states.StateMachine

/**
 * The texts of the files of a flattened composition, laid out one after the other, so that the checks of a single
 * state machine can run on the flattened one and still mark each problem in the file it stands in.
 *
 * The checks find where an element is written through [locationOf] and report ranges, without knowing which file a
 * range belongs to. Each file therefore gets offsets of its own: those of the first file are as in its text, those of
 * each further file start behind the end of the one before. [split] takes the problems apart by these offsets and moves
 * each back into the offsets of its file. Lines and columns are not moved, since they count within a file anyway.
 *
 * An element of the flattened state machine is found as the element it copies; an element the flattening did not copy,
 * such as the code of a macro, is found in the file whose source map knows it.
 *
 * @param flattened The flattened composition.
 * @param sourceMaps The source map of the read of each file of the composition, in the order of
 * [FlatteningResult.Flattened.sources].
 */
internal class CompositionText(
    private val flattened: FlatteningResult.Flattened,
    private val sourceMaps: List<SourceMap>
) {

    /** The offset each file starts at, in the order of the files. */
    private val starts: List<Int> = sourceMaps.runningFold(0) { start, map -> start + map.endOffset + 1 }.dropLast(1)

    /** Returns the location of [element], an element of the flattened state machine, in the offsets of its file. */
    fun locationOf(element: Any): SourceLocation? {
        flattened.elementSources[element]?.let { source ->
            val index = flattened.sources.indexOfFirst { it === source.machine }
            return sourceMaps.getOrNull(index)?.locationOf(source.original)?.let { shifted(it, starts[index]) }
        }
        sourceMaps.forEachIndexed { index, map ->
            map.locationOf(element)?.let { return shifted(it, starts[index]) }
        }
        return null
    }

    /**
     * Returns each of [diagnostics], found with [locationOf], with the file it stands in and its range in the offsets
     * of that file.
     */
    fun split(diagnostics: List<Diagnostic>): List<Pair<StateMachine, Diagnostic>> = diagnostics.map { diagnostic ->
        val index = starts.indexOfLast { it <= diagnostic.range.start.offset }.coerceAtLeast(0)
        flattened.sources[index] to diagnostic.copy(range = shifted(diagnostic.range, -starts[index]))
    }

    /** Returns [location] with every range moved by [delta]. */
    private fun shifted(location: SourceLocation, delta: Int): SourceLocation = SourceLocation(
        shifted(location.range, delta),
        location.nameRange?.let { shifted(it, delta) },
        location.codeRange?.let { shifted(it, delta) },
        location.operandRanges.map { shifted(it, delta) }
    )

    /** Returns [range] moved by [delta]. */
    private fun shifted(range: SourceRange, delta: Int): SourceRange =
        SourceRange(shifted(range.start, delta), shifted(range.end, delta))

    /** Returns [position] moved by [delta]. */
    private fun shifted(position: SourcePosition, delta: Int): SourcePosition = position.copy(offset = position.offset + delta)
}
