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

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Guards the documentation style of the Kotlin sources of every source set and of the Gradle scripts.
 *
 * Documentation lives in KDoc on declarations, and in the Gradle scripts in the same documentation comments on their
 * declarations and statements. A line comment or a block comment other than KDoc is not allowed anywhere, not even as
 * a trailing comment or a commented-out statement: code that needs an explanation gets a well-named function or value
 * with KDoc, or the explanation goes into the KDoc of the enclosing declaration. [SourceComments] finds the comments,
 * so text in a string, a char or a backticked name that only looks like a comment is no violation.
 */
class DocumentationStyleTest {

    /** The text of every guarded file, by its path relative to the project folder. */
    private val sources: Map<String, String> by lazy { SourceFiles.read(setOf("kt", "kts", "gradle")) }

    /**
     * The guard reads the Kotlin sources of the main, test and compatibility test source sets and every Gradle script,
     * so it cannot pass on a walk that misses them.
     */
    @Test
    fun `the guarded files are the Kotlin sources and the Gradle scripts`() {
        val paths = sources.keys
        listOf("build.gradle", "settings.gradle", "gradle/compatibility-test.gradle").forEach {
            assertTrue(it in paths, "$it is not guarded")
        }
        listOf("src/main/kotlin/", "src/test/kotlin/", "src/compatibilityTest/kotlin/").forEach { root ->
            assertTrue(paths.any { it.startsWith(root) && it.endsWith(".kt") }, "no Kotlin source below $root is guarded")
        }
    }

    /**
     * No guarded file holds a line comment or a block comment other than KDoc.
     */
    @Test
    fun `Kotlin sources and Gradle scripts have no line comments and no block comments other than KDoc`() {
        val violations = sources.toSortedMap().flatMap { (path, text) ->
            SourceComments(text, dialectOf(path)).scan()
                .filterNot { it.isDocumentation }
                .map { "$path:${it.line}  ${it.text.lineSequence().first()}" }
        }
        assertEquals(
            emptyList(),
            violations,
            "Line comments and block comments other than KDoc are not allowed. Document the declaration in KDoc, or " +
                "extract a well-named function or value and document that",
        )
    }

    private fun dialectOf(path: String): SourceComments.Dialect =
        if (path.endsWith(".gradle")) SourceComments.Dialect.GROOVY else SourceComments.Dialect.KOTLIN
}
