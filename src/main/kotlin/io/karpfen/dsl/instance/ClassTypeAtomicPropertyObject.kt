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
 * A single-object relation of a [DataObject].
 *
 * @param ofType The object property definition from the metamodel.
 * @param key The property name.
 * @param value The reference to the related object.
 */
class ClassTypeAtomicPropertyObject(
    ofType: ClassTypeProperty,
    key: String,
    private var value: ObjectReference
) : ClassTypePropertyObject(ofType, key) {

    override fun isList(): Boolean {
        return false
    }

    /**
     * Returns the related object, or null while the reference is unresolved or empty.
     */
    fun getValue(): DataObject? {
        return value.obj
    }

    /**
     * Returns the reference to the related object, resolved or not.
     */
    fun getValueRef(): ObjectReference {
        return value
    }

    /**
     * Replaces the reference to the related object without validating it.
     */
    fun setValueRef(ref: ObjectReference) {
        this.value = ref
    }

    /**
     * Points the current reference at [value], updating its object and its id in place.
     *
     * @throws IllegalArgumentException If [value] is not of the class type this relation expects.
     */
    fun setValue(value: DataObject) {
        if (value.ofType.name != getClassType()) {
            throw IllegalArgumentException("Expected a DataObject of type ${getClassType()}")
        }
        this.value.obj = value
        this.value.objectId = value.id
    }

    /**
     * Clears the reference, leaving the relation empty (no target object).
     */
    fun clear() {
        this.value = ObjectReference("")
    }
}
