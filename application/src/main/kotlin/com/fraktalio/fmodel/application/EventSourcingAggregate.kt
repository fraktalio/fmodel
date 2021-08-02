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
import arrow.core.nonFatalOrThrow
import com.fraktalio.fmodel.domain.Decider
import com.fraktalio.fmodel.domain.Saga
import kotlinx.coroutines.flow.*

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
     * @return [Flow] of [Either] [Error] or [E]/Event
     */
    suspend fun handle(command: C): Flow<Either<Error, E>> =
        command
            .fetchEvents()
            .calculateNewEvents(command)
            .saveEither()
            .catch<Either<Error, E>> {
                emit(Either.Left(Error.CommandHandlingFailed(it)))
            }

    /**
     * Handles the flow of command messages of type [C]
     *
     * @param commands [Flow] of Command messages of type [C]
     * @return [Flow] of [Either] [Error] or [E]/Event
     */
    fun handle(commands: Flow<C>): Flow<Either<Error, E>> =
        commands.flatMapConcat { handle(it) }


    private suspend fun Flow<E>.calculateNewEvents(command: C): Flow<E> {
        try {
            val currentEvents = this
            val currentState = this.fold(decider.initialState, decider.evolve)
            if (decider.isTerminal(currentState)) throw UnsupportedOperationException("Aggregate is in terminal state!")
            var resultingEvents = decider.decide(command, currentState)

            if (saga != null)
                resultingEvents.flatMapConcat { saga.react(it) }.collect {
                    val newEvents = flowOf(currentEvents, resultingEvents).flattenConcat().calculateNewEvents(it)
                    resultingEvents = flowOf(resultingEvents, newEvents).flattenConcat()
                }

            return resultingEvents
        } catch (e: Throwable) {
            val nonFatalException = e.nonFatalOrThrow()
            return flow { throw nonFatalException }
        }
    }
}
