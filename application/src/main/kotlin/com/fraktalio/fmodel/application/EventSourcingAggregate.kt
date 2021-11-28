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
import com.fraktalio.fmodel.domain.IDecider
import com.fraktalio.fmodel.domain.ISaga
import kotlinx.coroutines.flow.*

/**
 * Event sourcing aggregate is using/delegating a `decider` of type [IDecider]<[C], [S], [E]> to handle commands and produce events.
 * In order to handle the command, aggregate needs to fetch the current state (represented as a list of events) via [EventRepository.fetchEvents] function, and then delegate the command to the `decider` which can produce new event(s) as a result.
 * Produced events are then stored via [EventRepository.save] suspending function.
 *
 * [EventSourcingAggregate] extends [IDecider] and [EventRepository] interfaces,
 * clearly communicating that it is composed out of these two behaviours.
 *
 * The Delegation pattern has proven to be a good alternative to `implementation inheritance`,
 * and Kotlin supports it natively requiring zero boilerplate code. [eventSourcingAggregate] function is a good example.
 *
 * @param C Commands of type [C] that this aggregate can handle
 * @param S Aggregate state of type [S]
 * @param E Events of type [E] that this aggregate can publish
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
interface EventSourcingAggregate<C, S, E> : IDecider<C, S, E>, EventRepository<C, E> {

    fun Flow<E>.computeNewEvents(command: C): Flow<E> =
        flow {
            val currentState = fold(initialState) { s, e -> evolve(s, e) }
            val resultingEvents = decide(command, currentState)
            emitAll(resultingEvents)
        }
}

/**
 * Orchestrating Event sourcing aggregate is using/delegating a `decider` of type [IDecider]<[C], [S], [E]> to handle commands and produce events.
 * In order to handle the command, aggregate needs to fetch the current state (represented as a list of events) via [EventRepository.fetchEvents] function, and then delegate the command to the `decider` which can produce new event(s) as a result.
 * If the `decider` is combined out of many deciders via `combine` function, an optional `saga` of type [ISaga] could be used to react on new events and send new commands to the 'decider` recursively, in single transaction.
 * Produced events are then stored via [EventRepository.save] suspending function.
 *
 * [EventSourcingOrchestratingAggregate] extends [ISaga] and [EventSourcingAggregate] interfaces,
 * clearly communicating that it is composed out of these two behaviours.
 *
 * The Delegation pattern has proven to be a good alternative to `implementation inheritance`,
 * and Kotlin supports it natively requiring zero boilerplate code. [eventSourcingOrchestratingAggregate] function is a good example.
 *
 * @param C Commands of type [C] that this aggregate can handle
 * @param S Aggregate state of type [S]
 * @param E Events of type [E] that this aggregate can publish
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
interface EventSourcingOrchestratingAggregate<C, S, E> : ISaga<E, C>, EventSourcingAggregate<C, S, E> {

    /**
     * Computes new Events based on the previous events and the [command].
     *
     * @param command of type [C]
     * @return The Flow of newly computed events of type [E]
     */
    override fun Flow<E>.computeNewEvents(command: C): Flow<E> =
        flow {
            val currentState = fold(initialState) { s, e -> evolve(s, e) }
            var resultingEvents = decide(command, currentState)

            resultingEvents.flatMapConcat { react(it) }.collect {
                val newEvents =
                    flowOf(this@computeNewEvents, resultingEvents).flattenConcat().computeNewEvents(it)
                resultingEvents = flowOf(resultingEvents, newEvents).flattenConcat()
            }

            emitAll(resultingEvents)
        }
}

