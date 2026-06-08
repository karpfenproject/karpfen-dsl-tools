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
import instance.ObjectReference
import instance.SimpleAtomicPropertyObject
import instance.SimpleListPropertyObject
import instance.SimplePropertyObject
import meta.ClassType
import meta.Metamodel
import meta.SimpleProperty
import meta.SimplePropertyType
import org.json.JSONArray
import org.json.JSONObject

/**
 * Turns JSON documents into the karpfen runtime object model ([DataObject]s) against a [Metamodel].
 *
 * This is the bridge that lets the rest of the toolchain treat a JSON event payload exactly like a
 * payload written in the kmodel syntax: once it is a [DataObject], the same path resolution, macro
 * machinery and serialisation work without caring where it came from.
 *
 * A JSON object may carry the reserved keys `__id__` and `__type__`. When [existingResolver] is
 * supplied and an object with a matching `__id__` already exists, its simple properties (and any
 * provided relations) are updated in place rather than producing a fresh object. For freshly arriving
 * data, such as event payloads, leave [existingResolver] as `null` so every call builds a new object.
 *
 * @property metamodel        The metamodel whose types the JSON is interpreted against.
 * @property existingResolver Optional lookup of an already-existing object by id. Return `null` when
 *                            no such object exists; the converter then constructs a new one.
 */
