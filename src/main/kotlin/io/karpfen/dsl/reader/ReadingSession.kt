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

import io.karpfen.dsl.diagnostics.DiagnosticCode
import io.karpfen.dsl.parser.reportSyntaxErrorsTo
import io.karpfen.dsl.source.SourceLocation
import io.karpfen.dsl.source.SourceMapBuilder
import io.karpfen.dsl.source.SourceRange
import io.karpfen.dsl.source.SourceText
import org.antlr.v4.runtime.CharStream
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.ParserRuleContext
import org.antlr.v4.runtime.Recognizer
import org.antlr.v4.runtime.tree.TerminalNode

/**
 * The state of one read of one text: the locator of its tokens, the diagnostics found so far and the source
 * map built so far.
 *
 * The converters of a reader share one session, which their constructors receive. A session belongs to a
 * single read and is not thread-safe; [read] creates one per call.
 *
 * @param text The text being read.
 */
internal class ReadingSession private constructor(private val text: String) {

    /** Turns tokens and contexts of the parse of [text] into ranges. */
    val locator = TokenLocator(SourceText(text))

    private val diagnostics = DiagnosticCollector()

    private val sourceMap = SourceMapBuilder()

    private val syntaxErrorListener = SyntaxDiagnosticListener(locator, diagnostics)

    /**
     * Returns a fresh ANTLR stream of [text].
     */
    fun charStream(): CharStream = CharStreams.fromString(text)

    /**
     * Makes [recognizer] report its syntax errors as diagnostics of this session, instead of to the console.
     */
    fun <R : Recognizer<*, *>> reportingSyntaxErrors(recognizer: R): R =
        recognizer.apply { reportSyntaxErrorsTo(syntaxErrorListener) }

    /**
     * Reports a problem of kind [code] at [range].
     */
    fun report(code: DiagnosticCode, range: SourceRange, message: String) {
        diagnostics.report(code, range, message)
    }

    /**
     * Records that [element] is defined by [construct], named by the quoted string [name] and, for an `EVAL`
     * body, holds the code at [codeRange].
     */
    fun record(element: Any, construct: ParserRuleContext, name: TerminalNode? = null, codeRange: SourceRange? = null) {
        record(element, SourceLocation(locator.rangeOf(construct), name?.let { locator.innerRangeOf(it) }, codeRange))
    }

    /**
     * Records [location] as a location of [element].
     */
    fun record(element: Any, location: SourceLocation) {
        sourceMap.record(element, location)
    }

    /**
     * Returns the first location recorded for [element], or null.
     */
    fun locationOf(element: Any): SourceLocation? = sourceMap.locationOf(element)

    /**
     * Returns the range to mark when [element] is at fault: its name if it has a non-empty one, otherwise its
     * whole construct, or the start of the text if it has no location.
     */
    fun problemRangeOf(element: Any): SourceRange {
        val location = sourceMap.locationOf(element) ?: return locator.startOfText()
        return location.nameRange?.takeIf { it.length > 0 } ?: location.range
    }

    /**
     * Removes [element] from the source map, for an element left out of the model after it was recorded.
     */
    fun forget(element: Any) {
        sourceMap.forget(element)
    }

    /**
     * Bundles [model] with the diagnostics and the source map of this session.
     */
    private fun <M> resultWith(model: M?): ParseResult<M> = ParseResult(model, diagnostics.diagnostics, sourceMap.build())

    /**
     * Reports that the conversion of this session failed with [failure].
     */
    private fun reportFailure(failure: Throwable) {
        report(DiagnosticCode.READER_FAILURE, locator.startOfText(), "The text could not be read: $failure")
    }

    /** Runs reads. */
    companion object {

        /**
         * Reads [text] with a new session that [conversion] fills, and returns what it read.
         *
         * Should the conversion fail nevertheless, on nesting deeper than the parser can recurse or through a
         * defect of the reader, the failure becomes a [DiagnosticCode.READER_FAILURE] and the result holds no
         * model, so that a reader never throws.
         */
        fun <M> read(text: String, conversion: (ReadingSession) -> M?): ParseResult<M> {
            val session = ReadingSession(text)
            val model = try {
                conversion(session)
            } catch (failure: RuntimeException) {
                session.reportFailure(failure)
                null
            } catch (failure: StackOverflowError) {
                session.reportFailure(failure)
                null
            }
            return session.resultWith(model)
        }
    }
}
