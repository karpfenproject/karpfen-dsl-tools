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
package io.karpfen.dsl.reader.kmeta

import io.karpfen.dsl.diagnostics.DiagnosticCode
import io.karpfen.dsl.diagnostics.Severity
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests the names the KMeta reader reports as defined twice: types of the same name, and keys of the same name in one
 * type.
 */
class KmetaConsistencyTest {

    /** A second type of a name is reported at its name, and the metamodel keeps both. */
    @Test
    fun `a type defined twice is reported at the later name`() {
        val text = """
            type "A" "first" { prop("x", "number") }
            type "B" "other" { }
            type "A" "again" { }
        """.trimIndent()

        val result = KmetaReader().read(text)

        assertEquals(listOf(DiagnosticCode.DUPLICATE_TYPE), result.diagnostics.map { it.code })
        assertEquals(Severity.ERROR, result.diagnostics.single().severity)
        assertEquals("A", result.diagnostics.single().range.textIn(text))
        assertEquals(3, result.diagnostics.single().range.start.line)
        assertEquals(3, result.model!!.types.size)
    }

    /** A key used twice in one type is reported at the later key, whether props or relations hold it. */
    @Test
    fun `a key defined twice in one type is reported at the later key`() {
        val text = """
            type "P" "point" { prop("x", "number") }
            type "A" "a" {
                prop("x", "number")
                has("y", "P")
                knows("x", "P")
                prop("y", "string")
                prop("z", "number")
            }
        """.trimIndent()

        val result = KmetaReader().read(text)

        assertEquals(listOf(DiagnosticCode.DUPLICATE_KEY, DiagnosticCode.DUPLICATE_KEY), result.diagnostics.map { it.code })
        assertEquals(listOf(5 to "x", 6 to "y"), result.diagnostics.map { it.range.start.line to it.range.textIn(text) })
    }

    /** The same key in different types, and different keys in one type, are fine. */
    @Test
    fun `the same key in different types is fine`() {
        val text = """
            type "P" "point" { prop("x", "number") prop("y", "number") }
            type "Q" "point" { prop("x", "number") has("p", "P") }
        """.trimIndent()

        assertEquals(emptyList(), KmetaReader().read(text).diagnostics)
    }
}
