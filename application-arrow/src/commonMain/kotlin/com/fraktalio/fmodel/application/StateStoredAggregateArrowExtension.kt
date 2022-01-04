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
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

/**
 * Extension function - Handles the command message of type [C]
 *
 * @param command Command message of type [C]
 * @return Either [Error] or State of type [S]
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
@FlowPreview
suspend fun <C, S, E> StateStoredAggregate<C, S, E>.handleEither(command: C): Either<Error, S> {
    /**
     * Inner function - Computes new State based on the previous State and the [command] or fails.
     *
     * @param command of type [C]
     * @return [Either] the newly computed state of type [S] or [Error]
     */
    suspend fun S?.eitherComputeNewStateOrFail(command: C): Either<Error, S> =
        Either.catch {
            computeNewState(command)
        }.mapLeft { throwable ->
            Error.CalculatingNewStateFailed(this, command, throwable)
        }

    /**
     * Inner function - Fetch state - either version
     *
     * @receiver Command of type [C]
     * @return [Either] [Error] or the State of type [S]?
     */
    suspend fun C.eitherFetchStateOrFail(): Either<Error.FetchingStateFailed<C>, S?> =
        Either.catch {
            fetchState()
        }.mapLeft { throwable -> Error.FetchingStateFailed(this, throwable) }

    /**
     * Inner function - Save state - either version
     *
     * @receiver State of type [S]
     * @return [Either] [Error] or the newly saved State of type [S]
     */
    suspend fun S.eitherSaveOrFail(): Either<Error.StoringStateFailed<S>, S> =
        Either.catch {
            save()
        }.mapLeft { throwable -> Error.StoringStateFailed(this, throwable) }

    // Arrow provides a Monad instance for Either. Except for the types signatures, our program remains unchanged when we compute over Either. All values on the left side assume to be Right biased and, whenever a Left value is found, the computation short-circuits, producing a result that is compatible with the function type signature.
    return either {
        command.eitherFetchStateOrFail().bind()
            .eitherComputeNewStateOrFail(command).bind()
            .eitherSaveOrFail().bind()
    }
}

/**
 * Extension function - Handles the [Flow] of command messages of type [C]
 *
 * @param commands [Flow] of Command messages of type [C]
 * @return [Flow] of [Either] [Error] or State of type [S]
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
@FlowPreview
fun <C, S, E> StateStoredAggregate<C, S, E>.handleEither(commands: Flow<C>): Flow<Either<Error, S>> =
    commands
        .map { handleEither(it) }
        .catch { emit(Either.Left(Error.CommandPublishingFailed(it))) }

/**
 * Extension function - Publishes the command of type [C] to the state stored aggregate of type  [StateStoredAggregate]<[C], [S], *>
 * @receiver command of type [C]
 * @param aggregate of type [StateStoredAggregate]<[C], [S], *>
 * @return [Either] [Error] or successfully stored State of type [S]
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
@FlowPreview
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
@FlowPreview
fun <C, S> Flow<C>.publishEitherTo(aggregate: StateStoredAggregate<C, S, *>): Flow<Either<Error, S>> =
    aggregate.handleEither(this)
