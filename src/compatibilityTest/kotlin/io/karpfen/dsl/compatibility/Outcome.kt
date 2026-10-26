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
package io.karpfen.dsl.compatibility

import io.karpfen.dsl.dsl.textual.captureConsole
import io.karpfen.dsl.fixtures.StructureDump

/**
 * What a throwing converter made of a text, in a form that compares equal between version 1.0 and the current
 * version.
 */
sealed interface Outcome {

    /**
     * The converter returned a model.
     *
     * @property structure The structure of the model, see [StructureDump].
     */
    data class Converted(val structure: String) : Outcome

    /**
     * The converter threw.
     *
     * @property exceptionType The fully qualified class name of the exception.
     * @property message The message of the exception, with the package names of the current version written
     * as those of version 1.0.
     */
    data class Failed(val exceptionType: String, val message: String?) : Outcome

    /** Runs conversions. */
    companion object {

        /**
         * Runs [conversion] with the console silenced, since version 1.0 prints, and captures its outcome.
         */
        fun of(conversion: () -> Any?): Outcome {
            var outcome: Outcome? = null
            captureConsole {
                outcome = try {
                    Converted(StructureDump.of(conversion()))
                } catch (failure: Exception) {
                    failedWith(failure)
                } catch (failure: StackOverflowError) {
                    failedWith(failure)
                }
            }
            return checkNotNull(outcome)
        }

        private fun failedWith(failure: Throwable): Failed = Failed(failure.javaClass.name, legacyPackages(failure.message))

        /**
         * Writes the packages of the current version in [message] as those of version 1.0: the grammar packages
         * `io.karpfen.dsl.grammar.P` as `P`, every other package `io.karpfen.dsl.P` as `P`.
         */
        private fun legacyPackages(message: String?): String? =
            message?.replace("io.karpfen.dsl.grammar.", "")?.replace("io.karpfen.dsl.", "")
    }
}
