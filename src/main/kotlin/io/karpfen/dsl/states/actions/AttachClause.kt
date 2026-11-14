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
package io.karpfen.dsl.states.actions

/**
 * The `ATTACH` clause of a `SETOBJ` or `APPENDOBJ` rule: it names the attachable state machine that the runtime
 * attaches to the object the rule creates, in place of the one the `behaves after` of its type names.
 *
 * The name is a value in the three forms of a rule: a [ValueActionRightSide] holds the name itself, an
 * [EvalActionRightSide] or a [MacroActionRightSide] returns it as a string when the rule runs. The clause is a class
 * of its own, not a bare right side, so that a source map can tell where the whole clause, keyword included, is
 * written.
 *
 * @property stateMachineName The value that yields the name of the attachable state machine.
 */
data class AttachClause(val stateMachineName: ActionRightSide)
