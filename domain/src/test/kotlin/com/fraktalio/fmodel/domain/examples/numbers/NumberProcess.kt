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

package com.fraktalio.fmodel.domain.examples.numbers

import com.fraktalio.fmodel.domain.examples.numbers.api.*
import com.fraktalio.fmodel.domain.examples.numbers.api.NumberCommand.EvenNumberCommand.AddEvenNumber
import com.fraktalio.fmodel.domain.examples.numbers.api.NumberCommand.EvenNumberCommand.SubtractEvenNumber
import com.fraktalio.fmodel.domain.examples.numbers.api.NumberCommand.OddNumberCommand.AddOddNumber
import com.fraktalio.fmodel.domain.examples.numbers.api.NumberCommand.OddNumberCommand.SubtractOddNumber
import com.fraktalio.fmodel.domain.examples.numbers.api.NumberEvent.EvenNumberEvent.EvenNumberAdded
import com.fraktalio.fmodel.domain.examples.numbers.api.NumberEvent.EvenNumberEvent.EvenNumberSubtracted
import com.fraktalio.fmodel.domain.examples.numbers.api.NumberEvent.OddNumberEvent.OddNumberAdded
import com.fraktalio.fmodel.domain.examples.numbers.api.NumberEvent.OddNumberEvent.OddNumberSubtracted

/**
 * Very simple Number (stateless) process, just for fun ;)
 *
 * It reacts on Action Results of type of any Event (Even or Odd) and issue a Command/Action (Odd or Even)
 * For example if the EvenNumberAdded happened with value 4, a new command of type AddOddNumber will be published with value EvenNumberAdded-1=3
 *
 * In addition to Saga, the Process can keep its own state (as series of internal process events, if you like).
 * Saga is a special case of any Process.
 *
 * AR = NumberEvent, S = NumberState, E = NumberEvent, A = NumberCommand
 *
 * @return number Process instance
 */
fun numberProcess() = com.fraktalio.fmodel.domain.Process<NumberEvent, NumberState?, NumberEvent, NumberCommand>(
    react = { _: NumberState?, e: NumberEvent ->
        when (e) {
            is EvenNumberAdded -> listOf(
                AddOddNumber(
                    Description("${e.value.get - 1}"),
                    NumberValue(e.value.get - 1)
                )
            )
            is EvenNumberSubtracted -> listOf(
                SubtractOddNumber(
                    Description("${e.value.get - 1}"),
                    NumberValue(e.value.get - 1)
                )
            )
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
    evolve = { _: NumberState?, _: NumberEvent ->
        null
    },
    initialState = null,
    // Never terminal
    isTerminal = { false },
    // Simplest case for ingest: ingesting the Action Result of type NumberEvent and forwarding them as list of Events to the output. State is ignored.
    ingest = { ar: NumberEvent, _: NumberState? ->
        listOf(ar)
    },
    // Simplest case for pending: the list of pending Actions is always empty. We only react on React function
    pending = {
        emptyList()
    }
)

