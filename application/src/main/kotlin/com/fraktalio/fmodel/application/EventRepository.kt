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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Event repository interface
 *
 * Used by [EventSourcingAggregate]
 *
 * @param C Command
 * @param E Event
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
interface EventRepository<C, E> {
    /**
     * Fetch events
     *
     * @receiver Command of type [C]
     *
     * @return [Flow] of Events of type [E]
     */
    fun C.fetchEvents(): Flow<E>

    /**
     * Save event
     *
     * @receiver Event of type [E]
     * @return newly saved Event of type [E]
     */
    suspend fun E.save(): E

    /**
     * Save events
     *
     * @receiver [Flow] of Events of type [E]
     * @return newly saved [Flow] of Events of type [E]
     */
    fun Flow<E>.save(): Flow<E> = map { it.save() }
}

/**
 * Handles the command of type [C],
 * compute the new `flow of events` based on the current/fetched flow of events and the command being handled,
 * and save new events.
 *
 * @param C Commands of type [C] that this [decider] can handle
 * @param S State of type [S]
 * @param E Events of type [E]
 * @param command The command being handled
 * @param decider The decider to compute new state / new events
 * @param saga Optional saga to orchestrate the computation of the new state / new events
 * @return The persisted, new flow of events
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
fun <C, S, E> EventRepository<C, E>.handle(
    command: C,
    decider: IDecider<C, S, E>,
    saga: ISaga<E, C>? = null
): Flow<E> =
    if (saga == null) command.publishTo(eventSourcingAggregate(decider, this))
    else command.publishTo(eventSourcingOrchestratingAggregate(decider, this, saga))

/**
 * Handle the command of type [C],
 * compute the new `flow of events` based on the current/fetched flow of events and the command being handled,
 * and save new events / [Either.isRight],
 * or fail transparently by returning [Error] / [Either.isLeft].
 *
 *
 * @param C Commands of type [C] that this [decider] can handle
 * @param S State of type [S]
 * @param E Events of type [E]
 * @param command The command being handled
 * @param decider The decider to compute new state / new events
 * @param saga Optional saga to orchestrate the computation of the new state / new events
 * @return The persisted, new flow of events
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
fun <C, S, E> EventRepository<C, E>.eitherHandleOrFail(
    command: C,
    decider: IDecider<C, S, E>,
    saga: ISaga<E, C>? = null
): Flow<Either<Error, E>> =
    if (saga == null) command.publishEitherTo(eventSourcingAggregate(decider, this))
    else command.publishEitherTo(eventSourcingOrchestratingAggregate(decider, this, saga))

/**
 * Handle the [Flow] of commands of type [C],
 * compute the new `flow of events` based on the current/fetched flow of events and the command being handled,
 * and save new events.
 *
 *
 * @param C Commands of type [C] that this [decider] can handle
 * @param S State of type [S]
 * @param E Events of type [E]
 * @param command The command being handled
 * @param decider The decider to compute new state / new events
 * @param saga Optional saga to orchestrate the computation of the new state / new events
 * @return The persisted, new flow of events
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
fun <C, S, E> EventRepository<C, E>.handle(
    command: Flow<C>,
    decider: IDecider<C, S, E>,
    saga: ISaga<E, C>? = null
): Flow<E> =
    if (saga == null) command.publishTo(eventSourcingAggregate(decider, this))
    else command.publishTo(eventSourcingOrchestratingAggregate(decider, this, saga))

/**
 * Handle the command of type [C],
 * compute the new `flow of events` based on the current/fetched flow of events and the command being handled,
 * and save new events / [Either.isRight],
 * or fail transparently by returning [Error] / [Either.isLeft].
 *
 * @param C Commands of type [C] that this [decider] can handle
 * @param S State of type [S]
 * @param E Events of type [E]
 * @param command The command being handled
 * @param decider The decider to compute new state / new events
 * @param saga Optional saga to orchestrate the computation of the new state / new events
 * @return The persisted, new flow of events
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
fun <C, S, E> EventRepository<C, E>.eitherHandleOrFail(
    command: Flow<C>,
    decider: IDecider<C, S, E>,
    saga: ISaga<E, C>? = null
): Flow<Either<Error, E>> =
    if (saga == null) command.publishEitherTo(eventSourcingAggregate(decider, this))
    else command.publishEitherTo(eventSourcingOrchestratingAggregate(decider, this, saga))
