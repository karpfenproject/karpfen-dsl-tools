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

import io.karpfen.dsl.instance.ClassTypePropertyObject
import io.karpfen.dsl.instance.DataObject
import io.karpfen.dsl.instance.Model
import io.karpfen.dsl.instance.SimpleAtomicPropertyObject
import io.karpfen.dsl.instance.SimpleListPropertyObject
import io.karpfen.dsl.instance.SimplePropertyObject
import io.karpfen.dsl.meta.AssociationType
import io.karpfen.dsl.parser.StringLiteralSyntax
import io.karpfen.dsl.printer.CodeWriter
import io.karpfen.dsl.printer.DocumentPrinter

/**
 * Writes [Model]s, and single objects with the objects they embed, as KModel text in the canonical layout.
 *
 * The layout indents by four spaces and writes one statement per line. Within an object the `prop`
 * statements come first, in the order of [DataObject.properties], one statement per value of a list
 * property; then the relations follow in the order of [DataObject.relations]: an embedded object as a
 * `has` statement with its own `make object` block, a linked object as a `knows` statement with its
 * identifier. Values are written with [Any.toString], so a number reads `"1.0"` however it was written
 * before. Empty lists and empty relations are left out, as KModel has no statement for them. Comments are
 * not part of a model and are not written, so reading a KModel text and writing it again drops its comments. A
 * link to an object without an id outside the document is the default object of a builder and is left out too, see
 * [ObjectTree.linkedIdsOf]. A single object is written as a fragment, see [DocumentPrinter]. The key and the class type of an object follow the
 * literal syntax of object signatures, every other value that of statements.
 *
 * The printer holds no state; one instance serves any number of calls, also concurrent ones on models that
 * are not changed meanwhile.
 */
class KmodelPrinter internal constructor(private val tree: ObjectTree) : DocumentPrinter<Model> {

    /**
     * Creates a printer.
     */
    constructor() : this(ObjectTree())

    private val literals = StringLiteralSyntax.KMODEL

    private val signatureLiterals = StringLiteralSyntax.KMODEL_SIGNATURE

    /**
     * Returns [document] as a KModel document that starts from its root object.
     *
     * Parsed models hold just their root object. Models made with a builder hold every object they created;
     * the root object is then the one no other object embeds, and among several of those the one of the
     * root class type of the metamodel.
     *
     * @throws IllegalArgumentException If the model has no unique root object, an object embeds itself or is
     * embedded more than once, an object links to an object of the document that has no id, a value cannot be
     * written as a KModel string literal, or the objects nest deeper than the printer can recurse.
     */
    override fun print(document: Model): String = CodeWriter.document { writer ->
        val root = tree.rootOf(document)
        writeObject(root, "", writer, tree.objectsWrittenFrom(root))
    }

    /**
     * Returns the block `make object "id":"Class" { ... }` of [obj] with the objects it embeds, as a fragment whose
     * first line stands at [indentLevel].
     *
     * The fragment fits both as the root of a document and after the arrow of a `has` statement.
     *
     * @throws IllegalArgumentException If an object embeds itself or is embedded more than once, an object links to
     * an object of the fragment that has no id, a value cannot be written as a KModel string literal, the objects
     * nest deeper than the printer can recurse, or [indentLevel] is negative.
     */
    fun printObject(obj: DataObject, indentLevel: Int = 0): String = CodeWriter.fragment(indentLevel) { writer ->
        writeObject(obj, "", writer, tree.objectsWrittenFrom(obj))
    }

    /**
     * Writes [obj] as a `make object` block behind [prefix].
     *
     * @param written The objects the document or fragment writes, see [ObjectTree.objectsWrittenFrom].
     */
    private fun writeObject(obj: DataObject, prefix: String, writer: CodeWriter, written: Set<DataObject>) {
        writer.block("${prefix}make object ${signatureOf(obj)}") {
            obj.properties.forEach { writeProperty(it, writer) }
            obj.relations.forEach { writeRelation(it, writer, written) }
        }
    }

    /**
     * Returns the signature `"id":"Class"` of [obj], each literal written for the text that follows it up to the
     * opening brace of the block.
     */
    private fun signatureOf(obj: DataObject): String {
        val classType = signatureLiterals.quote(obj.ofType.name, followedBy = BLOCK_OPENING)
        val id = signatureLiterals.quote(obj.id, followedBy = ":$classType$BLOCK_OPENING")
        return "$id:$classType"
    }

    private fun writeProperty(property: SimplePropertyObject, writer: CodeWriter) {
        val key = literals.quote(property.key)
        valuesOf(property).forEach { writer.line("prop($key) -> ${literals.quote(it.toString())}") }
    }

    private fun writeRelation(relation: ClassTypePropertyObject, writer: CodeWriter, written: Set<DataObject>) {
        val key = literals.quote(relation.key)
        when (relation.getPropertyType().associationType) {
            AssociationType.EMBEDDED -> tree.embeddedObjectsOf(relation).forEach {
                writeObject(it, "has($key) -> ", writer, written)
            }
            AssociationType.LINK -> tree.linkedIdsOf(relation, written).forEach {
                writer.line("knows($key) -> ${literals.quote(it)}")
            }
        }
    }

    /**
     * Returns the values of [property]: one for a single-valued property, all of them for a list.
     *
     * @throws IllegalArgumentException If [property] is neither a single-valued nor a list property.
     */
    private fun valuesOf(property: SimplePropertyObject): List<Any> = when (property) {
        is SimpleAtomicPropertyObject -> listOf(property.getValue())
        is SimpleListPropertyObject -> property.getValues()
        else -> throw IllegalArgumentException("Unsupported kind of property value: ${property::class.simpleName}")
    }

    private companion object {

        /** The text that follows the signature of an object: the opening brace of its block. */
        const val BLOCK_OPENING = " {"
    }
}
