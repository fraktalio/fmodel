/*
 * Copyright (c) 2022 Fraktalio D.O.O. All rights reserved.
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

import com.fraktalio.fmodel.application.EventRepository
import com.fraktalio.fmodel.domain.examples.numbers.api.NumberCommand
import com.fraktalio.fmodel.domain.examples.numbers.api.NumberEvent
import com.fraktalio.fmodel.domain.examples.numbers.api.NumberEvent.EvenNumberEvent
import com.fraktalio.fmodel.domain.examples.numbers.api.NumberEvent.OddNumberEvent
import kotlinx.coroutines.flow.*

/**
 * A very simple event store ;)  It is initially empty.
 */
private var numberEventStorage: List<NumberEvent?> = emptyList()

/**
 * Number (event) repository
 *
 * @constructor Creates Number repository
 */
class NumberRepository : EventRepository<NumberCommand?, NumberEvent?> {

    override fun NumberCommand?.fetchEvents(): Flow<NumberEvent?> =
        numberEventStorage.asFlow().map { numberEvent ->
            when (numberEvent) {
                is EvenNumberEvent -> numberEvent
                is OddNumberEvent -> numberEvent
                else -> throw UnsupportedOperationException("fetched null event from the event store")
            }
        }

    override fun Flow<NumberEvent?>.save(): Flow<NumberEvent?> = flow {
        numberEventStorage = numberEventStorage.plus(this@save.toList())
        emitAll(numberEventStorage.asFlow())
    }

    fun deleteAll() {
        numberEventStorage = emptyList()
    }
}

/**
 * Number repository
 *
 * @return Number repository instance
 */
fun numberRepository(): EventRepository<NumberCommand?, NumberEvent?> =
    NumberRepository()

