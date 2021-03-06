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
 * State repository interface.
 *
 * Used by [StateStoredAggregate]
 *
 * @param C Command / ActionResult
 * @param S State
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
interface StateRepository<C, S> {
    /**
     * Fetch state
     *
     * @receiver Command of type [C]
     * @return the State of type [S] or null
     */
    suspend fun C.fetchState(): S?

    /**
     * Save state
     *
     * @receiver State of type [S]
     * @return newly saved State of type [S]
     */
    suspend fun S.save(): S

    /**
     * Fetch state - either version
     *
     * @receiver Command of type [C]
     * @return [Either] [Error] or the State of type [S]?
     */
    suspend fun C.fetchStateEither(): Either<Error.FetchingStateFailed<C>, S?> =
        Either.catch {
            fetchState()
        }.mapLeft { throwable -> Error.FetchingStateFailed(this, throwable) }

    /**
     * Save state - either version
     *
     * @receiver State of type [S]
     * @return [Either] [Error] or the newly saved State of type [S]
     */
    suspend fun S.saveEither(): Either<Error.StoringStateFailed<S>, S> =
        Either.catch {
            this.save()
        }.mapLeft { throwable -> Error.StoringStateFailed(this, throwable) }


}
