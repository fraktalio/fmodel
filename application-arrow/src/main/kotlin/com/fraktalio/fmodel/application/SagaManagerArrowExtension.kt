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
 * Extension function - Handles the action result of type [AR].
 *
 * @param actionResult Action Result represent the outcome of some action you want to handle in some way
 * @return [Either] [Error] or [Sequence] of Actions of type [A]
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
suspend fun <AR, A> SagaManager<AR, A>.handleEither(actionResult: AR): Either<Error, Sequence<A>> {
    suspend fun A.eitherPublishOrFail(): Either<Error.PublishingActionFailed<A>, A> =
        Either.catch {
            this.publish()
        }.mapLeft { throwable -> Error.PublishingActionFailed(this, throwable) }

    suspend fun Sequence<A>.eitherPublishOrFail(): Either<Error.PublishingActionFailed<A>, Sequence<A>> =
        either {
            asIterable().map { it.eitherPublishOrFail().bind() }.asSequence()
        }

    fun AR.eitherCalculateNewActionsOrFail(): Either<Error, Sequence<A>> =
        Either.catch {
            react(this)
        }.mapLeft { throwable ->
            Error.CalculatingNewActionsFailed(this, throwable)
        }

    return either {
        actionResult
            .eitherCalculateNewActionsOrFail().bind()
            .eitherPublishOrFail().bind()
    }
}
