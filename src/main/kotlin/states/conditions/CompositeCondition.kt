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
package states.conditions

/**
 * A condition made up of several clauses that are evaluated in order and must all hold.
 *
 * This is how a CONDITION block with more than one entry is represented. The clauses are evaluated
 * top to bottom and short-circuit on the first one that is false. At most one of the clauses is an
 * [EventCondition], and when present it is the first clause so that the matched event is in scope for
 * the guard clauses that follow it.
 *
 * @property clauses The ordered list of clauses; all must be true for the condition to hold.
 */
class CompositeCondition(val clauses: List<Condition>) :
    Condition(ConditionType.COMPOSITE) {
    override fun toString(): String =
        "CompositeCondition(clauses=${clauses.map { it.conditionType }})"
}
