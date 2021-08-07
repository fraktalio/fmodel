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
 * State stored aggregate is using/delegating a [StateStoredAggregate.decider] of type [Decider]<[C], [S], [E]> to handle commands and produce new state.
 * In order to handle the command, aggregate needs to fetch the current state via [StateRepository.fetchState] function first, and then delegate the command to the [StateStoredAggregate.decider] which can produce new state as a result.
 * If the [StateStoredAggregate.decider] is combined out of many deciders via `combine` function, an optional [StateStoredAggregate.saga] could be used to react on new events and send new commands to the [StateStoredAggregate.decider] recursively, in one transaction.
 * New state is then stored via [StateRepository.save] suspending function.
 *
 * [StateStoredAggregate] implements an interface [StateRepository] by delegating all of its public members to a specified object.
 * The Delegation pattern has proven to be a good alternative to implementation inheritance, and Kotlin supports it natively requiring zero boilerplate code.
 *
 * @param C Commands of type [C] that this aggregate can handle
 * @param S Aggregate state of type [S]
 * @param E Events of type [E] that are used internally to build/fold new state
 * @property decider A decider component of type [Decider]<[C], [S], [E]>.
 * @property stateRepository Interface for [S]tate management/persistence - dependencies by delegation
 * @property saga A saga component of type [Saga]<[E], [C]>
 * @constructor Creates [StateStoredAggregate]
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
data class StateStoredAggregate<C, S, E>(
    private val decider: Decider<C, S, E>,
    private val stateRepository: StateRepository<C, S>,
    private val saga: Saga<E, C>? = null
) : StateRepository<C, S> by stateRepository {

    /**
     * Handles the command message of type [C]
     *
     * @param command Command message of type [C]
     * @return Either [Error] or [S]/State
     */
    suspend fun handleEither(command: C): Either<Error, S> =
        // Arrow provides a Monad instance for Either. Except for the types signatures, our program remains unchanged when we compute over Either. All values on the left side assume to be Right biased and, whenever a Left value is found, the computation short-circuits, producing a result that is compatible with the function type signature.
        either {
            (command.fetchStateEither().bind() ?: decider.initialState)
                .calculateNewStateEither(command).bind()
                .saveEither().bind()
        }

    /**
     * Handles the [Flow] of command messages of type [C]
     *
     * @param commands Command messages of type [Flow]<[C]>
     * @return [Flow] of [Either] [Error] or [S]/State
     */
    fun handleEither(commands: Flow<C>): Flow<Either<Error, S>> =
        commands.map { handleEither(it) }

    /**
     * Handles the command message of type [C]
     *
     * @param command Command message of type [C]
     * @return [S]/State
     */
    suspend fun handle(command: C): S =
        (command.fetchState() ?: decider.initialState)
            .calculateNewState(command)
            .save()

    /**
     * Handles the [Flow] of command messages of type [C]
     *
     * @param commands Command messages of type [Flow]<[C]>
     * @return [Flow] of [S]/State
     */
    fun handle(commands: Flow<C>): Flow<S> =
        commands.map { handle(it) }


    private suspend fun S.calculateNewStateEither(command: C): Either<Error, S> =
        Either.catch {
            calculateNewState(command)
        }.mapLeft { throwable ->
            Error.CalculatingNewStateFailed(this, command, throwable)
        }

    private suspend fun S.calculateNewState(command: C): S {
        val events = decider.decide(command, this)
        val newState = events.fold(this@calculateNewState, decider.evolve)
        if (saga != null) events.flatMapConcat { saga.react(it) }.collect { newState.calculateNewState(it) }
        return newState
    }

}

/**
 * Extension function - Publishes the command of type [C] to the event sourcing aggregate of type  [StateStoredAggregate]<[C], [S], *>
 * @receiver command of type [C]
 * @param aggregate of type [StateStoredAggregate]<[C], [S], *>
 * @return the stored state/[S]
 */
suspend fun <C, S> C.publishTo(aggregate: StateStoredAggregate<C, S, *>): S = aggregate.handle(this)

/**
 * Extension function - Publishes the command of type [C] to the event sourcing aggregate of type  [StateStoredAggregate]<[C], [S], *>
 * @receiver [Flow] of commands of type [C]
 * @param aggregate of type [StateStoredAggregate]<[C], [S], *>
 * @return the [Flow] of stored state/[S]
 */
fun <C, S> Flow<C>.publishTo(aggregate: StateStoredAggregate<C, S, *>): Flow<S> = aggregate.handle(this)

/**
 * Extension function - Publishes the command of type [C] to the event sourcing aggregate of type  [StateStoredAggregate]<[C], [S], *>
 * @receiver command of type [C]
 * @param aggregate of type [StateStoredAggregate]<[C], [S], *>
 * @return [Either] [Error] or successfully stored state/[S]
 */
suspend fun <C, S> C.publishEitherTo(aggregate: StateStoredAggregate<C, S, *>): Either<Error, S> =
    aggregate.handleEither(this)

/**
 * Extension function - Publishes the command of type [C] to the event sourcing aggregate of type  [StateStoredAggregate]<[C], [S], *>
 * @receiver [Flow] of commands of type [C]
 * @param aggregate of type [StateStoredAggregate]<[C], [S], *>
 * @return the [Flow] of [Either] [Error] or successfully  stored state/[S]
 */
fun <C, S> Flow<C>.publishToEither(aggregate: StateStoredAggregate<C, S, *>): Flow<Either<Error, S>> =
    aggregate.handleEither(this)
