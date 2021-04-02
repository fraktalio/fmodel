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

/**
 * Process manager pattern is a durable event scheduler that encapsulates process specific logic and maintain a central point of control deciding what to execute next ([A]) once a process is completed.
 * Process managers maintain state ([S]) so for example say a payment was taken from a customer, the fact an order must now be sent to them is persisted in the process manager.
 *
 * @param AR Action Result of type [AR] that this process manager can handle. Action Result can be mapped from any event published by different aggregates.
 * @param S Process manager state of type [S]. Essentially, it represents the TO-DO list.
 * @param E Events of type [E] that are used internally to build/fold the new state of the process manager.
 * @param A Actions of type [A] that should be taken. Actions can be mapped to aggregate commands.
 * @property process A process component of type  [Process]<[AR], [S], [E], [A]>
 * @property publishActionsAndStoreState A suspending function that takes the newly produced state by [Process] and stores it by additionally publishing actions that should be taken further.
 * @property fetchState A suspending function that takes the action result of type [AR] and results with [either] error [Error.FetchingStateFailed] or success [S]?
 * @constructor Creates [ProcessManager]
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
data class ProcessManager<AR, S, E, A>(
    val process: Process<AR, S, E, A>,
    val publishActionsAndStoreState: suspend (S, Iterable<A>) -> Either<Error.PublishingActionsOrStoringStateFailed<S, A>, Success.ActionsPublishedAndStateStoredSuccessfully<S, A>>,
    val fetchState: suspend (AR) -> Either<Error.FetchingStateFailed, S?>
) {

    /**
     * Handles the action result of type [AR]
     *
     * @param actionResult Action Result represent the outcome of some action you want to handle in some way
     * @return Either [Error] or [Success]
     */
    suspend fun handle(actionResult: AR): Either<Error, Success.ActionsPublishedAndStateStoredSuccessfully<S, A>> =
        // Arrow provides a Monad instance for Either. Except for the types signatures, our program remains unchanged when we compute over Either. All values on the left side assume to be Right biased and, whenever a Left value is found, the computation short-circuits, producing a result that is compatible with the function type signature.
        either {
            val state = validate(fetchState(actionResult).bind() ?: process.initialState).bind()
            val events = process.ingest(actionResult, state)
            publishActionsAndStoreState(
                events.fold(state, process.evolve),
                events.map { process.react(state, it) }.flatten()
            ).bind()
        }

    private fun validate(state: S): Either<Error, S> {
        return if (process.isTerminal(state)) Either.Left(Error.ProcessManagerIsInTerminalState(state))
        else Either.Right(state)
    }

}

