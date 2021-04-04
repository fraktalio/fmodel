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

package com.fraktalio.fmodel.examples.numbers.even.query

import com.fraktalio.fmodel.application.MaterializedView
import com.fraktalio.fmodel.application.ViewStateRepository
import com.fraktalio.fmodel.domain.View
import com.fraktalio.fmodel.examples.numbers.api.EvenNumberState
import com.fraktalio.fmodel.examples.numbers.api.NumberEvent

/**
 * Even number materialized view
 *
 * @param view pure declaration of our program logic
 * @param repository Even number repository
 * @return Even number materialized view
 */
fun evenNumberMaterializedView(
    view: View<EvenNumberState?, NumberEvent.EvenNumberEvent?>,
    repository: ViewStateRepository<NumberEvent.EvenNumberEvent?, EvenNumberState?>
): MaterializedView<EvenNumberState?, NumberEvent.EvenNumberEvent?> = MaterializedView(
    view = view,
    viewStateRepository = repository
)
