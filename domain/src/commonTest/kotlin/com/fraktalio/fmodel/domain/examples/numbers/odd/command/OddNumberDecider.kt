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

package com.fraktalio.fmodel.domain.examples.numbers.odd.command

import com.fraktalio.fmodel.domain.Decider
import com.fraktalio.fmodel.domain.examples.numbers.api.Description
import com.fraktalio.fmodel.domain.examples.numbers.api.NumberCommand.OddNumberCommand
import com.fraktalio.fmodel.domain.examples.numbers.api.NumberCommand.OddNumberCommand.AddOddNumber
import com.fraktalio.fmodel.domain.examples.numbers.api.NumberCommand.OddNumberCommand.SubtractOddNumber
import com.fraktalio.fmodel.domain.examples.numbers.api.NumberEvent.OddNumberEvent
import com.fraktalio.fmodel.domain.examples.numbers.api.NumberEvent.OddNumberEvent.OddNumberAdded
import com.fraktalio.fmodel.domain.examples.numbers.api.NumberEvent.OddNumberEvent.OddNumberSubtracted
import com.fraktalio.fmodel.domain.examples.numbers.api.NumberValue
import com.fraktalio.fmodel.domain.examples.numbers.api.OddNumberState
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf

/**
 * Odd number decider - pure declaration of our program logic
 *
 * @return Odd number decider instance
 */
fun oddNumberDecider(): Decider<OddNumberCommand?, OddNumberState, OddNumberEvent?> =
    Decider(
        initialState = OddNumberState(Description("Initial state"), NumberValue(0)),
        decide = { c, s ->
            when (c) {
                is AddOddNumber -> flowOf(OddNumberAdded(c.description, s.value + c.value))
                is SubtractOddNumber -> flowOf(OddNumberSubtracted(c.description, s.value - c.value))
                null -> emptyFlow()
            }
        },
        evolve = { s, e ->
            when (e) {
                is OddNumberAdded -> OddNumberState(s.description + e.description, e.value)
                is OddNumberSubtracted -> OddNumberState(s.description - e.description, e.value)
                null -> s
            }
        }
    )





