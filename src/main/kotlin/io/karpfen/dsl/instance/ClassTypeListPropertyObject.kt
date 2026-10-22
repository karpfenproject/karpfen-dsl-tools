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

import io.karpfen.dsl.meta.ClassTypeProperty

/**
 * A list-valued relation of a [DataObject].
 *
 * @param ofType The object property definition from the metamodel.
 * @param key The property name.
 * @param values The references to the related objects.
 */
class ClassTypeListPropertyObject(
    ofType: ClassTypeProperty,
    key: String,
    private var values: MutableList<ObjectReference>
) : ClassTypePropertyObject(ofType, key) {

    override fun isList(): Boolean {
        return true
    }

    /**
     * Returns the related objects.
     *
     * @throws NullPointerException If a reference is not resolved yet.
     */
    fun getValues(): List<DataObject> {
        return values.map { it.obj!! }
    }

    /**
     * Returns the references to the related objects, resolved or not.
     */
    fun getValueRefs(): List<ObjectReference> {
        return values
    }

    /**
     * Replaces all references with references to [incomingValues].
     *
     * @throws IllegalArgumentException If an object is not of the class type this relation expects; the
     * objects before it have been added by then.
     */
    fun setValues(incomingValues: List<DataObject>) {
        values.clear()
        for (value in incomingValues) {
            if (value.ofType.name != getClassType()) {
                throw IllegalArgumentException("Expected a DataObject of type ${getClassType()}")
            }
            this.values.add(ObjectReference(value.id, value))
        }
    }

    /**
     * Appends a reference to [value], validating its type against the metamodel.
     *
     * @throws IllegalArgumentException If [value] is not of the class type this relation expects.
     */
    fun appendValue(value: DataObject) {
        if (value.ofType.name != getClassType()) {
            throw IllegalArgumentException("Expected a DataObject of type ${getClassType()}")
        }
        this.values.add(ObjectReference(value.id, value))
    }

    /**
     * Removes every reference whose target object has the given [id].
     */
    fun removeById(id: String) {
        values.removeIf { it.objectId == id || it.obj?.id == id }
    }

    /**
     * Removes all references, leaving an empty list.
     */
    fun clear() {
        values.clear()
    }
}
