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
package io.karpfen.dsl.conventions

import io.karpfen.dsl.conventions.SourceComments.Dialect.GROOVY
import io.karpfen.dsl.conventions.SourceComments.Dialect.KOTLIN
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Checks that [SourceComments] finds the comments of Kotlin and Groovy sources and nothing that only looks like one.
 */
class SourceCommentsTest {

    /**
     * Line comments, block comments and documentation comments are found with the line they start on.
     */
    @Test
    fun `comments of every kind are found`() {
        val source = "/** Doc. */\nval a = 1 // trailing\n/* block\n */ val b = 2\n/**/"

        val comments = SourceComments(source, KOTLIN).scan()

        assertEquals(listOf("/** Doc. */", "// trailing", "/* block\n */", "/**/"), comments.map { it.text })
        assertEquals(listOf(1, 2, 3, 5), comments.map { it.line })
        assertEquals(listOf(true, false, false, false), comments.map { it.isDocumentation })
    }

    /**
     * Comment delimiters inside strings, raw strings, templates, chars and backticked names are no comments, while a
     * comment after them is one.
     */
    @Test
    fun `Kotlin text that only looks like a comment is no comment`() {
        val lookalikes = listOf(
            "val url = \"http://example.org\"",
            "val s = \"a \\\" // b\"",
            "val t = \"\${\"/*\"} //\"",
            "val raw = \"\"\"a // b \${\"/*\"} c\"\"\"\"",
            "val c = '\"' + '\\'' + \"//\"",
            "fun `a // b`() {}"
        )
        lookalikes.forEach { line ->
            assertEquals(listOf("// real"), SourceComments("$line // real", KOTLIN).scan().map { it.text }, line)
        }
    }

    /**
     * Kotlin nests block comments, Groovy does not.
     */
    @Test
    fun `Kotlin nests block comments`() {
        val source = "/* a /* b */ c */ x"

        assertEquals(listOf("/* a /* b */ c */"), SourceComments(source, KOTLIN).scan().map { it.text })
        assertEquals(listOf("/* a /* b */"), SourceComments(source, GROOVY).scan().map { it.text })
    }

    /**
     * Groovy strings between single quotes, between three quotes and with templates hide comment delimiters too.
     */
    @Test
    fun `Groovy text that only looks like a comment is no comment`() {
        val lookalikes = listOf(
            "def url = 'https://example.org'",
            "def s = '''a // b\n/* c */'''",
            "def t = \"\${x ? \"//\" : '/*'}\"",
            "def u = \"\"\"a // b\"\"\""
        )
        lookalikes.forEach { line ->
            assertEquals(listOf("// real"), SourceComments("$line // real", GROOVY).scan().map { it.text }, line)
        }
    }
}
