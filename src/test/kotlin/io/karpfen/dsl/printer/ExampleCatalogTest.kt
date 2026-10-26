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

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Guards the printer tests against running on fewer examples than there are.
 */
class ExampleCatalogTest {

    private val catalog = ExampleCatalog()

    /**
     * The catalog finds the five examples of this repository and the sixteen copied runtime examples.
     */
    @Test
    fun `the catalog finds every example`() {
        assertEquals(21, catalog.allFiles().size)
        assertEquals(10, catalog.metamodels().size)
        assertEquals(6, catalog.models().size)
        assertEquals(5, catalog.stateMachines().size)
    }

    /**
     * Event metamodels are read with the types of their domain metamodel as base types.
     */
    @Test
    fun `event metamodels come with base types`() {
        val events = catalog.metamodels().filter { it.file.name == "EVENTS.kmeta" }
        assertEquals(4, events.size)
        events.forEach { assertTrue(it.baseTypes.isNotEmpty(), it.toString()) }
    }
}
