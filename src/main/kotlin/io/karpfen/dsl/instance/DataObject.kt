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
package io.karpfen.dsl.instance

import io.karpfen.dsl.meta.ClassType

/**
 * An object of a model: an instance of a class type with its property values and relations.
 *
 * The constructor does not check the given properties and relations against the class type; the
 * converters and builders create them from the metamodel.
 *
 * @property ofType The class type this object instantiates.
 * @property id The identifier of this object, empty for anonymous objects.
 * @property properties The simple property values of this object.
 * @property relations The relations of this object to other objects.
 */
class DataObject(
    val ofType: ClassType,
    val id: String = "",
    val properties: MutableList<SimplePropertyObject>,
    val relations: MutableList<ClassTypePropertyObject>
) {

    /**
     * Returns the values of the simple property named [key]: one element for a single-valued property and
     * an empty list when this object has no such property.
     */
    fun getProp(key: String): List<Any> {
        val prop = properties.firstOrNull { it.key == key } ?: return listOf()
        return if (prop.isList()) {
            (prop as SimpleListPropertyObject).getValues()
        } else {
            listOf((prop as SimpleAtomicPropertyObject).getValue())
        }
    }

    /**
     * Returns the objects related through the relation named [key]: at most one element for a single-object
     * relation and an empty list when this object has no such relation.
     */
    fun getRel(key: String): List<DataObject> {
        val rel = relations.firstOrNull { it.key == key } ?: return listOf()
        return if (rel.isList()) {
            (rel as ClassTypeListPropertyObject).getValues().filterNotNull()
        } else {
            listOf((rel as ClassTypeAtomicPropertyObject).getValue()).filterNotNull()
        }
    }

    /**
     * Assigns simple property values by property name.
     *
     * A list property expects a list value and a single-valued property expects a single value; keys
     * this object has no property for are ignored.
     *
     * @throws IllegalArgumentException If a value does not match the primitive type of its property.
     */
    fun assignProps(props: Map<String, Any>) {
        for (incomingProp in props) {
            val incomingKey = incomingProp.key
            val incomingValue = incomingProp.value

            for (prop: SimplePropertyObject in properties) {
                if (prop.key == incomingKey) {
                    if (prop.isList()) {
                        (prop as SimpleListPropertyObject).setValues(incomingValue as List<Any>)
                    } else {
                        (prop as SimpleAtomicPropertyObject).setValue(incomingValue)
                    }
                }
            }
        }
    }

    /**
     * Assigns related objects by relation name.
     *
     * A list relation expects a list of [DataObject]s and a single-object relation expects one
     * [DataObject]; keys this object has no relation for are ignored.
     *
     * @throws IllegalArgumentException If an object is not of the class type its relation expects.
     */
    fun assignRels(rels: Map<String, Any>) {
        for (incomingRel in rels) {
            val incomingKey = incomingRel.key
            val incomingValue = incomingRel.value

            for (rel: ClassTypePropertyObject in relations) {
                if (rel.key == incomingKey) {
                    if (rel.isList()) {
                        (rel as ClassTypeListPropertyObject).setValues(incomingValue as List<DataObject>)
                    } else {
                        (rel as ClassTypeAtomicPropertyObject).setValue(incomingValue as DataObject)
                    }
                }
            }
        }
    }
}
