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

import io.karpfen.dsl.diagnostics.Diagnostic
import io.karpfen.dsl.diagnostics.DiagnosticCode
import io.karpfen.dsl.dsl.textual.KmetaDSLConverter
import io.karpfen.dsl.dsl.textual.KmodelDSLConverter
import io.karpfen.dsl.dsl.textual.KstatesDSLConverter
import io.karpfen.dsl.fixtures.StructureDump
import io.karpfen.dsl.reader.kmeta.KmetaReader
import io.karpfen.dsl.reader.kmodel.KmodelReader
import io.karpfen.dsl.reader.kstates.KstatesReader
import io.karpfen.dsl.source.SourcePosition
import java.io.File
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Tests that a text that starts with a byte order mark reads like the same text without it, in all three languages,
 * through the readers and the converters, while the positions of its problems count the mark as the first char of the
 * first line; and that a mark anywhere else is not ignored.
 */
class ByteOrderMarkTest {

    private val mark = "﻿"

    private val metaText = """

        type "Item" "An item" { prop("weight", "number") }
        type "Root" "The root" { has("items", list("Item")) }
    """

    private val metamodel = KmetaDSLConverter.parseKmetaString(metaText)

    private val modelText = """
        make object "root":"Root" {
            has("items") -> make object "a":"Item" { prop("weight") -> "1" }
        }
    """

    private val machineText = """
        STATEMACHINE ATTACHED TO "Root" {
            STATES {
                INITIAL STATE "idle" { }
                STATE "busy" { }
            }
            TRANSITIONS {
                TRANSITION "idle" -> "busy" { }
            }
        }
    """.trimIndent()

    /** Each reader reads a marked text without problems and to the model of the text without the mark. */
    @Test
    fun `the readers read a marked text like the text without the mark`() {
        assertSameReading(KmetaReader().read(metaText), KmetaReader().read(mark + metaText))
        assertSameReading(KmodelReader(metamodel).read(modelText), KmodelReader(metamodel).read(mark + modelText))
        assertSameReading(KstatesReader().read(machineText), KstatesReader().read(mark + machineText))
    }

    /** Each converter returns for a marked string or file the model of the text without the mark. */
    @Test
    fun `the converters convert a marked text like the text without the mark`() {
        assertEquals(
            StructureDump.of(metamodel),
            StructureDump.of(KmetaDSLConverter.parseKmetaString(mark + metaText))
        )
        assertEquals(
            StructureDump.of(KmodelDSLConverter.parseKmodelString(modelText, metamodel)),
            StructureDump.of(KmodelDSLConverter.parseKmodelString(mark + modelText, metamodel))
        )
        assertEquals(
            StructureDump.of(KmodelDSLConverter.parseSingleObject(modelText, metamodel)),
            StructureDump.of(KmodelDSLConverter.parseSingleObject(mark + modelText, metamodel))
        )
        assertEquals(
            StructureDump.of(KstatesDSLConverter.parseKstatesString(machineText)),
            StructureDump.of(KstatesDSLConverter.parseKstatesString(mark + machineText))
        )

        val folder = Files.createTempDirectory("byte-order-mark").toFile()
        try {
            val metaFile = written(folder, "root.kmeta", metaText)
            val modelFile = written(folder, "root.kmodel", modelText)
            val machineFile = written(folder, "root.kstates", machineText)
            assertEquals(StructureDump.of(metamodel), StructureDump.of(KmetaDSLConverter.parseKmetaFile(metaFile.path)))
            assertEquals(
                StructureDump.of(KmodelDSLConverter.parseKmodelString(modelText, metamodel)),
                StructureDump.of(KmodelDSLConverter.parseKmodelFile(modelFile.path, metamodel))
            )
            assertEquals(
                StructureDump.of(KstatesDSLConverter.parseKstatesString(machineText)),
                StructureDump.of(KstatesDSLConverter.parseKstatesFile(machineFile.path))
            )
        } finally {
            folder.deleteRecursively()
        }
    }

    /** A KMeta problem on the first line and one on a later line of a marked and indented text are marked right. */
    @Test
    fun `kmeta findings of a marked text point at their places`() {
        val text = mark + "type \"A\" \"a\" { ) }\n    type \"B\" \"b\" { }\n    type \"B\" \"again\" { }"

        val diagnostics = KmetaReader().read(text).diagnostics

        assertEquals(listOf(DiagnosticCode.SYNTAX_ERROR, DiagnosticCode.DUPLICATE_TYPE), diagnostics.map { it.code })
        assertEquals(listOf(")", "B"), diagnostics.map { it.range.textIn(text) })
        assertEquals(SourcePosition(16, 1, 17), diagnostics[0].range.start)
        assertEquals(SourcePosition(51, 3, 11), diagnostics[1].range.start)
    }

    /** A KStates problem on the first line and one on a later line of a marked text are marked right. */
    @Test
    fun `kstates findings of a marked text point at their places`() {
        val text = mark + machineText
            .replace("STATEMACHINE ATTACHED", "STATEMACHINE \"x\" ATTACHED")
            .replace("-> \"busy\"", "-> \"bsy\"")

        val diagnostics = KstatesReader().read(text).diagnostics

        assertEquals(listOf(DiagnosticCode.ATTACHED_HEADER, DiagnosticCode.UNKNOWN_STATE), diagnostics.map { it.code })
        assertEquals(listOf("x", "bsy"), diagnostics.map { it.range.textIn(text) })
        assertEquals(SourcePosition(15, 1, 16), diagnostics[0].range.start)
        assertEquals(7, diagnostics[1].range.start.line)
        assertEquals(text.lines()[6].indexOf("bsy") + 1, diagnostics[1].range.start.column)
    }

    /** A mark after the start is no byte order mark of the file and stays a syntax error. */
    @Test
    fun `a mark after the start is a syntax error`() {
        val metaWithMark = metaText.replace("type \"Root\"", mark + "type \"Root\"")
        val machineWithMark = machineText.replace("TRANSITIONS", mark + "TRANSITIONS")

        assertTrue(KmetaReader().read(metaWithMark).hasErrors)
        assertTrue(KmodelReader(metamodel).read(modelText.replace("make", mark + "make")).hasErrors)
        assertTrue(KstatesReader().read(machineWithMark).hasErrors)
        assertTrue(KstatesReader().read(mark + mark + machineText).hasErrors)
    }

    /** Asserts that [marked] has the problems and the model of [plain], which has no problems. */
    private fun <M> assertSameReading(plain: ParseResult<M>, marked: ParseResult<M>) {
        assertEquals(emptyList<Diagnostic>(), plain.diagnostics)
        assertEquals(emptyList<Diagnostic>(), marked.diagnostics)
        assertEquals(StructureDump.of(plain.model), StructureDump.of(marked.model))
    }

    /** Writes [text] behind a byte order mark into the file [name] of [folder], encoded as UTF-8. */
    private fun written(folder: File, name: String, text: String): File =
        File(folder, name).apply { writeText(mark + text, Charsets.UTF_8) }
}
