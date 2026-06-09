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
 * Represents a single-object property in a DataObject.
 *
 * @property ofType The ClassTypeProperty definition from the metamodel.
 * @property key The property name.
 * @property value The stored ObjectReference.
 */
class ClassTypeAtomicPropertyObject(
    ofType: ClassTypeProperty,
    key: String,
    private var value: ObjectReference
) : ClassTypePropertyObject(ofType, key) {

    override fun isList(): Boolean {
        return false
    }

    fun getValue(): DataObject? {
        return value.obj
    }

    fun getValueRef(): ObjectReference {
        return value
    }

    fun setValueRef(ref: ObjectReference) {
        this.value = ref
    }

    fun setValue(value: DataObject) {
        if(value.ofType.name != getClassType()) {
            throw IllegalArgumentException("Expected a DataObject of type ${getClassType()}")
        }
        this.value.obj = value
        this.value.objectId = value.id
    }

    /** Clears the reference, leaving the relation empty (no target object). */
    fun clear() {
        this.value = ObjectReference("")
    }

}