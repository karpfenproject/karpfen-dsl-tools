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
package io.karpfen.dsl.printer.kmodel

import io.karpfen.dsl.dsl.functional.ModelBuilder
import io.karpfen.dsl.dsl.textual.KmetaDSLConverter
import io.karpfen.dsl.dsl.textual.KmodelDSLConverter
import io.karpfen.dsl.instance.ClassTypeListPropertyObject
import io.karpfen.dsl.instance.DataObject
import io.karpfen.dsl.instance.Model
import io.karpfen.dsl.instance.ObjectReference
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertSame

/**
 * Checks which object a KModel document starts from and that every link the printer writes reads back as a link to
 * the object it refers to: KModel refers to a linked object by its id, among the objects of the document, the last
 * one of an id winning.
 */
class KmodelPrinterReferenceTest {

    private val printer = KmodelPrinter()

    /** Robots in a room that each robot links back to, and that robots link to each other in. */
    private val metamodel = KmetaDSLConverter.parseKmetaString(
        """
        type "Robot" "A robot" {
            prop("name", "string")
            knows("home", "Room")
            knows("buddies", list("Robot"))
        }
        type "Room" "The root" { has("robots", list("Robot")) }
        """
    )

    /**
     * A builder makes a default object for every single-valued relation, here a room for the link of each robot to
     * its home, and keeps it in the model when the link is set. The document starts from the room with an id.
     */
    @Test
    fun `a model built in code with a link back to its root prints from the root`() {
        val builder = ModelBuilder(metamodel)
        val room = builder.makeObject("Room", "lab")
        val robot = builder.makeObject("Robot", "r1")
        robot.assignRels(mapOf("home" to room))
        room.assignRels(mapOf("robots" to listOf(robot)))
        val built = builder.build()

        val printed = printer.print(built)
        val reread = KmodelDSLConverter.parseKmodelString(printed, metamodel)

        assertEquals("lab", reread.objects.single().id, printed)
        assertSame(reread.objects.single(), reread.objects.single().getRel("robots").single().getRel("home").single())
    }

    /**
     * A link to an object whose id a later object of the document has too would read back as a link to that later
     * object, so it is rejected; a link to the later object is written.
     */
    @Test
    fun `a link to an object whose id a later object has too is rejected`() {
        val first = robot("x")
        val second = robot("x")
        linkBuddies(first, second)
        assertEquals(listOf("x"), linkedIdsIn(printer.print(modelOf(room(first, second)))))

        linkBuddies(second, first)

        assertFailsWith<IllegalArgumentException> { printer.print(modelOf(room(first, second))) }
        assertFailsWith<IllegalArgumentException> { printer.printObject(room(first, second)) }
    }

    /**
     * A document holds only the objects its root embeds, so a link to an object nothing embeds would refer to no
     * object; a fragment stands in a document that may hold it, so it writes the link.
     */
    @Test
    fun `a link to an object no object embeds is rejected in a document and written in a fragment`() {
        val outside = robot("outside")
        val linking = robot("r")
        linkBuddies(linking, outside)
        val room = room(linking)

        assertFailsWith<IllegalArgumentException> { printer.print(modelOf(room, outside)) }
        assertEquals(listOf("outside"), linkedIdsIn(printer.printObject(linking)))
    }

    /**
     * Returns the ids that the `knows` statements of [text] refer to.
     */
    private fun linkedIdsIn(text: String): List<String> =
        Regex("knows\\(\"buddies\"\\) -> \"([^\"]*)\"").findAll(text).map { it.groupValues[1] }.toList()

    /**
     * Returns a model of [objects].
     */
    private fun modelOf(vararg objects: DataObject): Model = Model(objects.toMutableList(), metamodel)

    /**
     * Returns a robot with the id [id] and no buddies.
     */
    private fun robot(id: String): DataObject =
        DataObject(metamodel.getTypeByName("Robot")!!, id, mutableListOf(), mutableListOf())

    /**
     * Makes [robot] link to [buddy].
     */
    private fun linkBuddies(robot: DataObject, buddy: DataObject) {
        val property = metamodel.getTypeByName("Robot")!!.getObjectProperty("buddies")
        val references = mutableListOf(ObjectReference(buddy.id, buddy))
        robot.relations.add(ClassTypeListPropertyObject(property, "buddies", references))
    }

    /**
     * Returns a room that embeds [robots].
     */
    private fun room(vararg robots: DataObject): DataObject {
        val type = metamodel.getTypeByName("Room")!!
        val references = robots.map { ObjectReference(it.id, it) }.toMutableList()
        val relation = ClassTypeListPropertyObject(type.getObjectProperty("robots"), "robots", references)
        return DataObject(type, "lab", mutableListOf(), mutableListOf(relation))
    }
}
