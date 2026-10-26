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

import java.io.File

/**
 * Holds texts in files for the conversions of files, one file per extension, which each text overwrites.
 *
 * @param directory The directory of the files, by default below the build directory of the project the tests run in.
 */
class DocumentFiles(private val directory: File = File("build/tmp/compatibilityTest/documents")) {

    /**
     * Writes [text] into the file with [extension] and returns its path.
     */
    fun holding(text: String, extension: String): String {
        directory.mkdirs()
        val file = directory.resolve("document.$extension")
        file.writeText(text)
        return file.path
    }
}
