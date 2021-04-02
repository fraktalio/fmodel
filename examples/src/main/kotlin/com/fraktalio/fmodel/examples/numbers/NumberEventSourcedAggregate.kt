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
import arrow.core.Either.Companion.catch
import com.fraktalio.fmodel.datatypes.Error
import com.fraktalio.fmodel.datatypes.EventSourcingAggregate
import com.fraktalio.fmodel.datatypes.Success
import com.fraktalio.fmodel.datatypes.combineDeciders
import com.fraktalio.fmodel.examples.numbers.api.EvenNumberState
import com.fraktalio.fmodel.examples.numbers.api.NumberCommand
import com.fraktalio.fmodel.examples.numbers.api.NumberEvent
import com.fraktalio.fmodel.examples.numbers.api.OddNumberState
import com.fraktalio.fmodel.examples.numbers.even.command.EVEN_NUMBER_DECIDER
import com.fraktalio.fmodel.examples.numbers.odd.command.ODD_NUMBER_DECIDER
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Represents the very simple event store ;)  It is initially empty.
 */
var numberEventStorage: List<NumberEvent> = emptyList()
val numberEventStorageMutex = Mutex()

/**
 * EventSourcingAggregate instance for the all Numbers.
 */
val NUMBER_AGGREGATE: EventSourcingAggregate<Either<NumberCommand.EvenNumberCommand?, NumberCommand.OddNumberCommand?>, Pair<EvenNumberState, OddNumberState>, Either<NumberEvent.EvenNumberEvent?, NumberEvent.OddNumberEvent?>> =
    EventSourcingAggregate(
        decider = EVEN_NUMBER_DECIDER.combineDeciders(ODD_NUMBER_DECIDER),
        fetchEvents = {
            catch {
                numberEventStorage.map { numberEvent ->
                    when (numberEvent) {
                        is NumberEvent.EvenNumberEvent -> Either.Left(numberEvent)
                        is NumberEvent.OddNumberEvent -> Either.Right(numberEvent)
                    }
                }
            }.mapLeft { throwable -> Error.FetchingEventsFailed(throwable) }
        },
        storeEvents = {
            catch {
                numberEventStorageMutex.withLock {
                    numberEventStorage = numberEventStorage.plus(it.map { either ->
                        when (either) {
                            is Either.Left -> either.value
                            is Either.Right -> either.value
                        }
                    }.filterNotNull())
                }
                //Call event handlers explicitly
                it.toList().forEach { e -> NUMBER_MATERIALIZED_VIEW.handle(e) }
                Success.EventsStoredSuccessfully(it)

            }.mapLeft { throwable -> Error.StoringEventsFailed(it, throwable) }

        }
    )
