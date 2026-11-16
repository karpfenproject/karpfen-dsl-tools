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

import java.lang.reflect.Field

/**
 * The fields of the models and of the syntax-level files that version 1.0 does not have, each with the value it holds
 * for a text without the construct it stands for: the name and the types of an attachable state machine, the `ATTACH`
 * clause of a rule, the `behaves after` clause of a type, the mark of a fragment, and the fragment of a container state
 * and the mark of a final state. A text without these constructs must read exactly as with
 * version 1.0, so the comparison leaves such a field out while it holds that value, and writes it otherwise, which
 * then makes the comparison fail.
 */
object FieldsSinceVersion1 {

    /** The value of each field for a text without its construct, by the simple class name and the field name. */
    private val unusedValues: Map<Pair<String, String>, Any?> = mapOf(
        ("StateMachine" to "name") to null,
        ("StateMachine" to "attachableTo") to emptyList<String>(),
        ("KstatesFile" to "name") to null,
        ("KstatesFile" to "attachableTo") to emptyList<String>(),
        ("StateMachine" to "isFragment") to false,
        ("KstatesFile" to "isFragment") to false,
        ("State" to "fragment") to null,
        ("State" to "isFinal") to false,
        ("ParsedState" to "fragment") to null,
        ("ParsedState" to "isFinal") to false,
        ("ActionRule" to "attach") to null,
        ("ParsedActionRule" to "attach") to null,
        ("ClassType" to "behavesAfter") to null,
        ("TypeDefinition" to "behavesAfter") to null
    )

    /** Returns true if [field] of [owner] is one of these fields and holds [value], the value for a text without it. */
    fun isUnused(owner: Any, field: Field, value: Any?): Boolean {
        val key = owner.javaClass.simpleName to field.name
        return key in unusedValues && unusedValues[key] == value
    }
}
