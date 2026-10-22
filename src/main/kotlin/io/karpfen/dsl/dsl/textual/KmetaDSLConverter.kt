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
package io.karpfen.dsl.dsl.textual

import io.karpfen.dsl.meta.AssociationType
import io.karpfen.dsl.meta.ClassType
import io.karpfen.dsl.meta.ClassTypeProperty
import io.karpfen.dsl.meta.ClassTypeReference
import io.karpfen.dsl.meta.Metamodel
import io.karpfen.dsl.meta.SimpleProperty
import io.karpfen.dsl.meta.SimplePropertyType
import io.karpfen.dsl.parser.KmetaFileParser
import io.karpfen.dsl.parser.ListValue
import io.karpfen.dsl.parser.Rule
import io.karpfen.dsl.parser.RuleValue
import io.karpfen.dsl.parser.StringValue
import io.karpfen.dsl.parser.TypeDefinition
import org.slf4j.LoggerFactory
import java.io.File

/**
 * Converts KMeta text and files into [Metamodel]s.
 *
 * The parsed types are logged at debug level.
 */
object KmetaDSLConverter {

    private val logger = LoggerFactory.getLogger(KmetaDSLConverter::class.java)

    /**
     * Parses KMeta text and converts it into a [Metamodel].
     *
     * The last type defined in [content] becomes the root class type of the metamodel.
     *
     * [baseTypes] are class types that are already known from another metamodel and may be referenced
     * by the types in this file but are not redefined here. This is what lets an EVENTS.kmeta describe
     * event payloads that embed or link to domain types (e.g. an event carrying a `Vector`) without
     * duplicating those definitions. The base types are also included in the resulting metamodel so
     * that nested references resolve when the payload is later turned into objects.
     *
     * @param content The KMeta content as a string.
     * @param baseTypes Already-resolved class types that may be referenced but are not defined here.
     * @return The parsed metamodel.
     * @throws IllegalArgumentException If a property has an unknown primitive type, a relation refers to an
     * unknown class type, or [content] defines no type at all.
     */
    fun parseKmetaString(content: String, baseTypes: List<ClassType> = emptyList()): Metamodel {
        val parsedFile = KmetaFileParser().parseString(content.trimIndent())
        val classTypes = parsedFile.types.map { toClassType(it) }

        resolveReferences(classTypes, classTypes + baseTypes)

        if (classTypes.isEmpty()) {
            throw IllegalArgumentException("KMeta content does not define any types")
        }

        val allTypes = (classTypes + baseTypes).toMutableList()
        return Metamodel(allTypes, classTypes.last())
    }

    /**
     * Parses the KMeta file at [filePath] and converts it into a [Metamodel], see [parseKmetaString].
     *
     * @param filePath The path to the KMeta file to parse.
     * @return The parsed metamodel.
     */
    fun parseKmetaFile(filePath: String): Metamodel {
        val content = File(filePath).readText()
        return parseKmetaString(content)
    }

    /**
     * Converts a [Metamodel] back into KMeta text; not implemented yet.
     *
     * @param metamodel The metamodel to convert.
     * @return The KMeta text.
     * @throws NotImplementedError Always, this operation is not implemented yet.
     */
    fun convertToKmetaString(metamodel: Metamodel): String {
        throw NotImplementedError()
    }

    /**
     * Converts one type definition into a [ClassType] whose relations are not resolved yet.
     */
    private fun toClassType(definition: TypeDefinition): ClassType {
        logger.debug("Type {}: {}", definition.name, definition.description)
        val simpleProperties = definition.props.map { toSimpleProperty(it) }
        val embedded = definition.has.map { toClassTypeProperty(it, AssociationType.EMBEDDED) }
        val linked = definition.knows.map { toClassTypeProperty(it, AssociationType.LINK) }
        return ClassType(definition.name, definition.description, simpleProperties, embedded + linked)
    }

    private fun toSimpleProperty(rule: Rule): SimpleProperty {
        val typeName = typeNameOf(rule.value)
        logger.debug("  prop {}: {}", rule.key, rule.value)
        val propertyType = when (typeName) {
            "string" -> SimplePropertyType.STRING
            "number" -> SimplePropertyType.NUMBER
            "boolean" -> SimplePropertyType.BOOLEAN
            else -> throw IllegalArgumentException("Unknown rule value type for prop '${rule.key}'")
        }
        return SimpleProperty(rule.key, propertyType, rule.value is ListValue)
    }

    private fun toClassTypeProperty(rule: Rule, associationType: AssociationType): ClassTypeProperty {
        val property = ClassTypeProperty(
            rule.key,
            ClassTypeReference(associationType, typeNameOf(rule.value)),
            rule.value is ListValue,
            associationType
        )
        logger.debug("  {} {}", associationType, property)
        return property
    }

    private fun typeNameOf(value: RuleValue): String = when (value) {
        is StringValue -> value.value
        is ListValue -> value.value
    }

    /**
     * Resolves the class type references of the relations of [classTypes] against [resolvableTypes], the
     * types of this file and the supplied base types.
     *
     * @throws IllegalArgumentException If a relation refers to a class type that is not resolvable.
     */
    private fun resolveReferences(classTypes: List<ClassType>, resolvableTypes: List<ClassType>) {
        for (classType in classTypes) {
            for (classTypeProp in classType.objectProperties) {
                val reference = classTypeProp.reference
                val targetClassType = resolvableTypes.find { it.name == reference.classTypeName }
                    ?: throw IllegalArgumentException(
                        "Unknown target class type '${reference.classTypeName}' for property"
                    )
                reference.classType = targetClassType
            }
        }
    }
}
