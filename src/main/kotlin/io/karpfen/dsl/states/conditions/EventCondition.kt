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
package io.karpfen.dsl.states.conditions

/**
 * Represents an event-based condition for state transitions.
 *
 * Triggers when a specific event is received in a given domain.
 *
 * @property eventDomain The domain or source of the event.
 * @property eventValue The specific event value to match.
 */
data class EventCondition(val eventDomain: String, val eventValue: String) :
    Condition(ConditionType.EVENT) {
    override fun toString(): String =
        "EventCondition(eventDomain=$eventDomain, eventValue=$eventValue)"
}
