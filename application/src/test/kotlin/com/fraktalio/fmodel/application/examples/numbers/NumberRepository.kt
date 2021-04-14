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

package com.fraktalio.fmodel.application.examples.numbers

import arrow.core.Either
import com.fraktalio.fmodel.application.Error
import com.fraktalio.fmodel.application.EventRepository
import com.fraktalio.fmodel.application.Success
import com.fraktalio.fmodel.domain.examples.numbers.api.NumberCommand.EvenNumberCommand
import com.fraktalio.fmodel.domain.examples.numbers.api.NumberCommand.OddNumberCommand
import com.fraktalio.fmodel.domain.examples.numbers.api.NumberEvent
import com.fraktalio.fmodel.domain.examples.numbers.api.NumberEvent.EvenNumberEvent
import com.fraktalio.fmodel.domain.examples.numbers.api.NumberEvent.OddNumberEvent
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * A very simple event store ;)  It is initially empty.
 */
private var numberEventStorage: List<NumberEvent?> = emptyList()
private val numberEventStorageMutex = Mutex()

/**
 * Number (event) repository
 *
 * @constructor Creates Number repository
 */
class NumberRepository :
    EventRepository<Either<EvenNumberCommand?, OddNumberCommand?>, Either<EvenNumberEvent?, OddNumberEvent?>> {

    override suspend fun Either<EvenNumberCommand?, OddNumberCommand?>.fetchEvents(): Either<Error.FetchingEventsFailed, Iterable<Either<EvenNumberEvent?, OddNumberEvent?>>> =
        Either.catch {
            numberEventStorage.map { numberEvent ->
                when (numberEvent) {
                    is EvenNumberEvent -> Either.Left(numberEvent)
                    is OddNumberEvent -> Either.Right(numberEvent)
                    else -> throw UnsupportedOperationException("fetched null event from the event store")
                }
            }
        }.mapLeft { throwable -> Error.FetchingEventsFailed(throwable) }


    override suspend fun Either<EvenNumberEvent?, OddNumberEvent?>.save(): Either<Error.StoringEventFailed<Either<EvenNumberEvent?, OddNumberEvent?>>, Success.EventStoredSuccessfully<Either<EvenNumberEvent?, OddNumberEvent?>>> =
        Either.catch {
            numberEventStorageMutex.withLock {
                numberEventStorage = numberEventStorage.plus(
                    when (this) {
                        is Either.Left -> this.value
                        is Either.Right -> this.value
                    }
                )
            }
            Success.EventStoredSuccessfully(this)
        }.mapLeft { throwable -> Error.StoringEventFailed(this, throwable) }
}

/**
 * Number repository
 *
 * @return Number repository instance
 */
fun numberRepository(): EventRepository<Either<EvenNumberCommand?, OddNumberCommand?>, Either<EvenNumberEvent?, OddNumberEvent?>> =
    NumberRepository()

