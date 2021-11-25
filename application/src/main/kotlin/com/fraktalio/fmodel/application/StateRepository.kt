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
import com.fraktalio.fmodel.domain.Decider
import com.fraktalio.fmodel.domain.Saga
import kotlinx.coroutines.flow.Flow

/**
 * State repository interface.
 *
 * @param C Command
 * @param S State
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
interface StateRepository<C, S> {
    /**
     * Fetch state
     *
     * @receiver Command of type [C]
     * @return the State of type [S] or null
     */
    suspend fun C.fetchState(): S?

    /**
     * Save state
     *
     * @receiver State of type [S]
     * @return newly saved State of type [S]
     */
    suspend fun S.save(): S

    /**
     * Fetch state - either version
     *
     * @receiver Command of type [C]
     * @return [Either] [Error] or the State of type [S]?
     */
    suspend fun C.eitherFetchStateOrFail(): Either<Error.FetchingStateFailed<C>, S?> =
        Either.catch {
            fetchState()
        }.mapLeft { throwable -> Error.FetchingStateFailed(this, throwable) }

    /**
     * Save state - either version
     *
     * @receiver State of type [S]
     * @return [Either] [Error] or the newly saved State of type [S]
     */
    suspend fun S.eitherSaveOrFail(): Either<Error.StoringStateFailed<S>, S> =
        Either.catch {
            this.save()
        }.mapLeft { throwable -> Error.StoringStateFailed(this, throwable) }
}

/**
 * Handle the command of type [C],
 * compute the new state of type [S] based on the current/fetched state and the command being handled,
 * and save new state.
 *
 * Dependency injection
 * ____________________
 * Internally, the [stateStoredAggregate] is used to create the object/instance of [StateStoredAggregate]<[C], [S], [E]>.
 * The Delegation pattern has proven to be a good alternative to implementation inheritance, and Kotlin supports it natively requiring zero boilerplate code.
 * Kotlin natively supports dependency injection with receivers.
 * -------------------
 *
 * @param R The type of the repository - [R] : [StateRepository]<[C], [S]>
 * @param C Commands of type [C] that this [decider] can handle
 * @param S State of type [S]
 * @param E Events of type [E] that are used internally by [decider] to build/fold new state
 * @param command The command being handled
 * @param decider The decider to compute new state
 * @param saga Optional saga to orchestrate the computation of the new state
 * @return The persisted, new state
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
suspend fun <R, C, S, E> R.handle(
    command: C,
    decider: Decider<C, S, E>,
    saga: Saga<E, C>? = null
): S where R : StateRepository<C, S> =
    if (saga == null) command.publishTo(stateStoredAggregate(decider, this))
    else command.publishTo(stateStoredOrchestratingAggregate(decider, this, saga))

/**
 * Handle the command of type [C],
 * compute the new state of type [S] based on the current/fetched state and the command being handled,
 * and save new state / [Either.isRight], or fail transparently by returning [Error] / [Either.isLeft].
 *
 * Dependency injection
 * ____________________
 * Internally, the [stateStoredAggregate] is used to create the object/instance of [StateStoredAggregate]<[C], [S], [E]>.
 * The Delegation pattern has proven to be a good alternative to implementation inheritance, and Kotlin supports it natively requiring zero boilerplate code.
 * Kotlin natively supports dependency injection with receivers.
 * -------------------
 *
 * @param R The type of the repository - [R] : [StateRepository]<[C], [S]>
 * @param C Commands of type [C] that this [decider] can handle
 * @param S State of type [S]
 * @param E Events of type [E] that are used internally by [decider] to build/fold new state
 * @param command The command being handled
 * @param decider The decider to compute new state
 * @param saga Optional saga to orchestrate the computation of the new state
 * @return The persisted, new state
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
suspend fun <R, C, S, E> R.eitherHandleOrFail(
    command: C,
    decider: Decider<C, S, E>,
    saga: Saga<E, C>? = null
): Either<Error, S> where R : StateRepository<C, S> =
    if (saga == null) command.publishEitherTo(stateStoredAggregate(decider, this))
    else command.publishEitherTo(stateStoredOrchestratingAggregate(decider, this, saga))

/**
 * Handle the commands of type [Flow]<[C]>,
 * compute the new state of type [S] based on the current/fetched state and the command being handled,
 * and save new state.
 *
 * Dependency injection
 * ____________________
 * Internally, the [stateStoredAggregate] is used to create the object/instance of [StateStoredAggregate]<[C], [S], [E]>.
 * The Delegation pattern has proven to be a good alternative to implementation inheritance, and Kotlin supports it natively requiring zero boilerplate code.
 * Kotlin natively supports dependency injection with receivers.
 * -------------------
 *
 * @param R The type of the repository - [R] : [StateRepository]<[C], [S]>
 * @param C Commands of type [C] that this [decider] can handle
 * @param S State of type [S]
 * @param E Events of type [E] that are used internally by [decider] to build/fold new state
 * @param commands The commands being handled
 * @param decider The decider to compute new state
 * @param saga Optional saga to orchestrate the computation of the new state
 * @return The persisted, new state as a [Flow]<[S]>
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
fun <R, C, S, E> R.handle(
    commands: Flow<C>,
    decider: Decider<C, S, E>,
    saga: Saga<E, C>? = null
): Flow<S> where R : StateRepository<C, S> =
    if (saga == null) commands.publishTo(stateStoredAggregate(decider, this))
    else commands.publishTo(stateStoredOrchestratingAggregate(decider, this, saga))

/**
 * Handle the command of type [Flow]<[C]>,
 * compute the new state of type [S] based on the current/fetched state and the command being handled,
 * and save new state / [Either.isRight],
 * or fail transparently by returning [Error] / [Either.isLeft]
 * within a flow [Flow]<[Either]<[Error], [S]>>
 *
 * Dependency injection
 * ____________________
 * Internally, the [stateStoredAggregate] is used to create the object/instance of [StateStoredAggregate]<[C], [S], [E]>.
 * The Delegation pattern has proven to be a good alternative to implementation inheritance, and Kotlin supports it natively requiring zero boilerplate code.
 * Kotlin natively supports dependency injection with receivers.
 * -------------------
 *
 * @param R The type of the repository - [R] : [StateRepository]<[C], [S]>
 * @param C Commands of type [C] that this [decider] can handle
 * @param S State of type [S]
 * @param E Events of type [E] that are used internally by [decider] to build/fold new state
 * @param commands The commands being handled
 * @param decider The decider to compute new state
 * @param saga Optional saga to orchestrate the computation of the new state
 * @return The persisted, new state - [Flow]<[Either]<[Error], [S]>>
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
fun <R, C, S, E> R.eitherHandleOrFail(
    commands: Flow<C>,
    decider: Decider<C, S, E>,
    saga: Saga<E, C>? = null
): Flow<Either<Error, S>> where R : StateRepository<C, S> =
    if (saga == null) commands.publishEitherTo(stateStoredAggregate(decider, this))
    else commands.publishEitherTo(stateStoredOrchestratingAggregate(decider, this, saga))
