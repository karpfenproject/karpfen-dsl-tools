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
package instance

import meta.ClassTypeProperty

/**
 * Represents a list-valued object property in a DataObject.
 *
 * @property ofType The ClassTypeProperty definition from the metamodel.
 * @property key The property name.
 * @property values The list of stored ObjectReferences.
 */
class ClassTypeListPropertyObject(
    ofType: ClassTypeProperty,
    key: String,
    private var values: MutableList<ObjectReference>
) : ClassTypePropertyObject(ofType, key) {

    override fun isList(): Boolean {
        return true
    }

    fun getValues(): List<DataObject> {
        return values.map { it.obj!! }
    }

    fun getValueRefs(): List<ObjectReference> {
        return values
    }

    fun setValues(incomingValues: List<DataObject>) {
        values.clear()
        for(value in incomingValues) {
            if(value.ofType.name != getClassType()) {
                throw IllegalArgumentException("Expected a DataObject of type ${getClassType()}")
            }
            this.values.add(ObjectReference(value.id, value))
        }
    }

    /** Appends a single object reference to the list, validating its type against the metamodel. */
    fun appendValue(value: DataObject) {
        if(value.ofType.name != getClassType()) {
            throw IllegalArgumentException("Expected a DataObject of type ${getClassType()}")
        }
        this.values.add(ObjectReference(value.id, value))
    }

    /** Removes every reference whose target object has the given id. */
    fun removeById(id: String) {
        values.removeIf { it.objectId == id || it.obj?.id == id }
    }

    /** Removes all references, leaving an empty list. */
    fun clear() {
        values.clear()
    }
}