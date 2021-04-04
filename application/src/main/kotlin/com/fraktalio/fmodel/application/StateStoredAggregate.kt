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

/**
 * State stored aggregate is using/delegating a [Decider] to handle commands and produce new state.
 * In order to handle the command, aggregate needs to fetch the current state via [AggregateStateRepository.fetchState] function first, and then delegate the command to the decider which can produce new state as a result.
 * New state is then stored via [AggregateStateRepository.save] suspending function.
 *
 * [StateStoredAggregate] implements an interface [AggregateStateRepository] by delegating all of its public members to a specified object.
 * The Delegation pattern has proven to be a good alternative to implementation inheritance, and Kotlin supports it natively requiring zero boilerplate code.
 *
 * @param C Commands of type [C] that this aggregate can handle
 * @param S Aggregate state of type [S]
 * @param E Events of type [E] that are used internally to build/fold new state
 * @property decider A decider component of type [Decider]<[C], [S], [E]>.
 * @property aggregateStateRepository Interface for [S]tate management/persistence - dependencies by delegation
 * @constructor Creates [StateStoredAggregate]
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
data class StateStoredAggregate<C, S, E>(
    private val decider: Decider<C, S, E>,
    private val aggregateStateRepository: AggregateStateRepository<C, S>
) : AggregateStateRepository<C, S> by aggregateStateRepository {

    /**
     * Handles the command message of type [C]
     *
     * @param command Command message of type [C]
     * @return Either [Error] or [Success]
     */
    suspend fun handle(command: C): Either<Error, Success.StateStoredAndEventsPublishedSuccessfully<S, E>> =
        // Arrow provides a Monad instance for Either. Except for the types signatures, our program remains unchanged when we compute over Either. All values on the left side assume to be Right biased and, whenever a Left value is found, the computation short-circuits, producing a result that is compatible with the function type signature.
        either {
            val currentState = command.fetchState().bind()
            val state = validate(currentState ?: decider.initialState).bind()
            val events = decider.decide(command, state)
            events.fold(state, decider.evolve).save()
                .map { s -> Success.StateStoredAndEventsPublishedSuccessfully(s.state, events) }.bind()
        }

    private fun validate(state: S): Either<Error, S> =
        if (decider.isTerminal(state)) Either.Left(Error.AggregateIsInTerminalState(state))
        else Either.Right(state)
}

