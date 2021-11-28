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

package com.fraktalio.fmodel.application.examples.numbers.odd.command

import com.fraktalio.fmodel.application.EventRepository
import com.fraktalio.fmodel.domain.examples.numbers.api.NumberCommand.OddNumberCommand
import com.fraktalio.fmodel.domain.examples.numbers.api.NumberEvent.OddNumberEvent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * A very simple event store ;)  It is initially empty.
 */
private var oddNumberEventStorage: List<OddNumberEvent?> = emptyList()
private val oddNumberEventStorageMutex = Mutex()

/**
 * Odd number repository
 *
 * @constructor Creates Odd number repository
 */
class OddNumberRepository : EventRepository<OddNumberCommand?, OddNumberEvent?> {

    override fun OddNumberCommand?.fetchEvents(): Flow<OddNumberEvent?> =
        oddNumberEventStorage.asFlow()


    override suspend fun OddNumberEvent?.save(): OddNumberEvent? {
        oddNumberEventStorageMutex.withLock {
            oddNumberEventStorage = oddNumberEventStorage.plus(this)
        }
        return this
    }

}

/**
 * Odd number repository
 *
 * @return Odd number repository instance
 */
fun oddNumberRepository(): EventRepository<OddNumberCommand?, OddNumberEvent?> =
    OddNumberRepository()

