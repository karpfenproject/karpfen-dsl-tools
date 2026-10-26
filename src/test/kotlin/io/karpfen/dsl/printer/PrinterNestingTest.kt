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
package io.karpfen.dsl.printer

import io.karpfen.dsl.dsl.textual.KmetaDSLConverter
import io.karpfen.dsl.instance.ClassTypeAtomicPropertyObject
import io.karpfen.dsl.instance.DataObject
import io.karpfen.dsl.instance.Model
import io.karpfen.dsl.instance.ObjectReference
import io.karpfen.dsl.printer.kmodel.KmodelPrinter
import io.karpfen.dsl.printer.kstates.KstatesPrinter
import io.karpfen.dsl.reader.TextReader
import io.karpfen.dsl.reader.kmodel.KmodelReader
import io.karpfen.dsl.reader.kstates.KstatesReader
import io.karpfen.dsl.states.State
import io.karpfen.dsl.states.StateMachine
import io.karpfen.dsl.states.actions.ActionBlock
import io.karpfen.dsl.states.actions.ActionItem
import io.karpfen.dsl.states.actions.ActionOperationType
import io.karpfen.dsl.states.actions.ActionRule
import io.karpfen.dsl.states.actions.InScopeBlock
import io.karpfen.dsl.states.actions.ValueActionRightSide
import kotlin.test.Test
import kotlin.test.assertFailsWith

/**
 * Tests that the printers reject an element that nests deeper than they can recurse with an
 * [IllegalArgumentException], as they reject every element they cannot write, instead of letting a
 * [StackOverflowError] escape to callers that only catch exceptions.
 *
 * The depth at which the stack overflows depends on the stack of the thread and on the state of the compiler, so the
 * tests nest far deeper than any stack holds, and read texts of growing depth: whatever a reader reads without errors
 * prints or is rejected, at any depth.
 */
class PrinterNestingTest {

    /**
     * States nested in states.
     */
    @Test
    fun `deeply nested states are rejected`() {
        val outermost = nestedStates(FAR_TOO_DEEP)
        val printer = KstatesPrinter()

        assertFailsWith<IllegalArgumentException> { printer.print(StateMachine("R", mutableListOf(outermost), mutableListOf(), mutableListOf())) }
        assertFailsWith<IllegalArgumentException> { printer.printState(outermost) }
    }

    /**
     * `IF IN SCOPE` blocks nested in `IF IN SCOPE` blocks.
     */
    @Test
    fun `deeply nested actions are rejected`() {
        val rule = ActionRule(ActionOperationType.SET, "x", ValueActionRightSide("1"))
        val outermost = (1..FAR_TOO_DEEP).fold<Int, ActionItem>(rule) { inner, _ ->
            InScopeBlock(listOf("a"), ActionBlock(mutableListOf(inner)))
        }
        val state = State("s", ActionBlock(mutableListOf()), ActionBlock(mutableListOf(outermost)), mutableListOf(), null, true)

        assertFailsWith<IllegalArgumentException> { KstatesPrinter().printState(state) }
    }

    /**
     * Objects embedded in objects.
     */
    @Test
    fun `deeply nested objects are rejected`() {
        val nodeType = nodeMetamodel.rootClass
        val outermost = (1..FAR_TOO_DEEP).fold(DataObject(nodeType, "n0", mutableListOf(), mutableListOf())) { inner, depth ->
            val child = ClassTypeAtomicPropertyObject(nodeType.getObjectProperty("child"), "child", ObjectReference(inner.id, inner))
            DataObject(nodeType, "n$depth", mutableListOf(), mutableListOf(child))
        }
        val printer = KmodelPrinter()

        assertFailsWith<IllegalArgumentException> { printer.print(Model(mutableListOf(outermost), nodeMetamodel)) }
        assertFailsWith<IllegalArgumentException> { printer.printObject(outermost) }
    }

    /**
     * Texts nested ever deeper, up to depths no reader reads, either read with errors, or print, or are rejected by
     * the printer.
     */
    @Test
    fun `whatever a reader reads without errors prints or is rejected`() {
        val kstatesPrinter = KstatesPrinter()
        val kmodelPrinter = KmodelPrinter()
        READ_DEPTHS.forEach { depth ->
            printOrReject(KstatesReader(), nestedStatesText(depth)) { kstatesPrinter.print(it) }
            printOrReject(KmodelReader(nodeMetamodel), nestedObjectsText(depth)) { kmodelPrinter.print(it) }
        }
    }

    /**
     * Reads [text] and, if it reads without errors, prints the model with [print], which may only throw an
     * [IllegalArgumentException].
     */
    private fun <M : Any> printOrReject(reader: TextReader<M>, text: String, print: (M) -> String) {
        val model = reader.read(text).takeUnless { it.hasErrors }?.model ?: return
        try {
            print(model)
        } catch (rejected: IllegalArgumentException) {
            return
        }
    }

    /**
     * Returns the outermost of [depth] states nested in each other.
     */
    private fun nestedStates(depth: Int): State {
        val outermost = State("s0", ActionBlock(mutableListOf()), ActionBlock(mutableListOf()), mutableListOf(), null, true)
        (1..depth).fold(outermost) { outer, level ->
            State("s$level", ActionBlock(mutableListOf()), ActionBlock(mutableListOf()), mutableListOf(), outer, false)
                .also { outer.innerStates.add(it) }
        }
        return outermost
    }

    /**
     * Returns a state machine whose initial state holds [depth] states nested in each other.
     */
    private fun nestedStatesText(depth: Int): String =
        "STATEMACHINE ATTACHED TO \"R\" { STATES { INITIAL STATE \"s\" { " +
            (1..depth).joinToString(" ") { "STATE \"s$it\" {" } + " }".repeat(depth) + " } } TRANSITIONS { } }"

    /**
     * Returns a node that embeds [depth] nodes nested in each other.
     */
    private fun nestedObjectsText(depth: Int): String =
        "make object \"n\":\"Node\" { " + (1..depth).joinToString(" ") { "has(\"child\") -> make object \"n$it\":\"Node\" {" } +
            " }".repeat(depth) + " }"

    /** The metamodel of nodes that embed a node. */
    private val nodeMetamodel = KmetaDSLConverter.parseKmetaString("type \"Node\" \"a node\" { has(\"child\", \"Node\") }")

    private companion object {

        /** A depth of nesting that overflows any stack the printers run on. */
        const val FAR_TOO_DEEP = 200_000

        /** The depths of nesting of the texts read, beyond the depth at which the readers fail. */
        val READ_DEPTHS = listOf(500, 1_000, 1_500, 2_000, 3_000, 4_000)
    }
}
