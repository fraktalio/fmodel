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

/**
 * View state repository interface
 *
 * @param E Event
 * @param S State
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
interface ViewStateRepository<E, S> {
    suspend fun E.fetchState(): S?
    suspend fun E.fetchStateEither(): Either<Error.FetchingStateFailed, S?> =
        Either.catch {
            fetchState()
        }.mapLeft { throwable -> Error.FetchingStateFailed(throwable) }

    suspend fun S.save(): S
    suspend fun S.saveEither(): Either<Error.StoringStateFailed<S>, Success.StateStoredSuccessfully<S>> =
        Either.catch {
            val state = this.save()
            Success.StateStoredSuccessfully(state)
        }.mapLeft { throwable -> Error.StoringStateFailed(this, throwable) }
}
