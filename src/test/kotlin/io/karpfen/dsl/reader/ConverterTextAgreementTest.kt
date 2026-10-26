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

import io.karpfen.dsl.dsl.textual.KmetaDSLConverter
import io.karpfen.dsl.dsl.textual.KmodelDSLConverter
import io.karpfen.dsl.fixtures.StructureDump
import io.karpfen.dsl.instance.Model
import io.karpfen.dsl.meta.Metamodel
import io.karpfen.dsl.reader.kmeta.KmetaReader
import io.karpfen.dsl.reader.kmodel.KmodelReader
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests that the KMeta and KModel readers read the text their converters read, which trim the indentation all lines
 * share and make every line break `\n`, while the ranges they report refer to the text as written.
 *
 * A string literal that an escaped line break continues on the next line shows the difference: its value loses the
 * trimmed indentation, and an escaped `\r\n` is one line break.
 */
class ConverterTextAgreementTest {

    private val metamodel = KmetaDSLConverter.parseKmetaString("type \"Root\" \"r\" { prop(\"name\", \"string\") }")

    /**
     * In a KMeta text whose lines are all indented, the continued literal holds the trimmed indentation.
     */
    @Test
    fun `an indented kmeta text reads as the converter reads it`() {
        assertKmetaAgrees("  type \"A\" \"line\\\n    two\" { prop(\"x\", \"number\") }\n")
    }

    /**
     * In a KMeta text with `\r\n` line breaks, an escaped line break continues the literal.
     */
    @Test
    fun `a kmeta text with crlf line breaks reads as the converter reads it`() {
        val text = "type \"A\" \"line\\\r\ntwo\" {\r\n  prop(\"x\", \"number\")\r\n}\r\n"
        val result = assertKmetaAgrees(text)

        val type = result.model!!.types.single()
        assertEquals("A", result.sourceMap.nameRangeOf(type)?.textIn(text))
        assertEquals(3, result.sourceMap.rangeOf(type.simpleProperties.single())?.start?.line)
    }

    /**
     * In a KModel text whose lines are all indented, the continued literal holds the trimmed indentation.
     */
    @Test
    fun `an indented kmodel text reads as the converter reads it`() {
        assertKmodelAgrees("  make object \"r\":\"Root\" {\n    prop(\"name\") -> \"a\\\n    b\"\n  }\n")
    }

    /**
     * In a KModel text with `\r\n` line breaks, an escaped line break continues the literal.
     */
    @Test
    fun `a kmodel text with crlf line breaks reads as the converter reads it`() {
        val text = "make object \"r\":\"Root\" {\r\n  prop(\"name\") -> \"a\\\r\n  b\"\r\n}"
        val result = assertKmodelAgrees(text)

        assertEquals("r", result.sourceMap.nameRangeOf(result.model!!.objects.single())?.textIn(text))
    }

    private fun assertKmetaAgrees(text: String): ParseResult<Metamodel> {
        val result = KmetaReader().read(text)
        assertEquals(emptyList(), result.diagnostics)
        assertEquals(StructureDump.of(KmetaDSLConverter.parseKmetaString(text)), StructureDump.of(result.model))
        return result
    }

    private fun assertKmodelAgrees(text: String): ParseResult<Model> {
        val result = KmodelReader(metamodel).read(text)
        assertEquals(emptyList(), result.diagnostics)
        val converted = KmodelDSLConverter.parseKmodelString(text, metamodel)
        assertEquals(StructureDump.of(converted), StructureDump.of(result.model))
        return result
    }
}
