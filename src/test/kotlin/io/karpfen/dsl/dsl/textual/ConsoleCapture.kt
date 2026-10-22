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
package io.karpfen.dsl.dsl.textual

import java.io.ByteArrayOutputStream
import java.io.PrintStream

/**
 * What a piece of code wrote to the console.
 *
 * @property out The text written to standard output.
 * @property err The text written to standard error.
 */
data class ConsoleOutput(val out: String, val err: String)

/**
 * Runs [action] with standard output and standard error redirected and returns what it wrote to each.
 */
fun captureConsole(action: () -> Unit): ConsoleOutput {
    val originalOut = System.out
    val originalErr = System.err
    val out = ByteArrayOutputStream()
    val err = ByteArrayOutputStream()
    System.setOut(PrintStream(out, true, Charsets.UTF_8))
    System.setErr(PrintStream(err, true, Charsets.UTF_8))
    try {
        action()
    } finally {
        System.setOut(originalOut)
        System.setErr(originalErr)
    }
    return ConsoleOutput(out.toString(Charsets.UTF_8), err.toString(Charsets.UTF_8))
}
