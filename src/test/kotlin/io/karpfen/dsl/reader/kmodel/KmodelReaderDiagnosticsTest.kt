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
import io.karpfen.dsl.diagnostics.Severity
import io.karpfen.dsl.dsl.textual.KmetaDSLConverter
import io.karpfen.dsl.instance.ClassTypeAtomicPropertyObject
import io.karpfen.dsl.instance.ClassTypeListPropertyObject
import io.karpfen.dsl.instance.DataObject
import io.karpfen.dsl.instance.Model
import io.karpfen.dsl.reader.ParseResult
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

/**
 * Tests the problems the KModel reader reports on broken texts, where it reports them, and the partial model
 * it builds nevertheless.
 */
class KmodelReaderDiagnosticsTest {

    private val metamodel = KmetaDSLConverter.parseKmetaString(
        """
        type "Point" "a point" {
            prop("x", "number")
            prop("label", "string")
        }
        type "Robot" "a robot" {
            prop("speed", "number")
            has("position", "Point")
            knows("target", "Point")
            knows("visited", list("Point"))
        }
        type "Room" "a room" {
            has("robot", "Robot")
            has("points", list("Point"))
        }
        """.trimIndent()
    )

    private val reader = KmodelReader(metamodel)

    /**
     * An object of an unknown class type is reported at the class name and left out; its siblings stay.
     */
    @Test
    fun `an unknown class type is reported at the class name`() {
        val text = room(
            """
            has("points") -> make object "a":"Pointt" { prop("x") -> "1.0" }
            has("points") -> make object "b":"Point" { prop("x") -> "2.0" }
            """
        )

        val result = reader.read(text)

        assertCodes(listOf(DiagnosticCode.UNKNOWN_CLASS_TYPE), result)
        assertEquals("Pointt", result.diagnostics.single().range.textIn(text))
        assertEquals(listOf("b"), result.root().getRel("points").map { it.id })
    }

    /**
     * Unknown property and relation keys are reported at the key; the other statements of the object stay.
     */
    @Test
    fun `unknown keys are reported at the key`() {
        val text = room(
            """
            has("robot") -> make object "r":"Robot" {
                prop("sped") -> "1.0"
                prop("speed") -> "2.0"
                has("positon") -> make object "p":"Point" { }
            }
            """
        )

        val result = reader.read(text)

        assertCodes(listOf(DiagnosticCode.UNKNOWN_PROPERTY, DiagnosticCode.UNKNOWN_RELATION), result)
        assertEquals(listOf("sped", "positon"), result.diagnostics.map { it.range.textIn(text) })
        assertEquals(listOf(2.0), robotOf(result).getProp("speed"))
    }

    /**
     * `has` on a knows relation and `knows` on a has relation are reported at the key.
     */
    @Test
    fun `a relation of the wrong kind is reported at the key`() {
        val text = room(
            """
            has("robot") -> make object "r":"Robot" {
                has("target") -> make object "t":"Point" { }
                knows("position") -> "t"
            }
            """
        )

        val result = reader.read(text)

        assertCodes(listOf(DiagnosticCode.WRONG_RELATION_KIND, DiagnosticCode.WRONG_RELATION_KIND), result)
        assertEquals(listOf("target", "position"), result.diagnostics.map { it.range.textIn(text) })
        assertTrue(robotOf(result).relations.isEmpty())
    }

    /**
     * A value that is not a number is reported at the value and left out.
     */
    @Test
    fun `an invalid number is reported at the value`() {
        val text = room("""has("robot") -> make object "r":"Robot" { prop("speed") -> "fast" }""")

        val result = reader.read(text)

        assertCodes(listOf(DiagnosticCode.INVALID_NUMBER), result)
        assertEquals("fast", result.diagnostics.single().range.textIn(text))
        assertEquals(emptyList(), robotOf(result).getProp("speed"))
    }

