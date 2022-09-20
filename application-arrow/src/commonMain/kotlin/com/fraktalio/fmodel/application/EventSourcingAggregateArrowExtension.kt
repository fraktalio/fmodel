/*
 * Copyright (c) 2022 Fraktalio D.O.O. All rights reserved.
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

import arrow.core.continuations.Effect
import arrow.core.continuations.effect
import com.fraktalio.fmodel.application.Error.CommandHandlingFailed
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*

/**
 * Extension function - Handles the command message of type [C]
 *
 * @param command Command message of type [C]
 * @return [Flow] of [Effect] (either [Error] or Events of type [E])
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
@FlowPreview
fun <C, S, E> EventSourcingAggregate<C, S, E>.handleWithEffect(command: C): Flow<Effect<Error, E>> =
    command
        .fetchEvents()
        .computeNewEvents(command)
        .save()
        .map { effect<Error, E> { it } }
        .catch { emit(effect { shift(CommandHandlingFailed(command)) }) }

/**
 * Extension function - Handles the command message of type [C]
 *
 * @param command Command message of type [C]
 * @return [Flow] of [Effect] (either [Error] or Events of type [E])
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
@FlowPreview
fun <C, S, E> EventSourcingOrchestratingAggregate<C, S, E>.handleWithEffect(command: C): Flow<Effect<Error, E>> =
    command
        .fetchEvents()
        .computeNewEventsByOrchestrating(command) { it.fetchEvents() }
        .save()
        .map { effect<Error, E> { it } }
        .catch { emit(effect { shift(CommandHandlingFailed(command)) }) }

/**
 * Extension function - Handles the command message of type [C]
 *
 * @param command Command message of type [C]
 * @return [Flow] of [Effect] (either [Error] or Events of type [Pair]<[E], [V]>)
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
@FlowPreview
fun <C, S, E, V> EventSourcingLockingAggregate<C, S, E, V>.handleOptimisticallyWithEffect(command: C): Flow<Effect<Error, Pair<E, V>>> =
    flow {
        val events = command.fetchEvents()
        emitAll(
            events.map { it.first }
                .computeNewEvents(command)
                .save(events.lastOrNull())
                .map { effect<Error, Pair<E, V>> { it } }
                .catch { emit(effect { shift(CommandHandlingFailed(command)) }) }
        )
    }

/**
 * Extension function - Handles the command message of type [C]
 *
 * @param command Command message of type [C]
 * @return [Flow] of [Effect] (either [Error] or Events of type [Pair]<[E], [V]>)
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
@FlowPreview
fun <C, S, E, V> EventSourcingLockingOrchestratingAggregate<C, S, E, V>.handleOptimisticallyWithEffect(command: C): Flow<Effect<Error, Pair<E, V>>> =
    command
        .fetchEvents().map { it.first }
        .computeNewEventsByOrchestrating(command) { it.fetchEvents().map { pair -> pair.first } }
        .save(latestVersionProvider)
        .map { effect<Error, Pair<E, V>> { it } }
        .catch { emit(effect { shift(CommandHandlingFailed(command)) }) }


@FlowPreview
fun <C, S, E> EventSourcingAggregate<C, S, E>.handleWithEffect(commands: Flow<C>): Flow<Effect<Error, E>> =
    commands
        .flatMapConcat { handleWithEffect(it) }
        .catch { emit(effect { shift(CommandHandlingFailed(it)) }) }

@FlowPreview
fun <C, S, E> EventSourcingOrchestratingAggregate<C, S, E>.handleWithEffect(commands: Flow<C>): Flow<Effect<Error, E>> =
    commands
        .flatMapConcat { handleWithEffect(it) }
        .catch { emit(effect { shift(CommandHandlingFailed(it)) }) }

@FlowPreview
fun <C, S, E, V> EventSourcingLockingAggregate<C, S, E, V>.handleOptimisticallyWithEffect(commands: Flow<C>): Flow<Effect<Error, Pair<E, V>>> =
    commands
        .flatMapConcat { handleOptimisticallyWithEffect(it) }
        .catch { emit(effect { shift(CommandHandlingFailed(it)) }) }

@FlowPreview
fun <C, S, E, V> EventSourcingLockingOrchestratingAggregate<C, S, E, V>.handleOptimisticallyWithEffect(commands: Flow<C>): Flow<Effect<Error, Pair<E, V>>> =
    commands
        .flatMapConcat { handleOptimisticallyWithEffect(it) }
        .catch { emit(effect { shift(CommandHandlingFailed(it)) }) }

@FlowPreview
fun <C, E> C.publishWithEffect(aggregate: EventSourcingAggregate<C, *, E>): Flow<Effect<Error, E>> =
    aggregate.handleWithEffect(this)

@FlowPreview
fun <C, E> C.publishWithEffect(aggregate: EventSourcingOrchestratingAggregate<C, *, E>): Flow<Effect<Error, E>> =
    aggregate.handleWithEffect(this)

@FlowPreview
fun <C, E, V> C.publishOptimisticallyWithEffect(aggregate: EventSourcingLockingAggregate<C, *, E, V>): Flow<Effect<Error, Pair<E, V>>> =
    aggregate.handleOptimisticallyWithEffect(this)

@FlowPreview
fun <C, E, V> C.publishOptimisticallyWithEffect(aggregate: EventSourcingLockingOrchestratingAggregate<C, *, E, V>): Flow<Effect<Error, Pair<E, V>>> =
    aggregate.handleOptimisticallyWithEffect(this)

@FlowPreview
fun <C, E> Flow<C>.publishWithEffect(aggregate: EventSourcingAggregate<C, *, E>): Flow<Effect<Error, E>> =
    aggregate.handleWithEffect(this)

@FlowPreview
fun <C, E> Flow<C>.publishWithEffect(aggregate: EventSourcingOrchestratingAggregate<C, *, E>): Flow<Effect<Error, E>> =
    aggregate.handleWithEffect(this)

@FlowPreview
fun <C, E, V> Flow<C>.publishOptimisticallyWithEffect(aggregate: EventSourcingLockingAggregate<C, *, E, V>): Flow<Effect<Error, Pair<E, V>>> =
    aggregate.handleOptimisticallyWithEffect(this)

@FlowPreview
fun <C, E, V> Flow<C>.publishOptimisticallyWithEffect(aggregate: EventSourcingLockingOrchestratingAggregate<C, *, E, V>): Flow<Effect<Error, Pair<E, V>>> =
    aggregate.handleOptimisticallyWithEffect(this)
