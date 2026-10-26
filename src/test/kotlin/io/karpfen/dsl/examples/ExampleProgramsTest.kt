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
package io.karpfen.dsl.examples

import io.karpfen.dsl.dsl.textual.ConsoleOutput
import io.karpfen.dsl.dsl.textual.captureConsole
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Runs the example programs, whose output is what they print to standard output, as from the command line.
 */
class ExampleProgramsTest {

    /**
     * The KMeta example prints the types it parsed.
     */
    @Test
    fun `the kmeta example prints the parsed types`() {
        val console = run("io.karpfen.dsl.examples.KmetaParserExampleKt")

        assertTrue(console.out.startsWith("Parsed 2 type(s):"), console.out)
        assertTrue(console.out.contains("Type: Person") && console.out.contains("• zipCode: string"), console.out)
        assertEquals("", console.err)
    }

    /**
     * The KModel example prints the objects it parsed.
     */
    @Test
    fun `the kmodel example prints the parsed objects`() {
        val console = run("io.karpfen.dsl.examples.KmodelParserExampleKt")

        assertTrue(console.out.startsWith("Parsed 1 object(s)"), console.out)
        assertTrue(console.out.contains("Object: make object \"APB 2101:Room\""), console.out)
        assertEquals("", console.err)
    }

    /**
     * Runs the `main` function of the program class [className] with no arguments and returns what it printed.
     */
    private fun run(className: String): ConsoleOutput = captureConsole {
        Class.forName(className).getMethod("main", Array<String>::class.java).invoke(null, emptyArray<String>())
    }
}
