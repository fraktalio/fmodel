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

package com.fraktalio.fmodel.examples.numbers.even.command

import com.fraktalio.fmodel.application.AggregateEventRepository
import com.fraktalio.fmodel.application.EventSourcingAggregate
import com.fraktalio.fmodel.domain.Decider
import com.fraktalio.fmodel.examples.numbers.api.EvenNumberState
import com.fraktalio.fmodel.examples.numbers.api.NumberCommand
import com.fraktalio.fmodel.examples.numbers.api.NumberEvent


/**
 * Even number aggregate
 *
 * @param decider the core domain logic algorithm - pure declaration of our program logic
 * @param repository the event-sourcing repository for Even numbers
 * @return the event-sourcing aggregate instance for Even numbers
 */
fun evenNumberAggregate(
    decider: Decider<NumberCommand.EvenNumberCommand?, EvenNumberState, NumberEvent.EvenNumberEvent?>,
    repository: AggregateEventRepository<NumberCommand.EvenNumberCommand?, NumberEvent.EvenNumberEvent?>
): EventSourcingAggregate<NumberCommand.EvenNumberCommand?, EvenNumberState, NumberEvent.EvenNumberEvent?> =

    EventSourcingAggregate(
        decider = decider,
        aggregateEventRepository = repository
    )
