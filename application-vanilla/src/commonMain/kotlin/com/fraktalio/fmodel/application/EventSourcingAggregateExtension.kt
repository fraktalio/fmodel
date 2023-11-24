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
import kotlinx.coroutines.flow.*

/**
 * Extension function - Handles the command message of type [C]
 *
 * @param command Command message of type [C]
 * @return [Flow] of stored Events of type [E]
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
fun <C, S, E> EventSourcingAggregate<C, S, E>.handle(command: C): Flow<E> =
    command
        .fetchEvents()
        .computeNewEvents(command)
        .save()

/**
 * Extension function - Handles the command message of type [C] with metadata
 *
 * @param command Command message of type [C]
 * @param metaData Metadata of type [Map]<[String], [Any]>
 * @return [Flow] of stored Events of type [E]
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
fun <C, S, E> EventSourcingAggregate<C, S, E>.handle(
    command: C,
    metaData: Map<String, Any>
): Flow<Pair<E, Map<String, Any>>> =
    command
        .fetchEventsAndMetaData().map { it.first }
        .computeNewEvents(command)
        .saveWithMetaData(metaData)

/**
 * Extension function - Handles the command message of type [C]
 *
 * @param command Command message of type [C]
 * @return [Flow] of stored Events of type [E]
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
@ExperimentalCoroutinesApi
fun <C, S, E> EventSourcingOrchestratingAggregate<C, S, E>.handle(command: C): Flow<E> =
    command
        .fetchEvents()
        .computeNewEventsByOrchestrating(command) { it.fetchEvents() }
        .save()

/**
 * Extension function - Handles the command message of type [C] with metadata
 *
 * @param command Command message of type [C]
 * @param metaData Metadata of type [Map]<[String], [Any]>
 * @return [Flow] of stored Events of type [E]
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
@ExperimentalCoroutinesApi
fun <C, S, E> EventSourcingOrchestratingAggregate<C, S, E>.handle(
    command: C,
    metaData: Map<String, Any>
): Flow<Pair<E, Map<String, Any>>> =
    command
        .fetchEventsAndMetaData().map { it.first }
        .computeNewEventsByOrchestrating(command) { it.fetchEventsAndMetaData().map { it.first } }
        .saveWithMetaData(metaData)

/**
 * Extension function - Handles the command message of type [C]
 *
 * @param command Command message of type [C]
 * @return [Flow] of stored Events of type [Pair]<[E], [V]>
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
fun <C, S, E, V> EventSourcingLockingAggregate<C, S, E, V>.handleOptimistically(command: C): Flow<Pair<E, V>> = flow {
    val events = command.fetchEvents()
    emitAll(
        events.map { it.first }
            .computeNewEvents(command)
            .save(events.map { it.second }.lastOrNull())
    )
}

/**
 * Extension function - Handles the command message of type [C] with metadata
 *
 * @param command Command message of type [C]
 * @param metaData Metadata of type [Map]<[String], [Any]>
 * @return [Flow] of stored Events of type [Pair]<[E], [V]>
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
fun <C, S, E, V> EventSourcingLockingAggregate<C, S, E, V>.handleOptimistically(
    command: C,
    metaData: Map<String, Any>
): Flow<Triple<E, V, Map<String, Any>>> = flow {
    val events = command.fetchEventsAndMetaData()
    emitAll(
        events.map { it.first }
            .computeNewEvents(command)
            .saveWithMetaData(events.map { it.second }.lastOrNull(), metaData)
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
fun <C, S, E, V> EventSourcingLockingOrchestratingAggregate<C, S, E, V>.handleOptimistically(command: C): Flow<Pair<E, V>> =
    command
        .fetchEvents().map { it.first }
        .computeNewEventsByOrchestrating(command) { it.fetchEvents().map { pair -> pair.first } }
        .save(latestVersionProvider)

/**
 * Extension function - Handles the command message of type [C] with metadata
 *
 * @param command Command message of type [C]
 * @param metaData Metadata of type [Map]<[String], [Any]>
 * @return [Flow] of stored Events of type [Pair]<[E], [V]>
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
@ExperimentalCoroutinesApi
fun <C, S, E, V> EventSourcingLockingOrchestratingAggregate<C, S, E, V>.handleOptimistically(
    command: C,
    metaData: Map<String, Any>
): Flow<Triple<E, V, Map<String, Any>>> =
    command
        .fetchEventsAndMetaData().map { it.first }
        .computeNewEventsByOrchestrating(command) { it.fetchEventsAndMetaData().map { pair -> pair.first } }
        .saveWithMetaData(latestVersionProvider, metaData)


/**
 * Extension function - Handles the flow of command messages of type [C]
 *
 * @param commands Command messages of type [Flow]<[C]>
 * @return [Flow] of stored Events of type [E]
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
@ExperimentalCoroutinesApi
fun <C, S, E> EventSourcingAggregate<C, S, E>.handle(commands: Flow<C>): Flow<E> =
    commands.flatMapConcat { handle(it) }

/**
 * Extension function - Handles the flow of command messages with metadata of type [Flow]<[Pair]<[C], [Map]<[String], [Any]>>>
 *
 * @param commandsWithMetadata Command messages of type  [Flow]<[Pair]<[C], [Map]<[String], [Any]>>>
 * @return [Flow] of stored Events of type [Flow]<[Pair]<[E], [Map]<[String], [Any]>>>
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
@ExperimentalCoroutinesApi
fun <C, S, E> EventSourcingAggregate<C, S, E>.handleWithMetaData(commandsWithMetadata: Flow<Pair<C, Map<String, Any>>>): Flow<Pair<E, Map<String, Any>>> =
    commandsWithMetadata.flatMapConcat { handle(it.first, it.second) }

/**
 * Extension function - Handles the flow of command messages of type [Flow]<[C]>
 *
 * @param commands Command messages of type [Flow]<[C]>
 * @return [Flow] of stored Events of type [E]
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
@ExperimentalCoroutinesApi
fun <C, S, E> EventSourcingOrchestratingAggregate<C, S, E>.handle(commands: Flow<C>): Flow<E> =
    commands.flatMapConcat { handle(it) }

/**
 * Extension function - Handles the flow of command messages  with metadata of type [Flow]<[Pair]<[C], [Map]<[String], [Any]>>>
 *
 * @param commandsWithMetadata Command messages with metadata of type [Flow]<[Pair]<[C], [Map]<[String], [Any]>>>
 * @return [Flow] of stored Events with metadata of type [Flow]<[Pair]<[E], [Map]<[String], [Any]>>>
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
@ExperimentalCoroutinesApi
fun <C, S, E> EventSourcingOrchestratingAggregate<C, S, E>.handleWithMetaData(commandsWithMetadata: Flow<Pair<C, Map<String, Any>>>): Flow<Pair<E, Map<String, Any>>> =
    commandsWithMetadata.flatMapConcat { handle(it.first, it.second) }

/**
 * Extension function - Handles the flow of command messages of type [Flow]<[C]>
 *
 * @param commands Command messages of type [Flow]<[C]>
 * @return [Flow] of stored Events of type [Pair]<[E], [V]>
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
@ExperimentalCoroutinesApi
fun <C, S, E, V> EventSourcingLockingAggregate<C, S, E, V>.handleOptimistically(commands: Flow<C>): Flow<Pair<E, V>> =
    commands.flatMapConcat { handleOptimistically(it) }

/**
 * Extension function - Handles the flow of command messages with metadata of type [Flow]<[Pair]<[C], [Map]<[String], [Any]>>>
 *
 * @param commandsWithMetadata Command messages with metadata of type [Flow]<[Pair]<[C], [Map]<[String], [Any]>>>
 * @return [Flow] of stored Events of type [Triple]<[E], [V], [Map]<[String], [Any]>>
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
@ExperimentalCoroutinesApi
fun <C, S, E, V> EventSourcingLockingAggregate<C, S, E, V>.handleOptimisticallyWithMetaData(commandsWithMetadata: Flow<Pair<C, Map<String, Any>>>): Flow<Triple<E, V, Map<String, Any>>> =
    commandsWithMetadata.flatMapConcat { handleOptimistically(it.first, it.second) }

/**
 * Extension function - Handles the flow of command messages of type [Flow]<[C]>
 *
 * @param commands Command messages of type [Flow]<[C]>
 * @return [Flow] of stored Events of type [Pair]<[E], [V]>
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
@ExperimentalCoroutinesApi
fun <C, S, E, V> EventSourcingLockingOrchestratingAggregate<C, S, E, V>.handleOptimistically(commands: Flow<C>): Flow<Pair<E, V>> =
    commands.flatMapConcat { handleOptimistically(it) }

/**
 * Extension function - Handles the flow of command messages of type [Flow]<[Pair]<[C], [Map]<[String], [Any]>>>
 *
 * @param commandsWithMetadata Command messages with metadata of type [Flow]<[Pair]<[C], [Map]<[String], [Any]>>>
 * @return [Flow] of stored Events of type [Triple]<[E], [V], [Map]<[String], [Any]>>
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
@ExperimentalCoroutinesApi
fun <C, S, E, V> EventSourcingLockingOrchestratingAggregate<C, S, E, V>.handleOptimisticallyWithMetaData(
    commandsWithMetadata: Flow<Pair<C, Map<String, Any>>>
): Flow<Triple<E, V, Map<String, Any>>> =
    commandsWithMetadata.flatMapConcat { handleOptimistically(it.first, it.second) }

fun <C, E> C.publishTo(aggregate: EventSourcingAggregate<C, *, E>): Flow<E> =
    aggregate.handle(this)

fun <C, E> C.publishTo(
    aggregate: EventSourcingAggregate<C, *, E>,
    withMetaData: Map<String, Any>
): Flow<Pair<E, Map<String, Any>>> =
    aggregate.handle(this, withMetaData)

fun <C, E, V> C.publishOptimisticallyTo(aggregate: EventSourcingLockingAggregate<C, *, E, V>): Flow<Pair<E, V>> =
    aggregate.handleOptimistically(this)

fun <C, E, V> C.publishOptimisticallyTo(
    aggregate: EventSourcingLockingAggregate<C, *, E, V>,
    withMetaData: Map<String, Any>
): Flow<Triple<E, V, Map<String, Any>>> =
    aggregate.handleOptimistically(this, withMetaData)

@ExperimentalCoroutinesApi
fun <C, E> C.publishTo(aggregate: EventSourcingOrchestratingAggregate<C, *, E>): Flow<E> =
    aggregate.handle(this)

@ExperimentalCoroutinesApi
fun <C, E> C.publishTo(
    aggregate: EventSourcingOrchestratingAggregate<C, *, E>,
    withMetaData: Map<String, Any>
): Flow<Pair<E, Map<String, Any>>> =
    aggregate.handle(this, withMetaData)

@ExperimentalCoroutinesApi
fun <C, E, V> C.publishOptimisticallyTo(aggregate: EventSourcingLockingOrchestratingAggregate<C, *, E, V>): Flow<Pair<E, V>> =
    aggregate.handleOptimistically(this)

@ExperimentalCoroutinesApi
fun <C, E, V> C.publishOptimisticallyTo(
    aggregate: EventSourcingLockingOrchestratingAggregate<C, *, E, V>,
    withMetaData: Map<String, Any>
):
        Flow<Triple<E, V, Map<String, Any>>> =
    aggregate.handleOptimistically(this, withMetaData)

@ExperimentalCoroutinesApi
fun <C, E> Flow<C>.publishTo(aggregate: EventSourcingAggregate<C, *, E>): Flow<E> =
    aggregate.handle(this)

@ExperimentalCoroutinesApi
fun <C, E> Flow<Pair<C, Map<String, Any>>>.publishWithMetaDataTo(aggregate: EventSourcingAggregate<C, *, E>): Flow<Pair<E, Map<String, Any>>> =
    aggregate.handleWithMetaData(this)

@ExperimentalCoroutinesApi
fun <C, E, V> Flow<C>.publishOptimisticallyTo(aggregate: EventSourcingLockingAggregate<C, *, E, V>): Flow<Pair<E, V>> =
    aggregate.handleOptimistically(this)

@ExperimentalCoroutinesApi
fun <C, E, V> Flow<Pair<C, Map<String, Any>>>.publishOptimisticallyWithMetaDataTo(aggregate: EventSourcingLockingAggregate<C, *, E, V>): Flow<Triple<E, V, Map<String, Any>>> =
    aggregate.handleOptimisticallyWithMetaData(this)

@ExperimentalCoroutinesApi
fun <C, E> Flow<C>.publishTo(aggregate: EventSourcingOrchestratingAggregate<C, *, E>): Flow<E> =
    aggregate.handle(this)

@ExperimentalCoroutinesApi
fun <C, E> Flow<Pair<C, Map<String, Any>>>.publishWithMetaDataTo(aggregate: EventSourcingOrchestratingAggregate<C, *, E>): Flow<Pair<E, Map<String, Any>>> =
    aggregate.handleWithMetaData(this)

@ExperimentalCoroutinesApi
fun <C, E, V> Flow<C>.publishOptimisticallyTo(aggregate: EventSourcingLockingOrchestratingAggregate<C, *, E, V>): Flow<Pair<E, V>> =
    aggregate.handleOptimistically(this)

@ExperimentalCoroutinesApi
fun <C, E, V> Flow<Pair<C, Map<String, Any>>>.publishOptimisticallyWithMetaDataTo(aggregate: EventSourcingLockingOrchestratingAggregate<C, *, E, V>):
        Flow<Triple<E, V, Map<String, Any>>> =
    aggregate.handleOptimisticallyWithMetaData(this)