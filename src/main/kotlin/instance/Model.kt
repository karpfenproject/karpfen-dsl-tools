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

data class Model(val objects: MutableList<DataObject>){

    fun objectMapByType(): Map<String, List<DataObject>> {
        return objects.groupBy { it.ofType.name }
    }

    fun get(typeName: String, id: String): DataObject? {
        return objects.firstOrNull { it.ofType.name == typeName && it.id == id }
    }

    companion object {
        fun findBySimplePropValue(objects: List<DataObject>, propKey: String, propValue: Any): List<DataObject> {
            return objects.filter { obj ->
                obj.properties.any { prop ->
                    prop.key == propKey && when (prop) {
                        is SimpleAtomicPropertyObject -> prop.getValue() == propValue
                        is SimpleListPropertyObject -> {
                            val incomingList = propValue as List<Any>
                            val propList = prop.getValues()
                            incomingList.size == propList.size && incomingList.all { it in propList }
                        }
                        else -> false
                    }
                }
            }
        }
    }

}