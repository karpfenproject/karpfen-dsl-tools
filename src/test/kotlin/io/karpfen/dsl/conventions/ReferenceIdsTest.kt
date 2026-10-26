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
package io.karpfen.dsl.conventions

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Covers [ReferenceIds], the recognizer of reference ids behind the documentation style rules: it finds every kind of
 * id it lists and leaves ordinary prose alone, including words and codes that resemble an id.
 */
class ReferenceIdsTest {

    /**
     * Returns the texts of the reference ids in [text].
     */
    private fun idsIn(text: String): List<String> = ReferenceIds.findIn(text).map { it.text }

    /**
     * Section signs, decisions, work packages, decision records and sections of a specification are found.
     */
    @Test
    fun `finds section signs, decisions, work packages, ADRs and spec sections`() {
        val text = "See §3.4, § 7, decision 53, Decisions #12, WP-PARSE, ADR 0003, ADR-7, " +
            "spec section 3.1 and Specification Section 4."
        assertEquals(
            listOf(
                "§3.4", "§ 7", "decision 53", "Decisions #12", "WP-PARSE", "ADR 0003", "ADR-7", "spec section 3.1",
                "Specification Section 4"
            ),
            idsIn(text)
        )
    }

    /**
     * Review rounds and the keys of review findings are found.
     */
    @Test
    fun `finds review rounds and the keys of review findings`() {
        assertEquals(listOf("review round 2, \"name-range-quotes\""), idsIn("(review round 2, \"name-range-quotes\")"))
        assertEquals(listOf("review finding printer-deep-nesting"), idsIn("fixes the review finding printer-deep-nesting"))
        assertEquals(listOf("review: `kdoc-missing`"), idsIn("(review: `kdoc-missing`)"))
    }

    /**
     * Short keys are found where the text uses them as references.
     */
    @Test
    fun `finds short keys where the text uses them as references`() {
        assertEquals(listOf("phase P2", "finding E7"), idsIn("See phase P2 and finding E7."))
        assertEquals(listOf("Decisions D1, D2 or D3"), idsIn("Decisions D1, D2 or D3 hold."))
        assertEquals(listOf("(D2)", "(P1, P2)", "D8"), idsIn("as decided (D2), done in (P1, P2); D8: the printer"))
        assertEquals(listOf("rule 7 of the work-package plan", "record 0010"), idsIn("rule 7 of the work-package plan, record 0010"))
    }

    /**
     * Ordinary prose is no reference id.
     */
    @Test
    fun `ordinary prose is no reference id`() {
        listOf(
            "the decision that the readers never throw",
            "a section of the guide, read 3 times",
            "the color 0xFFD1E0, the key Ctrl+D and 2D1 coordinates",
            "a WP- prefix alone, ADR without a number, the § sign alone",
            "D3.js and the D2 diagram language",
            "the diagnostics `reader-failure` and \"unknown-property\" are codes",
            "One step 2 of the algorithm; the steps 1 to 3",
            "the parser records 3 errors, a record of 2 entries"
        ).forEach { prose -> assertEquals(emptyList(), idsIn(prose), prose) }
    }
}
