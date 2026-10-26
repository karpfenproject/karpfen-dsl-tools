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

import io.karpfen.dsl.source.SourceLocation
import io.karpfen.dsl.source.SourceMap
import io.karpfen.dsl.source.SourceRange
import kotlin.test.fail

/**
 * Checks the locations a [SourceMap] holds for the elements of a model against the [text] they were read from,
 * and collects every deviation, so that one run shows all of them.
 *
 * Positions are checked against lines and columns counted independently of the code under test.
 *
 * @param text The text the model was read from.
 * @param sourceMap The source map of the model.
 */
class LocationChecker(private val text: String, private val sourceMap: SourceMap) {

    private val deviations = mutableListOf<String>()

    /** The number of locations checked so far. */
    var checked = 0
        private set

    /**
     * Checks that [element] has a location whose construct starts with [keyword], ignoring case, whose name is
     * [name] and whose code is [code], each only if given; returns the location.
     */
    fun expect(element: Any, what: String, keyword: String? = null, name: String? = null, code: String? = null): SourceLocation? {
        val location = sourceMap.locationOf(element)
        if (location == null) {
            deviations.add("$what has no location")
            return null
        }
        check(location, what, keyword, name, code)
        return location
    }

    /**
     * Checks every location of [element] like [expect] and that there are [count] of them.
     */
    fun expectEach(element: Any, what: String, count: Int, keyword: String? = null, name: String? = null) {
        val locations = sourceMap.locationsOf(element)
        if (locations.size != count) {
            deviations.add("$what has ${locations.size} locations instead of $count")
        }
        locations.forEach { check(it, what, keyword, name, null) }
    }

    /**
     * Checks that the construct of [inner] lies within the construct of [outer].
     */
    fun expectWithin(outer: Any, inner: Any, what: String) {
        val outerRange = sourceMap.rangeOf(outer) ?: return
        val innerRange = sourceMap.rangeOf(inner) ?: return
        if (!outerRange.encloses(innerRange)) {
            deviations.add("$what at $innerRange does not lie within $outerRange")
        }
    }

    /**
     * Returns the text [range] covers.
     */
    fun textOf(range: SourceRange): String = range.textIn(text)

    /**
     * Fails with every deviation found so far.
     */
    fun assertNoDeviations(subject: String) {
        if (deviations.isNotEmpty()) {
            fail("$subject: ${deviations.size} deviations, the first ones:\n" + deviations.take(10).joinToString("\n"))
        }
    }

    private fun check(location: SourceLocation, what: String, keyword: String?, name: String?, code: String?) {
        checked += 1
        val range = location.range
        listOfNotNull(range, location.nameRange, location.codeRange).forEach { checkPositions(it, what) }
        if (keyword != null && !range.textIn(text).startsWith(keyword, ignoreCase = true)) {
            deviations.add("$what starts with '${range.textIn(text).take(20)}' instead of '$keyword'")
        }
        checkPart(range, location.nameRange, name, "name", what)
        checkPart(range, location.codeRange, code, "code", what)
    }

    private fun checkPart(range: SourceRange, part: SourceRange?, expected: String?, partName: String, what: String) {
        if (expected == null) {
            return
        }
        when {
            part == null -> deviations.add("$what has no $partName range")
            part.textIn(text) != expected -> deviations.add("$what has the $partName '${part.textIn(text)}' instead of '$expected'")
            !range.encloses(part) -> deviations.add("The $partName of $what lies outside its construct")
        }
    }

    private fun checkPositions(range: SourceRange, what: String) {
        TextPositions.deviationOf(text, range)?.let { deviations.add("$what: $it") }
    }
}
