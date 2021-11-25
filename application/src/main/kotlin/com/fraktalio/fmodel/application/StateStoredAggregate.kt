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
import kotlinx.coroutines.flow.*

/**
 * State stored aggregate is using a [StateStoredAggregate.decider] of type [Decider]<[C], [S], [E]> to handle commands and produce new state.
 * In order to handle the command, aggregate needs to fetch the current state via [StateRepository.fetchState] function first, and then delegate the command to the [StateStoredAggregate.decider] which can compute new state as a result.
 * New state is then stored via [StateRepository.save] suspending function.
 *
 * [StateStoredAggregate] implements an interface [StateRepository].
 *
 * @param C Commands of type [C] that this aggregate can handle
 * @param S Aggregate state of type [S]
 * @param E Events of type [E] that are used internally to build/fold new state
 * @property decider A decider component of type [Decider]<[C], [S], [E]>.
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
interface StateStoredAggregate<C, S, E> : StateRepository<C, S> {
    val decider: Decider<C, S, E>

    /**
     * Computes new State based on the previous State and the [command].
     *
     * @param command of type [C]
     * @return The newly computed state of type [S]
     */
    suspend fun S.computeNewState(command: C): S {
        val events = decider.decide(command, this)
        return events.fold(this) { s, e -> decider.evolve(s, e) }
    }

    /**
     * Computes new State based on the previous State and the [command] or fails.
     *
     * @param command of type [C]
     * @return [Either] the newly computed state of type [S] or [Error]
     */
    suspend fun S.eitherComputeNewStateOrFail(command: C): Either<Error, S> =
        Either.catch {
            computeNewState(command)
        }.mapLeft { throwable ->
            Error.CalculatingNewStateFailed(this, command, throwable)
        }

    /**
     * Handles the command message of type [C]
     *
     * @param command Command message of type [C]
     * @return State of type [S]
     */
    suspend fun handle(command: C): S =
        (command.fetchState() ?: decider.initialState)
            .computeNewState(command)
            .save()

    /**
     * Handles the [Flow] of command messages of type [C]
     *
     * @param commands [Flow] of Command messages of type [C]
     * @return [Flow] of State of type [S]
     */
    fun handle(commands: Flow<C>): Flow<S> =
        commands.map { handle(it) }

    /**
     * Handles the command message of type [C]
     *
     * @param command Command message of type [C]
     * @return Either [Error] or State of type [S]
     */
    suspend fun handleEither(command: C): Either<Error, S> =
        // Arrow provides a Monad instance for Either. Except for the types signatures, our program remains unchanged when we compute over Either. All values on the left side assume to be Right biased and, whenever a Left value is found, the computation short-circuits, producing a result that is compatible with the function type signature.
        either {
            (command.eitherFetchStateOrFail().bind() ?: decider.initialState)
                .eitherComputeNewStateOrFail(command).bind()
                .eitherSaveOrFail().bind()
        }

    /**
     * Handles the [Flow] of command messages of type [C]
     *
     * @param commands [Flow] of Command messages of type [C]
     * @return [Flow] of [Either] [Error] or State of type [S]
     */
    fun handleEither(commands: Flow<C>): Flow<Either<Error, S>> =
        commands
            .map { handleEither(it) }
            .catch { emit(Either.Left(Error.CommandPublishingFailed(it))) }
}

/**
 * Orchestrating State stored aggregate is extending [StateStoredAggregate] by introducing [StateStoredOrchestratingAggregate.saga] property. It is using a [StateStoredAggregate.decider] of type [Decider]<[C], [S], [E]> to handle commands and produce new state.
 * In order to handle the command, aggregate needs to fetch the current state via [StateRepository.fetchState] function first, and then delegate the command to the [StateStoredAggregate.decider] which can compute new state as a result.
 * If the [StateStoredAggregate.decider] is combined out of many deciders via `combine` function, a [StateStoredOrchestratingAggregate.saga] could be used to react on new events and send new commands to the [StateStoredAggregate.decider] recursively, in one transaction.
 * New state is then stored via [StateRepository.save] suspending function.
 *
 * [StateStoredOrchestratingAggregate] extends interface [StateStoredAggregate].
 *
 * @param C Commands of type [C] that this aggregate can handle
 * @param S Aggregate state of type [S]
 * @param E Events of type [E] that are used internally to build/fold new state
 * @property decider A decider component of type [Decider]<[C], [S], [E]>
 * @property saga A saga component of type [Saga]<[E], [C]>
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
interface StateStoredOrchestratingAggregate<C, S, E> : StateStoredAggregate<C, S, E> {
    val saga: Saga<E, C>

    /**
     * Computes new State based on the previous State and the [command].
     *
     * @param command of type [C]
     * @return The newly computed state of type [S]
     */
    override suspend fun S.computeNewState(command: C): S {
        val events = decider.decide(command, this)
        val newState = events.fold(this) { s, e -> decider.evolve(s, e) }
        events.flatMapConcat { saga.react(it) }.collect { newState.computeNewState(it) }
        return newState
    }
}

