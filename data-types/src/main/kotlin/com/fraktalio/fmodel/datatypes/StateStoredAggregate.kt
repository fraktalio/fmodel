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

package com.fraktalio.fmodel.datatypes

import arrow.core.Either
import arrow.core.computations.either
import arrow.higherkind

/**
 * State stored aggregate is using/delegating a [Decider] to handle commands and produce new state.
 * In order to handle the command, aggregate needs to fetch the current state via [fetchState] function first, and then delegate the command to the decider which can produce new state as a result.
 * New state is then stored via [storeState] suspending function.
 * It is the responsibility of the user to implement these functions [fetchState] and [storeState] per need.
 * These two functions are producing side effects (infrastructure), and they are deliberately separated from the decider (pure domain logic).
 *
 * @param C Commands of type [C] that this aggregate can handle
 * @param S Aggregate state of type [S]
 * @param E Events of type [E] that are used internally to build/fold new state
 * @property decider A decider component of type [Decider]<[C], [S], [E]>.
 * @property storeState A suspending function that takes the newly produced state by [Decider] and stores it (produces side effect by modifying object/data outside its own scope) by resulting with [either] error [Error.StoringStateFailed] or success [Success.StateStoredSuccessfully]
 * @property fetchState A suspending function that takes the command of type [C] and results with [either] error [Error.FetchingStateFailed] or success [Iterable]<[E]>
 * @constructor Creates [StateStoredAggregate]
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
@higherkind
data class StateStoredAggregate<C, S, E>(
    val decider: Decider<C, S, E>,
    val storeState: suspend (S) -> Either<Error.StoringStateFailed<S>, Success.StateStoredSuccessfully<S>>,
    val fetchState: suspend (C) -> Either<Error.FetchingStateFailed, S?>
) : StateStoredAggregateOf<C, S, E> {

    /**
     * Handles the command message of type [C]
     *
     * @param command Command message of type [C]
     * @return Either [Error] or [Success]
     */
    suspend fun handle(command: C): Either<Error, Success> =
        // Arrow provides a Monad instance for Either. Except for the types signatures, our program remains unchanged when we compute over Either. All values on the left side assume to be Right biased and, whenever a Left value is found, the computation short-circuits, producing a result that is compatible with the function type signature.
        either {
            val currentState = fetchState(command).bind()
            val state = validate(currentState ?: decider.initialState).bind()
            storeState(decider.decide(command, state).fold(state, decider.evolve)).bind()
        }

    private fun validate(state: S): Either<Error, S> {
        return if (decider.isTerminal(state)) Either.left(Error.AggregateIsInTerminalState(state))
        else Either.right(state)
    }

    companion object
}

