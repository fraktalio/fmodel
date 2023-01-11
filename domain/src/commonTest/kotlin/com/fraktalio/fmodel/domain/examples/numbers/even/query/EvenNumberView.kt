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

package com.fraktalio.fmodel.domain.examples.numbers.even.query

import com.fraktalio.fmodel.domain.View
import com.fraktalio.fmodel.domain.examples.numbers.api.Description
import com.fraktalio.fmodel.domain.examples.numbers.api.EvenNumberState
import com.fraktalio.fmodel.domain.examples.numbers.api.NumberEvent.EvenNumberEvent
import com.fraktalio.fmodel.domain.examples.numbers.api.NumberEvent.EvenNumberEvent.EvenNumberAdded
import com.fraktalio.fmodel.domain.examples.numbers.api.NumberEvent.EvenNumberEvent.EvenNumberSubtracted
import com.fraktalio.fmodel.domain.examples.numbers.api.NumberValue
import com.fraktalio.fmodel.domain.view

/**
 * Even number view -  pure declaration of our program logic
 *
 * @return Even number view instance
 */
fun evenNumberView(): View<EvenNumberState, EvenNumberEvent?> =
    view {
        initialState {
            EvenNumberState(Description("Initial state"), NumberValue(0))
        }
        evolve { s, e ->
            when (e) {
                is EvenNumberAdded -> EvenNumberState(s.description + e.description, s.value + e.value)
                is EvenNumberSubtracted -> EvenNumberState(s.description - e.description, s.value - e.value)
                null -> s
            }
        }
    }


