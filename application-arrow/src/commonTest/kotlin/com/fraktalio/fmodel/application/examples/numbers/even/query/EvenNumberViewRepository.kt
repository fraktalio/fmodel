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

package com.fraktalio.fmodel.application.examples.numbers.even.query

import com.fraktalio.fmodel.application.ViewStateRepository
import com.fraktalio.fmodel.domain.examples.numbers.api.EvenNumberState
import com.fraktalio.fmodel.domain.examples.numbers.api.NumberEvent.EvenNumberEvent

/**
 * A very simple state store ;)  It is initially empty.
 */
private var evenNumberStateStorage: EvenNumberState? = null

/**
 * Even number repository implementation
 *
 * @constructor Creates Even number repository
 */
class EvenNumberViewRepository : ViewStateRepository<EvenNumberEvent?, EvenNumberState?> {

    override suspend fun EvenNumberEvent?.fetchState(): EvenNumberState? = evenNumberStateStorage

    override suspend fun EvenNumberState?.save(): EvenNumberState? {
        evenNumberStateStorage = this

        return evenNumberStateStorage
    }

    fun deleteAll() {
        evenNumberStateStorage = null
    }

}

/**
 * Even number state repository
 *
 * @return state repository instance for Even numbers
 */
fun evenNumberViewRepository(): ViewStateRepository<EvenNumberEvent?, EvenNumberState?> =
    EvenNumberViewRepository()

