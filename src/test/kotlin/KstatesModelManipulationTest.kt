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
import dsl.textual.KstatesDSLConverter
import states.actions.ActionOperationType
import states.actions.ActionRule
import states.actions.WithBlock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Tests for the extended model-manipulation operations: the new keywords (and their lowercase aliases),
 * the unary / binary / ternary arities, and the WITH ... AS binding block.
 */
class KstatesModelManipulationTest {

    private fun machineWithDo(doBody: String): String = """
        STATEMACHINE ATTACHED TO "Robot" {
            STATES {
                INITIAL STATE "s" {
                    DO {
                        $doBody
                    }
                }
            }
            TRANSITIONS { }
        }
    """.trimIndent()

    private fun doItemsOf(doBody: String) =
        KstatesDSLConverter.parseKstatesString(machineWithDo(doBody)).states.first().onDo.items

    @Test
    fun `parses all new operations with correct arity`() {
        val items = doItemsOf(
            """
            SET("scalar", "1.0")
            SETLIST("list", MACRO("m"))
            DROPLIST("list")
            SETOBJ("self", "rel", MACRO("m"))
            APPENDOBJ("self", "rel", MACRO("m"))
            DROPOBJ("obj")
            """.trimIndent()
        )
        val rules = items.filterIsInstance<ActionRule>()
        assertEquals(ActionOperationType.SET, rules[0].operationType)

        assertEquals(ActionOperationType.SETLIST, rules[1].operationType)
        assertNull(rules[1].secondSide)

        // DROPLIST is unary: no second operand, no right side.
        assertEquals(ActionOperationType.DROPLIST, rules[2].operationType)
        assertEquals("list", rules[2].leftSide)
        assertNull(rules[2].rightSide)

        // SETOBJ / APPENDOBJ are ternary: source, relation, target.
        assertEquals(ActionOperationType.SETOBJ, rules[3].operationType)
        assertEquals("self", rules[3].leftSide)
        assertEquals("rel", rules[3].secondSide)
        assertTrue(rules[3].rightSide != null)

        assertEquals(ActionOperationType.APPENDOBJ, rules[4].operationType)
        assertEquals("rel", rules[4].secondSide)

        // DROPOBJ is unary.
        assertEquals(ActionOperationType.DROPOBJ, rules[5].operationType)
        assertEquals("obj", rules[5].leftSide)
        assertNull(rules[5].rightSide)
    }

    @Test
    fun `lowercase aliases parse to the same operations`() {
        val items = doItemsOf(
            """
            set("scalar", "1.0")
            append("list", "x")
            setlist("list", MACRO("m"))
            droplist("list")
            setobj("self", "rel", MACRO("m"))
            appendobj("self", "rel", MACRO("m"))
            dropobj("obj")
            """.trimIndent()
        )
        val ops = items.filterIsInstance<ActionRule>().map { it.operationType }
        assertEquals(
            listOf(
                ActionOperationType.SET,
                ActionOperationType.APPEND,
                ActionOperationType.SETLIST,
                ActionOperationType.DROPLIST,
                ActionOperationType.SETOBJ,
                ActionOperationType.APPENDOBJ,
                ActionOperationType.DROPOBJ
            ),
            ops
        )
    }

    @Test
    fun `WITH AS binds a macro result over a body`() {
        val items = doItemsOf(
            """
            WITH MACRO("opposite", "a", "b") AS "dir" {
                SET("direction->x", EVAL { return 1.0 })
                SET("direction->y", EVAL { return 2.0 })
            }
            """.trimIndent()
        )
        val withBlock = items.filterIsInstance<WithBlock>().single()
        assertEquals("dir", withBlock.name)
        assertEquals("opposite", withBlock.macro.macroName)
        assertEquals(listOf("a", "b"), withBlock.macro.args)
        assertEquals(2, withBlock.body.items.size)
    }

    @Test
    fun `wrong arity is rejected`() {
        // DROPOBJ is unary; calling it with a right side is invalid.
        assertFailsWith<IllegalArgumentException> {
            doItemsOf("""DROPOBJ("obj", "extra")""")
        }
        // SETOBJ is ternary; calling it binary is invalid.
        assertFailsWith<IllegalArgumentException> {
            doItemsOf("""SETOBJ("self", MACRO("m"))""")
        }
        // SET is binary; calling it unary is invalid.
        assertFailsWith<IllegalArgumentException> {
            doItemsOf("""SET("scalar")""")
        }
    }
}
