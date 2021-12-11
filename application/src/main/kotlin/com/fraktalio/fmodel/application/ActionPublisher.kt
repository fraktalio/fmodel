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
 * Action publisher interface
 *
 * Used by Process Manager and Saga Manager
 *
 * @param A Action
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
interface ActionPublisher<A> {
    suspend fun A.publish(): A

    suspend fun A.publishEither(): Either<Error.PublishingActionFailed<A>, A> =
        Either.catch {
            this.publish()
        }.mapLeft { throwable -> Error.PublishingActionFailed(this, throwable) }

    suspend fun Sequence<A>.publishEither(): Either<Error.PublishingActionFailed<A>, Sequence<A>> =
        either<Error.PublishingActionFailed<A>, List<A>> {
            this@publishEither.asIterable().map { it.publishEither().bind() }
        }.map { it.asSequence() }
}
