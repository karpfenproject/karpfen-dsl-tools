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
package io.karpfen.dsl.dsl.textual

import io.karpfen.dsl.instance.SimpleAtomicPropertyObject
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Converts the example model file against the example metamodel file.
 */
class KmodelParserIntegrationTest {

    /**
     * The root room holds its relations, the robot and both obstacles.
     */
    @Test
    fun testKmodelParserIntegration() {
        val metamodelFilePath = "example/metamodel_dsl_example.kmeta"
        val metamodel = KmetaDSLConverter.parseKmetaFile(metamodelFilePath)

        val modelFilePath = "example/model_dsl_example.kmodel"
        val model = KmodelDSLConverter.parseKmodelFile(modelFilePath, metamodel)

        assertEquals(1, model.objects.size)

        val rootObject = model.objects.first()
        assertEquals("APB 2101", rootObject.id)
        assertEquals("Room", rootObject.ofType.name)
        assertEquals(6, rootObject.relations.size)

        val robot = rootObject.getRel("robot").first()
        assertEquals("turtle", robot.id)
        assertEquals("Robot", robot.ofType.name)
        assertEquals(1, robot.properties.size)
        assertEquals("speed", robot.properties.first().key)
        assertEquals(1.0, (robot.properties.first() as SimpleAtomicPropertyObject).getAsNumber())
        assertEquals(2, robot.relations.size)
        assertEquals("chair", rootObject.getRel("obstacles").first().id)
        assertEquals("table", rootObject.getRel("obstacles").last().id)
    }
}
