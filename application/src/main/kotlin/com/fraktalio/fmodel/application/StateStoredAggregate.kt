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


/**
 * State stored aggregate is using/delegating a `decider` of type [IDecider]<[C], [S], [E]> to handle commands and produce new state.
 * In order to handle the command, aggregate needs to fetch the current state via [StateRepository.fetchState] function first, and then delegate the command to the `decider` which can compute new state as a result.
 * New state is then stored via [StateRepository.save] suspending function.
 *
 * [StateStoredAggregate] extends [IDecider] and [StateRepository] interfaces,
 * clearly communicating that it is composed out of these two behaviours.
 *
 * The Delegation pattern has proven to be a good alternative to `implementation inheritance`,
 * and Kotlin supports it natively requiring zero boilerplate code. [stateStoredAggregate] function is a good example.
 *
 *
 * @param C Commands of type [C] that this aggregate can handle
 * @param S Aggregate state of type [S]
 * @param E Events of type [E] that are used internally to build/fold new state
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
interface StateStoredAggregate<C, S, E> : IDecider<C, S, E>, StateRepository<C, S> {
    /**
     * Computes new State based on the previous State and the [command].
     *
     * @param command of type [C]
     * @return The newly computed state of type [S]
     */
    suspend fun S.computeNewState(command: C): S = decide(command, this)
        .fold(this) { s, e -> evolve(s, e) }
}

/**
 * Orchestrating State stored aggregate is using/delegating a `decider` of type [IDecider]<[C], [S], [E]> to handle commands and produce new state.
 * In order to handle the command, aggregate needs to fetch the current state via [StateRepository.fetchState] function first, and then delegate the command to the `decider` which can compute new state as a result.
 * If the `decider` is combined out of many deciders via `combine` function, a `saga` could be used to react on new events and send new commands to the `decider` recursively, in single transaction.
 * New state is then stored via [StateRepository.save] suspending function.
 *
 * [StateStoredOrchestratingAggregate] extends [ISaga] and [StateStoredAggregate] interfaces,
 * clearly communicating that it is composed out of these two behaviours.
 *
 * The Delegation pattern has proven to be a good alternative to `implementation inheritance`,
 * and Kotlin supports it natively requiring zero boilerplate code. [stateStoredOrchestratingAggregate] function is a good example.
 *
 *
 * @param C Commands of type [C] that this aggregate can handle
 * @param S Aggregate state of type [S]
 * @param E Events of type [E] that are used internally to build/fold new state
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
interface StateStoredOrchestratingAggregate<C, S, E> : ISaga<E, C>, StateStoredAggregate<C, S, E> {
    /**
     * Computes new State based on the previous State and the [command].
     *
     * @param command of type [C]
     * @return The newly computed state of type [S]
     */
    override suspend fun S.computeNewState(command: C): S {
        val events = decide(command, this)
        val newState = events.fold(this) { s, e -> evolve(s, e) }
        events.flatMap { react(it) }.forEach { newState.computeNewState(it) }
        return newState
    }
}

/**
 * State stored aggregate factory function.
 *
 * The Delegation pattern has proven to be a good alternative to implementation inheritance, and Kotlin supports it natively requiring zero boilerplate code.
 *
 * @param C Commands of type [C] that this aggregate can handle
 * @param S Aggregate state of type [S]
 * @param E Events of type [E] that are used internally to build/fold new state
 * @param decider A decider component of type [IDecider]<[C], [S], [E]>
 * @param stateRepository An aggregate state repository of type [StateRepository]<[C], [S]>
 * @return An object/instance of type [StateStoredAggregate]<[C], [S], [E]>
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
fun <C, S, E> stateStoredAggregate(
    decider: IDecider<C, S, E>,
    stateRepository: StateRepository<C, S>
): StateStoredAggregate<C, S, E> =
    object :
        StateStoredAggregate<C, S, E>,
        StateRepository<C, S> by stateRepository,
        IDecider<C, S, E> by decider {}

/**
 * State stored orchestrating aggregate factory function.
 *
 * The Delegation pattern has proven to be a good alternative to implementation inheritance, and Kotlin supports it natively requiring zero boilerplate code.
 *
 * @param C Commands of type [C] that this aggregate can handle
 * @param S Aggregate state of type [S]
 * @param E Events of type [E] that are used internally to build/fold new state
 * @param decider A decider component of type [IDecider]<[C], [S], [E]>
 * @param stateRepository An aggregate state repository of type [StateRepository]<[C], [S]>
 * @param saga A saga component of type [ISaga]<[E], [C]>
 * @return An object/instance of type [StateStoredOrchestratingAggregate]<[C], [S], [E]>
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
fun <C, S, E> stateStoredOrchestratingAggregate(
    decider: IDecider<C, S, E>,
    stateRepository: StateRepository<C, S>,
    saga: ISaga<E, C>
): StateStoredOrchestratingAggregate<C, S, E> =
    object : StateStoredOrchestratingAggregate<C, S, E>,
        StateRepository<C, S> by stateRepository,
        IDecider<C, S, E> by decider,
        ISaga<E, C> by saga {}


/**
 * Extension function - Handles the command message of type [C]
 *
 * @param command Command message of type [C]
 * @return State of type [S]
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
suspend fun <C, S, E> StateStoredAggregate<C, S, E>.handle(command: C): S =
    (command.fetchState() ?: initialState)
        .computeNewState(command)
        .save()

/**
 * Handles the command message of type [C]
 *
 * @param command Command message of type [C]
 * @return Either [Error] or [S]/State
 */
suspend fun <C, S, E> StateStoredAggregate<C, S, E>.handleEither(command: C): Either<Error, S> {

    suspend fun C.eitherFetchStateOrFail(): Either<Error.FetchingStateFailed, S?> =
        Either.catch {
            fetchState()
        }.mapLeft { throwable -> Error.FetchingStateFailed(throwable) }

    suspend fun S.eitherSaveOrFail(): Either<Error.StoringStateFailed<S>, S> =
        Either.catch {
            this.save()
        }.mapLeft { throwable -> Error.StoringStateFailed(this, throwable) }

    suspend fun S.eitherComputeNewStateOrFail(command: C): Either<Error, S> =
        Either.catch {
            computeNewState(command)
        }.mapLeft { throwable ->
            Error.CalculatingNewStateFailed(this, throwable)
        }

    // Arrow provides a Monad instance for Either. Except for the types signatures, our program remains unchanged when we compute over Either. All values on the left side assume to be Right biased and, whenever a Left value is found, the computation short-circuits, producing a result that is compatible with the function type signature.
    return either {
        (command.eitherFetchStateOrFail().bind() ?: initialState)
            .eitherComputeNewStateOrFail(command).bind()
            .eitherSaveOrFail().bind()
    }
}
