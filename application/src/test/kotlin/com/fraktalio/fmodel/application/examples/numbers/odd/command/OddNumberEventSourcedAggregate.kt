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

package com.fraktalio.fmodel.application.examples.numbers.odd.command

import com.fraktalio.fmodel.application.AggregateEventRepository
import com.fraktalio.fmodel.application.EventSourcingAggregate
import com.fraktalio.fmodel.domain.Decider
import com.fraktalio.fmodel.domain.examples.numbers.api.NumberCommand.OddNumberCommand
import com.fraktalio.fmodel.domain.examples.numbers.api.NumberEvent.OddNumberEvent
import com.fraktalio.fmodel.domain.examples.numbers.api.OddNumberState

/**
 * Odd number aggregate
 *
 * @param decider the core domain logic algorithm - pure declaration of our program logic
 * @param repository the event-sourcing repository for Odd numbers
 * @return the event-sourcing aggregate instance for Odd numbers
 */
fun oddNumberAggregate(
    decider: Decider<OddNumberCommand?, OddNumberState, OddNumberEvent?>,
    repository: AggregateEventRepository<OddNumberCommand?, OddNumberEvent?>
): EventSourcingAggregate<OddNumberCommand?, OddNumberState, OddNumberEvent?> =

    EventSourcingAggregate(
        decider = decider,
        aggregateEventRepository = repository
    )
