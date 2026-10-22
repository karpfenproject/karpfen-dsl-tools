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
package io.karpfen.dsl.parser

import org.slf4j.Marker
import org.slf4j.event.Level
import org.slf4j.helpers.LegacyAbstractLogger
import org.slf4j.helpers.MessageFormatter

/**
 * An SLF4J logger with every level enabled that records the formatted messages, so tests can assert what
 * code logs without a logging backend.
 */
class RecordingLogger : LegacyAbstractLogger() {

    /**
     * One logged message.
     *
     * @property level The level it was logged at.
     * @property message The message with its arguments filled in.
     */
    data class Record(val level: Level, val message: String)

    /** The recorded messages, in logging order. */
    val records = mutableListOf<Record>()

    override fun isTraceEnabled(): Boolean = true

    override fun isDebugEnabled(): Boolean = true

    override fun isInfoEnabled(): Boolean = true

    override fun isWarnEnabled(): Boolean = true

    override fun isErrorEnabled(): Boolean = true

    override fun getFullyQualifiedCallerName(): String? = null

    override fun handleNormalizedLoggingCall(
        level: Level,
        marker: Marker?,
        messagePattern: String?,
        arguments: Array<out Any?>?,
        throwable: Throwable?
    ) {
        records.add(Record(level, MessageFormatter.basicArrayFormat(messagePattern, arguments)))
    }
}
