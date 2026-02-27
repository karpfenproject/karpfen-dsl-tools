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

import meta.ClassType

class DataObject(
    val ofType: ClassType,
    val id: String = "",
    val properties: MutableList<SimplePropertyObject>,
    val relations: MutableList<ClassTypePropertyObject>
){

    fun getProp(key: String): List<Any> {
        val prop = properties.firstOrNull { it.key == key } ?: return listOf()
        return if (prop.isList()) {
            (prop as SimpleListPropertyObject).getValues()
        } else {
            listOf((prop as SimpleAtomicPropertyObject).getValue())
        }
    }

    fun getRel(key: String): List<DataObject> {
        val rel = relations.firstOrNull { it.key == key } ?: return listOf()
        return if (rel.isList()) {
            (rel as ClassTypeListPropertyObject).getValues()
        } else {
            listOf((rel as ClassTypeAtomicPropertyObject).getValue())
        }
    }

    init {
        //TODO If initialized properties and relations are not empty, check that they match the metamodel definition
    }

    fun assignProps(props: Map<String, Any>) {
        for(incomingProp in props){
            val incomingKey = incomingProp.key
            val incomingValue = incomingProp.value

            for(prop: SimplePropertyObject in properties){
                if(prop.key == incomingKey){
                    if(prop.isList()){
                        (prop as SimpleListPropertyObject).setValues(incomingValue as List<Any>)
                    }else{
                        (prop as SimpleAtomicPropertyObject).setValue(incomingValue)
                    }
                }
            }
        }
    }

    fun assignRels(rels: Map<String, Any>) {
        for(incomingRel in rels){
            val incomingKey = incomingRel.key
            val incomingValue = incomingRel.value

            for(rel: ClassTypePropertyObject in relations){
                if(rel.key == incomingKey){
                    if(rel.isList()){
                        (rel as ClassTypeListPropertyObject).setValues(incomingValue as List<DataObject>)
                    }else{
                        (rel as ClassTypeAtomicPropertyObject).setValue(incomingValue as DataObject)
                    }
                }
            }
        }
    }

}
