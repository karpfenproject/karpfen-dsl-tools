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

import io.karpfen.dsl.grammar.kmodel.KmodelParser
import io.karpfen.dsl.reader.presentToken
import org.antlr.v4.runtime.tree.ErrorNode
import java.util.Collections
import java.util.IdentityHashMap

/**
 * Tells for each `make object` block of a KModel parse tree whether the statements the parser gave it are surely
 * those written for its object.
 *
 * A syntax error in the braces hands statements to the wrong block. A block that is not closed takes in the statements
 * that follow it in the blocks around it, and ends with the closing brace of one of them, so that a block further out
 * is left without its closing brace. The blocks that may have taken in statements are then the object that the last
 * statement of the block without a closing brace embeds, the object the last statement of that object embeds, and so
 * on. A closing brace left over after the root object means that a block ended too early and handed its remaining
 * statements to the blocks around it, which may be any of them. Statements in the wrong block are no problems of the
 * object they landed in, so the reader does not report them; the syntax error is reported instead.
 *
 * @param doubtfulBlocks The blocks whose statements may belong to other objects, by identity.
 * @param allDoubtful True if the statements of every block may belong to other objects.
 */
internal class StatementOwnership private constructor(
    private val doubtfulBlocks: Set<KmodelParser.Make_object_blockContext>,
    private val allDoubtful: Boolean
) {

    /**
     * Returns true if the statements of [block] are surely those written for its object.
     */
    fun ownsItsStatements(block: KmodelParser.Make_object_blockContext): Boolean =
        !allDoubtful && block !in doubtfulBlocks

    /** Finds the blocks whose statements may belong to other objects. */
    companion object {

        /**
         * Returns the ownership of the statements of the blocks of [file], see [StatementOwnership].
         */
        fun of(file: KmodelParser.Kmodel_fileContext): StatementOwnership {
            if (hasLeftoverInput(file)) {
                return StatementOwnership(emptySet(), allDoubtful = true)
            }
            val doubtful = Collections.newSetFromMap(IdentityHashMap<KmodelParser.Make_object_blockContext, Boolean>())
            val pending = ArrayDeque(listOfNotNull(file.make_object_block()))
            while (pending.isNotEmpty()) {
                val block = pending.removeLast()
                if (block.presentToken(KmodelParser.RBRACE) == null) {
                    doubtful.addAll(lastEmbeddedBlocksBelow(block))
                }
                pending.addAll(embeddedBlocksOf(block))
            }
            return StatementOwnership(doubtful, allDoubtful = false)
        }

        /**
         * Returns true if the parser met input after the root object that it skipped or could not match, such as a
         * closing brace left over.
         */
        private fun hasLeftoverInput(file: KmodelParser.Kmodel_fileContext): Boolean =
            file.exception != null || file.children.orEmpty().any { it is ErrorNode }

        /**
         * Returns the object blocks that the last statement of [block] embeds, the one that the last statement of that
         * block embeds, and so on, outermost first.
         */
        private fun lastEmbeddedBlocksBelow(block: KmodelParser.Make_object_blockContext): List<KmodelParser.Make_object_blockContext> =
            generateSequence(lastEmbeddedBlockOf(block)) { lastEmbeddedBlockOf(it) }.toList()

        /**
         * Returns the object block that the last statement of [block] embeds, or null if that statement embeds none.
         */
        private fun lastEmbeddedBlockOf(block: KmodelParser.Make_object_blockContext): KmodelParser.Make_object_blockContext? =
            block.statement().lastOrNull()?.has_statement()?.make_object_block()

        /**
         * Returns the object blocks that the statements of [block] embed.
         */
        private fun embeddedBlocksOf(block: KmodelParser.Make_object_blockContext): List<KmodelParser.Make_object_blockContext> =
            block.statement().mapNotNull { it.has_statement()?.make_object_block() }
    }
}
