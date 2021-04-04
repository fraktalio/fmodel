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

package com.fraktalio.fmodel.examples.numbers.even.command

import arrow.core.Either
import com.fraktalio.fmodel.application.AggregateEventRepository
import com.fraktalio.fmodel.application.Success
import com.fraktalio.fmodel.examples.numbers.api.NumberCommand
import com.fraktalio.fmodel.examples.numbers.api.NumberEvent
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * A very simple event store ;)  It is initially empty.
 */
private var evenNumberEventStorage: List<NumberEvent.EvenNumberEvent?> = emptyList()
private val evenNumberEventStorageMutex = Mutex()

/**
 * Even number repository implementation
 *
 * @constructor Creates Even number repository
 */
class EvenNumberRepository : AggregateEventRepository<NumberCommand.EvenNumberCommand?, NumberEvent.EvenNumberEvent?> {

    override suspend fun NumberCommand.EvenNumberCommand?.fetchEvents(): Either<Error.FetchingEventsFailed, Iterable<NumberEvent.EvenNumberEvent?>> =
        Either.catch {
            evenNumberEventStorage
        }.mapLeft { throwable -> Error.FetchingEventsFailed(throwable) }


    override suspend fun NumberEvent.EvenNumberEvent?.save(): Either<Error.StoringEventFailed<NumberEvent.EvenNumberEvent?>, Success.EventStoredSuccessfully<NumberEvent.EvenNumberEvent?>> =
        Either.catch {
            evenNumberEventStorageMutex.withLock {
                evenNumberEventStorage = evenNumberEventStorage.plus(this)
            }
            Success.EventStoredSuccessfully(this)
        }.mapLeft { throwable -> Error.StoringEventFailed(this, throwable) }
}

/**
 * Even number repository
 *
 * @return event repository instance for Even numbers
 */
fun evenNumberRepository(): AggregateEventRepository<NumberCommand.EvenNumberCommand?, NumberEvent.EvenNumberEvent?> =
    EvenNumberRepository()

