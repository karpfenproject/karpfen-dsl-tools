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
package dsl.functional

import instance.ClassTypeAtomicPropertyObject
import instance.ClassTypeListPropertyObject
import instance.ClassTypePropertyObject
import instance.DataObject
import instance.Model
import instance.SimpleAtomicPropertyObject
import instance.SimpleListPropertyObject
import instance.SimplePropertyObject
import meta.ClassType
import meta.Metamodel
import meta.SimplePropertyType

class ModelBuilder(val meta: Metamodel) {

    val dataObjects = mutableListOf<DataObject>()

    fun makeObject(classT: String, id: String = ""): DataObject {
        //Check that classT is in the metamodel
        val classType = meta.getTypeByName(classT)
            ?: throw IllegalArgumentException("Class type $classT is not defined in the metamodel")

        val propList = initializePropList(classType)
        val relList = initializeRelList(classType)

        val newObject = DataObject(classType, id, propList.toMutableList(), relList.toMutableList())
        dataObjects.add(newObject)

        return newObject
    }

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

    private fun initializeRelList(classType: ClassType): List<ClassTypePropertyObject> {
        val relList = mutableListOf<ClassTypePropertyObject>()
        for (relType in classType.objectProperties) {
            if (relType.isList) {
                relList.add(ClassTypeListPropertyObject(relType, relType.key, mutableListOf<DataObject>()))
            } else {
                relList.add(ClassTypeAtomicPropertyObject(relType, relType.key, makeObject(relType.ofType.name, "")))
            }
        }
        return relList
    }

    fun build(): Model {
        return Model(dataObjects)
    }

}