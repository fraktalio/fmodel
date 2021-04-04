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

package com.fraktalio.fmodel.examples.numbers

import arrow.core.Either
import com.fraktalio.fmodel.application.AggregateEventRepository
import com.fraktalio.fmodel.application.EventSourcingAggregate
import com.fraktalio.fmodel.domain.Decider
import com.fraktalio.fmodel.domain.combineDeciders
import com.fraktalio.fmodel.examples.numbers.api.EvenNumberState
import com.fraktalio.fmodel.examples.numbers.api.NumberCommand
import com.fraktalio.fmodel.examples.numbers.api.NumberEvent
import com.fraktalio.fmodel.examples.numbers.api.OddNumberState


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
fun numberAggregate(
    evenNumberDecider: Decider<NumberCommand.EvenNumberCommand?, EvenNumberState, NumberEvent.EvenNumberEvent?>,
    oddNumberDecider: Decider<NumberCommand.OddNumberCommand?, OddNumberState, NumberEvent.OddNumberEvent?>,
    repository: AggregateEventRepository<Either<NumberCommand.EvenNumberCommand?, NumberCommand.OddNumberCommand?>, Either<NumberEvent.EvenNumberEvent?, NumberEvent.OddNumberEvent?>>
): EventSourcingAggregate<Either<NumberCommand.EvenNumberCommand?, NumberCommand.OddNumberCommand?>, Pair<EvenNumberState, OddNumberState>, Either<NumberEvent.EvenNumberEvent?, NumberEvent.OddNumberEvent?>> =

    EventSourcingAggregate(
        decider = evenNumberDecider.combineDeciders(oddNumberDecider),
        aggregateEventRepository = repository
    )
