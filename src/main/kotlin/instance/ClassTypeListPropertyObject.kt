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

class ClassTypeListPropertyObject(
    ofType: ClassTypeProperty,
    key: String,
    private var values: MutableList<DataObject>
) : ClassTypePropertyObject(ofType, key) {

    override fun isList(): Boolean {
        return true
    }

    fun getValues(): List<DataObject> {
        return values
    }

    fun setValues(incomingValues: List<DataObject>) {
        values.clear()
        for(value in incomingValues) {
            if(value.ofType != getClassType()) {
                throw IllegalArgumentException("Expected a DataObject of type ${getClassType().name}")
            }
            this.values.add(value)
        }
    }
}