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

import io.karpfen.dsl.instance.ClassTypeAtomicPropertyObject
import io.karpfen.dsl.instance.ClassTypeListPropertyObject
import io.karpfen.dsl.instance.ClassTypePropertyObject
import io.karpfen.dsl.instance.DataObject
import io.karpfen.dsl.instance.Model
import io.karpfen.dsl.instance.ObjectReference
import io.karpfen.dsl.instance.SimpleAtomicPropertyObject
import io.karpfen.dsl.instance.SimpleListPropertyObject
import io.karpfen.dsl.instance.SimplePropertyObject
import io.karpfen.dsl.meta.AssociationType
import io.karpfen.dsl.meta.ClassType
import io.karpfen.dsl.meta.Metamodel
import io.karpfen.dsl.parser.HasRelation
import io.karpfen.dsl.parser.KmodelFileParser
import io.karpfen.dsl.parser.KmodelSemantics
import io.karpfen.dsl.parser.KnowsRelation
import io.karpfen.dsl.parser.ModelObject
import io.karpfen.dsl.parser.ObjectProperty
import io.karpfen.dsl.printer.kmodel.KmodelPrinter
import org.slf4j.LoggerFactory
import java.io.File

/**
 * Converts KModel text and files into [Model]s that conform to a [Metamodel].
 *
 * The converted objects are logged at debug level.
 */
object KmodelDSLConverter {

    private val logger = LoggerFactory.getLogger(KmodelDSLConverter::class.java)

    /**
     * Parses KModel text and converts it into a [Model] whose `knows` references are resolved.
     *
     * @param content The KModel content as a string.
     * @param meta The metamodel the model must conform to.
     * @return The parsed model; its objects hold the root object.
     * @throws IllegalArgumentException If the model uses a class type, property or relation the metamodel
     * does not define, or a `knows` reference names an object that does not exist.
     */
    fun parseKmodelString(content: String, meta: Metamodel): Model {
        val parsedFile = KmodelFileParser().parseString(content.trimIndent())
        val rootObjects = mutableListOf<DataObject>()

        for (obj in parsedFile.objects) {
            rootObjects.add(parseMakeObjects(obj, meta))
        }

        resolveLinks(collectAllObjects(rootObjects))

        return Model(rootObjects, meta)
    }

    /**
     * Parses a single kmodel object (one `make object` block) and returns its root [DataObject].
     *
     * This is the kmodel counterpart of [JsonToModelConverter.toDataObject]: it is meant for small,
     * self-contained snippets such as an event payload written in the kmodel syntax. The snippet should
     * not rely on `knows` references into the surrounding model, because those ids cannot be resolved
     * from inside an isolated payload.
     *
     * @param content The kmodel snippet as a string.
     * @param meta The metamodel the snippet must conform to.
     * @return The root data object of the snippet.
     * @throws IllegalArgumentException If the snippet does not conform to the metamodel or contains no object.
     */
    fun parseSingleObject(content: String, meta: Metamodel): DataObject {
        val model = parseKmodelString(content, meta)
        return model.objects.firstOrNull()
            ?: throw IllegalArgumentException("kmodel payload did not contain an object")
    }

    /**
     * Parses the KModel file at [filePath] and converts it into a [Model], see [parseKmodelString].
     *
     * @param filePath The path to the KModel file to parse.
     * @param meta The metamodel the model must conform to.
     * @return The parsed model.
     */
    fun parseKmodelFile(filePath: String, meta: Metamodel): Model {
        val content = File(filePath).readText()
        return parseKmodelString(content, meta)
    }

