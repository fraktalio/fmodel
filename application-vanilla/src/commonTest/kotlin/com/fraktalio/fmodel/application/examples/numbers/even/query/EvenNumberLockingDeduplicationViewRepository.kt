/*
 * Copyright (c) 2023 Fraktalio D.O.O. All rights reserved.
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

package com.fraktalio.fmodel.application.examples.numbers.even.query

import com.fraktalio.fmodel.application.ViewStateLockingDeduplicationRepository
import com.fraktalio.fmodel.domain.examples.numbers.api.Description
import com.fraktalio.fmodel.domain.examples.numbers.api.EvenNumberState
import com.fraktalio.fmodel.domain.examples.numbers.api.NumberEvent.EvenNumberEvent
import com.fraktalio.fmodel.domain.examples.numbers.api.NumberValue

/**
 * A very simple state store ;)  It is initially empty.
 */
private var evenNumberStateStorage: Pair<EvenNumberState, Long> =
    Pair(EvenNumberState(Description("0"), NumberValue(0)), 0)

/**
 * Even number repository implementation
 *
 * @constructor Creates Even number repository
 */
class EvenNumberLockingDeduplicationViewRepository :
    ViewStateLockingDeduplicationRepository<EvenNumberEvent?, EvenNumberState?, Long, Long> {

    fun deleteAll() {
        evenNumberStateStorage = Pair(EvenNumberState(Description("0"), NumberValue(0)), 0)
    }

    override suspend fun EvenNumberEvent?.fetchState(): Pair<EvenNumberState?, Long?> = evenNumberStateStorage

    override suspend fun EvenNumberState?.save(
        eventVersion: Long,
        currentStateVersion: Long?
    ): Pair<EvenNumberState, Long> {
        val newState = this ?: EvenNumberState(Description("0"), NumberValue(0))
        if (eventVersion != ((currentStateVersion ?: 0) + 1)) throw RuntimeException("Duplicate event!!!")
        evenNumberStateStorage =
            if (currentStateVersion != null) Pair(newState, eventVersion) else Pair(newState, 0)
        return evenNumberStateStorage
    }
}

/**
 * Even number locking state repository
 *
 * @return state repository instance for Even numbers
 */
fun evenNumberLockingDeduplicationViewRepository(): ViewStateLockingDeduplicationRepository<EvenNumberEvent?, EvenNumberState?, Long, Long> =
    EvenNumberLockingDeduplicationViewRepository()

