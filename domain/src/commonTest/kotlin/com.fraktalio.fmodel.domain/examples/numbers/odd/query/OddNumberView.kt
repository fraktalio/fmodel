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

package com.fraktalio.fmodel.domain.examples.numbers.odd.query

import com.fraktalio.fmodel.domain.View
import com.fraktalio.fmodel.domain.examples.numbers.api.Description
import com.fraktalio.fmodel.domain.examples.numbers.api.NumberEvent.OddNumberEvent
import com.fraktalio.fmodel.domain.examples.numbers.api.NumberEvent.OddNumberEvent.OddNumberAdded
import com.fraktalio.fmodel.domain.examples.numbers.api.NumberEvent.OddNumberEvent.OddNumberSubtracted
import com.fraktalio.fmodel.domain.examples.numbers.api.NumberValue
import com.fraktalio.fmodel.domain.examples.numbers.api.OddNumberState

/**
 * Odd number view -  pure declaration of our program logic
 *
 * @return Odd number view instance
 */
fun oddNumberView(): View<OddNumberState?, OddNumberEvent?> = View(
    initialState = OddNumberState(
        Description(
            "Initial state"
        ), NumberValue(0)
    ),
    evolve = { oddNumberState, e ->
        when {
            e is OddNumberAdded && (oddNumberState != null) -> OddNumberState(
                Description(oddNumberState.description.get + " + " + e.description.get),
                NumberValue(oddNumberState.value.get + e.value.get)
            )
            e is OddNumberSubtracted && (oddNumberState != null) -> OddNumberState(
                Description(oddNumberState.description.get + " - " + e.description.get),
                NumberValue(oddNumberState.value.get - e.value.get)
            )
            else -> oddNumberState
        }
    }
)