    /**
     * Collects [rootObjects] and, recursively, all objects embedded in them.
     *
     * @param rootObjects The objects to start from.
     * @return The root objects and all their embedded objects.
     */
    fun collectAllObjects(rootObjects: MutableList<DataObject>): Set<DataObject> {
        val allObjects = mutableSetOf<DataObject>()
        for (obj in rootObjects) {
            allObjects.add(obj)
            for (hasProp in obj.relations.filter { it.getPropertyType().associationType == AssociationType.EMBEDDED }) {
                if (hasProp is ClassTypeAtomicPropertyObject) {
                    val value = hasProp.getValue()
                    if (value != null && allObjects.contains(value).not()) {
                        allObjects.add(value)
                        allObjects.addAll(collectAllObjects(mutableListOf(value)))
                    }
                } else if (hasProp is ClassTypeListPropertyObject) {
                    for (value in hasProp.getValues()) {
                        if (allObjects.contains(value).not()) {
                            allObjects.add(value)
                            allObjects.addAll(collectAllObjects(mutableListOf(value)))
                        }
                    }
                }
            }
        }
        return allObjects
    }

    /**
     * Converts one parsed `make object` block and its embedded objects into a [DataObject].
     *
     * Linked (`knows`) relations are left as unresolved references by identifier; [parseKmodelString]
     * resolves them once the whole model is known.
     *
     * @throws IllegalArgumentException If the object uses a class type, property or relation the metamodel
     * does not define.
     */
    fun parseMakeObjects(obj: ModelObject, meta: Metamodel): DataObject {
        logger.debug("Object {} of type {}", obj.key, obj.className)

        val objectKey = obj.key
        val objectType = obj.className

        val classType = meta.types.find { it.name == objectType }
            ?: throw IllegalArgumentException("Unknown class type '$objectType' for object '$objectKey'")

        val simpleObjectProperties = convertProps(obj.props, classType, objectKey)
        val classTypeProperties = mutableListOf<ClassTypePropertyObject>()
        classTypeProperties.addAll(convertEmbeddings(obj.has, classType, objectKey, meta))
        classTypeProperties.addAll(convertLinks(obj.knows, classType, objectKey))

        return DataObject(classType, objectKey, simpleObjectProperties, classTypeProperties)
    }

    /**
     * Converts a [Model] back into KModel text in the canonical layout, starting from its root object and
     * without comments; see [KmodelPrinter].
     *
     * @param model The model to convert.
     * @return The KModel text, ending with a line break.
     * @throws IllegalArgumentException If the model has no unique root object or a value cannot be written.
     */
    fun convertToKmodelString(model: Model): String = KmodelPrinter().print(model)

    /**
     * Converts the `prop` statements of one object; the statements of one key form a single value or a list, see
     * [KmodelSemantics].
     */
    private fun convertProps(
        props: List<ObjectProperty>,
        classType: ClassType,
        objectKey: String
    ): MutableList<SimplePropertyObject> {
        val simpleObjectProperties = mutableListOf<SimplePropertyObject>()
        for (bag in props.groupBy { it.key }) {
            val propKey = bag.key
            val propType = classType.simpleProperties.find { it.key == propKey }
                ?: throw IllegalArgumentException(
                    "Unknown property '$propKey' for class type '${classType.name}' in object '$objectKey'"
                )
            if (!KmodelSemantics.formsList(bag.value.size, propType.isList)) {
                val propValue = KmodelSemantics.valueOf(propType.propertyType, bag.value[0].value)
                simpleObjectProperties.add(SimpleAtomicPropertyObject(propType, propKey, propValue))
            } else {
                val propValuesList = bag.value.map { KmodelSemantics.valueOf(propType.propertyType, it.value) }
                simpleObjectProperties.add(SimpleListPropertyObject(propType, propKey, propValuesList.toMutableList()))
            }
        }
        return simpleObjectProperties
    }

