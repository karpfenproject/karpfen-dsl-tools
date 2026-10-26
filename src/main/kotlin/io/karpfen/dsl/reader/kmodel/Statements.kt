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
package io.karpfen.dsl.reader.kmodel

import io.karpfen.dsl.instance.DataObject
import io.karpfen.dsl.meta.ClassTypeProperty
import io.karpfen.dsl.meta.SimpleProperty
import org.antlr.v4.runtime.ParserRuleContext
import org.antlr.v4.runtime.tree.TerminalNode

/**
 * A `prop` statement whose key the class type of its object defines.
 *
 * @property context The statement in the parse tree.
 * @property property The property the key names.
 * @property keyToken The quoted key.
 * @property valueToken The quoted value.
 */
internal class PropStatement(
    val context: ParserRuleContext,
    val property: SimpleProperty,
    val keyToken: TerminalNode,
    val valueToken: TerminalNode
) {

    /** The key without quotes. */
    val key: String
        get() = property.key

    /** The value without quotes, the way the throwing converter unquotes it. */
    val value: String
        get() = valueToken.text.trim('"', '\'')
}

/**
 * A `has` statement whose key names an embedding relation of the class type of its object.
 *
 * @property context The statement in the parse tree.
 * @property relation The relation the key names.
 * @property keyToken The quoted key.
 * @property child The embedded object.
 */
internal class HasStatement(
    val context: ParserRuleContext,
    val relation: ClassTypeProperty,
    val keyToken: TerminalNode,
    val child: DataObject
) {

    /** The key without quotes. */
    val key: String
        get() = relation.key
}

/**
 * A `knows` statement whose key names a linking relation of the class type of its object.
 *
 * @property context The statement in the parse tree.
 * @property relation The relation the key names.
 * @property keyToken The quoted key.
 * @property targetToken The quoted id of the object referred to.
 */
internal class KnowsStatement(
    val context: ParserRuleContext,
    val relation: ClassTypeProperty,
    val keyToken: TerminalNode,
    val targetToken: TerminalNode
) {

    /** The key without quotes. */
    val key: String
        get() = relation.key

    /** The id of the object referred to, without quotes, the way the throwing converter unquotes it. */
    val targetId: String
        get() = targetToken.text.trim('"', '\'')
}
