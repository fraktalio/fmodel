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

package com.fraktalio.fmodel.examples.numbers.odd.command

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
private var oddNumberEventStorage: List<NumberEvent.OddNumberEvent?> = emptyList()
private val oddNumberEventStorageMutex = Mutex()

/**
 * Odd number repository
 *
 * @constructor Creates Odd number repository
 */
class OddNumberRepository() : AggregateEventRepository<NumberCommand.OddNumberCommand?, NumberEvent.OddNumberEvent?> {

    override suspend fun NumberCommand.OddNumberCommand?.fetchEvents(): Either<Error.FetchingEventsFailed, Iterable<NumberEvent.OddNumberEvent?>> =
        Either.catch {
            oddNumberEventStorage
        }.mapLeft { throwable -> Error.FetchingEventsFailed(throwable) }


    override suspend fun NumberEvent.OddNumberEvent?.save(): Either<Error.StoringEventFailed<NumberEvent.OddNumberEvent?>, Success.EventStoredSuccessfully<NumberEvent.OddNumberEvent?>> =
        Either.catch {
            oddNumberEventStorageMutex.withLock {
                oddNumberEventStorage = oddNumberEventStorage.plus(this)
            }
            Success.EventStoredSuccessfully(this)
        }.mapLeft { throwable -> Error.StoringEventFailed(this, throwable) }
}

/**
 * Odd number repository
 *
 * @return Odd number repository instance
 */
fun oddNumberRepository(): AggregateEventRepository<NumberCommand.OddNumberCommand?, NumberEvent.OddNumberEvent?> =
    OddNumberRepository()

