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

package com.fraktalio.fmodel.examples.numbers.even

import com.fraktalio.fmodel.examples.numbers.api.Description
import com.fraktalio.fmodel.examples.numbers.api.NumberCommand
import com.fraktalio.fmodel.examples.numbers.api.NumberValue
import com.fraktalio.fmodel.examples.numbers.even.command.evenNumberAggregate
import com.fraktalio.fmodel.examples.numbers.even.command.evenNumberDecider
import com.fraktalio.fmodel.examples.numbers.even.command.evenNumberRepository
import com.fraktalio.fmodel.examples.numbers.even.query.evenNumberMaterializedView
import com.fraktalio.fmodel.examples.numbers.even.query.evenNumberView
import com.fraktalio.fmodel.examples.numbers.even.query.evenNumberViewRepository
import kotlinx.coroutines.runBlocking

fun main(args: Array<String>) = runBlocking {

    val eventSourcingAggregate = evenNumberAggregate(evenNumberDecider(), evenNumberRepository())
    val evenNumberMaterializedView = evenNumberMaterializedView(evenNumberView(), evenNumberViewRepository())

    println(
        eventSourcingAggregate.handle(
            NumberCommand.EvenNumberCommand.AddEvenNumber(
                Description("Add 2"),
                NumberValue(2)
            )
        )
            .map { it.map { eventStoredSuccessfully -> evenNumberMaterializedView.handle(eventStoredSuccessfully.event) } }
    )
    println(
        eventSourcingAggregate.handle(
            NumberCommand.EvenNumberCommand.AddEvenNumber(
                Description("Add 4"),
                NumberValue(4)
            )
        )
            .map { it.map { eventStoredSuccessfully -> evenNumberMaterializedView.handle(eventStoredSuccessfully.event) } }
    )
    println(
        eventSourcingAggregate.handle(
            NumberCommand.EvenNumberCommand.AddEvenNumber(
                Description("Add 8"),
                NumberValue(8)
            )
        )
            .map { it.map { eventStoredSuccessfully -> evenNumberMaterializedView.handle(eventStoredSuccessfully.event) } }
    )
    println(
        eventSourcingAggregate.handle(
            NumberCommand.EvenNumberCommand.SubtractEvenNumber(
                Description("Subtract 2"),
                NumberValue(2)
            )
        )
            .map { it.map { eventStoredSuccessfully -> evenNumberMaterializedView.handle(eventStoredSuccessfully.event) } }
    )

}

