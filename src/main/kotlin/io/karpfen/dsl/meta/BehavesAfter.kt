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
package io.karpfen.dsl.meta

/**
 * The clause `behaves after "name"` of a KMeta type: every object of the type gets an instance of the attachable
 * state machine of that name when it comes into being, at the start of a run or when a rule creates it.
 *
 * The clause is a class of its own, not a bare string on the type, so that a source map can tell where the clause and
 * the name in it are written.
 *
 * @property stateMachineName The name of the attachable state machine.
 */
data class BehavesAfter(val stateMachineName: String)
