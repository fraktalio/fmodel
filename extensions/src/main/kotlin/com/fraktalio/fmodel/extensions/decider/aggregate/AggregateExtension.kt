/*
 * Copyright (c) 2021 Fraktalio D.O.O. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.fraktalio.fmodel.extensions.decider.aggregate

import arrow.Kind3
import arrow.extension
import com.fraktalio.fmodel.datatypes.*
import com.fraktalio.fmodel.extensions.decider._decider.contravariant.lmapOnC

@extension
interface EventSourcingAggregateExtension : Contravariant<ForEventSourcingAggregate> {

    override fun <Cn, C, S, E> Kind3<ForEventSourcingAggregate, C, S, E>.lmapOnC(f: (Cn) -> C): Kind3<ForEventSourcingAggregate, Cn, S, E> {
        val aggregate = this.fix()
        return EventSourcingAggregate(
            storeEvents = { e -> aggregate.storeEvents(e) },
            fetchEvents = { c -> aggregate.fetchEvents(f(c)) },
            decider = aggregate.decider.lmapOnC(f)
        )
    }

    companion object
}

@extension
interface StateStoredAggregateExtension : Contravariant<ForStateStoredAggregate> {

    override fun <Cn, C, S, E> Kind3<ForStateStoredAggregate, C, S, E>.lmapOnC(f: (Cn) -> C): Kind3<ForStateStoredAggregate, Cn, S, E> {
        val aggregate = this.fix()
        return StateStoredAggregate(
            storeState = { s -> aggregate.storeState(s) },
            fetchState = { c -> aggregate.fetchState(f(c)) },
            decider = aggregate.decider.lmapOnC(f)
        )
    }

    companion object
}
