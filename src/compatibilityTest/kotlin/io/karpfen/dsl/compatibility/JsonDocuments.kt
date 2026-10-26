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
package io.karpfen.dsl.compatibility

import io.karpfen.dsl.instance.ClassTypeAtomicPropertyObject
import io.karpfen.dsl.instance.ClassTypeListPropertyObject
import io.karpfen.dsl.instance.ClassTypePropertyObject
import io.karpfen.dsl.instance.DataObject
import io.karpfen.dsl.instance.SimpleAtomicPropertyObject
import io.karpfen.dsl.instance.SimpleListPropertyObject
import io.karpfen.dsl.meta.AssociationType
import org.json.JSONArray
import org.json.JSONObject

/**
 * Writes objects of a model as the JSON documents `JsonToModelConverter` reads: the id as `__id__`, every property
 * value under its key, every embedded object as a nested document and every linked object as a document with just
 * its id.
 */
object JsonDocuments {

    /**
     * Returns [obj] as a JSON document.
     */
    fun of(obj: DataObject): String = documentOf(obj).toString()

    private fun documentOf(obj: DataObject): JSONObject {
        val document = JSONObject().put("__id__", obj.id)
        for (property in obj.properties) {
            when (property) {
                is SimpleAtomicPropertyObject -> document.put(property.key, property.getValue())
                is SimpleListPropertyObject -> document.put(property.key, JSONArray(property.getValues()))
            }
        }
        for (relation in obj.relations) {
            val related = relatedOf(relation).map { if (relation.isEmbedding()) documentOf(it) else linkTo(it) }
            val value: Any? = if (relation is ClassTypeListPropertyObject) JSONArray(related) else related.singleOrNull()
            document.put(relation.key, value)
        }
        return document
    }

    private fun linkTo(obj: DataObject): JSONObject = JSONObject().put("__id__", obj.id)

    private fun relatedOf(relation: ClassTypePropertyObject): List<DataObject> = when (relation) {
        is ClassTypeAtomicPropertyObject -> listOfNotNull(relation.getValue())
        is ClassTypeListPropertyObject -> relation.getValues()
        else -> emptyList()
    }

    private fun ClassTypePropertyObject.isEmbedding(): Boolean =
        getPropertyType().associationType == AssociationType.EMBEDDED
}
