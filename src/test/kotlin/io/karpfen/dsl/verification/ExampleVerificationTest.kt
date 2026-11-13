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

import io.karpfen.dsl.diagnostics.Diagnostic
import io.karpfen.dsl.reader.kmeta.KmetaReader
import io.karpfen.dsl.reader.kmodel.KmodelReader
import io.karpfen.dsl.reader.kstates.KstatesReader
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Verifies every example project, each state machine attached to the object its model runs it on, and pins what the
 * verification reports: no error at all, and exactly the warnings listed here, each checked by hand.
 */
class ExampleVerificationTest {

    /** Every example project reads without errors and the verification reports no error in it. */
    @Test
    fun `the example projects have no errors`() {
        for (project in projects) {
            val findings = project.verify()
            assertTrue(findings.errors.isEmpty(), "$project:\n" + findings.errors.joinToString("\n") { describe(it) })
        }
    }

    /** The verification warns exactly about the places listed for each example project. */
    @Test
    fun `the example projects produce exactly the expected warnings`() {
        for (project in projects) {
            val warnings = project.verify().warnings.map { describe(it) }
            assertEquals(project.expectedWarnings, warnings, project.toString())
        }
    }

    /** Returns [diagnostic] as its code, its line and its message. */
    private fun describe(diagnostic: Diagnostic): String =
        "${diagnostic.code.id} line ${diagnostic.range.start.line}: ${diagnostic.message}"

    /**
     * An example project: the files of one folder and the object each state machine runs on.
     *
     * @param folder The folder of the project, relative to the project folder the tests run in.
     * @param attachments The object id for each state machine file of the folder, by file name.
     * @param expectedWarnings The warnings the verification reports, as [describe] writes them.
     */
    private class ExampleProject(
        private val folder: String,
        private val attachments: Map<String, String>,
        val expectedWarnings: List<String> = emptyList()
    ) {

        /** Reads the files of the folder and verifies them. */
        fun verify(): ProjectFindings {
            val files = File(folder).listFiles().orEmpty().sortedBy { it.name }
            val domainFile = files.single { it.extension == "kmeta" && it.name != EVENTS_FILE }
            val metamodel = KmetaReader().read(domainFile.readText())
            assertTrue(!metamodel.hasErrors, "$domainFile: ${metamodel.diagnostics}")
            val domainTypes = metamodel.model!!.types
            val events = files.singleOrNull { it.name == EVENTS_FILE }?.let { KmetaReader(domainTypes).read(it.readText()) }
            assertTrue(events?.hasErrors != true, "$folder events: ${events?.diagnostics}")
            val model = KmodelReader(metamodel.model).read(files.single { it.extension == "kmodel" }.readText())
            assertTrue(!model.hasErrors, "$folder model: ${model.diagnostics}")
            val machines = files.filter { it.extension == "kstates" }.map { file ->
                val read = KstatesReader().read(file.readText())
                assertTrue(read.diagnostics.isEmpty(), "$file: ${read.diagnostics}")
                StateMachineInput(read, attachments.getValue(file.name))
            }
            return ProjectVerifier().verify(ProjectInput(metamodel, events, model, machines))
        }

        override fun toString(): String = folder
    }

    private companion object {

        /** The name of the event metamodel of a project. */
        const val EVENTS_FILE = "EVENTS.kmeta"

        /** The folder of the copies of the runtime examples. */
        const val RUNTIME_EXAMPLES = "src/test/resources/examples/karpfen-runtime"

        /** Every example project with a state machine. */
        val projects = listOf(
            ExampleProject("example/statemachine_full_example", mapOf("cleaning_robot.kstates" to "turtle")),
            ExampleProject("example/statemachine_state_options_example", mapOf("crossing.kstates" to "crossing")),
            ExampleProject("$RUNTIME_EXAMPLES/autonomous_car_example", mapOf("autonomous_car.kstates" to "av-1")),
            ExampleProject("$RUNTIME_EXAMPLES/delivery_drone_example", mapOf("delivery_drone.kstates" to "scout")),
            ExampleProject("$RUNTIME_EXAMPLES/drone_swarm_example", mapOf("drone_swarm.kstates" to "alpha-swarm")),
            ExampleProject("$RUNTIME_EXAMPLES/single_robot_example", mapOf("cleaning_robot.kstates" to "turtle"))
        )
    }
}
