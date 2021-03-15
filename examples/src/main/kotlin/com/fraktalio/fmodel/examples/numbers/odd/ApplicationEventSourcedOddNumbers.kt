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

package com.fraktalio.fmodel.examples.numbers.odd

import com.fraktalio.fmodel.examples.numbers.api.Description
import com.fraktalio.fmodel.examples.numbers.api.NumberCommand
import com.fraktalio.fmodel.examples.numbers.api.NumberValue
import com.fraktalio.fmodel.examples.numbers.odd.command.ODD_NUMBER_AGGREGATE
import com.fraktalio.fmodel.examples.numbers.odd.command.oddNumberEventStorage
import com.fraktalio.fmodel.examples.numbers.odd.query.ODD_NUMBER_MATERIALIZED_VIEW
import com.fraktalio.fmodel.examples.numbers.odd.query.oddNumberStateStorage
import kotlinx.coroutines.runBlocking

fun main(args: Array<String>) = runBlocking { // start main coroutine

    println("  " + ODD_NUMBER_AGGREGATE.decider.initialState)
    ODD_NUMBER_AGGREGATE.handle(NumberCommand.OddNumberCommand.AddOddNumber(Description("Add 1"), NumberValue(1)))
    ODD_NUMBER_AGGREGATE.handle(NumberCommand.OddNumberCommand.AddOddNumber(Description("Add 3"), NumberValue(3)))
    ODD_NUMBER_AGGREGATE.handle(NumberCommand.OddNumberCommand.AddOddNumber(Description("Add 7"), NumberValue(7)))
    ODD_NUMBER_AGGREGATE.handle(
        NumberCommand.OddNumberCommand.SubtractOddNumber(
            Description("Subtract 3"),
            NumberValue(3)
        )
    )


    println("Recreating/Replaying the View (state storage) with the current state: $oddNumberStateStorage")
    oddNumberStateStorage = null
    oddNumberEventStorage.forEach { if (it != null) ODD_NUMBER_MATERIALIZED_VIEW.handle(it) }

    println("Recreated state: $oddNumberStateStorage")
}

