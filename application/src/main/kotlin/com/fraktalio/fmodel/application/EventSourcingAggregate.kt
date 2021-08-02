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

package com.fraktalio.fmodel.application

import arrow.core.Either
import arrow.core.computations.either
import com.fraktalio.fmodel.domain.Decider
import com.fraktalio.fmodel.domain.Saga

/**
 *
 * Event sourcing aggregate is using/delegating a [EventSourcingAggregate.decider] of type [Decider]<[C], [S], [E]> to handle commands and produce events.
 * In order to handle the command, aggregate needs to fetch the current state (represented as a list of events) via [EventRepository.fetchEvents] function, and then delegate the command to the [EventSourcingAggregate.decider] which can produce new event(s) as a result.
 * If the [EventSourcingAggregate.decider] is combined out of many deciders via `combine` function, an optional [EventSourcingAggregate.saga] could be used to react on new events and send new commands to the [EventSourcingAggregate.decider] recursively, in one transaction.
 * Produced events are then stored via [EventRepository.save] suspending function.
 *
 * [EventSourcingAggregate] implements an interface [EventRepository] by delegating all of its public members to a specified object.
 * The Delegation pattern has proven to be a good alternative to implementation inheritance, and Kotlin supports it natively requiring zero boilerplate code.
 *
 * @param C Commands of type [C] that this aggregate can handle
 * @param S Aggregate state of type [S]
 * @param E Events of type [E] that this aggregate can publish
 * @property decider A decider component of type [Decider]<[C], [S], [E]>.
 * @property eventRepository Interface for [E]vent management/persistence - dependencies by delegation
 * @property saga A saga component of type [Saga]<[E], [C]>
 * @constructor Creates [EventSourcingAggregate]
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
data class EventSourcingAggregate<C, S, E>(
    private val decider: Decider<C, S, E>,
    private val eventRepository: EventRepository<C, E>,
    private val saga: Saga<E, C>? = null
) : EventRepository<C, E> by eventRepository {

    /**
     * Handles the command message of type [C]
     *
     * @param command Command message of type [C]
     * @return Either [Error] or [Iterable] of [E]
     */
    suspend fun handle(command: C): Either<Error, Iterable<E>> =
        // Arrow provides a Monad instance for Either. Except for the types signatures, our program remains unchanged when we compute over Either. All values on the left side assume to be Right biased and, whenever a Left value is found, the computation short-circuits, producing a result that is compatible with the function type signature.
        either {
            command
                .fetchEventsEither().bind()
                .calculateNewEvents(command).bind()
                .saveEither().bind()
        }

    private fun S.validateIfTerminal(): S =
        if (decider.isTerminal(this)) throw UnsupportedOperationException("Aggregate is in terminal state")
        else this

    private suspend fun Iterable<E>.calculateNewEvents(command: C): Either<Error, Iterable<E>> =
        Either.catch {
            val currentEvents = this@calculateNewEvents
            val currentState = currentEvents.fold(decider.initialState, decider.evolve).validateIfTerminal()
            var newEvents = decider.decide(command, currentState)

            if (saga != null) newEvents
                .flatMap { saga.react(it) }
                .forEach { c ->
                    either<Error, Iterable<E>> {
                        newEvents = newEvents.plus(
                            currentEvents.plus(newEvents)
                                .calculateNewEvents(c).bind()
                        )
                        newEvents
                    }
                }
            newEvents
        }.mapLeft { throwable ->
            Error.CalculatingNewEventsFailed(this, throwable)
        }
}
