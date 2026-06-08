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
package dsl.textual

import instance.ClassTypeAtomicPropertyObject
import instance.ClassTypeListPropertyObject
import instance.ClassTypePropertyObject
import instance.DataObject
import instance.Model
import instance.ObjectReference
import instance.SimpleAtomicPropertyObject
import instance.SimpleListPropertyObject
import instance.SimplePropertyObject
import meta.AssociationType
import meta.Metamodel
import meta.SimplePropertyType
import parser.KmodelFileParser
import parser.ModelObject
import java.io.File
import kotlin.collections.iterator

/**
 * Converts KModel DSL strings and files into Model instances.
 */
object KmodelDSLConverter {

    /**
     * Parses a KModel string and converts it to a Model instance.
     *
     * @param content The KModel content as a string.
     * @param meta The metamodel defining the structure for validation.
     * @return The parsed model instance.
     */
    fun parseKmodelString(content: String, meta: Metamodel): Model {
        val d = KmodelFileParser().parseString(content.trimIndent())
        val rootObjects = mutableListOf<DataObject>()

        for(obj in d.objects) {
            rootObjects.add(parseMakeObjects(obj, meta))
        }

        val flatObjects = collectAllObjects(rootObjects)
        //Now that we have parsed all objects, we need to resolve the references in the ClassTypePropertyObjects
        val allObjMap = flatObjects.filter() { it.id.isNotEmpty() || it.id == "_" }.associateBy { it.id }
        for(obj in flatObjects) {
            for (knowsProp in obj.relations.filter { it.getPropertyType().associationType == AssociationType.LINK }) {
                if (knowsProp is ClassTypeAtomicPropertyObject) {
                    val ref = knowsProp.getValueRef()
                    val resolvedObj = allObjMap[ref.objectId]
                    if (resolvedObj != null) {
                        knowsProp.setValue(resolvedObj)
                    } else {
                        throw IllegalArgumentException("Could not resolve reference to object with id '${ref.objectId}' in property '${knowsProp.key}' of object '${obj.id}'")
                    }
                } else if (knowsProp is ClassTypeListPropertyObject) {
                    val refs = knowsProp.getValueRefs()
                    val resolvedObjs = mutableListOf<DataObject>()
                    for(ref in refs) {
                        val resolvedObj = allObjMap[ref.objectId]
                        if (resolvedObj != null) {
                            resolvedObjs.add(resolvedObj)
                        } else {
                            throw IllegalArgumentException("Could not resolve reference to object with id '${ref.objectId}' in property '${knowsProp.key}' of object '${obj.id}'")
                        }
                    }
                    knowsProp.setValues(resolvedObjs)
                }
            }
        }

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
     * @param meta The metamodel defining the structure for validation.
     * @return The root data object of the snippet.
     */
    fun parseSingleObject(content: String, meta: Metamodel): DataObject {
        val model = parseKmodelString(content, meta)
        return model.objects.firstOrNull()
            ?: throw IllegalArgumentException("kmodel payload did not contain an object")
    }

    /**
     * Parses a KModel file and converts it to a Model instance.
     *
     * @param filePath The path to the KModel file to parse.
     * @param meta The metamodel defining the structure for validation.
     * @return The parsed model instance.
     */
    fun parseKmodelFile(filePath: String, meta: Metamodel): Model {
        val content = File(filePath).readText()
        return parseKmodelString(content, meta)
    }

    /**
     * Recursively collects all DataObjects from a root list including embedded objects.
     *
     * @param rootObjects The root objects to start collection from.
     * @return A set of all collected objects.
     */
    fun collectAllObjects(rootObjects: MutableList<DataObject>): Set<DataObject> {
        val allObjects = mutableSetOf<DataObject>()
        for(obj in rootObjects) {
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

    fun parseMakeObjects(obj: ModelObject, meta: Metamodel): DataObject {

        println("Object: ${obj.key}")
        println("   Type: ${obj.className}")

        val objectKey = obj.key
        val objectType = obj.className
        val simpleObjectProperties = mutableListOf<SimplePropertyObject>()
        val classTypeProperties = mutableListOf<ClassTypePropertyObject>()

        val classType = meta.types.find { it.name == objectType }
        if (classType == null) {
            throw IllegalArgumentException("Unknown class type '$objectType' for object '$objectKey'")
        }

        val propBags = obj.props.groupBy { it.key }
        for (bag in propBags) {
            val propKey = bag.key
            val propType = classType.simpleProperties.find {it.key == propKey }
            if (propType == null) {
                throw IllegalArgumentException("Unknown property '$propKey' for class type '$objectType' in object '$objectKey'")
            }
            if (bag.value.size == 1 && propType.isList.not()) {
                val propValue = bag.value[0].value
                val property = when(propType.propertyType){
                    SimplePropertyType.STRING -> SimpleAtomicPropertyObject(propType, propKey, propValue)
                    SimplePropertyType.NUMBER -> SimpleAtomicPropertyObject(propType, propKey, propValue.toDouble())
                    SimplePropertyType.BOOLEAN -> SimpleAtomicPropertyObject(propType, propKey, propValue.toBoolean())
                }
                simpleObjectProperties.add(property)
            } else {
                val propValuesList = mutableListOf<Any>()
                for (prop in bag.value) {
                    val propValue = prop.value
                    val parsedValue = when(propType.propertyType){
                        SimplePropertyType.STRING -> propValue
                        SimplePropertyType.NUMBER -> propValue.toDouble()
                        SimplePropertyType.BOOLEAN -> propValue.toBoolean()
                    }
                    propValuesList.add(parsedValue)
                }
                val property = SimpleListPropertyObject(propType, propKey, propValuesList)
                simpleObjectProperties.add(property)
            }
        }

        val embedBags = obj.has.groupBy { it.key }
        for (bag in embedBags) {
            val embedKey = bag.key
            val embedType = classType.objectProperties.find { it.key == embedKey }
            if (embedType == null) {
                throw IllegalArgumentException("Unknown object property '$embedKey' for class type '$objectType' in object '$objectKey'")
            }
            if (bag.value.size == 1 && embedType.isList.not()) {
                val embedObjectValue = bag.value.first().childObject
                val embedObject = parseMakeObjects(embedObjectValue, meta)
                val property = ClassTypeAtomicPropertyObject(embedType, embedKey, ObjectReference(embedObject.id, embedObject))
                classTypeProperties.add(property)
            } else {
                val embedObjectsList = mutableListOf<ObjectReference>()
                for (embed in bag.value) {
                    val embedObjectValue = embed.childObject
                    val embedObject = parseMakeObjects(embedObjectValue, meta)
                    embedObjectsList.add(ObjectReference(embedObject.id, embedObject))
                }
                val property = ClassTypeListPropertyObject(embedType, embedKey, embedObjectsList)
                classTypeProperties.add(property)
            }
        }

        val knowsBags = obj.knows.groupBy { it.key }
        for (bag in knowsBags) {
            val knowsKey = bag.key
            val knowsType = classType.objectProperties.find { it.key == knowsKey }
            if (knowsType == null) {
                throw IllegalArgumentException("Unknown object property '$knowsKey' for class type '$objectType' in object '$objectKey'")
            }
            if (bag.value.size == 1 && knowsType.isList.not()) {
                val knowsObjectReferenceName = bag.value.first().value
                val knowsObjectReference = ObjectReference(knowsObjectReferenceName, null)
                val property = ClassTypeAtomicPropertyObject(knowsType, knowsKey, knowsObjectReference)
                classTypeProperties.add(property)
            } else {
                val knowsObjectReferencesList = mutableListOf<ObjectReference>()
                for (knows in bag.value) {
                    val knowsObjectReferenceName = knows.value
                    val knowsObjectReference = ObjectReference(knowsObjectReferenceName, null)
                    knowsObjectReferencesList.add(knowsObjectReference)
                }
                val property = ClassTypeListPropertyObject(knowsType, knowsKey, knowsObjectReferencesList)
                classTypeProperties.add(property)
            }
        }

        val dataObject = DataObject(classType, objectKey, simpleObjectProperties, classTypeProperties)
        return dataObject
    }

    /**
     * Converts a Model instance back to KModel DSL string format.
     *
     * @param model The model to convert.
     * @return The KModel DSL string representation.
     * @throws NotImplementedError This operation is not yet implemented.
     */
    fun convertToKmodelString(model: Model): String {
        throw NotImplementedError("Conversion from Model to Kmodel DSL string is not implemented yet.")
    }


}