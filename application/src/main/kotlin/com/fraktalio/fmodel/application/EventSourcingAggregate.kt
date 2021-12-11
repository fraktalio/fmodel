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
import com.fraktalio.fmodel.domain.IDecider
import com.fraktalio.fmodel.domain.ISaga
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

/**
 * Event sourcing aggregate is using/delegating a `decider` of type [IDecider]<[C], [S], [E]> to handle commands and produce events.
 * In order to handle the command, aggregate needs to fetch the current state (represented as a sequence of events) via [EventRepository.fetchEvents] function, and then delegate the command to the `decider` which can produce new event(s) as a result.
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

    fun Sequence<E>.computeNewEvents(command: C): Sequence<E> =
        decide(command, fold(initialState) { s, e -> evolve(s, e) })
}

/**
 * Orchestrating Event sourcing aggregate is using/delegating a `decider` of type [IDecider]<[C], [S], [E]> to handle commands and produce events.
 * In order to handle the command, aggregate needs to fetch the current state (represented as a sequence of events) via [EventRepository.fetchEvents] function, and then delegate the command to the `decider` which can produce new event(s) as a result.
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
     * @return The Sequence of newly computed events of type [E]
     */
    override fun Sequence<E>.computeNewEvents(command: C): Sequence<E> {
        var newEvents = decide(command, this.fold(initialState, evolve))
        newEvents
            .flatMap { react(it) }
            .forEach {
                newEvents = newEvents.plus(this.plus(newEvents).computeNewEvents(it))
            }
        return newEvents
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


/**
 * Extension function - Handles the command message of type [C]
 *
 * @param command Command message of type [C]
 * @return [Either] [Error] or [Sequence] of Events of type [E] that are saved
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
suspend fun <C, S, E> EventSourcingAggregate<C, S, E>.handleEither(command: C): Either<Error, Sequence<E>> {

    suspend fun C.eitherFetchEventsOrFail(): Either<Error.FetchingEventsFailed, Sequence<E>> =
        Either.catch {
            fetchEvents()
        }.mapLeft { throwable -> Error.FetchingEventsFailed(throwable) }

    suspend fun E.eitherSaveOrFail(): Either<Error.StoringEventFailed<E>, E> =
        Either.catch {
            this.save()
        }.mapLeft { throwable -> Error.StoringEventFailed(this, throwable) }

    suspend fun Sequence<E>.eitherSaveOrFail(): Either<Error.StoringEventFailed<E>, Sequence<E>> =
        either<Error.StoringEventFailed<E>, List<E>> {
            this@eitherSaveOrFail.asIterable().map { it.eitherSaveOrFail().bind() }
        }.map { it.asSequence() }

    fun Sequence<E>.eitherComputeNewEventsOrFail(command: C): Either<Error, Sequence<E>> =
        Either.catch {
            computeNewEvents(command)
        }.mapLeft { throwable ->
            Error.CalculatingNewEventsFailed(this.toList(), throwable)
        }

    // Arrow provides a Monad instance for Either. Except for the types signatures, our program remains unchanged when we compute over Either. All values on the left side assume to be Right biased and, whenever a Left value is found, the computation short-circuits, producing a result that is compatible with the function type signature.
    return either {
        command
            .eitherFetchEventsOrFail().bind()
            .eitherComputeNewEventsOrFail(command).bind()
            .eitherSaveOrFail().bind()
    }
}

/**
 * Extension function - Handles the command message of type [C]
 *
 * @param command Command message of type [C]
 * @return [Sequence] of Events of type [E] that are saved, or throws an exception
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
suspend fun <C, S, E> EventSourcingAggregate<C, S, E>.handle(command: C): Sequence<E> =
    command
        .fetchEvents()
        .computeNewEvents(command)
        .save()

/**
 * Extension function - Handles the command message of type [C]
 * This function uses dispatcher from the new context, shifting fetching/saving into the different thread if a new dispatcher is specified, and back to the original dispatcher when it completes.
 *
 * @param command Command message of type [C]
 * @param context The new context to supply a concrete dispatcher / IO is by default
 * @return [Either] [Error] or [Sequence] of Events of type [E] that are saved
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */

suspend fun <C, S, E> EventSourcingAggregate<C, S, E>.handleEither(
    command: C,
    context: CoroutineContext = IO
): Either<Error, Sequence<E>> {

    suspend fun C.eitherFetchEventsOrFail(): Either<Error.FetchingEventsFailed, Sequence<E>> =
        Either.catch {
            withContext(context) { fetchEvents() }
        }.mapLeft { throwable -> Error.FetchingEventsFailed(throwable) }

    suspend fun E.eitherSaveOrFail(): Either<Error.StoringEventFailed<E>, E> =
        Either.catch {
            withContext(context) { save() }
        }.mapLeft { throwable -> Error.StoringEventFailed(this, throwable) }

    suspend fun Sequence<E>.eitherSaveOrFail(): Either<Error.StoringEventFailed<E>, Sequence<E>> =
        either<Error.StoringEventFailed<E>, List<E>> {
            this@eitherSaveOrFail.asIterable().map { it.eitherSaveOrFail().bind() }
        }.map { it.asSequence() }

    fun Sequence<E>.eitherComputeNewEventsOrFail(command: C): Either<Error, Sequence<E>> =
        Either.catch {
            computeNewEvents(command)
        }.mapLeft { throwable ->
            Error.CalculatingNewEventsFailed(this.toList(), throwable)
        }

    // Arrow provides a Monad instance for Either. Except for the types signatures, our program remains unchanged when we compute over Either. All values on the left side assume to be Right biased and, whenever a Left value is found, the computation short-circuits, producing a result that is compatible with the function type signature.
    return either {
        command
            .eitherFetchEventsOrFail().bind()
            .eitherComputeNewEventsOrFail(command).bind()
            .eitherSaveOrFail().bind()
    }
}

/**
 * Extension function - Handles the command message of type [C] in the new context (`IO` by default)
 *
 * This function uses dispatcher from the new context, shifting fetching/saving into the different thread if a new dispatcher is specified, and back to the original dispatcher when it completes.
 *
 * @param command Command message of type [C]
 * @param context The new context to supply a concrete dispatcher / IO is by default
 * @return [Sequence] of Events of type [E] that are saved, or throws an exception
 */
suspend fun <C, S, E> EventSourcingAggregate<C, S, E>.handle(command: C, context: CoroutineContext = IO): Sequence<E> {
    suspend fun C.fetchEventsAwait(): Sequence<E> = withContext(context) { fetchEvents() }
    suspend fun Sequence<E>.saveAwait(): Sequence<E> = withContext(context) { save() }

    return command
        .fetchEventsAwait()
        .computeNewEvents(command)
        .saveAwait()

}
