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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

/**
 * Event repository/store interface
 *
 * @param C Command
 * @param E Event
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
interface EventRepository<C, E> {
    fun C.fetchEvents(): Flow<E>
    suspend fun E.save(): E

    fun C.fetchEventsEither(): Flow<Either<Error.FetchingEventsFailed, E>> =
        fetchEvents()
            .map { Either.Right(it) }
            .catch<Either<Error.FetchingEventsFailed, E>> {
                emit(Either.Left(Error.FetchingEventsFailed(it)))
            }

    fun Flow<E>.save(): Flow<E> = map { it.save() }

    suspend fun E.saveEither(): Either<Error.StoringEventFailed<E>, Success.EventStoredSuccessfully<E>> =
        Either.catch {
            val storedEvent = this.save()
            Success.EventStoredSuccessfully<E>(storedEvent)
        }.mapLeft { throwable -> Error.StoringEventFailed(this, throwable) }

    fun Flow<E>.saveEither(): Flow<Either<Error.StoringEventFailed<E>, Success.EventStoredSuccessfully<E>>> =
        map { it.saveEither() }


}
