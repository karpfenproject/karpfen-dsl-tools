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

import io.karpfen.dsl.meta.AssociationType
import io.karpfen.dsl.meta.Metamodel

/**
 * A model: a collection of objects and the metamodel they conform to.
 *
 * @property objects The objects of the model; parsed models hold their root object here.
 * @property metamodel The metamodel that defines the structure of the objects.
 */
data class Model(val objects: MutableList<DataObject>, val metamodel: Metamodel? = null) {

    /**
     * Groups the objects of [objects] by the name of their class type.
     */
    fun objectMapByType(): Map<String, List<DataObject>> {
        return objects.groupBy { it.ofType.name }
    }

    /**
     * Returns the object of [objects] with class type [typeName] and identifier [id], or null if there is none.
     */
    fun get(typeName: String, id: String): DataObject? {
        return objects.firstOrNull { it.ofType.name == typeName && it.id == id }
    }

    /**
     * Traversals and searches that work on any list of objects.
     */
    companion object {

        /**
         * Returns [rootObjects] and, recursively, every object embedded in them through a `has` relation, in depth-first
         * order: each object before the objects embedded in it, and those in the order of its relations and of their
         * values. Objects that a `knows` relation leads to are not followed, so the result is the content of the trees
         * that start at [rootObjects], each object once.
         *
         * A model read from text holds only its root object in [objects], so `collectAllObjects(model.objects)` returns
         * every object of the model.
         *
         * @param rootObjects The objects to start from.
         * @return The root objects and all objects embedded in them, without repetitions.
         */
        fun collectAllObjects(rootObjects: List<DataObject>): Set<DataObject> {
            val collected = linkedSetOf<DataObject>()
            rootObjects.forEach { collectInto(collected, it) }
            return collected
        }

        /**
         * Adds [obj] and, unless it was already there, the objects embedded in it to [collected].
         */
        private fun collectInto(collected: MutableSet<DataObject>, obj: DataObject) {
            if (!collected.add(obj)) {
                return
            }
            for (relation in obj.relations.filter { it.getPropertyType().associationType == AssociationType.EMBEDDED }) {
                when (relation) {
                    is ClassTypeAtomicPropertyObject -> relation.getValue()?.let { collectInto(collected, it) }
                    is ClassTypeListPropertyObject -> relation.getValues().forEach { collectInto(collected, it) }
                }
            }
        }

        /**
         * Returns the [objects] whose simple property [propKey] holds [propValue].
         *
         * For a list property, [propValue] must be a list with the same size whose elements all occur in the
         * property's values.
         */
        @Suppress("UNCHECKED_CAST")
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
