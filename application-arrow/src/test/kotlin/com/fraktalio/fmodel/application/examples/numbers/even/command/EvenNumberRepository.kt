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

package com.fraktalio.fmodel.application.examples.numbers.even.command

import com.fraktalio.fmodel.application.EventRepository
import com.fraktalio.fmodel.domain.examples.numbers.api.NumberCommand.EvenNumberCommand
import com.fraktalio.fmodel.domain.examples.numbers.api.NumberEvent.EvenNumberEvent
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * A very simple event store ;)  It is initially empty.
 */
private var evenNumberEventStorage: Sequence<EvenNumberEvent?> = emptySequence()
private val evenNumberEventStorageMutex = Mutex()

/**
 * Even number repository implementation
 *
 * @constructor Creates Even number repository
 */
class EvenNumberRepository : EventRepository<EvenNumberCommand?, EvenNumberEvent?> {

    override suspend fun EvenNumberCommand?.fetchEvents(): Sequence<EvenNumberEvent?> = evenNumberEventStorage


    override suspend fun EvenNumberEvent?.save(): EvenNumberEvent? {
        evenNumberEventStorageMutex.withLock {
            evenNumberEventStorage = evenNumberEventStorage.plus(this)
        }
        return this
    }

}

/**
 * Even number repository
 *
 * @return event repository instance for Even numbers
 */
fun evenNumberRepository(): EventRepository<EvenNumberCommand?, EvenNumberEvent?> =
    EvenNumberRepository()

