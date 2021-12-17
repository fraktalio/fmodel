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

package com.fraktalio.fmodel.application.examples.numbers.odd.query

import com.fraktalio.fmodel.application.ViewStateRepository
import com.fraktalio.fmodel.domain.examples.numbers.api.NumberEvent.OddNumberEvent
import com.fraktalio.fmodel.domain.examples.numbers.api.OddNumberState
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * A very simple state store ;)  It is initially empty.
 */
private var oddNumberStateStorage: OddNumberState? = null
private val oddNumberStateStorageMutex = Mutex()

/**
 * Odd number repository implementation
 *
 * @constructor Creates Odd number repository
 */
class OddNumberViewRepository : ViewStateRepository<OddNumberEvent?, OddNumberState?> {

    override suspend fun OddNumberEvent?.fetchState(): OddNumberState? = oddNumberStateStorage

    override suspend fun OddNumberState?.save(): OddNumberState? {
        oddNumberStateStorageMutex.withLock {
            oddNumberStateStorage = this
        }
        return this
    }
}

/**
 * Odd number state repository
 *
 * @return state repository instance for Odd numbers
 */
fun oddNumberViewRepository(): ViewStateRepository<OddNumberEvent?, OddNumberState?> =
    OddNumberViewRepository()

