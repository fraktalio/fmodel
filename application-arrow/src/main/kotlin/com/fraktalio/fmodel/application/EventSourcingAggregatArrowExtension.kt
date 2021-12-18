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
 * Extension function - Handles the command message of type [C]
 *
 * @param command Command message of type [C]
 * @return [Either] [Error] or [Sequence] of Events of type [E] that are saved
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
suspend fun <C, S, E> EventSourcingAggregate<C, S, E>.handleEither(command: C): Either<Error, Sequence<E>> {

    suspend fun C.eitherFetchEventsOrFail(): Either<Error.FetchingEventsFailed, Sequence<E>> =
        Either.catch {
            fetchEvents()
        }.mapLeft { throwable -> Error.FetchingEventsFailed(throwable) }

    suspend fun E.eitherSaveOrFail(): Either<Error.StoringEventFailed<E>, E> =
        Either.catch {
            this.save()
        }.mapLeft { throwable -> Error.StoringEventFailed(this, throwable) }

    suspend fun Sequence<E>.eitherSaveOrFail(): Either<Error.StoringEventFailed<E>, Sequence<E>> =
        either<Error.StoringEventFailed<E>, List<E>> {
            this@eitherSaveOrFail.asIterable().map { it.eitherSaveOrFail().bind() }
        }.map { it.asSequence() }

    fun Sequence<E>.eitherComputeNewEventsOrFail(command: C): Either<Error, Sequence<E>> =
        Either.catch {
            computeNewEvents(command)
        }.mapLeft { throwable ->
            Error.CalculatingNewEventsFailed(this.toList(), throwable)
        }

    // Arrow provides a Monad instance for Either. Except for the types signatures, our program remains unchanged when we compute over Either. All values on the left side assume to be Right biased and, whenever a Left value is found, the computation short-circuits, producing a result that is compatible with the function type signature.
    return either {
        command
            .eitherFetchEventsOrFail().bind()
            .eitherComputeNewEventsOrFail(command).bind()
            .eitherSaveOrFail().bind()
    }
}
