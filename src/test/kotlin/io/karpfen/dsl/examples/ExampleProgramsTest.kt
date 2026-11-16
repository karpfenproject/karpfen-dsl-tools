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
     * The KMeta example finds no problem and prints the types it read.
     */
    @Test
    fun `the kmeta example prints the types it read`() {
        val console = run("io.karpfen.dsl.examples.KmetaReaderExampleKt")

        assertTrue(console.out.startsWith("No problems.\nRead 2 type(s), root type Person:\n"), console.out)
        assertTrue(console.out.contains("Type: Person") && console.out.contains("      - zipCode: string"), console.out)
        assertTrue(console.out.contains("      - friends: list(Person)"), console.out)
        assertEquals("", console.err)
    }

    /**
     * The KModel example finds no problem and prints the objects it read, the one a `knows` leads to by its id.
     */
    @Test
    fun `the kmodel example prints the objects it read`() {
        val console = run("io.karpfen.dsl.examples.KmodelReaderExampleKt")

        assertTrue(console.out.startsWith("No problems.\nNo problems.\nRead 1 root object(s)\n"), console.out)
        assertTrue(console.out.contains("Object \"APB 2101\" of type Room"), console.out)
        assertTrue(console.out.contains("  - home knows \"dock\""), console.out)
        assertEquals("", console.err)
    }

    /**
     * Runs the `main` function of the program class [className] with no arguments and returns what it printed.
     */
    private fun run(className: String): ConsoleOutput = captureConsole {
        Class.forName(className).getMethod("main", Array<String>::class.java).invoke(null, emptyArray<String>())
    }
}
