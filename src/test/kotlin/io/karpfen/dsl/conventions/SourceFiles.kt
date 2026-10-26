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
 * The source files of this repository, which the tests of its conventions read: the files that the build hands to the
 * tests in the system property [PROPERTY], which it also declares as their inputs. The build decides which files these
 * are, so the tests check exactly the files whose changes run them again; they walk no folder themselves.
 */
object SourceFiles {

    /** The system property in which the build hands over the paths of the files, relative to the project folder. */
    const val PROPERTY = "karpfen.dsl.filesTheTestsRead"

    /**
     * Returns the text of every source file whose extension is one of [extensions], by its path relative to the
     * project folder with '/' separators.
     *
     * @throws IllegalStateException If the tests do not run in the Gradle build, which hands over the files.
     */
    fun read(extensions: Set<String>): Map<String, String> {
        val paths = checkNotNull(System.getProperty(PROPERTY)) {
            "Run the tests through the Gradle build, which hands them the files to check in the system property $PROPERTY."
        }
        return paths.split(File.pathSeparator)
            .filter { File(it).extension in extensions }
            .associateWith { File(it).readText() }
    }
}
