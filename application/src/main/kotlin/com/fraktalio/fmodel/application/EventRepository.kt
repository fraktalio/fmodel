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
 * Event repository/store interface
 *
 * @param C Command
 * @param E Event
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
interface EventRepository<C, E> {
    suspend fun C.fetchEvents(): Sequence<E>
    suspend fun E.save(): E

    suspend fun C.fetchEventsEither(): Either<Error.FetchingEventsFailed, Sequence<E>> =
        Either.catch {
            fetchEvents()
        }.mapLeft { throwable -> Error.FetchingEventsFailed(throwable) }

    suspend fun E.saveEither(): Either<Error.StoringEventFailed<E>, E> =
        Either.catch {
            this.save()
        }.mapLeft { throwable -> Error.StoringEventFailed(this, throwable) }

    suspend fun Iterable<E>.saveEither(): Either<Error.StoringEventFailed<E>, Sequence<E>> =
        either<Error.StoringEventFailed<E>, List<E>> { map { it.saveEither().bind() } }.map { it.asSequence() }
}
