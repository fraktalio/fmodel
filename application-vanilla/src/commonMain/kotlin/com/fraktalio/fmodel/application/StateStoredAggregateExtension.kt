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
import kotlinx.coroutines.flow.map

/**
 * Extension function - Handles the command message of type [C]
 *
 * @param command Command message of type [C]
 * @return State of type [S]
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
@FlowPreview
suspend fun <C, S, E> StateStoredAggregate<C, S, E>.handle(command: C): S =
    command.fetchState().computeNewState(command).save()

/**
 * Extension function - Handles the [Flow] of command messages of type [C]
 *
 * @param commands [Flow] of Command messages of type [C]
 * @return [Flow] of State of type [S]
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
@FlowPreview
fun <C, S, E> StateStoredAggregate<C, S, E>.handle(commands: Flow<C>): Flow<S> = commands.map { handle(it) }

/**
 * Extension function - Publishes the command of type [C] to the state stored aggregate of type  [StateStoredAggregate]<[C], [S], *>
 * @receiver command of type [C]
 * @param aggregate of type [StateStoredAggregate]<[C], [S], *>
 * @return the stored State of type [S]
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
@FlowPreview
suspend fun <C, S> C.publishTo(aggregate: StateStoredAggregate<C, S, *>): S = aggregate.handle(this)

/**
 * Extension function - Publishes the command of type [C] to the state stored aggregate of type  [StateStoredAggregate]<[C], [S], *>
 * @receiver [Flow] of commands of type [C]
 * @param aggregate of type [StateStoredAggregate]<[C], [S], *>
 * @return the [Flow] of State of type [S]
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
@FlowPreview
fun <C, S> Flow<C>.publishTo(aggregate: StateStoredAggregate<C, S, *>): Flow<S> = aggregate.handle(this)