/**
 * State stored orchestrating aggregate factory function.
 *
 * The Delegation pattern has proven to be a good alternative to implementation inheritance, and Kotlin supports it natively requiring zero boilerplate code.
 *
 * @param C Commands of type [C] that this aggregate can handle
 * @param S Aggregate state of type [S]
 * @param E Events of type [E] that are used internally to build/fold new state
 * @param decider A decider component of type [Decider]<[C], [S], [E]>
 * @param stateRepository An aggregate state repository of type [StateRepository]<[C], [S]>
 * @param saga A saga component of type [Saga]<[E], [C]>
 * @return An object/instance of type [StateStoredOrchestratingAggregate]<[C], [S], [E]>
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
fun <C, S, E> stateStoredOrchestratingAggregate(
    decider: Decider<C, S, E>,
    stateRepository: StateRepository<C, S>,
    saga: Saga<E, C>
): StateStoredOrchestratingAggregate<C, S, E> =
    object : StateStoredOrchestratingAggregate<C, S, E>, StateRepository<C, S> by stateRepository {
        override val decider = decider
        override val saga = saga
    }

/**
 * Extension function - State stored aggregate factory function.
 *
 * The Delegation pattern has proven to be a good alternative to implementation inheritance, and Kotlin supports it natively requiring zero boilerplate code.
 *
 * @param C Commands of type [C] that this aggregate can handle
 * @param S Aggregate state of type [S]
 * @param E Events of type [E] that are used internally to build/fold new state
 * @param decider A decider component of type [Decider]<[C], [S], [E]>
 * @param stateRepository An aggregate state repository of type [StateRepository]<[C], [S]>
 * @return An object/instance of type [StateStoredAggregate]<[C], [S], [E]>
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
fun <C, S, E> stateStoredAggregate(
    decider: Decider<C, S, E>,
    stateRepository: StateRepository<C, S>
): StateStoredAggregate<C, S, E> =
    object : StateStoredAggregate<C, S, E>, StateRepository<C, S> by stateRepository {
        override val decider = decider
    }


/**
 * Extension function - Publishes the command of type [C] to the state stored aggregate of type  [StateStoredAggregate]<[C], [S], *>
 * @receiver command of type [C]
 * @param aggregate of type [StateStoredAggregate]<[C], [S], *>
 * @return the stored State of type [S]
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
suspend fun <C, S> C.publishTo(aggregate: StateStoredAggregate<C, S, *>): S = aggregate.handle(this)

/**
 * Extension function - Publishes the command of type [C] to the state stored aggregate of type  [StateStoredAggregate]<[C], [S], *>
 * @receiver [Flow] of commands of type [C]
 * @param aggregate of type [StateStoredAggregate]<[C], [S], *>
 * @return the [Flow] of State of type [S]
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
fun <C, S> Flow<C>.publishTo(aggregate: StateStoredAggregate<C, S, *>): Flow<S> = aggregate.handle(this)

/**
 * Extension function - Publishes the command of type [C] to the state stored aggregate of type  [StateStoredAggregate]<[C], [S], *>
 * @receiver command of type [C]
 * @param aggregate of type [StateStoredAggregate]<[C], [S], *>
 * @return [Either] [Error] or successfully stored State of type [S]
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
suspend fun <C, S> C.publishEitherTo(aggregate: StateStoredAggregate<C, S, *>): Either<Error, S> =
    aggregate.handleEither(this)

/**
 * Extension function - Publishes the command of type [C] to the state stored aggregate of type  [StateStoredAggregate]<[C], [S], *>
 * @receiver [Flow] of commands of type [C]
 * @param aggregate of type [StateStoredAggregate]<[C], [S], *>
 * @return the [Flow] of [Either] [Error] or successfully  stored State of type [S]
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
fun <C, S> Flow<C>.publishEitherTo(aggregate: StateStoredAggregate<C, S, *>): Flow<Either<Error, S>> =
    aggregate.handleEither(this)
