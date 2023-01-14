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

import arrow.core.Either
import arrow.core.continuations.either
import com.fraktalio.fmodel.application.Error.CommandHandlingFailed
import com.fraktalio.fmodel.application.Error.CommandPublishingFailed
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*

/**
 * Extension function - Handles the command message of type [C]
 *
 * @param command Command message of type [C]
 * @return [Flow] of [Either] (either [Error] or Events of type [E])
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
@FlowPreview
fun <C, S, E> EventSourcingAggregate<C, S, E>.handleWithEffect(command: C): Flow<Either<Error, E>> =
    command
        .fetchEvents()
        .computeNewEvents(command)
        .save()
        .map { either<Error, E> { it } }
        .catch { emit(either { raise(CommandHandlingFailed(command)) }) }

/**
 * Extension function - Handles the command message of type [C]
 *
 * @param command Command message of type [C]
 * @return [Flow] of [Either] (either [Error] or Events of type [E])
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
@FlowPreview
fun <C, S, E> EventSourcingOrchestratingAggregate<C, S, E>.handleWithEffect(command: C): Flow<Either<Error, E>> =
    command
        .fetchEvents()
        .computeNewEventsByOrchestrating(command) { it.fetchEvents() }
        .save()
        .map { either<Error, E> { it } }
        .catch { emit(either { raise(CommandHandlingFailed(command)) }) }

/**
 * Extension function - Handles the command message of type [C]
 *
 * @param command Command message of type [C]
 * @return [Flow] of [Either] (either [Error] or Events of type [Pair]<[E], [V]>)
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
@FlowPreview
fun <C, S, E, V> EventSourcingLockingAggregate<C, S, E, V>.handleOptimisticallyWithEffect(command: C): Flow<Either<Error, Pair<E, V>>> =
    flow {
        val events = command.fetchEvents()
        emitAll(
            events.map { it.first }
                .computeNewEvents(command)
                .save(events.lastOrNull())
                .map { either<Error, Pair<E, V>> { it } }
                .catch { emit(either { raise(CommandHandlingFailed(command)) }) }
        )
    }

/**
 * Extension function - Handles the command message of type [C]
 *
 * @param command Command message of type [C]
 * @return [Flow] of [Either] (either [Error] or Events of type [Pair]<[E], [V]>)
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
@FlowPreview
fun <C, S, E, V> EventSourcingLockingOrchestratingAggregate<C, S, E, V>.handleOptimisticallyWithEffect(command: C): Flow<Either<Error, Pair<E, V>>> =
    command
        .fetchEvents().map { it.first }
        .computeNewEventsByOrchestrating(command) { it.fetchEvents().map { pair -> pair.first } }
        .save(latestVersionProvider)
        .map { either<Error, Pair<E, V>> { it } }
        .catch { emit(either { raise(CommandHandlingFailed(command)) }) }


@FlowPreview
fun <C, S, E> EventSourcingAggregate<C, S, E>.handleWithEffect(commands: Flow<C>): Flow<Either<Error, E>> =
    commands
        .flatMapConcat { handleWithEffect(it) }
        .catch { emit(either { raise(CommandPublishingFailed(it)) }) }

@FlowPreview
fun <C, S, E> EventSourcingOrchestratingAggregate<C, S, E>.handleWithEffect(commands: Flow<C>): Flow<Either<Error, E>> =
    commands
        .flatMapConcat { handleWithEffect(it) }
        .catch { emit(either { raise(CommandPublishingFailed(it)) }) }

@FlowPreview
fun <C, S, E, V> EventSourcingLockingAggregate<C, S, E, V>.handleOptimisticallyWithEffect(commands: Flow<C>): Flow<Either<Error, Pair<E, V>>> =
    commands
        .flatMapConcat { handleOptimisticallyWithEffect(it) }
        .catch { emit(either { raise(CommandPublishingFailed(it)) }) }

@FlowPreview
fun <C, S, E, V> EventSourcingLockingOrchestratingAggregate<C, S, E, V>.handleOptimisticallyWithEffect(commands: Flow<C>): Flow<Either<Error, Pair<E, V>>> =
    commands
        .flatMapConcat { handleOptimisticallyWithEffect(it) }
        .catch { emit(either { raise(CommandPublishingFailed(it)) }) }

@FlowPreview
fun <C, E> C.publishWithEffect(aggregate: EventSourcingAggregate<C, *, E>): Flow<Either<Error, E>> =
    aggregate.handleWithEffect(this)

@FlowPreview
fun <C, E> C.publishWithEffect(aggregate: EventSourcingOrchestratingAggregate<C, *, E>): Flow<Either<Error, E>> =
    aggregate.handleWithEffect(this)

@FlowPreview
fun <C, E, V> C.publishOptimisticallyWithEffect(aggregate: EventSourcingLockingAggregate<C, *, E, V>): Flow<Either<Error, Pair<E, V>>> =
    aggregate.handleOptimisticallyWithEffect(this)

@FlowPreview
fun <C, E, V> C.publishOptimisticallyWithEffect(aggregate: EventSourcingLockingOrchestratingAggregate<C, *, E, V>): Flow<Either<Error, Pair<E, V>>> =
    aggregate.handleOptimisticallyWithEffect(this)

@FlowPreview
fun <C, E> Flow<C>.publishWithEffect(aggregate: EventSourcingAggregate<C, *, E>): Flow<Either<Error, E>> =
    aggregate.handleWithEffect(this)

@FlowPreview
fun <C, E> Flow<C>.publishWithEffect(aggregate: EventSourcingOrchestratingAggregate<C, *, E>): Flow<Either<Error, E>> =
    aggregate.handleWithEffect(this)

@FlowPreview
fun <C, E, V> Flow<C>.publishOptimisticallyWithEffect(aggregate: EventSourcingLockingAggregate<C, *, E, V>): Flow<Either<Error, Pair<E, V>>> =
    aggregate.handleOptimisticallyWithEffect(this)

@FlowPreview
fun <C, E, V> Flow<C>.publishOptimisticallyWithEffect(aggregate: EventSourcingLockingOrchestratingAggregate<C, *, E, V>): Flow<Either<Error, Pair<E, V>>> =
    aggregate.handleOptimisticallyWithEffect(this)
