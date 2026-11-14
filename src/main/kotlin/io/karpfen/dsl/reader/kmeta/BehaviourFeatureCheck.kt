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
package io.karpfen.dsl.reader.kmeta

import io.karpfen.dsl.diagnostics.DiagnosticCode
import io.karpfen.dsl.grammar.kmeta.KmetaParser
import io.karpfen.dsl.reader.ReadingSession
import io.karpfen.dsl.reader.kstates.FeatureCheck
import io.karpfen.dsl.reader.presentToken
import io.karpfen.dsl.states.KstatesFeature

/**
 * Reports every `behaves after` clause of a KMeta text at its keyword `behaves` while [KstatesFeature.ATTACHMENT] is
 * not among [switchedOn], with the message the KStates reader gives the constructs of a feature that is off. A clause
 * whose keyword the text does not contain is not reported.
 *
 * The check only reports; the types keep their clause.
 *
 * @param session The read the type definitions belong to; it receives the problems.
 * @param switchedOn The features that may be used.
 */
internal class BehaviourFeatureCheck(private val session: ReadingSession, private val switchedOn: Set<KstatesFeature>) {

    /** Checks the clauses of [definitions], in text order. */
    fun check(definitions: List<KmetaParser.Type_definitionContext>) {
        if (KstatesFeature.ATTACHMENT in switchedOn) return
        for (definition in definitions) {
            val keyword = definition.behaviour_clause()?.presentToken(KmetaParser.BEHAVES) ?: continue
            session.report(
                DiagnosticCode.KMETA_FEATURE_OFF,
                session.locator.rangeOf(keyword),
                FeatureCheck.messageOf(KstatesFeature.ATTACHMENT)
            )
        }
    }
}
