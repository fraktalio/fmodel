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

import com.fraktalio.fmodel.application.MaterializedView
import com.fraktalio.fmodel.application.ViewStateRepository
import com.fraktalio.fmodel.application.materializedView
import com.fraktalio.fmodel.domain.View
import com.fraktalio.fmodel.domain.combine
import com.fraktalio.fmodel.domain.examples.numbers.api.EvenNumberState
import com.fraktalio.fmodel.domain.examples.numbers.api.NumberEvent
import com.fraktalio.fmodel.domain.examples.numbers.api.NumberEvent.EvenNumberEvent
import com.fraktalio.fmodel.domain.examples.numbers.api.NumberEvent.OddNumberEvent
import com.fraktalio.fmodel.domain.examples.numbers.api.OddNumberState


/**
 * All numbers - materialized view.
 *
 * It combines two views for even and od numbers into one view for all numbers.
 *
 * @param oddView Odd number view -  pure declaration of our program logic
 * @param evenView Even number view -  pure declaration of our program logic
 * @param repository All numbers repository
 * @return Materialized view for all the numbers
 */
fun numberMaterializedView(
    oddView: View<OddNumberState?, OddNumberEvent?>,
    evenView: View<EvenNumberState?, EvenNumberEvent?>,
    repository: ViewStateRepository<NumberEvent?, Pair<EvenNumberState?, OddNumberState?>>
): MaterializedView<Pair<EvenNumberState?, OddNumberState?>, NumberEvent?> =
    materializedView(
        view = evenView.combine(oddView),
        viewStateRepository = repository
    )
