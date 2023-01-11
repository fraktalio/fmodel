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

package com.fraktalio.fmodel.application.examples.numbers

import com.fraktalio.fmodel.application.ViewStateRepository
import com.fraktalio.fmodel.domain.examples.numbers.api.*
import com.fraktalio.fmodel.domain.examples.numbers.api.NumberEvent.EvenNumberEvent
import com.fraktalio.fmodel.domain.examples.numbers.api.NumberEvent.OddNumberEvent

/**
 * A very simple state store ;)  It is initially empty.
 */
var numberStateStorage1: EvenNumberState? = EvenNumberState(Description("0"), NumberValue(0))
var numberStateStorage2: OddNumberState? = OddNumberState(Description("0"), NumberValue(0))

/**
 * Number (state) repository implementation
 *
 * @constructor Creates number repository
 */

class NumberViewRepository :
    ViewStateRepository<NumberEvent?, Pair<EvenNumberState?, OddNumberState?>> {

    override suspend fun Pair<EvenNumberState?, OddNumberState?>.save(): Pair<EvenNumberState?, OddNumberState?> {
        when {
            this.first != null -> numberStateStorage1 = this.first
            this.second != null -> numberStateStorage2 = this.second
        }

        return this
    }

    override suspend fun NumberEvent?.fetchState(): Pair<EvenNumberState?, OddNumberState?> =
        when (this) {
            is EvenNumberEvent -> Pair(numberStateStorage1, null)
            is OddNumberEvent -> Pair(null, numberStateStorage2)
            else -> throw UnsupportedOperationException("fetching state failed")
        }

    fun deleteAll() {
        numberStateStorage1 = EvenNumberState(Description("0"), NumberValue(0))
        numberStateStorage2 = OddNumberState(Description("0"), NumberValue(0))
    }
}

/**
 * Number state repository
 *
 * @return state repository instance for all numbers
 */
fun numberViewRepository(): ViewStateRepository<NumberEvent?, Pair<EvenNumberState?, OddNumberState?>> =
    NumberViewRepository()

