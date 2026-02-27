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
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ConstructorIntegrationTests {

    @Test
    fun testMetamodelModelConstruction() {
        val metamodel = MetamodelConstructorExample().setupRobotRoomMetamodel()
        val model = ModelConstructorExample().setupRobotRoomModel(metamodel)

        assertTrue(metamodel.getClassTypes().contains("Point"))
        assertTrue(metamodel.getClassTypes().contains("TwoDObject"))
        assertTrue(metamodel.getClassTypes().contains("Obstacle"))
        assertTrue(metamodel.getClassTypes().contains("Robot"))
        assertTrue(metamodel.getClassTypes().contains("Room"))

        assertTrue(model.get("Room", "APB 2101") != null)
        assertEquals(model.get("Robot", "turtle")!!.getProp("speed")[0], 1.0)
        assertEquals(model.get("Obstacle", "table")!!.getRel("boundingBox")[0]
                .getRel("position")[0].getProp("x")[0], 5.0)
    }

}