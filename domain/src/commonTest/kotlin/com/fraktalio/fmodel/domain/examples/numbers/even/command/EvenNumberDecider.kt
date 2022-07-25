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

package com.fraktalio.fmodel.domain.examples.numbers.even.command

import com.fraktalio.fmodel.domain.Decider
import com.fraktalio.fmodel.domain.examples.numbers.api.Description
import com.fraktalio.fmodel.domain.examples.numbers.api.EvenNumberState
import com.fraktalio.fmodel.domain.examples.numbers.api.NumberCommand.EvenNumberCommand
import com.fraktalio.fmodel.domain.examples.numbers.api.NumberCommand.EvenNumberCommand.AddEvenNumber
import com.fraktalio.fmodel.domain.examples.numbers.api.NumberCommand.EvenNumberCommand.SubtractEvenNumber
import com.fraktalio.fmodel.domain.examples.numbers.api.NumberEvent.EvenNumberEvent
import com.fraktalio.fmodel.domain.examples.numbers.api.NumberEvent.EvenNumberEvent.EvenNumberAdded
import com.fraktalio.fmodel.domain.examples.numbers.api.NumberEvent.EvenNumberEvent.EvenNumberSubtracted
import com.fraktalio.fmodel.domain.examples.numbers.api.NumberValue
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf

/**
 * Even number decider - pure declaration of our program logic
 *
 * @return Even number decider instance
 */
fun evenNumberDecider(): Decider<EvenNumberCommand?, EvenNumberState, EvenNumberEvent?> =
    Decider(
        initialState = EvenNumberState(Description("Initial state"), NumberValue(0)),
        decide = { c, s ->
            if (c != null && c.value.get > 1000) flow<EvenNumberEvent> { throw UnsupportedOperationException("Sorry") } else
                when (c) {
                    is AddEvenNumber -> flowOf(EvenNumberAdded(c.description, s.value + c.value))
                    is SubtractEvenNumber -> flowOf(EvenNumberSubtracted(c.description, s.value - c.value))
                    null -> emptyFlow()
                }
        },
        evolve = { s, e ->
            when (e) {
                is EvenNumberAdded -> EvenNumberState(s.description + e.description, e.value)
                is EvenNumberSubtracted -> EvenNumberState(s.description - e.description, e.value)
                null -> s
            }
        }
    )

