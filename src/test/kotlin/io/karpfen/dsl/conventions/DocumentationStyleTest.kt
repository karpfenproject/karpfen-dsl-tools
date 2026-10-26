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
 * Guards the documentation style of the Kotlin sources of every source set, of the Gradle scripts and, for reference
 * ids, of the other text documents: the markdown documents, the CI workflows and the grammars.
 *
 * Documentation lives in KDoc on declarations, and in the Gradle scripts in the same documentation comments on their
 * declarations and top-level statements. A line comment or a block comment other than KDoc is not allowed anywhere, not
 * even as a trailing comment or a commented-out statement: code that needs an explanation gets a well-named function
 * or value with KDoc, or the explanation goes into the KDoc of the enclosing declaration. [SourceComments] finds the
 * comments, so text in a string, a char or a backticked name that only looks like a comment is no violation. KDoc
 * itself must stand where it documents a declaration, see [KDocPlacement], so that it cannot stand in for a trailing
 * comment or a comment between statements.
 *
 * Documentation is written for a human reader and names what it refers to in words instead of by an id that has to be
 * looked up in another document. [ReferenceIds] lists the ids that are rejected; they are searched in every comment of
 * the sources and scripts and in the whole text of the other documents.
 */
class DocumentationStyleTest {

    /** The text of every Kotlin source and Gradle script, by its path relative to the project folder. */
    private val sources: Map<String, String> by lazy { SourceFiles.read(setOf("kt", "kts", "gradle")) }

    /** The text of every markdown document, CI workflow and grammar, by its path relative to the project folder. */
    private val documents: Map<String, String> by lazy { SourceFiles.read(setOf("md", "yml", "g4")) }

    /**
     * The guard reads the Kotlin sources of the main, test and compatibility test source sets and every Gradle script,
     * so it cannot pass on a list of files that misses them.
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
     * The guard of the reference ids reads the README, the grammar guides, the CI workflow and the grammars.
     */
    @Test
    fun `the guarded documents are the README, the guides, the workflow and the grammars`() {
        val paths = documents.keys
        listOf("README.md", "guides/kstates_grammar_guide.md", ".github/workflows/build.yml").forEach {
            assertTrue(it in paths, "$it is not guarded")
        }
        assertEquals(3, paths.count { it.endsWith(".g4") }, "the grammars are not guarded")
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

    /**
     * Every KDoc of a guarded file documents a declaration, or in a script a top-level statement.
     */
    @Test
    fun `KDoc stands on its own line before the declaration it documents`() {
        val violations = sources.toSortedMap().flatMap { (path, text) ->
            KDocPlacement.check(text, dialectOf(path), isScript = path.endsWith(".kts") || path.endsWith(".gradle"))
                .map { "$path:${it.comment.line}  ${it.reason}: ${it.comment.text.lineSequence().first()}" }
        }
        assertEquals(
            emptyList(),
            violations,
            "KDoc documents a declaration, or in a script a top-level statement. Move the explanation into the KDoc of " +
                "the enclosing declaration, or extract a well-named function or value and document that",
        )
    }

    /**
     * No comment of a source or script and no other document refers to a document, a section or a decision by id.
     */
    @Test
    fun `documentation names what it refers to instead of using reference ids`() {
        val inComments = sources.toSortedMap().flatMap { (path, text) ->
            SourceComments(text, dialectOf(path)).scan().flatMap { comment ->
                ReferenceIds.findIn(comment.text).map { "$path:${comment.lineAt(it.offset)}  ${it.text}" }
            }
        }
        val inDocuments = documents.toSortedMap().flatMap { (path, text) ->
            ReferenceIds.findIn(text).map { "$path:${lineOf(text, it.offset)}  ${it.text}" }
        }
        assertEquals(
            emptyList(),
            inComments + inDocuments,
            "Documentation must not refer to documents, sections or decisions by id. Name the thing in words",
        )
    }

    /**
     * Returns the language of the file at [path].
     */
    private fun dialectOf(path: String): SourceComments.Dialect =
        if (path.endsWith(".gradle")) SourceComments.Dialect.GROOVY else SourceComments.Dialect.KOTLIN

    /**
     * Returns the line of the char at [offset] of [text], counted from 1.
     */
    private fun lineOf(text: String, offset: Int): Int = text.substring(0, offset).count { it == '\n' } + 1
}
