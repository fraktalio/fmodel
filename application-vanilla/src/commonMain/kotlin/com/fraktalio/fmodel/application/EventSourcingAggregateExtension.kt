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

package com.fraktalio.fmodel.application

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*

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
    command
        .fetchEvents()
        .computeNewEvents(command)
        .save()

/**
 * Extension function - Handles the command message of type [C]
 *
 * @param command Command message of type [C]
 * @return [Flow] of stored Events of type [E]
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
@ExperimentalCoroutinesApi
@FlowPreview
fun <C, S, E> EventSourcingOrchestratingAggregate<C, S, E>.handle(command: C): Flow<E> =
    command
        .fetchEvents()
        .computeNewEventsByOrchestrating(command) { it.fetchEvents() }
        .save()

/**
 * Extension function - Handles the command message of type [C]
 *
 * @param command Command message of type [C]
 * @return [Flow] of stored Events of type [Pair]<[E], [V]>
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
@FlowPreview
fun <C, S, E, V> EventSourcingLockingAggregate<C, S, E, V>.handleOptimistically(command: C): Flow<Pair<E, V>> = flow {
    val events = command.fetchEvents()
    emitAll(
        events.map { it.first }
            .computeNewEvents(command)
            .save(events.map { it.second }.lastOrNull())
    )
}

/**
 * Extension function - Handles the command message of type [C]
 *
 * @param command Command message of type [C]
 * @return [Flow] of stored Events of type [Pair]<[E], [V]>
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
@ExperimentalCoroutinesApi
@FlowPreview
fun <C, S, E, V> EventSourcingLockingOrchestratingAggregate<C, S, E, V>.handleOptimistically(command: C): Flow<Pair<E, V>> =
    command
        .fetchEvents().map { it.first }
        .computeNewEventsByOrchestrating(command) { it.fetchEvents().map { pair -> pair.first } }
        .save(latestVersionProvider)


@ExperimentalCoroutinesApi
@FlowPreview
fun <C, S, E> EventSourcingAggregate<C, S, E>.handle(commands: Flow<C>): Flow<E> =
    commands.flatMapConcat { handle(it) }

@ExperimentalCoroutinesApi
@FlowPreview
fun <C, S, E> EventSourcingOrchestratingAggregate<C, S, E>.handle(commands: Flow<C>): Flow<E> =
    commands.flatMapConcat { handle(it) }

@ExperimentalCoroutinesApi
@FlowPreview
fun <C, S, E, V> EventSourcingLockingAggregate<C, S, E, V>.handleOptimistically(commands: Flow<C>): Flow<Pair<E, V>> =
    commands.flatMapConcat { handleOptimistically(it) }

@ExperimentalCoroutinesApi
@FlowPreview
fun <C, S, E, V> EventSourcingLockingOrchestratingAggregate<C, S, E, V>.handleOptimistically(commands: Flow<C>): Flow<Pair<E, V>> =
    commands.flatMapConcat { handleOptimistically(it) }


@FlowPreview
fun <C, E> C.publishTo(aggregate: EventSourcingAggregate<C, *, E>): Flow<E> =
    aggregate.handle(this)

@FlowPreview
fun <C, E, V> C.publishOptimisticallyTo(aggregate: EventSourcingLockingAggregate<C, *, E, V>): Flow<Pair<E, V>> =
    aggregate.handleOptimistically(this)

@ExperimentalCoroutinesApi
@FlowPreview
fun <C, E> C.publishTo(aggregate: EventSourcingOrchestratingAggregate<C, *, E>): Flow<E> =
    aggregate.handle(this)

@ExperimentalCoroutinesApi
@FlowPreview
fun <C, E, V> C.publishOptimisticallyTo(aggregate: EventSourcingLockingOrchestratingAggregate<C, *, E, V>): Flow<Pair<E, V>> =
    aggregate.handleOptimistically(this)


@ExperimentalCoroutinesApi
@FlowPreview
fun <C, E> Flow<C>.publishTo(aggregate: EventSourcingAggregate<C, *, E>): Flow<E> =
    aggregate.handle(this)

@ExperimentalCoroutinesApi
@FlowPreview
fun <C, E, V> Flow<C>.publishOptimisticallyTo(aggregate: EventSourcingLockingAggregate<C, *, E, V>): Flow<Pair<E, V>> =
    aggregate.handleOptimistically(this)

@ExperimentalCoroutinesApi
@FlowPreview
fun <C, E> Flow<C>.publishTo(aggregate: EventSourcingOrchestratingAggregate<C, *, E>): Flow<E> =
    aggregate.handle(this)

@ExperimentalCoroutinesApi
@FlowPreview
fun <C, E, V> Flow<C>.publishOptimisticallyTo(aggregate: EventSourcingLockingOrchestratingAggregate<C, *, E, V>): Flow<Pair<E, V>> =
    aggregate.handleOptimistically(this)