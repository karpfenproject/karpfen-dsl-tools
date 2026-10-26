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

import java.io.File

/**
 * The source files of this repository, which the tests of its conventions read: the files below the project folder
 * the tests run in, without the build output of the project and the hidden folders.
 */
object SourceFiles {

    /**
     * Returns the text of every source file whose extension is one of [extensions], by its path relative to the
     * project folder with '/' separators.
     */
    fun read(extensions: Set<String>): Map<String, String> {
        val root = File("").absoluteFile
        return root.walkTopDown()
            .onEnter { it == root || !(it.name.startsWith(".") || it == root.resolve("build")) }
            .filter { it.isFile && it.extension in extensions }
            .associate { it.relativeTo(root).invariantSeparatorsPath to it.readText() }
    }
}
