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

import arrow.core.Either.Companion.catch
import com.fraktalio.fmodel.datatypes.Error
import com.fraktalio.fmodel.datatypes.EventSourcingAggregate
import com.fraktalio.fmodel.datatypes.Success
import com.fraktalio.fmodel.examples.numbers.api.EvenNumberState
import com.fraktalio.fmodel.examples.numbers.api.NumberCommand
import com.fraktalio.fmodel.examples.numbers.api.NumberEvent
import com.fraktalio.fmodel.examples.numbers.even.query.EVEN_NUMBER_MATERIALIZED_VIEW
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Represents the very simple event store ;)  It is initially empty.
 */
var evenNumberEventStorage: List<NumberEvent.EvenNumberEvent?> = emptyList()
val evenNumberEventStorageMutex = Mutex()

/**
 * EventSourcingAggregate instance for the Even Numbers.
 */
val EVEN_NUMBER_AGGREGATE: EventSourcingAggregate<NumberCommand.EvenNumberCommand?, EvenNumberState, NumberEvent.EvenNumberEvent?> =
    EventSourcingAggregate(
        decider = EVEN_NUMBER_DECIDER,
        fetchEvents = {
            catch {
                evenNumberEventStorage
            }.mapLeft { throwable -> Error.FetchingEventsFailed(throwable) }
        },
        storeEvents = {
            catch {
                evenNumberEventStorageMutex.withLock {
                    evenNumberEventStorage = evenNumberEventStorage.plus(it)
                }
                it.filterNotNull().forEach { e -> EVEN_NUMBER_MATERIALIZED_VIEW.handle(e) }
                Success.EventsStoredSuccessfully(it)

            }.mapLeft { throwable -> Error.StoringEventsFailed(it, throwable) }

        }
    )
