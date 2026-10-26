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
 * Covers [KDocPlacement] and the block tracking of [CodeBlocks] behind it: KDoc on every kind of declaration passes,
 * KDoc in the place of a trailing comment or a comment between statements does not, in Kotlin files, Kotlin scripts
 * and Groovy Gradle scripts.
 */
class KDocPlacementTest {

    /** The reason against KDoc inside a function body, a lambda or a closure. */
    private val inside = "stands inside a function body, a lambda or a closure"

    /**
     * Returns the lines and reasons of the misplaced KDoc in [source].
     */
    private fun misplaced(source: String, dialect: SourceComments.Dialect = KOTLIN, isScript: Boolean = false) =
        KDocPlacement.check(source, dialect, isScript).map { it.comment.line to it.reason }

    /**
     * KDoc on top-level declarations, members, companion objects, enum entries, constructor properties and members of
     * anonymous objects is in place.
     */
    @Test
    fun `KDoc on declarations is in place`() {
        val source = """
            /** A license header. */
            package a

            /** A class. */
            @Suppress("x (y)")
            public data class A(
                /** A property. */
                private val a: Int,
            ) {
                /** A member. */
                override fun toString(): String = "a"

                /** An object. */
                companion object {
                    /** A constant. */
                    const val B = 1
                }
            }

            /** An enum. */
            enum class E(val label: String) {
                /** An entry. */
                FIRST("1"),

                /** Another. */
                @Deprecated("no")
                SECOND("2");

                /** A member after the entries. */
                val upper: String get() = label.uppercase()
            }

            /** A class with a private constructor. */
            internal class C private constructor(val c: Int) {
                /** A secondary constructor. */
                constructor() : this(0)

                /** A member of an anonymous object. */
                val listener = object : Runnable {
                    /** Runs. */
                    override fun run() = Unit
                }
            }

            /** A function. */
            fun f(x: KClass<*> = String::class) = listOf(1).map { it }
        """.trimIndent()

        assertEquals(emptyList(), misplaced(source))
    }

    /**
     * KDoc after code on its line is a trailing comment.
     */
    @Test
    fun `KDoc after code on the same line is a trailing comment`() {
        assertEquals(listOf(2 to "follows code on its line"), misplaced("/** A header. */\nval a = 1 /** note */\nval b = 2"))
    }

    /**
     * KDoc between statements or on a local declaration stands inside a function body, also in the body of an enum
     * entry.
     */
    @Test
    fun `KDoc between statements or on a local declaration stands inside a function body`() {
        val source = """
            /** A header. */
            fun f() {
                val a = 1
                /** Between statements. */
                println(a)
                listOf(a).forEach {
                    /** A local function in a lambda. */
                    fun g() = it
                }
            }

            /** An enum. */
            enum class E {
                /** An entry with a body. */
                A {
                    /** Its override. */
                    override fun f() {
                        /** Inside. */
                        g()
                    }
                }
            }
        """.trimIndent()

        assertEquals(listOf(4 to inside, 7 to inside, 18 to inside), misplaced(source))
    }

    /**
     * KDoc before another comment, at the end or before a package documents nothing.
     */
    @Test
    fun `KDoc that documents nothing is misplaced`() {
        val nothing = "documents nothing: another comment or the end follows"
        val beforeComment = "/** A header. */\n/** One. */\n/** Two. */\nval a = 1\n/** Last. */"
        val beforePackage = "/** A header. */\n/** A package. */\npackage a.b\n"

        assertEquals(listOf(2 to nothing, 5 to nothing), misplaced(beforeComment))
        assertEquals(listOf(2 to "is not followed by a declaration"), misplaced(beforePackage))
    }

    /**
     * In a Kotlin script, KDoc documents top-level statements and blocks, but not the statements inside them.
     */
    @Test
    fun `in a Kotlin script KDoc documents top-level statements and blocks, but not the statements inside them`() {
        val script = """
            /** A header. */
            plugins {
                id("a")
            }

            /** The dependencies. */
            dependencies {
                api(project(":a"))
                /** Between two dependencies. */
                api(project(":b"))
            }
        """.trimIndent()

        assertEquals(listOf(9 to inside), misplaced(script, isScript = true))
    }

    /**
     * In a Groovy Gradle script, KDoc documents top-level statements and the members of classes, but not the
     * statements inside closures and methods.
     */
    @Test
    fun `in a Groovy script KDoc documents top-level statements and class members only`() {
        val script = """
            /** A header. */
            plugins {
                id 'a'
            }

            /** A task class. */
            abstract class Archive extends DefaultTask {

                /** An input. */
                @Input
                abstract Property<String> getCommit()

                /** The action. */
                @TaskAction
                void write() {
                    def a = 1
                    /** Between statements. */
                    println a
                }
            }

            /** A condition. */
            if (project == rootProject) {
                tasks.named('check') {
                    /** Inside a closure. */
                    dependsOn 'x'
                }
            }
        """.trimIndent()

        assertEquals(listOf(17 to inside, 25 to inside), misplaced(script, GROOVY, isScript = true))
    }

    /**
     * The keyword before a brace tells the blocks apart.
     */
    @Test
    fun `blocks are told apart by the keyword before their brace`() {
        val blocks = SourceComments(
            "class A(val f: () -> Unit = { /**/ }) { /**/ init { /**/ } }\nenum class E { /**/ A({ /**/ }) { /**/ } }\n" +
                "val x = run { /**/ }",
            KOTLIN
        ).scanPlaced().map { it.block }

        assertEquals(
            listOf(
                BlockKind.CODE, BlockKind.DECLARATIONS, BlockKind.CODE, BlockKind.ENUM_BODY, BlockKind.CODE,
                BlockKind.DECLARATIONS, BlockKind.CODE
            ),
            blocks
        )
    }
}
