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

import com.fraktalio.fmodel.application.StateRepository
import com.fraktalio.fmodel.domain.examples.numbers.api.*

/**
 * A very simple state store ;)  It is initially empty.
 */
var numberStateAggregateStorage1: EvenNumberState = EvenNumberState(Description("0"), NumberValue(0))
var numberStateAggregateStorage2: OddNumberState = OddNumberState(Description("0"), NumberValue(0))

/**
 * Number (state) repository implementation
 *
 * @constructor Creates number repository
 */

class NumberStateRepository : StateRepository<NumberCommand?, Pair<EvenNumberState, OddNumberState>> {

    override suspend fun Pair<EvenNumberState, OddNumberState>.save(): Pair<EvenNumberState, OddNumberState> {
        numberStateAggregateStorage1 = this.first
        numberStateAggregateStorage2 = this.second

        return this
    }

    override suspend fun NumberCommand?.fetchState(): Pair<EvenNumberState, OddNumberState> =
        Pair(numberStateAggregateStorage1, numberStateAggregateStorage2)

    fun deleteAll() {
        numberStateAggregateStorage1 = EvenNumberState(Description("0"), NumberValue(0))
        numberStateAggregateStorage2 = OddNumberState(Description("0"), NumberValue(0))
    }

}

/**
 * Number state repository
 *
 * @return state repository instance for all numbers
 */
fun numberStateRepository(): StateRepository<NumberCommand?, Pair<EvenNumberState, OddNumberState>> =
    NumberStateRepository()
