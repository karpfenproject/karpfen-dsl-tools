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
package io.karpfen.dsl.printer.kmeta

import io.karpfen.dsl.meta.AssociationType
import io.karpfen.dsl.meta.ClassType
import io.karpfen.dsl.meta.ClassTypeProperty
import io.karpfen.dsl.meta.Metamodel
import io.karpfen.dsl.meta.SimpleProperty

/**
 * The structure of a [Metamodel] as plain values, so that two metamodels compare with `equals`.
 *
 * The class types themselves do not serve for that: a resolved relation refers to its target class type,
 * and comparing metamodels with cyclic relations would never end.
 *
 * @property types The class types in metamodel order.
 * @property rootName The name of the root class type.
 */
data class MetamodelStructure(val types: List<TypeStructure>, val rootName: String)

/**
 * The structure of a [ClassType].
 *
 * @property name The type name.
 * @property comment The type description.
 * @property simpleProperties The simple properties in order.
 * @property relations The object properties in order.
 */
data class TypeStructure(
    val name: String,
    val comment: String,
    val simpleProperties: List<SimpleProperty>,
    val relations: List<RelationStructure>
)

/**
 * The structure of a [ClassTypeProperty], with the name of the class type its reference resolved to.
 *
 * @property key The property name.
 * @property associationType Embedded or linked.
 * @property isList True for a list relation.
 * @property targetName The name of the target class type as written.
 * @property resolvedTargetName The name of the class type the reference resolved to, null if unresolved.
 */
data class RelationStructure(
    val key: String,
    val associationType: AssociationType,
    val isList: Boolean,
    val targetName: String,
    val resolvedTargetName: String?
)

/**
 * Returns the structure of this metamodel.
 */
fun Metamodel.structure(): MetamodelStructure = MetamodelStructure(types.map { it.structure() }, rootClass.name)

/**
 * Returns the structure of this class type.
 */
fun ClassType.structure(): TypeStructure =
    TypeStructure(name, comment, simpleProperties, objectProperties.map { it.structure() })

/**
 * Returns the structure of this object property.
 */
fun ClassTypeProperty.structure(): RelationStructure =
    RelationStructure(key, associationType, isList, reference.classTypeName, reference.classType?.name)
