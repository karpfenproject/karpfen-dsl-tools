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
import dsl.textual.JsonToModelConverter
import dsl.textual.KmetaDSLConverter
import dsl.textual.KmodelDSLConverter
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

/**
 * Tests for turning event payloads (JSON or kmodel) into the runtime object model, and for the
 * EVENTS.kmeta that references domain types via base types.
 */
class EventPayloadParsingTest {

    private val domainKmeta = """
        type "Vector" "A vector in 2D space" {
            prop("x", "number")
            prop("y", "number")
        }
    """.trimIndent()

    private val eventsKmeta = """
        type "setSpeed" "Change driving speed" {
            prop("factor", "number")
            prop("label", "string")
        }
        type "teleport" "Move to a position" {
            has("position", "Vector")
        }
    """.trimIndent()

    private fun eventMetamodel() =
        KmetaDSLConverter.parseKmetaString(eventsKmeta, KmetaDSLConverter.parseKmetaString(domainKmeta).types)

    @Test
    fun `event metamodel exposes both event and referenced domain types`() {
        val events = eventMetamodel()
        assertNotNull(events.getTypeByName("setSpeed"))
        assertNotNull(events.getTypeByName("teleport"))
        // The domain type is carried along so nested references resolve while parsing payloads.
        assertNotNull(events.getTypeByName("Vector"))
    }

    @Test
    fun `json payload becomes a data object with typed properties`() {
        val converter = JsonToModelConverter(eventMetamodel())
        val obj = converter.toDataObject("""{"factor": 1.5, "label": "fast"}""", "setSpeed")
        assertEquals("setSpeed", obj.ofType.name)
        assertEquals(1.5, obj.getProp("factor").first())
        assertEquals("fast", obj.getProp("label").first())
    }

    @Test
    fun `json payload can embed a domain type`() {
        val converter = JsonToModelConverter(eventMetamodel())
        val obj = converter.toDataObject("""{"position": {"x": 3.0, "y": 4.0}}""", "teleport")
        val position = obj.getRel("position").first()
        assertEquals("Vector", position.ofType.name)
        assertEquals(3.0, position.getProp("x").first())
        assertEquals(4.0, position.getProp("y").first())
    }

    @Test
    fun `kmodel payload parses into the same kind of object`() {
        val obj = KmodelDSLConverter.parseSingleObject(
            """make object "evt":"setSpeed" { prop("factor") -> "2.0" prop("label") -> "boost" }""",
            eventMetamodel()
        )
        assertEquals("setSpeed", obj.ofType.name)
        assertEquals(2.0, obj.getProp("factor").first())
        assertEquals("boost", obj.getProp("label").first())
    }
}
