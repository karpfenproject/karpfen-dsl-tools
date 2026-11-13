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
import io.karpfen.dsl.instance.SimpleAtomicPropertyObject
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests what the KModel reader reports beyond the metamodel: object ids used twice, several values for a key that holds
 * one, and boolean values other than `true` and `false`.
 */
class KmodelConsistencyTest {

    private val metamodel = KmetaDSLConverter.parseKmetaString(
        """
        type "Point" "a point" { prop("x", "number") }
        type "Robot" "a robot" {
            prop("ready", "boolean")
            prop("log", list("string"))
            has("position", "Point")
            knows("target", "Point")
            knows("visited", list("Point"))
        }
        type "Room" "a room" { has("robot", "Robot") has("robots", list("Robot")) has("points", list("Point")) }
        """.trimIndent()
    )

    private val reader = KmodelReader(metamodel)

    /** A second object with the id of an earlier one is reported at its id, also an embedded one. */
    @Test
    fun `an object id used twice is reported at the later id`() {
        val text = """
            make object "room":"Room" {
                has("robot") -> make object "r":"Robot" { has("position") -> make object "p":"Point" { } }
                has("points") -> make object "p":"Point" { }
                has("points") -> make object "q":"Point" { }
                has("points") -> make object "r":"Point" { }
            }
        """.trimIndent()

        val result = reader.read(text)

        assertEquals(listOf(DiagnosticCode.DUPLICATE_OBJECT_ID, DiagnosticCode.DUPLICATE_OBJECT_ID), result.diagnostics.map { it.code })
        assertEquals(listOf(3 to "p", 5 to "r"), result.diagnostics.map { it.range.start.line to it.range.textIn(text) })
    }

    /** A key that is no list is reported for each further value, at its key; a list key takes several values. */
    @Test
    fun `several values for a key that holds one are reported`() {
        val text = """
            make object "room":"Room" {
                has("robot") -> make object "r":"Robot" {
                    prop("ready") -> "true"
                    prop("ready") -> "false"
                    prop("log") -> "a"
                    prop("log") -> "b"
                    knows("target") -> "p"
                    knows("target") -> "q"
                    knows("visited") -> "p"
                    knows("visited") -> "q"
                }
                has("points") -> make object "p":"Point" { }
                has("points") -> make object "q":"Point" { }
            }
        """.trimIndent()

        val result = reader.read(text)

        assertEquals(listOf(DiagnosticCode.SEVERAL_VALUES, DiagnosticCode.SEVERAL_VALUES), result.diagnostics.map { it.code })
        assertEquals(listOf(4 to "ready", 8 to "target"), result.diagnostics.map { it.range.start.line to it.range.textIn(text) })
    }

    /** A boolean other than `true` and `false` is reported at the value and read as false; any case is fine. */
    @Test
    fun `a boolean other than true and false is reported`() {
        val text = """
            make object "room":"Room" {
                has("robots") -> make object "r":"Robot" { prop("ready") -> "yes" }
                has("robots") -> make object "t":"Robot" { prop("ready") -> "TRUE" }
            }
        """.trimIndent()

        val result = reader.read(text)

        assertEquals(listOf(DiagnosticCode.INVALID_BOOLEAN), result.diagnostics.map { it.code })
        assertEquals("yes", result.diagnostics.single().range.textIn(text))
        val robot = result.model!!.objects.single().getRel("robots").first()
        assertEquals(false, (robot.properties.single() as SimpleAtomicPropertyObject).getValue())
    }
}
