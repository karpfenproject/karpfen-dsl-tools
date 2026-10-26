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
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Requires the Apache 2.0 license header at the top of every source file of this repository: the Kotlin sources of
 * every source set, the Gradle build and settings scripts and the ANTLR grammars.
 *
 * The header is the block that this file starts with too: the copyright of Karl Kegel and the notice of the Apache
 * License, Version 2.0, between the delimiters of a documentation comment. It stands once in each file, as its first
 * lines, before the package declaration, the grammar declaration or the first statement of a script. The files are
 * the [SourceFiles] with the extensions of [SOURCE_EXTENSIONS].
 */
class LicenseHeaderTest {

    /** The text of every source file under guard, by its path relative to the project folder, with '/' separators. */
    private val sources: Map<String, String> by lazy { SourceFiles.read(SOURCE_EXTENSIONS) }

    /**
     * The guard reads the Kotlin sources of the main, test and compatibility test source sets, the Gradle scripts and
     * the three grammars, so it cannot pass on a walk that misses them.
     */
    @Test
    fun `the guarded files are the Kotlin sources, the Gradle scripts and the grammars`() {
        val paths = sources.keys
        listOf("build.gradle", "settings.gradle", "gradle/compatibility-test.gradle").forEach {
            assertTrue(it in paths, "$it is not guarded")
        }
        listOf("src/main/kotlin/", "src/test/kotlin/", "src/compatibilityTest/kotlin/").forEach { root ->
            assertTrue(paths.any { it.startsWith(root) && it.endsWith(".kt") }, "no Kotlin source below $root is guarded")
        }
        assertEquals(3, paths.count { it.endsWith(".g4") }, "the grammars are not guarded")
    }

    /**
     * Every source file starts with the license header, followed by a line break, and holds it only once.
     */
    @Test
    fun `every source file starts with the license header once`() {
        val violations = sources.filterValues { !startsWithHeaderOnce(it) }.keys.sorted()
        assertEquals(
            emptyList(),
            violations,
            "These files do not start with the license header that the other sources carry, or repeat it",
        )
    }

    /**
     * The rule accepts the header at the top, also with Windows line breaks, and rejects a missing, changed, late or
     * repeated header.
     */
    @Test
    fun `only an unchanged header at the top that is not repeated satisfies the rule`() {
        assertTrue(startsWithHeaderOnce("$HEADER\npackage a.b\n"))
        assertTrue(startsWithHeaderOnce("$HEADER\ngrammar G;\n".replace("\n", "\r\n")))
        assertFalse(startsWithHeaderOnce("package a.b\n"))
        assertFalse(startsWithHeaderOnce(HEADER.replace("2026", "2025") + "\npackage a.b\n"))
        assertFalse(startsWithHeaderOnce("\n$HEADER\npackage a.b\n"))
        assertFalse(startsWithHeaderOnce("$HEADER package a.b\n"))
        assertFalse(startsWithHeaderOnce("$HEADER\n$HEADER\npackage a.b\n"))
    }

    /** True if [text] starts with the header and a line break, whatever line breaks it uses, and holds no second copy. */
    private fun startsWithHeaderOnce(text: String): Boolean {
        val normalized = text.replace("\r\n", "\n")
        return normalized.startsWith("$HEADER\n") && normalized.indexOf(HEADER, startIndex = HEADER.length) < 0
    }

    private companion object {

        /** The extensions of the source files: Kotlin sources and scripts, Groovy Gradle scripts and ANTLR grammars. */
        val SOURCE_EXTENSIONS = setOf("kt", "kts", "gradle", "g4")

        /** The lines of the license header. */
        val HEADER_LINES = listOf(
            "/**",
            " * Copyright 2026 Karl Kegel",
            " *",
            " * Licensed under the Apache License, Version 2.0 (the \"License\");",
            " * you may not use this file except in compliance with the License.",
            " * You may obtain a copy of the License at",
            " *",
            " * http://www.apache.org/licenses/LICENSE-2.0",
            " *",
            " * Unless required by applicable law or agreed to in writing, software",
            " * distributed under the License is distributed on an \"AS IS\" BASIS,",
            " * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.",
            " * See the License for the specific language governing permissions and",
            " * limitations under the License.",
            " */",
        )

        /** The license header, with '\n' line breaks. */
        val HEADER = HEADER_LINES.joinToString("\n")
    }
}