    /**
     * Converts the `has` statements of one object, converting the embedded objects recursively; the statements of
     * one key form a single value or a list, see [KmodelSemantics].
     */
    private fun convertEmbeddings(
        embeddings: List<HasRelation>,
        classType: ClassType,
        objectKey: String,
        meta: Metamodel
    ): List<ClassTypePropertyObject> {
        val classTypeProperties = mutableListOf<ClassTypePropertyObject>()
        for (bag in embeddings.groupBy { it.key }) {
            val embedKey = bag.key
            val embedType = classType.objectProperties.find { it.key == embedKey }
                ?: throw IllegalArgumentException(
                    "Unknown object property '$embedKey' for class type '${classType.name}' in object '$objectKey'"
                )
            if (!KmodelSemantics.formsList(bag.value.size, embedType.isList)) {
                val embedObject = parseMakeObjects(bag.value.first().childObject, meta)
                val reference = ObjectReference(embedObject.id, embedObject)
                classTypeProperties.add(ClassTypeAtomicPropertyObject(embedType, embedKey, reference))
            } else {
                val embedObjectsList = mutableListOf<ObjectReference>()
                for (embed in bag.value) {
                    val embedObject = parseMakeObjects(embed.childObject, meta)
                    embedObjectsList.add(ObjectReference(embedObject.id, embedObject))
                }
                classTypeProperties.add(ClassTypeListPropertyObject(embedType, embedKey, embedObjectsList))
            }
        }
        return classTypeProperties
    }

    /**
     * Converts the `knows` statements of one object into unresolved references; the statements of one key form a
     * single value or a list, see [KmodelSemantics].
     */
    private fun convertLinks(
        links: List<KnowsRelation>,
        classType: ClassType,
        objectKey: String
    ): List<ClassTypePropertyObject> {
        val classTypeProperties = mutableListOf<ClassTypePropertyObject>()
        for (bag in links.groupBy { it.key }) {
            val knowsKey = bag.key
            val knowsType = classType.objectProperties.find { it.key == knowsKey }
                ?: throw IllegalArgumentException(
                    "Unknown object property '$knowsKey' for class type '${classType.name}' in object '$objectKey'"
                )
            if (!KmodelSemantics.formsList(bag.value.size, knowsType.isList)) {
                val reference = ObjectReference(bag.value.first().value, null)
                classTypeProperties.add(ClassTypeAtomicPropertyObject(knowsType, knowsKey, reference))
            } else {
                val references = bag.value.map { ObjectReference(it.value, null) }.toMutableList()
                classTypeProperties.add(ClassTypeListPropertyObject(knowsType, knowsKey, references))
            }
        }
        return classTypeProperties
    }

    /**
     * Resolves the linked (`knows`) relations of [flatObjects] against the objects among them that can be referred
     * to, see [KmodelSemantics.referableById].
     *
     * @throws IllegalArgumentException If a reference names an object that does not exist.
     */
    private fun resolveLinks(flatObjects: Set<DataObject>) {
        val allObjMap = KmodelSemantics.referableById(flatObjects)
        for (obj in flatObjects) {
            for (knowsProp in obj.relations.filter { it.getPropertyType().associationType == AssociationType.LINK }) {
                if (knowsProp is ClassTypeAtomicPropertyObject) {
                    val ref = knowsProp.getValueRef()
                    val resolvedObj = allObjMap[ref.objectId]
                        ?: throw unresolvedReference(ref, knowsProp, obj)
                    knowsProp.setValue(resolvedObj)
                } else if (knowsProp is ClassTypeListPropertyObject) {
                    val resolvedObjs = mutableListOf<DataObject>()
                    for (ref in knowsProp.getValueRefs()) {
                        val resolvedObj = allObjMap[ref.objectId]
                            ?: throw unresolvedReference(ref, knowsProp, obj)
                        resolvedObjs.add(resolvedObj)
                    }
                    knowsProp.setValues(resolvedObjs)
                }
            }
        }
    }

    private fun unresolvedReference(
        ref: ObjectReference,
        property: ClassTypePropertyObject,
        owner: DataObject
    ): IllegalArgumentException = IllegalArgumentException(
        "Could not resolve reference to object with id '${ref.objectId}' in property '${property.key}' of object '${owner.id}'"
    )
}
