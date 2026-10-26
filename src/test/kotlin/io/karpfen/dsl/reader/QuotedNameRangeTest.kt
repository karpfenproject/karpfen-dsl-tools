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
package io.karpfen.dsl.reader

import io.karpfen.dsl.diagnostics.DiagnosticCode
import io.karpfen.dsl.dsl.textual.KmetaDSLConverter
import io.karpfen.dsl.reader.kmeta.KmetaReader
import io.karpfen.dsl.reader.kmodel.KmodelReader
import io.karpfen.dsl.reader.kstates.KstatesReader
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests that the name range of an element, and every diagnostic placed on a name or value, covers exactly the value of
 * its string literal, also where the language strips more quote characters than the outer pair: KStates and the
 * object signatures of KModel strip a pair of single quotes inside the double quotes, the statements of KModel strip
 * every quote at the edges, and KMeta every double quote, an escaped one included.
 */
class QuotedNameRangeTest {

    /**
     * KStates reads `"'Idle'"` as `Idle`.
     */
    @Test
    fun `KStates names inside single quotes cover the name`() {
        val text = "STATEMACHINE ATTACHED TO \"'Robot'\" {\n    STATES {\n        INITIAL STATE \"'Idle'\" { }\n" +
            "        INITIAL STATE \"'Busy'\" { }\n    }\n    TRANSITIONS { }\n}\n"

        val result = KstatesReader().read(text)
        val stateMachine = result.model!!

        assertEquals("Robot", result.sourceMap.nameRangeOf(stateMachine)?.textIn(text))
        assertEquals(listOf("Idle", "Busy"), stateMachine.states.map { result.sourceMap.nameRangeOf(it)?.textIn(text) })
        assertEquals(
            listOf("Idle", "Busy"),
            result.diagnostics.filter { it.code == DiagnosticCode.INITIAL_STATE_COUNT }.map { it.range.textIn(text) }
        )
    }

    /**
     * KModel reads the key `"'r'"` of an object signature as `r`, and the keys and values of statements without any
     * quote at their edges.
     */
    @Test
    fun `KModel keys and values inside single quotes cover the value`() {
        val metamodel = KmetaDSLConverter.parseKmetaString("type \"Room\" \"a room\" { prop(\"name\", \"string\") }")
        val text = "make object \"'r'\":\"Room\" {\n    prop(\"'name'\") -> \"x\"\n    prop(\"''nmae''\") -> \"y\"\n}\n"

        val result = KmodelReader(metamodel).read(text)
        val room = result.model!!.objects.single()

        assertEquals("r", result.sourceMap.nameRangeOf(room)?.textIn(text))
        assertEquals("name", result.sourceMap.nameRangeOf(room.properties.single())?.textIn(text))
        assertEquals(listOf("nmae"), result.diagnostics.map { it.range.textIn(text) })
    }

    /**
     * KMeta reads `"A\""` as `A\`, since it strips the escaped quote too, and an empty name as an empty range between
     * its quotes.
     */
    @Test
    fun `KMeta names that end in an escaped quote cover the name`() {
        val text = "type \"A\\\"\" \"a\" { }\ntype \"\" \"b\" { }\n"

        val result = KmetaReader().read(text)
        val (named, unnamed) = result.model!!.types

        assertEquals("A\\", named.name)
        assertEquals(named.name, result.sourceMap.nameRangeOf(named)?.textIn(text))
        val emptyName = result.sourceMap.nameRangeOf(unnamed)!!
        assertEquals(0, emptyName.length)
        assertEquals(text.indexOf("\"\" \"b\"") + 1, emptyName.start.offset)
    }
}
