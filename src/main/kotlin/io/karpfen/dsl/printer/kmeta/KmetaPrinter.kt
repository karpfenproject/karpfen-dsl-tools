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
package io.karpfen.dsl.printer.kmeta

import io.karpfen.dsl.meta.AssociationType
import io.karpfen.dsl.meta.ClassType
import io.karpfen.dsl.meta.ClassTypeProperty
import io.karpfen.dsl.meta.Metamodel
import io.karpfen.dsl.meta.SimpleProperty
import io.karpfen.dsl.meta.SimplePropertyType
import io.karpfen.dsl.printer.CodeWriter
import io.karpfen.dsl.printer.DocumentPrinter
import io.karpfen.dsl.printer.StringLiteralSyntax

/**
 * Writes [Metamodel]s, and single class types and rules of them, as KMeta text in the canonical layout.
 *
 * The layout indents by four spaces, writes one rule per line and separates type definitions by a blank
 * line. The types follow the order of [Metamodel.types], except that the root class type comes last,
 * because KMeta reads the last type of a file as the root. Within a type the `prop` rules come first, in the
 * order of [ClassType.simpleProperties], followed by the `has` and `knows` rules in the order of
 * [ClassType.objectProperties]. Comments are not part of a metamodel and are not written, so reading a KMeta
 * text and writing it again drops its comments.
 *
 * A fragment is laid out for an element whose first line stands at a given indentation level: its first line
 * carries no indentation, every following line carries its full indentation, and there is no line break at
 * the end. A fragment thus replaces the text range of an existing element, which starts at its first token,
 * without changing the layout around it.
 *
 * The printer holds no state; one instance serves any number of calls, also concurrent ones on metamodels
 * that are not changed meanwhile.
 */
class KmetaPrinter : DocumentPrinter<Metamodel> {

    private val literals = StringLiteralSyntax.KMETA

    /**
     * Returns [document] as a KMeta document with all its class types, see [print] with base types.
     *
     * @throws IllegalArgumentException If a name or a description cannot be written as a KMeta string
     * literal.
     */
    override fun print(document: Metamodel): String = print(document, emptyList())

    /**
     * Returns [metamodel] as a KMeta document without the class types named in [baseTypes].
     *
     * This mirrors reading KMeta text with base types, as an event metamodel is read with the types of the
     * domain metamodel: those types belong to the metamodel but are defined in another document. The root
     * class type is always written, as the last type.
     *
     * @param baseTypes Class types that are defined elsewhere; they are left out by name.
     * @throws IllegalArgumentException If a name or a description cannot be written as a KMeta string
     * literal.
     */
    fun print(metamodel: Metamodel, baseTypes: Collection<ClassType>): String {
        val baseTypeNames = baseTypes.mapTo(HashSet()) { it.name }
        val root = metamodel.rootClass
        val types = metamodel.types.filter { it !== root && it.name !in baseTypeNames } + root
        return CodeWriter.document { writer -> writer.paragraphs(types) { writeType(it, writer) } }
    }

    /**
     * Returns the type definition `type "Name" "Description" { ... }` of [type] with its rules, laid out as a
     * fragment whose first line stands at [indentLevel].
     *
     * @throws IllegalArgumentException If a name or the description cannot be written as a KMeta string
     * literal, or [indentLevel] is negative.
     */
    fun printType(type: ClassType, indentLevel: Int = 0): String =
        CodeWriter.fragment(indentLevel) { writeType(type, it) }

    /**
     * Returns the `prop` rule of [property] as a one-line fragment, for example `prop("log", list("string"))`.
     *
     * @throws IllegalArgumentException If the property name cannot be written as a KMeta string literal.
     */
    fun printRule(property: SimpleProperty): String = propRule(property)

    /**
     * Returns the `has` rule of an embedded or the `knows` rule of a linked [property] as a one-line
     * fragment, for example `has("position", "Point")`.
     *
     * @throws IllegalArgumentException If a name cannot be written as a KMeta string literal.
     */
    fun printRule(property: ClassTypeProperty): String = relationRule(property)

    private fun writeType(type: ClassType, writer: CodeWriter) {
        writer.block("type ${literals.quote(type.name)} ${literals.quote(type.comment)}") {
            type.simpleProperties.forEach { writer.line(propRule(it)) }
            type.objectProperties.forEach { writer.line(relationRule(it)) }
        }
    }

    private fun propRule(property: SimpleProperty): String =
        "prop(${literals.quote(property.key)}, ${typeValue(typeNameOf(property.propertyType), property.isList)})"

    private fun relationRule(property: ClassTypeProperty): String {
        val keyword = when (property.associationType) {
            AssociationType.EMBEDDED -> "has"
            AssociationType.LINK -> "knows"
        }
        val target = typeValue(property.reference.classTypeName, property.isList)
        return "$keyword(${literals.quote(property.key)}, $target)"
    }

    /**
     * Returns the type part of a rule: `"T"` for a single value and `list("T")` for a list.
     */
    private fun typeValue(typeName: String, isList: Boolean): String {
        val quoted = literals.quote(typeName)
        return if (isList) "list($quoted)" else quoted
    }

    private fun typeNameOf(type: SimplePropertyType): String = when (type) {
        SimplePropertyType.STRING -> "string"
        SimplePropertyType.NUMBER -> "number"
        SimplePropertyType.BOOLEAN -> "boolean"
    }
}
