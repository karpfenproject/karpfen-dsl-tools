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
package io.karpfen.dsl.dsl.functional

import io.karpfen.dsl.instance.ClassTypeAtomicPropertyObject
import io.karpfen.dsl.instance.ClassTypeListPropertyObject
import io.karpfen.dsl.instance.ClassTypePropertyObject
import io.karpfen.dsl.instance.DataObject
import io.karpfen.dsl.instance.Model
import io.karpfen.dsl.instance.ObjectReference
import io.karpfen.dsl.instance.SimpleAtomicPropertyObject
import io.karpfen.dsl.instance.SimpleListPropertyObject
import io.karpfen.dsl.instance.SimplePropertyObject
import io.karpfen.dsl.meta.ClassType
import io.karpfen.dsl.meta.Metamodel
import io.karpfen.dsl.meta.SimplePropertyType

/**
 * Builds a [Model] of [meta] in code, without writing KModel text.
 *
 * Create objects with [makeObject], fill them with [DataObject.assignProps] and [DataObject.assignRels], and
 * call [build].
 *
 * @property meta The metamodel the objects are instances of.
 */
class ModelBuilder(val meta: Metamodel) {

    /** Every object created so far, including the default objects of single-object relations. */
    val dataObjects = mutableListOf<DataObject>()

    /**
     * Creates an object of class type [classT] with default values and registers it.
     *
     * Simple properties start as `false`, `""`, [Double.MIN_VALUE] or an empty list; every single-object
     * relation starts with a fresh default object of its target class type, created the same way.
     *
     * @param id The object identifier, empty for an anonymous object.
     * @throws IllegalArgumentException If the metamodel has no class type [classT].
     */
    fun makeObject(classT: String, id: String = ""): DataObject {
        val classType = meta.getTypeByName(classT)
            ?: throw IllegalArgumentException("Class type $classT is not defined in the metamodel")

        val propList = initializePropList(classType)
        val relList = initializeRelList(classType)

        val newObject = DataObject(classType, id, propList.toMutableList(), relList.toMutableList())
        dataObjects.add(newObject)

        return newObject
    }

    /**
     * Returns the properties of a new object of [classType] with their default values: an empty list, `false`, the
     * empty string or [Double.MIN_VALUE].
     */
    private fun initializePropList(classType: ClassType): List<SimplePropertyObject> {
        val propList = mutableListOf<SimplePropertyObject>()
        for (propType in classType.simpleProperties) {
            if (propType.isList) {
                propList.add(SimpleListPropertyObject(propType, propType.key, mutableListOf<Any>()))
            } else {
                val default: Any = when (propType.propertyType) {
                    SimplePropertyType.BOOLEAN -> false
                    SimplePropertyType.STRING -> ""
                    SimplePropertyType.NUMBER -> Double.MIN_VALUE
                }
                propList.add(SimpleAtomicPropertyObject(propType, propType.key, default))
            }
        }
        return propList
    }

    /**
     * Returns the relations of a new object of [classType]: an empty list, or a reference to a new default object of
     * the target type, which has no id.
     */
    private fun initializeRelList(classType: ClassType): List<ClassTypePropertyObject> {
        val relList = mutableListOf<ClassTypePropertyObject>()
        for (relType in classType.objectProperties) {
            if (relType.isList) {
                relList.add(ClassTypeListPropertyObject(relType, relType.key, mutableListOf<ObjectReference>()))
            } else {
                relList.add(ClassTypeAtomicPropertyObject(relType, relType.key,
                    ObjectReference("", makeObject(relType.reference.classTypeName, ""))))
            }
        }
        return relList
    }

    /**
     * Builds the model from all objects created so far.
     */
    fun build(): Model {
        return Model(dataObjects, meta)
    }
}
