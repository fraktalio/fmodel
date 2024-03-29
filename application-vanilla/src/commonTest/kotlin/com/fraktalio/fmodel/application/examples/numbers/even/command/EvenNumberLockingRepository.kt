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

import com.fraktalio.fmodel.application.EventLockingRepository
import com.fraktalio.fmodel.application.LatestVersionProvider
import com.fraktalio.fmodel.domain.examples.numbers.api.NumberCommand.EvenNumberCommand
import com.fraktalio.fmodel.domain.examples.numbers.api.NumberEvent.EvenNumberEvent
import kotlinx.coroutines.flow.*

/**
 * A very simple event store ;)  It is initially empty.
 */
private var evenNumberEventStorage: List<Pair<EvenNumberEvent, Long>> = emptyList()

/**
 * Even number repository implementation
 *
 * @constructor Creates Even number repository
 */
class EvenNumberLockingRepository : EventLockingRepository<EvenNumberCommand?, EvenNumberEvent?, Long> {

    override fun EvenNumberCommand?.fetchEvents(): Flow<Pair<EvenNumberEvent, Long>> =

        evenNumberEventStorage.asFlow()


    override val latestVersionProvider: LatestVersionProvider<EvenNumberEvent?, Long> =
        { evenNumberEventStorage.lastOrNull()?.second }


    override fun Flow<EvenNumberEvent?>.save(latestVersion: Long?): Flow<Pair<EvenNumberEvent, Long>> =
        filterNotNull().map {
            val result = Pair(it, if (latestVersion != null) latestVersion + 1 else 1)
            evenNumberEventStorage = evenNumberEventStorage.plus(result)
            result
        }


    override fun Flow<EvenNumberEvent?>.save(latestVersionProvider: LatestVersionProvider<EvenNumberEvent?, Long>): Flow<Pair<EvenNumberEvent, Long>> =
        flow {
            emitAll(save(latestVersionProvider(this@save.firstOrNull())))
        }

    fun deleteAll() {
        evenNumberEventStorage = emptyList()

    }

}

/**
 * Even number locking repository
 *
 * @return event locking repository instance for Even numbers
 */
fun evenNumberLockingRepository(): EventLockingRepository<EvenNumberCommand?, EvenNumberEvent?, Long> =
    EvenNumberLockingRepository()

