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

import io.karpfen.dsl.instance.ClassTypePropertyObject
import io.karpfen.dsl.instance.ObjectReference
import io.karpfen.dsl.source.SourceRange

/**
 * A `knows` reference that waits for the object it names, which may be defined later in the text.
 *
 * @property reference The reference in the model, which names the object by its id.
 * @property relation The `knows` relation of the owner the reference belongs to.
 * @property ownerKey The key of the object that owns [relation].
 * @property targetRange Where the id is written.
 */
internal class PendingLink(
    val reference: ObjectReference,
    val relation: ClassTypePropertyObject,
    val ownerKey: String,
    val targetRange: SourceRange
)

/**
 * The [PendingLink]s of one read, in text order.
 */
internal class PendingLinks {

    private val links = mutableListOf<PendingLink>()

    /** Every link added so far. */
    val all: List<PendingLink>
        get() = links.toList()

    /**
     * Adds [link].
     */
    fun add(link: PendingLink) {
        links.add(link)
    }
}
