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

package com.fraktalio.fmodel.application.examples.numbers.even.command

import com.fraktalio.fmodel.application.EventRepository
import com.fraktalio.fmodel.domain.examples.numbers.api.NumberCommand.EvenNumberCommand
import com.fraktalio.fmodel.domain.examples.numbers.api.NumberEvent.EvenNumberEvent
import kotlinx.coroutines.flow.*

/**
 * A very simple event store ;)  It is initially empty.
 */
private var evenNumberEventStorage: List<EvenNumberEvent?> = emptyList()

/**
 * Even number repository implementation
 *
 * @constructor Creates Even number repository
 */
class EvenNumberRepository : EventRepository<EvenNumberCommand?, EvenNumberEvent?> {

    override fun EvenNumberCommand?.fetchEvents(): Flow<EvenNumberEvent?> =
        evenNumberEventStorage.asFlow()

    override fun Flow<EvenNumberEvent?>.save(): Flow<EvenNumberEvent?> = flow {
        evenNumberEventStorage = evenNumberEventStorage.plus(this@save.toList())
        emitAll(evenNumberEventStorage.asFlow())
    }

    fun deleteAll() {
        evenNumberEventStorage = emptyList()

    }
}

/**
 * Even number repository
 *
 * @return event repository instance for Even numbers
 */
fun evenNumberRepository(): EventRepository<EvenNumberCommand?, EvenNumberEvent?> =
    EvenNumberRepository()

