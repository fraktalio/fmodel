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

package com.fraktalio.fmodel.domain.examples.numbers.odd.process

import com.fraktalio.fmodel.domain.examples.numbers.api.*
import com.fraktalio.fmodel.domain.examples.numbers.api.NumberCommand.*
import com.fraktalio.fmodel.domain.examples.numbers.api.NumberCommand.EvenNumberCommand.AddEvenNumber
import com.fraktalio.fmodel.domain.examples.numbers.api.NumberCommand.EvenNumberCommand.SubtractEvenNumber
import com.fraktalio.fmodel.domain.examples.numbers.api.NumberCommand.OddNumberCommand.AddOddNumber
import com.fraktalio.fmodel.domain.examples.numbers.api.NumberCommand.OddNumberCommand.SubtractOddNumber
import com.fraktalio.fmodel.domain.examples.numbers.api.NumberEvent.*
import com.fraktalio.fmodel.domain.examples.numbers.api.NumberEvent.EvenNumberEvent.EvenNumberAdded
import com.fraktalio.fmodel.domain.examples.numbers.api.NumberEvent.EvenNumberEvent.EvenNumberSubtracted
import com.fraktalio.fmodel.domain.examples.numbers.api.NumberEvent.OddNumberEvent.OddNumberAdded
import com.fraktalio.fmodel.domain.examples.numbers.api.NumberEvent.OddNumberEvent.OddNumberSubtracted

/**
 * Very simple Number (stateless) process, just for fun ;)
 *
 * It reacts on Action Results of type of any Event (Even) and issue a Command/Action (Odd)
 * For example if the OddNumberAdded happened with value 4, a new command of type AddEvenNumber will be published with value OddNumberAdded+1=5
 *
 * The Process can keep its own state (as series of internal process events, if you like).
 *
 * AR = OddNumberEvent, S = OddNumberState, E = OddNumberEvent, A = EvenNumberCommand
 *
 * @return number Process instance
 */
fun oddNumberProcess() = com.fraktalio.fmodel.domain.Process<OddNumberEvent?, OddNumberState?, OddNumberEvent?, EvenNumberCommand>(
    react = { _: OddNumberState?, e: OddNumberEvent? ->
        when (e) {
            is OddNumberAdded -> listOf(
                AddEvenNumber(
                    Description("${e.value.get + 1}"),
                    NumberValue(e.value.get + 1)
                )
            )
            is OddNumberSubtracted -> listOf(
                SubtractEvenNumber(
                    Description("${e.value.get + 1}"),
                    NumberValue(e.value.get + 1)
                )
            )
            else -> emptyList()
        }
    },
    // Simplest case for `evolve` function: we do not maintain the state at all ;)
    evolve = { _: OddNumberState?, _: OddNumberEvent? ->
        null
    },
    initialState = null,
    // Never terminal
    isTerminal = { false },
    // Simplest case for ingest: ingesting the Action Result of type OddNumberEvent and forwarding them as list of Events to the output. State is ignored.
    ingest = { ar: OddNumberEvent?, _: OddNumberState? ->
        listOf(ar)
    },
    // Simplest case for pending: the list of pending Actions is always empty. We only react on React function
    pending = {
        emptyList()
    }
)