    /**
     * An unresolvable single reference stays, pointing to no object; an unresolvable list entry is dropped and
     * the resolvable entries stay.
     */
    @Test
    fun `unresolved references are reported at the id`() {
        val text = room(
            """
            has("points") -> make object "p1":"Point" { }
            has("robot") -> make object "r":"Robot" {
                knows("target") -> "p9"
                knows("visited") -> "p1"
                knows("visited") -> "p8"
            }
            """
        )

        val result = reader.read(text)

        assertCodes(listOf(DiagnosticCode.UNRESOLVED_REFERENCE, DiagnosticCode.UNRESOLVED_REFERENCE), result)
        assertEquals(listOf("p9", "p8"), result.diagnostics.map { it.range.textIn(text) })
        val robot = robotOf(result)
        val target = robot.relations.first { it.key == "target" } as ClassTypeAtomicPropertyObject
        assertNull(target.getValue())
        assertEquals("p9", target.getValueRef().objectId)
        val visited = robot.relations.first { it.key == "visited" } as ClassTypeListPropertyObject
        assertEquals(listOf("p1"), visited.getValues().map { it.id })
        assertEquals(listOf("p1"), visited.getValueRefs().map { it.objectId })
    }

    /**
     * A reference to an object of another class type is reported at the id.
     */
    @Test
    fun `a reference to an object of another type is reported at the id`() {
        val text = room("""has("robot") -> make object "r":"Robot" { knows("target") -> "r" }""")

        val result = reader.read(text)

        assertCodes(listOf(DiagnosticCode.REFERENCE_TYPE_MISMATCH), result)
        assertEquals("r", result.diagnostics.single().range.textIn(text))
    }

    /**
     * An embedded object of another class type than its relation declares is accepted with a warning, as the
     * throwing converter accepts it.
     */
    @Test
    fun `an embedded object of another type is a warning`() {
        val text = room("""has("robot") -> make object "r":"Point" { }""")

        val result = reader.read(text)

        assertCodes(listOf(DiagnosticCode.EMBEDDED_TYPE_MISMATCH), result)
        assertEquals(Severity.WARNING, result.diagnostics.single().severity)
        assertEquals("Point", result.diagnostics.single().range.textIn(text))
        assertEquals(listOf("r"), result.root().getRel("robot").map { it.id })
    }

    /**
     * Objects inside an object of an unknown class type are not part of the model, but references to them do
     * not add errors of their own.
     */
    @Test
    fun `objects inside a left out object can still be referred to`() {
        val text = room(
            """
            has("points") -> make object "box":"Bx" {
                has("inner") -> make object "p1":"Point" { }
            }
            has("robot") -> make object "r":"Robot" { knows("target") -> "p1" }
            """
        )

        val result = reader.read(text)

        assertCodes(listOf(DiagnosticCode.UNKNOWN_CLASS_TYPE), result)
        val target = robotOf(result).relations.single() as ClassTypeAtomicPropertyObject
        assertEquals("p1", target.getValue()?.id)
    }

    /**
     * The reference of a resolved `knows` keeps its location, since it is resolved in place.
     */
    @Test
    fun `resolved references keep their location`() {
        val text = room(
            """
            has("points") -> make object "p1":"Point" { }
            has("robot") -> make object "r":"Robot" { knows("visited") -> "p1" }
            """
        )

        val result = reader.read(text)

        val visited = robotOf(result).relations.single() as ClassTypeListPropertyObject
        val reference = visited.getValueRefs().single()
        assertSame(result.root().getRel("points").single(), reference.obj)
        assertEquals("p1", result.sourceMap.nameRangeOf(reference)?.textIn(text))
    }

    private fun room(statements: String): String =
        "make object \"room\":\"Room\" {\n" + statements.trimIndent().prependIndent("    ") + "\n}\n"

    private fun ParseResult<Model>.root(): DataObject = model!!.objects.single()

    private fun robotOf(result: ParseResult<Model>): DataObject = result.root().getRel("robot").single()

    private fun assertCodes(expected: List<DiagnosticCode>, result: ParseResult<*>) {
        assertEquals(expected, result.diagnostics.map { it.code }, result.diagnostics.joinToString("\n"))
    }
}
