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

package com.fraktalio.fmodel.domain.examples.numbers

import com.fraktalio.fmodel.domain.Saga
import com.fraktalio.fmodel.domain.examples.numbers.api.Description
import com.fraktalio.fmodel.domain.examples.numbers.api.NumberCommand
import com.fraktalio.fmodel.domain.examples.numbers.api.NumberCommand.EvenNumberCommand.AddEvenNumber
import com.fraktalio.fmodel.domain.examples.numbers.api.NumberCommand.EvenNumberCommand.SubtractEvenNumber
import com.fraktalio.fmodel.domain.examples.numbers.api.NumberCommand.OddNumberCommand.AddOddNumber
import com.fraktalio.fmodel.domain.examples.numbers.api.NumberCommand.OddNumberCommand.SubtractOddNumber
import com.fraktalio.fmodel.domain.examples.numbers.api.NumberEvent
import com.fraktalio.fmodel.domain.examples.numbers.api.NumberEvent.EvenNumberEvent.EvenNumberAdded
import com.fraktalio.fmodel.domain.examples.numbers.api.NumberEvent.EvenNumberEvent.EvenNumberSubtracted
import com.fraktalio.fmodel.domain.examples.numbers.api.NumberEvent.OddNumberEvent.OddNumberAdded
import com.fraktalio.fmodel.domain.examples.numbers.api.NumberEvent.OddNumberEvent.OddNumberSubtracted
import com.fraktalio.fmodel.domain.examples.numbers.api.NumberValue
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf

/**
 * Very simple Number saga, just for fun ;)
 *
 * It reacts on Action Results of type of any Event (Even or Odd) and issue a Command/Action (Odd or Even)
 * For example if the EvenNumberAdded happened with value 4, a new command of type AddOddNumber will be published with value EvenNumberAdded-1=3
 *
 * @return number Saga instance
 */
fun numberSaga() = Saga<NumberEvent?, NumberCommand?>(
    react = { numberEvent ->
        when (numberEvent) {
            is EvenNumberAdded -> flowOf(
                AddOddNumber(
                    Description("${numberEvent.value.get - 1}"),
                    NumberValue(numberEvent.value.get - 1)
                )
            )

            is EvenNumberSubtracted -> flowOf(
                SubtractOddNumber(
                    Description("${numberEvent.value.get - 1}"),
                    NumberValue(numberEvent.value.get - 1)
                )
            )

            else -> emptyFlow()
        }
    }
)

/**
 * Even number saga - not doing much
 *
 * It reacts on Action Results of type of any [NumberEvent.EvenNumberEvent] and issue a Command/Action of type [NumberCommand.OddNumberCommand]
 *
 * @return even number Saga instance
 */
fun evenNumberSaga() = Saga<NumberEvent.EvenNumberEvent?, NumberCommand.OddNumberCommand>(
    react = { emptyFlow() }
)

/**
 * Odd number saga
 *
 * It reacts on Action Results of type of any [NumberEvent.OddNumberEvent] and issue a Command/Action of type [NumberCommand.EvenNumberCommand]
 *
 * @return odd number Saga instance
 */
fun oddNumberSaga() = Saga<NumberEvent.OddNumberEvent?, NumberCommand.EvenNumberCommand>(
    react = { numberEvent ->
        when (numberEvent) {
            is OddNumberAdded -> flowOf(
                AddEvenNumber(
                    Description("${numberEvent.value.get + 1}"),
                    NumberValue(numberEvent.value.get + 1)
                )
            )

            is OddNumberSubtracted -> flowOf(
                SubtractEvenNumber(
                    Description("${numberEvent.value.get + 1}"),
                    NumberValue(numberEvent.value.get + 1)
                )
            )

            else -> emptyFlow()
        }
    }
)


