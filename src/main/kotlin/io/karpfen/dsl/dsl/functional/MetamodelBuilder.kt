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
package io.karpfen.dsl.dsl.functional

import io.karpfen.dsl.meta.AssociationType
import io.karpfen.dsl.meta.ClassType
import io.karpfen.dsl.meta.ClassTypeProperty
import io.karpfen.dsl.meta.ClassTypeReference
import io.karpfen.dsl.meta.Metamodel
import io.karpfen.dsl.meta.SimpleProperty
import io.karpfen.dsl.meta.SimplePropertyType

/**
 * Builds a [Metamodel] in code, without writing KMeta text.
 *
 * Define a class type before any relation that points to it, set the root class and call [build].
 */
class MetamodelBuilder {

    private val classTypes = mutableMapOf<String, ClassType>()
    private var rootClass: String? = null

    /** The association type of embedded relations, `has` in KMeta. */
    public val EMBEDDED = AssociationType.EMBEDDED

    /** The association type of linked relations, `knows` in KMeta. */
    public val LINK = AssociationType.LINK

    /**
     * Defines a class type, replacing an earlier one of the same name.
     *
     * @return This builder, for chaining.
     */
    fun makeClass(
        name: String,
        comment: String,
        props: List<SimpleProperty> = emptyList(),
        rels: List<ClassTypeProperty> = emptyList()
    ): MetamodelBuilder {
        val classType = ClassType(name, comment, props, rels)
        classTypes[name] = classType
        return this
    }

    /**
     * Creates a single-valued boolean property.
     */
    fun makeBool(key: String): SimpleProperty {
        return SimpleProperty(key, SimplePropertyType.BOOLEAN, false)
    }

    /**
     * Creates a single-valued string property.
     */
    fun makeString(key: String): SimpleProperty {
        return SimpleProperty(key, SimplePropertyType.STRING, false)
    }

    /**
     * Creates a single-valued number property.
     */
    fun makeNumber(key: String): SimpleProperty {
        return SimpleProperty(key, SimplePropertyType.NUMBER, false)
    }

    /**
     * Creates a boolean list property.
     */
    fun makeBoolList(key: String): SimpleProperty {
        return SimpleProperty(key, SimplePropertyType.BOOLEAN, true)
    }

    /**
     * Creates a string list property.
     */
    fun makeStringList(key: String): SimpleProperty {
        return SimpleProperty(key, SimplePropertyType.STRING, true)
    }

    /**
     * Creates a number list property.
     */
    fun makeNumberList(key: String): SimpleProperty {
        return SimpleProperty(key, SimplePropertyType.NUMBER, true)
    }

    /**
     * Creates a single-object relation to the already defined class type [classT].
     *
     * @param relT [EMBEDDED] or [LINK].
     * @throws NullPointerException If [classT] has not been defined yet.
     */
    fun makeClassProp(key: String, classT: String, relT: AssociationType): ClassTypeProperty {
        return ClassTypeProperty(
            key,
            ClassTypeReference(relT, classT, classTypes[classT]!!),
            false,
            relT
        )
    }

    /**
     * Creates a list relation to the already defined class type [classT].
     *
     * @param relT [EMBEDDED] or [LINK].
     * @throws NullPointerException If [classT] has not been defined yet.
     */
    fun makeClassPropList(key: String, classT: String, relT: AssociationType): ClassTypeProperty {
        return ClassTypeProperty(
            key,
            ClassTypeReference(relT, classT, classTypes[classT]!!),
            true,
            relT
        )
    }

    /**
     * Sets the name of the root class type.
     */
    fun setRoot(rootClassName: String) {
        this.rootClass = rootClassName
    }

    /**
     * Builds the metamodel from the defined class types.
     *
     * @throws IllegalStateException If no root class is set or the root class is not defined.
     */
    fun build(): Metamodel {
        if (rootClass == null) {
            throw IllegalStateException("Root class must be set before building the metamodel.")
        }
        val rootClass = classTypes.get(rootClass!!) ?: throw IllegalStateException("Root class '$rootClass' is not defined in the metamodel.")
        return Metamodel(classTypes.values.toMutableList(), rootClass)
    }
}
