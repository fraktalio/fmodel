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

package com.fraktalio.fmodel.application

import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapConcat

/**
 * Extension function - Handles the command message of type [C]
 *
 * @param command Command message of type [C]
 * @return [Flow] of stored Events of type [E]
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
@FlowPreview
fun <C, S, E> EventSourcingAggregate<C, S, E>.handle(command: C): Flow<E> =
    command.fetchEvents().computeNewEvents(command).save()

/**
 * Extension function - Handles the flow of command messages of type [C]
 *
 * @param commands [Flow] of Command messages of type [C]
 * @return [Flow] of stored Events of type [E]
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
@FlowPreview
fun <C, S, E> EventSourcingAggregate<C, S, E>.handle(commands: Flow<C>): Flow<E> = commands.flatMapConcat { handle(it) }


/**
 * Extension function - Publishes the command of type [C] to the event sourcing aggregate of type  [EventSourcingAggregate]<[C], *, [E]>
 * @receiver command of type [C]
 * @param aggregate of type [EventSourcingAggregate]<[C], *, [E]>
 * @return the [Flow] of stored Events of type [E]
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
@FlowPreview
fun <C, E> C.publishTo(aggregate: EventSourcingAggregate<C, *, E>): Flow<E> = aggregate.handle(this)

/**
 * Extension function - Publishes [Flow] of commands of type [C] to the event sourcing aggregate of type  [EventSourcingAggregate]<[C], *, [E]>
 * @receiver [Flow] of commands of type [C]
 * @param aggregate of type [EventSourcingAggregate]<[C], *, [E]>
 * @return the [Flow] of stored Events of type [E]
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
@FlowPreview
fun <C, E> Flow<C>.publishTo(aggregate: EventSourcingAggregate<C, *, E>): Flow<E> = aggregate.handle(this)
