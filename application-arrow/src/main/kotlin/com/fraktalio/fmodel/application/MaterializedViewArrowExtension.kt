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

/**
 * Extension function - Handles the event of type [E]
 *
 * @param event Event of type [E] to be handled
 * @return State of type [S]
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
suspend fun <S, E> MaterializedView<S, E>.handle(event: E): S =
    event
        .fetchState()
        .computeNewState(event)
        .save()


suspend fun <S, E> MaterializedView<S, E>.handleEither(event: E): Either<Error, S> {
    /**
     * Inner function - Computes new State based on the Event or fails.
     *
     * @param event of type [E]
     * @return The newly computed state of type [S] or [Error]
     */
    fun S?.eitherComputeNewStateOrFail(event: E): Either<Error, S> =
        Either.catch {
            computeNewState(event)
        }.mapLeft { throwable -> Error.CalculatingNewStateFailed(this, throwable) }

    /**
     * Inner function - Fetch state - either version
     *
     * @receiver Event of type [E]
     * @return [Either] [Error] or the State of type [S]?
     */
    suspend fun E.eitherFetchStateOrFail(): Either<Error.FetchingViewStateFailed<E>, S?> =
        Either.catch {
            fetchState()
        }.mapLeft { throwable -> Error.FetchingViewStateFailed(this, throwable) }

    /**
     * Inner function - Save state - either version
     *
     * @receiver State of type [S]
     * @return [Either] [Error] or the newly saved State of type [S]
     */
    suspend fun S.eitherSaveOrFail(): Either<Error.StoringStateFailed<S>, S> =
        Either.catch {
            this.save()
        }.mapLeft { throwable -> Error.StoringStateFailed(this, throwable) }

    // Arrow provides a Monad instance for Either. Except for the types signatures, our program remains unchanged when we compute over Either. All values on the left side assume to be Right biased and, whenever a Left value is found, the computation short-circuits, producing a result that is compatible with the function type signature.
    return either {
        event
            .eitherFetchStateOrFail().bind()
            .eitherComputeNewStateOrFail(event).bind()
            .eitherSaveOrFail().bind()
    }
}
