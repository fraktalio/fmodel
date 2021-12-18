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

    suspend fun S?.eitherComputeNewStateOrFail(command: C): Either<Error, S> =
        Either.catch {
            computeNewState(command)
        }.mapLeft { throwable ->
            Error.CalculatingNewStateFailed(this, throwable)
        }

    // Arrow provides a Monad instance for Either. Except for the types signatures, our program remains unchanged when we compute over Either. All values on the left side assume to be Right biased and, whenever a Left value is found, the computation short-circuits, producing a result that is compatible with the function type signature.
    return either {
        command
            .eitherFetchStateOrFail().bind()
            .eitherComputeNewStateOrFail(command).bind()
            .eitherSaveOrFail().bind()
    }
}
