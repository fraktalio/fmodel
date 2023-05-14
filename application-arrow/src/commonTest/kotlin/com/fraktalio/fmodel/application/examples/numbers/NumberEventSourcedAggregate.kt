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

import com.fraktalio.fmodel.application.EventRepository
import com.fraktalio.fmodel.application.EventSourcingOrchestratingAggregate
import com.fraktalio.fmodel.domain.Decider
import com.fraktalio.fmodel.domain.Saga
import com.fraktalio.fmodel.domain.combine
import com.fraktalio.fmodel.domain.examples.numbers.api.EvenNumberState
import com.fraktalio.fmodel.domain.examples.numbers.api.NumberCommand
import com.fraktalio.fmodel.domain.examples.numbers.api.NumberCommand.EvenNumberCommand
import com.fraktalio.fmodel.domain.examples.numbers.api.NumberCommand.OddNumberCommand
import com.fraktalio.fmodel.domain.examples.numbers.api.NumberEvent
import com.fraktalio.fmodel.domain.examples.numbers.api.NumberEvent.OddNumberEvent
import com.fraktalio.fmodel.domain.examples.numbers.api.OddNumberState
import kotlinx.coroutines.ExperimentalCoroutinesApi


/**
 * Number aggregate
 *
 * It combines two already existing deciders into one that can handle ALL numbers (Even and Odd)
 *
 * @param evenNumberDecider the core domain logic algorithm for even numbers - pure declaration of our program logic
 * @param oddNumberDecider the core domain logic algorithm for odd numbers   - pure declaration of our program logic
 * @param repository the event-sourcing repository for all (even and odd) numbers
 * @return the event-sourcing aggregate instance for all (even and odd) numbers
 */
@OptIn(ExperimentalCoroutinesApi::class)
fun numberAggregate(
    evenNumberDecider: Decider<EvenNumberCommand?, EvenNumberState, NumberEvent.EvenNumberEvent?>,
    oddNumberDecider: Decider<OddNumberCommand?, OddNumberState, OddNumberEvent?>,
    evenNumberSaga: Saga<NumberEvent.EvenNumberEvent?, OddNumberCommand>,
    oddNumberSaga: Saga<OddNumberEvent?, EvenNumberCommand>,
    repository: EventRepository<NumberCommand?, NumberEvent?>
) =

    EventSourcingOrchestratingAggregate(
        decider = evenNumberDecider.combine(oddNumberDecider),
        eventRepository = repository,
        saga = evenNumberSaga.combine(oddNumberSaga)
    )
