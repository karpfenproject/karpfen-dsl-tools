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
import dsl.functional.ModelBuilder
import instance.Model
import meta.Metamodel

class ModelConstructorExample {

    fun setupRobotRoomModel(metamodel: Metamodel): Model {
        val m = ModelBuilder(metamodel)

        val obstacleA = m.makeObject("Obstacle", "chair").apply {
            assignRels(
                mapOf("boundingBox" to m.makeObject("TwoDObject").apply {
                    assignProps(mapOf("diameter" to 1.0))
                    assignRels(
                        mapOf("position" to m.makeObject("Point").apply {
                            assignProps(mapOf("x" to 2.0, "y" to 3.0))}))}))}


        val obstacleB = m.makeObject("Obstacle", "table").apply {
            assignRels(
                mapOf("boundingBox" to m.makeObject("TwoDObject").apply {
                    assignProps(mapOf("diameter" to 3.0))
                    assignRels(
                        mapOf("position" to m.makeObject("Point").apply {
                            assignProps(mapOf("x" to 5.0, "y" to 7.0))}))}))}


        val robot = m.makeObject("Robot", "turtle").apply {
            assignProps(mapOf("speed" to 1.0))
            assignRels(
                mapOf("boundingBox" to m.makeObject("TwoDObject").apply {
                    assignProps(mapOf("diameter" to 1.0))
                    assignRels(
                        mapOf(
                            "position" to m.makeObject("Point").apply {
                                assignProps(mapOf("x" to 5.0, "y" to 5.0))
                            },
                            "obstacles" to listOf(obstacleA, obstacleB)))}))}


        val room = m.makeObject("Room", "APB 2101").apply {
            assignRels(
                mapOf(
                    "topLeftCorner" to m.makeObject("Point").apply {
                        assignProps(mapOf("x" to 0.0, "y" to 10.0))
                    },
                    "topRightCorner" to m.makeObject("Point").apply {
                        assignProps(mapOf("x" to 10.0, "y" to 10.0))
                    },
                    "bottomLeftCorner" to m.makeObject("Point").apply {
                        assignProps(mapOf("x" to 0.0, "y" to 0.0))
                    },
                    "bottomRightCorner" to m.makeObject("Point").apply {
                        assignProps(mapOf("x" to 10.0, "y" to 0.0))
                    },
                    "robot" to robot,
                    "obstacles" to listOf(obstacleA, obstacleB)))}

        return m.build()
    }

}