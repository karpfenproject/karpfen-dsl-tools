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
import dsl.textual.KmetaDSLConverter
import kmeta.KmetaLexer
import org.antlr.v4.runtime.BaseErrorListener
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.RecognitionException
import org.antlr.v4.runtime.Recognizer
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Regression test for single-line comment handling in the KMeta grammar.
 *
 * Previously the KMeta lexer had no comment rule, so every `//` comment produced a cascade of
 * "token recognition error" messages. Parsing recovered (it did not fail), but the noise was reported
 * for each commented character. These tests assert that `//` comments are lexed cleanly and ignored.
 */
class KmetaCommentParsingTest {

    private val commentedContent = """
        // Top-level comment describing the metamodel
        type "Vector" "A vector in 2D space" { // inline comment after the brace
            prop("x", "number") // the x coordinate
            prop("y", "number")
        }

        // Another type follows
        type "Robot" "A robot" {
            has("position", "Vector")
        }
    """

    /** Collects ANTLR syntax/lexer errors instead of printing them to stderr. */
    private class CountingErrorListener : BaseErrorListener() {
        val errors = mutableListOf<String>()
        override fun syntaxError(
            recognizer: Recognizer<*, *>?,
            offendingSymbol: Any?,
            line: Int,
            charPositionInLine: Int,
            msg: String?,
            e: RecognitionException?
        ) {
            errors.add("line $line:$charPositionInLine $msg")
        }
    }

    @Test
    fun `single-line comments produce no lexer errors`() {
        val lexer = KmetaLexer(CharStreams.fromString(commentedContent.trimIndent()))
        val listener = CountingErrorListener()
        lexer.removeErrorListeners()
        lexer.addErrorListener(listener)

        lexer.allTokens // drain the whole token stream

        assertTrue(
            listener.errors.isEmpty(),
            "Expected no lexer errors for commented KMeta, but got:\n${listener.errors.joinToString("\n")}"
        )
    }

    @Test
    fun `commented kmeta parses into the expected types`() {
        val metamodel = KmetaDSLConverter.parseKmetaString(commentedContent)
        assertEquals(2, metamodel.types.size)
        assertTrue(metamodel.types.map { it.name }.containsAll(listOf("Vector", "Robot")))
    }
}