/**
 * Event Sourced aggregate factory function.
 *
 * The Delegation pattern has proven to be a good alternative to implementation inheritance, and Kotlin supports it natively requiring zero boilerplate code.
 *
 * @param C Commands of type [C] that this aggregate can handle
 * @param S Aggregate state of type [S]
 * @param E Events of type [E] that are used internally to build/fold new state
 * @param decider A decider component of type [IDecider]<[C], [S], [E]>
 * @param eventRepository An aggregate event repository of type [EventRepository]<[C], [E]>
 * @return An object/instance of type [EventSourcingAggregate]<[C], [S], [E]>
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
fun <C, S, E> eventSourcingAggregate(
    decider: IDecider<C, S, E>,
    eventRepository: EventRepository<C, E>
): EventSourcingAggregate<C, S, E> =
    object :
        EventSourcingAggregate<C, S, E>,
        EventRepository<C, E> by eventRepository,
        IDecider<C, S, E> by decider {}


/**
 * Event Sourced Orchestrating aggregate factory function.
 *
 * The Delegation pattern has proven to be a good alternative to implementation inheritance, and Kotlin supports it natively requiring zero boilerplate code.
 *
 * @param C Commands of type [C] that this aggregate can handle
 * @param S Aggregate state of type [S]
 * @param E Events of type [E] that are used internally to build/fold new state
 * @param decider A decider component of type [IDecider]<[C], [S], [E]>
 * @param eventRepository An aggregate event repository of type [EventRepository]<[C], [E]>
 * @param saga A saga component of type [ISaga]<[E], [C]> - orchestrates the deciders
 * @return An object/instance of type [EventSourcingOrchestratingAggregate]<[C], [S], [E]>
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
fun <C, S, E> eventSourcingOrchestratingAggregate(
    decider: IDecider<C, S, E>,
    eventRepository: EventRepository<C, E>,
    saga: ISaga<E, C>
): EventSourcingOrchestratingAggregate<C, S, E> =
    object :
        EventSourcingOrchestratingAggregate<C, S, E>,
        EventRepository<C, E> by eventRepository,
        IDecider<C, S, E> by decider,
        ISaga<E, C> by saga {}


// #################################################
// ##########  Native Kotlin Extensions   ##########
// #################################################

/**
 * Extension function - Handles the command message of type [C]
 *
 * @param command Command message of type [C]
 * @return [Flow] of stored Events of type [E]
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
fun <C, S, E> EventSourcingAggregate<C, S, E>.handle(command: C): Flow<E> =
    command
        .fetchEvents()
        .computeNewEvents(command)
        .save()

/**
 * Extension function - Handles the flow of command messages of type [C]
 *
 * @param commands [Flow] of Command messages of type [C]
 * @return [Flow] of stored Events of type [E]
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
fun <C, S, E> EventSourcingAggregate<C, S, E>.handle(commands: Flow<C>): Flow<E> =
    commands.flatMapConcat { handle(it) }


/**
 * Extension function - Publishes the command of type [C] to the event sourcing aggregate of type  [EventSourcingAggregate]<[C], *, [E]>
 * @receiver command of type [C]
 * @param aggregate of type [EventSourcingAggregate]<[C], *, [E]>
 * @return the [Flow] of stored Events of type [E]
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
fun <C, E> C.publishTo(aggregate: EventSourcingAggregate<C, *, E>): Flow<E> = aggregate.handle(this)

/**
 * Extension function - Publishes [Flow] of commands of type [C] to the event sourcing aggregate of type  [EventSourcingAggregate]<[C], *, [E]>
 * @receiver [Flow] of commands of type [C]
 * @param aggregate of type [EventSourcingAggregate]<[C], *, [E]>
 * @return the [Flow] of stored Events of type [E]
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
fun <C, E> Flow<C>.publishTo(aggregate: EventSourcingAggregate<C, *, E>): Flow<E> = aggregate.handle(this)


// #################################################
// ##########   Arrow Kotlin Extensions   ##########
// #################################################

/**
 * Extension function - Handles the command message of type [C]
 *
 * @param command Command message of type [C]
 * @return [Flow] of [Either] [Error] or Events of type [E]
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
fun <C, S, E> EventSourcingAggregate<C, S, E>.handleEither(command: C): Flow<Either<Error, E>> =
    command
        .fetchEvents()
        .computeNewEvents(command)
        .save()
        .map { Either.Right(it) }
        .catch<Either<Error, E>> {
            emit(Either.Left(Error.CommandHandlingFailed(command)))
        }

/**
 * Extension function - Handles the flow of command messages of type [C]
 *
 * @param commands [Flow] of Command messages of type [C]
 * @return [Flow] of [Either] [Error] or Events of type [E]
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
fun <C, S, E> EventSourcingAggregate<C, S, E>.handleEither(commands: Flow<C>): Flow<Either<Error, E>> =
    commands
        .flatMapConcat { handleEither(it) }
        .catch { emit(Either.Left(Error.CommandPublishingFailed(it))) }

/**
 * Extension function - Publishes the command of type [C] to the event sourcing aggregate of type  [EventSourcingAggregate]<[C], *, [E]>
 * @receiver command of type [C]
 * @param aggregate of type [EventSourcingAggregate]<[C], *, [E]>
 * @return the [Flow] of [Either] [Error] or successfully stored Events of type [E]
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
fun <C, E> C.publishEitherTo(aggregate: EventSourcingAggregate<C, *, E>): Flow<Either<Error, E>> =
    aggregate.handleEither(this)

/**
 * Extension function - Publishes [Flow] of commands of type [C] to the event sourcing aggregate of type  [EventSourcingAggregate]<[C], *, [E]>
 * @receiver [Flow] of commands of type [C]
 * @param aggregate of type [EventSourcingAggregate]<[C], *, [E]>
 * @return the [Flow] of [Either] [Error] or successfully stored Events of type [E]
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
fun <C, E> Flow<C>.publishEitherTo(aggregate: EventSourcingAggregate<C, *, E>): Flow<Either<Error, E>> =
    aggregate.handleEither(this)
