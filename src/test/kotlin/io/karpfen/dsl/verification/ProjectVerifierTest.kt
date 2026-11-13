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
package io.karpfen.dsl.verification

import io.karpfen.dsl.diagnostics.DiagnosticCode.UNKNOWN_PATH
import io.karpfen.dsl.diagnostics.DiagnosticCode.UNREACHABLE_STATE
import io.karpfen.dsl.reader.kmeta.KmetaReader
import io.karpfen.dsl.reader.kstates.KstatesReader
import io.karpfen.dsl.verification.VerifiedProject.Companion.machine
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

/**
 * Tests what the verification of a project leaves out when a text cannot be trusted, and how its findings are handed
 * out per state machine.
 */
class ProjectVerifierTest {

    /** A machine with a path that leads nowhere and a state that can never be active. */
    private val faulty = machine(
        "INITIAL STATE \"a\" { DO { SET(\"nothing\", \"1\") } }\nSTATE \"b\" { }"
    )

    /** Both problems of [faulty] are found in a sound project. */
    @Test
    fun `a sound project is checked in full`() {
        assertEquals(listOf(UNKNOWN_PATH, UNREACHABLE_STATE), VerifiedProject(faulty).codes)
    }

    /** A machine with a syntax error is not checked at all, since the parser may have lost parts of it. */
    @Test
    fun `a state machine with a syntax error is not checked`() {
        VerifiedProject(faulty.replace("STATE \"b\" { }", "STATE \"b\" { ")).assertClean()
    }

    /** A domain metamodel with errors leaves out the checks of types, but not those of the states. */
    @Test
    fun `a metamodel with errors leaves out the checks of types`() {
        val broken = VerifiedProject.METAMODEL.replace("prop(\"name\", \"string\")", "prop(\"name\", \"text\")")

        assertEquals(listOf(UNREACHABLE_STATE), VerifiedProject(faulty, metamodel = broken).codes)
    }

    /** Repeated state names leave out the checks of states, but not those of types. */
    @Test
    fun `repeated state names leave out the checks of states`() {
        val repeated = faulty.replace("STATE \"b\" { }", "STATE \"b\" { }\nSTATE \"b\" { }")

        assertEquals(listOf(UNKNOWN_PATH), VerifiedProject(repeated).codes)
    }

    /** The findings are handed out per state machine, and only for the machines verified. */
    @Test
    fun `findings are handed out per state machine`() {
        val metamodel = KmetaReader().read(VerifiedProject.METAMODEL)
        val first = StateMachineInput(KstatesReader().read(faulty))
        val second = StateMachineInput(KstatesReader().read(machine("INITIAL STATE \"a\" { }")))
        val findings = ProjectVerifier().verify(ProjectInput(metamodel, stateMachines = listOf(first, second)))

        assertEquals(listOf(UNKNOWN_PATH, UNREACHABLE_STATE), findings.of(first).map { it.code })
        assertEquals(emptyList(), findings.of(second))
        assertEquals(listOf(UNKNOWN_PATH), findings.errors.map { it.code })
        assertEquals(listOf(UNREACHABLE_STATE), findings.warnings.map { it.code })
        assertFailsWith<IllegalArgumentException> { findings.of(StateMachineInput(first.read)) }
    }
}