class JsonToModelConverter(
    private val metamodel: Metamodel,
    private val existingResolver: ((String) -> DataObject?)? = null
) {

    /**
     * Parses [jsonString] (expected to be a JSON object) into a [DataObject] of [typeName].
     */
    fun toDataObject(jsonString: String, typeName: String): DataObject {
        val trimmed = jsonString.trim()
        if (!trimmed.startsWith("{")) {
            throw IllegalArgumentException("Expected a JSON object for type '$typeName' but got: $jsonString")
        }
        return toDataObject(JSONObject(trimmed), typeName)
    }

    /**
     * Converts a [JSONObject] into a [DataObject] of [typeName].
     *
     * If the JSON carries an `__id__` matching an existing object (and an [existingResolver] is set),
     * that object's properties are updated in place and returned. Otherwise a new object is built using
     * the metamodel definition to decide which keys are simple properties versus relations.
     */
    fun toDataObject(json: JSONObject, typeName: String): DataObject {
        if (json.has("__id__")) {
            val resolver = existingResolver
            if (resolver != null) {
                val existing = resolver(json.getString("__id__"))
                if (existing != null) {
                    existing.assignProps(extractSimpleProps(json, existing.ofType))
                    val rels = extractRelations(json, existing.ofType)
                    if (rels.isNotEmpty()) existing.assignRels(rels)
                    return existing
                }
            }
        }

        val classType = metamodel.getTypeByName(typeName)
            ?: throw IllegalArgumentException("Unknown metamodel type '$typeName' for JSON parsing")

        val id = if (json.has("__id__")) json.getString("__id__") else ""
        return DataObject(classType, id, buildSimpleProps(json, classType), buildRelations(json, classType))
    }

    /**
     * Parses [jsonArrayString] (a JSON array) into a list of values of [elementType].
     * Primitive element types yield Kotlin primitives; class types yield [DataObject]s.
     */
    fun toList(jsonArrayString: String, elementType: String): List<Any> {
        val arr = JSONArray(jsonArrayString.trim())
        val results = mutableListOf<Any>()
        for (i in 0 until arr.length()) {
            when (elementType.lowercase()) {
                "number" -> results.add((arr.get(i) as Number).toDouble())
                "boolean" -> results.add(arr.get(i) as Boolean)
                "string" -> results.add(arr.get(i).toString())
                else -> results.add(toDataObject(arr.getJSONObject(i), elementType))
            }
        }
        return results
    }

    // ---- Property extraction (in-place update) -----------------------------

    private fun extractSimpleProps(json: JSONObject, classType: ClassType): Map<String, Any> {
        val result = mutableMapOf<String, Any>()
        for (prop in classType.simpleProperties) {
            if (!json.has(prop.key) || json.isNull(prop.key)) continue
            result[prop.key] = if (prop.isList) {
                val arr = json.getJSONArray(prop.key)
                (0 until arr.length()).map { parseSimpleValue(arr.get(it), prop) }
            } else {
                parseSimpleValue(json.get(prop.key), prop)
            }
        }
        return result
    }

    private fun extractRelations(json: JSONObject, classType: ClassType): Map<String, Any> {
        val result = mutableMapOf<String, Any>()
        for (objProp in classType.objectProperties) {
            if (!json.has(objProp.key) || json.isNull(objProp.key)) continue
            val targetTypeName = objProp.reference.classTypeName
            result[objProp.key] = if (objProp.isList) {
                val arr = json.getJSONArray(objProp.key)
                (0 until arr.length()).map { toDataObject(arr.getJSONObject(it), targetTypeName) }
            } else {
                toDataObject(json.getJSONObject(objProp.key), targetTypeName)
            }
        }
        return result
    }

    // ---- Property construction (new object) --------------------------------

    private fun buildSimpleProps(json: JSONObject, classType: ClassType): MutableList<SimplePropertyObject> {
        val result = mutableListOf<SimplePropertyObject>()
        for (prop in classType.simpleProperties) {
            if (!json.has(prop.key)) {
                if (prop.isList) {
                    result.add(SimpleListPropertyObject(prop, prop.key, mutableListOf()))
                } else {
                    result.add(SimpleAtomicPropertyObject(prop, prop.key, defaultValueForType(prop.propertyType)))
                }
                continue
            }
            if (json.isNull(prop.key)) continue

            if (prop.isList) {
                val arr = json.getJSONArray(prop.key)
                val values = (0 until arr.length()).map { parseSimpleValue(arr.get(it), prop) }.toMutableList()
                result.add(SimpleListPropertyObject(prop, prop.key, values))
            } else {
                result.add(SimpleAtomicPropertyObject(prop, prop.key, parseSimpleValue(json.get(prop.key), prop)))
            }
        }
        return result
    }

    private fun buildRelations(json: JSONObject, classType: ClassType): MutableList<ClassTypePropertyObject> {
        val result = mutableListOf<ClassTypePropertyObject>()
        for (objProp in classType.objectProperties) {
            val targetTypeName = objProp.reference.classTypeName

            if (!json.has(objProp.key) || json.isNull(objProp.key)) {
                if (objProp.isList) {
                    result.add(ClassTypeListPropertyObject(objProp, objProp.key, mutableListOf()))
                } else {
                    result.add(ClassTypeAtomicPropertyObject(objProp, objProp.key, ObjectReference("")))
                }
                continue
            }

            if (objProp.isList) {
                val arr = json.getJSONArray(objProp.key)
                val refs = (0 until arr.length()).map {
                    val child = toDataObject(arr.getJSONObject(it), targetTypeName)
                    ObjectReference(child.id, child)
                }.toMutableList()
                result.add(ClassTypeListPropertyObject(objProp, objProp.key, refs))
            } else {
                val child = toDataObject(json.getJSONObject(objProp.key), targetTypeName)
                result.add(ClassTypeAtomicPropertyObject(objProp, objProp.key, ObjectReference(child.id, child)))
            }
        }
        return result
    }

    private fun parseSimpleValue(raw: Any, prop: SimpleProperty): Any {
        return when (prop.propertyType) {
            SimplePropertyType.NUMBER -> (raw as Number).toDouble()
            SimplePropertyType.BOOLEAN -> raw as Boolean
            SimplePropertyType.STRING -> raw.toString()
        }
    }

    private fun defaultValueForType(type: SimplePropertyType): Any {
        return when (type) {
            SimplePropertyType.NUMBER -> 0.0
            SimplePropertyType.BOOLEAN -> false
            SimplePropertyType.STRING -> ""
        }
    }
}
