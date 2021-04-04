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

package com.fraktalio.fmodel.examples.numbers

import arrow.core.Either
import com.fraktalio.fmodel.application.AggregateEventRepository
import com.fraktalio.fmodel.application.Error
import com.fraktalio.fmodel.application.Success
import com.fraktalio.fmodel.examples.numbers.api.NumberCommand
import com.fraktalio.fmodel.examples.numbers.api.NumberEvent
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
    AggregateEventRepository<Either<NumberCommand.EvenNumberCommand?, NumberCommand.OddNumberCommand?>, Either<NumberEvent.EvenNumberEvent?, NumberEvent.OddNumberEvent?>> {

    override suspend fun Either<NumberCommand.EvenNumberCommand?, NumberCommand.OddNumberCommand?>.fetchEvents(): Either<Error.FetchingEventsFailed, Iterable<Either<NumberEvent.EvenNumberEvent?, NumberEvent.OddNumberEvent?>>> =
        Either.catch {
            numberEventStorage.map { numberEvent ->
                when (numberEvent) {
                    is NumberEvent.EvenNumberEvent -> Either.Left(numberEvent)
                    is NumberEvent.OddNumberEvent -> Either.Right(numberEvent)
                    null -> throw UnsupportedOperationException("fetched null event from the event store")
                }
            }
        }.mapLeft { throwable -> Error.FetchingEventsFailed(throwable) }


    override suspend fun Either<NumberEvent.EvenNumberEvent?, NumberEvent.OddNumberEvent?>.save(): Either<Error.StoringEventFailed<Either<NumberEvent.EvenNumberEvent?, NumberEvent.OddNumberEvent?>>, Success.EventStoredSuccessfully<Either<NumberEvent.EvenNumberEvent?, NumberEvent.OddNumberEvent?>>> =
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
fun numberRepository(): AggregateEventRepository<Either<NumberCommand.EvenNumberCommand?, NumberCommand.OddNumberCommand?>, Either<NumberEvent.EvenNumberEvent?, NumberEvent.OddNumberEvent?>> =
    NumberRepository()

