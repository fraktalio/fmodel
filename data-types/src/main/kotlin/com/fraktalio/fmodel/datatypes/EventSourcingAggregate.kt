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

package com.fraktalio.fmodel.datatypes

import arrow.core.Either
import arrow.core.computations.either

/**
 * Event sourcing aggregate is using/delegating a [Decider] to handle commands and produce events.
 * In order to handle the command, aggregate needs to fetch the current state (represented as a list of events) via [fetchEvents] function, and then delegate the command to the decider which can produce event(s) as a result.
 * Produced events are then stored via [storeEvents] suspending function.
 * It is the responsibility of the user to implement these functions [fetchEvents] and [storeEvents] per need.
 * These two functions are producing side effects (infrastructure), and they are deliberately separated from the decider (pure domain logic).
 *
 * @param C Commands of type [C] that this aggregate can handle
 * @param S Aggregate state of type [S]
 * @param E Events of type [E] that this aggregate can publish
 * @property decider A decider component of type [Decider]<[C], [S], [E]>.
 * @property storeEvents A suspending function that takes the newly produced events by [Decider] and stores them (produces side effect by modifying object/data outside its own scope) by resulting with [either] error [Error.StoringEventsFailed] or success [Success.EventsStoredSuccessfully]
 * @property fetchEvents A suspending function that takes the command of type [C] and results with [either] error [Error.FetchingEventsFailed] or success [Iterable]<[E]>
 * @constructor Creates [EventSourcingAggregate]
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
data class EventSourcingAggregate<C, S, E>(
    val decider: Decider<C, S, E>,
    val storeEvents: suspend (Iterable<E>) -> Either<Error.StoringEventsFailed<E>, Success.EventsStoredSuccessfully<E>>,
    val fetchEvents: suspend (C) -> Either<Error.FetchingEventsFailed, Iterable<E>>
) {

    /**
     * Handles the command message of type [C]
     *
     * @param command Command message of type [C]
     * @return Either [Error] or [Success]
     */
    suspend fun handle(command: C): Either<Error, Success> =
        // Arrow provides a Monad instance for Either. Except for the types signatures, our program remains unchanged when we compute over Either. All values on the left side assume to be Right biased and, whenever a Left value is found, the computation short-circuits, producing a result that is compatible with the function type signature.
        either {
            val events = fetchEvents(command).bind()
            val state: S = validate(events.fold(decider.initialState, decider.evolve)).bind()
            val newEvents = decider.decide(command, state)
            storeEvents(newEvents).bind()
        }

    private fun validate(state: S): Either<Error, S> =
        if (decider.isTerminal(state)) Either.Left(Error.AggregateIsInTerminalState(state))
        else Either.Right(state)

    /**
     * Left map over C (Command) - Contravariant
     *
     * @param Cn Command new
     * @param f
     */
    inline fun <Cn> lmapOnC(crossinline f: (Cn) -> C): EventSourcingAggregate<Cn, S, E> =
        EventSourcingAggregate(
            storeEvents = { e -> this.storeEvents(e) },
            fetchEvents = { c -> this.fetchEvents(f(c)) },
            decider = this.decider.lmapOnC(f)
        )
}
