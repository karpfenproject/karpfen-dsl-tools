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
package io.karpfen.dsl.reader.kmodel

import io.karpfen.dsl.diagnostics.DiagnosticCode
import io.karpfen.dsl.dsl.textual.KmetaDSLConverter
import io.karpfen.dsl.instance.ClassTypeAtomicPropertyObject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

/**
 * Tests which object a `knows` reference resolves to when an object of the model and an object outside it, inside
 * an object of an unknown class type, share an id.
 */
class KnowsPrecedenceTest {

    private val metamodel = KmetaDSLConverter.parseKmetaString(
        """
        type "Point" "a point" { prop("x", "number") }
        type "Box" "a box" { prop("x", "number") has("p", "Point") }
        type "Root" "the root" { has("p", "Point") knows("q", "Point") has("bs", list("Box")) }
        """.trimIndent()
    )

    /**
     * The object of the model wins over the object outside it, so the reference resolves to the object the
     * converter resolves it to once the unknown class type is fixed, and no reference error follows from the wrong
     * class type alone.
     */
    @Test
    fun `an object of the model wins over an object outside it with the same id`() {
        val text = """
            make object "r":"Root" {
                has("bs") -> make object "w":"Wrong" {
                    has("p") -> make object "a":"Box" { prop("x") -> "2.0" }
                }
                has("p") -> make object "a":"Point" { prop("x") -> "1.0" }
                knows("q") -> "a"
            }
        """.trimIndent()

        val result = KmodelReader(metamodel).read(text)

        assertEquals(listOf(DiagnosticCode.UNKNOWN_CLASS_TYPE), result.diagnostics.map { it.code })
        val root = result.model!!.objects.single()
        val q = root.relations.first { it.key == "q" } as ClassTypeAtomicPropertyObject
        assertSame(root.getRel("p").single(), q.getValue())
    }
}
