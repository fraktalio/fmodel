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
 * Extension function - Handles the command message of type [C] by the snapshotting, event sourced aggregate.
 *
 * @param command Command message of type [C]
 * @return State of type [S]
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
fun <C, S, E, I> I.handle(command: C): Flow<E> where I : StateComputation<C, S, E>,
                                                     I : EventComputation<C, S, E>,
                                                     I : StateRepository<C, S>,
                                                     I : EventSnapshottingRepository<C, S, E> =
    flow {
        // 1. Fetch the latest state snapshot or NULL
        val latestSnapshotState = command.fetchState()
        // 2. Fetch the latest events, since the latest state snapshot
        val latestEvents = command.fetchEvents(latestSnapshotState).toList()
        // 3. Compute the current state, based on the latest state snapshot and the latest events
        val currentState = latestEvents.fold(latestSnapshotState ?: initialState) { s, e -> evolve(s, e) }
        // 4. Compute the new events, based on the latest events, latest snapshot state and the command, and save it
        val newEvents = latestEvents.asFlow()
            .computeNewEvents(command, latestSnapshotState)
            .save()
        // 5. Compute the new state, based on the current state and the command and save it conditionally
        with(currentState.computeNewState(command)) {
            if (shouldCreateNewSnapshot(latestSnapshotState)) {
                save()
            }
        }
        emitAll(newEvents)
    }

/**
 * Extension function - Handles the command message of type [C] by the snapshotting, locking event sourced aggregate, optimistically
 *
 * @param command Command message of type [C]
 * @return State of type [Pair]<[S], [V]>, in which [V] is the type of the Version (optimistic locking)
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
suspend fun <C, S, E, V, I> I.handleOptimistically(command: C): Flow<Pair<E, V>> where I : StateComputation<C, S, E>,
                                                                                       I : EventComputation<C, S, E>,
                                                                                       I : StateLockingRepository<C, S, V>,
                                                                                       I : EventSnapshottingLockingRepository<C, S, E, V> =
    flow {
        // 1. Fetch the latest state snapshot or NULL
        val (latestSnapshotState, latestSnapshotVersion) = command.fetchState()
        // 2. Fetch the latest events, since the latest state snapshot
        val latestEvents = command.fetchEvents(Pair(latestSnapshotState, latestSnapshotVersion)).toList()
        // 3. Compute the current state, based on the latest state snapshot and the latest events
        val currentState = latestEvents.fold(latestSnapshotState ?: initialState) { s, e -> evolve(s, e.first) }
        // 4. Get the latest event version
        val latestEventVersion = latestEvents.map { it.second }.lastOrNull()
        // 5. Compute the new events, based on the latest events, latest snapshot state and the command, and save it
        val newEvents = latestEvents.asFlow()
            .map { it.first }
            .computeNewEvents(command, latestSnapshotState)
            .save(latestEventVersion)
        // 6. Get the new snapshot version = the last/latest event version
        val newSnapshotVersion = newEvents.map { it.second }.lastOrNull()
        // 7. Compute the new state, based on the current state and the command and save it conditionally
        with(currentState.computeNewState(command)) {
            if (shouldCreateNewSnapshot(
                    latestSnapshotState,
                    latestSnapshotVersion,
                    newSnapshotVersion
                )
            )
                save(latestSnapshotVersion, newSnapshotVersion)
        }
        emitAll(newEvents)
    }


/**
 * Extension function - Handles the command message of type [C] by the snapshotting, orchestrating event sourced aggregate
 *
 *
 * @param command Command message of type [C]
 * @return State of type [S]
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
@ExperimentalCoroutinesApi
fun <C, S, E, I> I.handle(command: C): Flow<E> where I : StateOrchestratingComputation<C, S, E>,
                                                     I : EventOrchestratingComputation<C, S, E>,
                                                     I : StateRepository<C, S>,
                                                     I : EventSnapshottingRepository<C, S, E> =
    flow {
        // 1. Fetch the latest state snapshot or NULL
        val latestSnapshotState = command.fetchState()
        // 2. Fetch the latest events, since the latest state snapshot
        val latestEvents = command.fetchEvents(latestSnapshotState).toList()
        // 3. Compute the current state, based on the latest state snapshot and the latest events
        val currentState = latestEvents.fold(latestSnapshotState ?: initialState) { s, e -> evolve(s, e) }
        // 4. Compute the new events, based on the latest events, latest snapshot state and the command, and save it
        val newEvents = latestEvents.asFlow()
            .computeNewEventsByOrchestrating(command, latestSnapshotState) { it.fetchEvents(latestSnapshotState) }
            .save()
        // 5. Compute the new state, based on the current state and the command and save it conditionally
        with(currentState.computeNewState(command)) {
            if (shouldCreateNewSnapshot(latestSnapshotState)) {
                save()
            }
        }
        emitAll(newEvents)
    }

/**
 * Extension function - Handles the command message of type [C] by the snapshotting, locking, orchestrating event sourced aggregate, optimistically
 *
 * @param command Command message of type [C]
 * @return State of type [Pair]<[S], [V]>, in which [V] is the type of the Version (optimistic locking)
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
@ExperimentalCoroutinesApi
suspend fun <C, S, E, V, I> I.handleOptimistically(command: C): Flow<Pair<E, V>> where I : StateOrchestratingComputation<C, S, E>,
                                                                                       I : EventOrchestratingComputation<C, S, E>,
                                                                                       I : StateLockingRepository<C, S, V>,
                                                                                       I : EventSnapshottingLockingRepository<C, S, E, V> =
    flow {
        // 1. Fetch the latest state snapshot or NULL
        val (latestSnapshotState, latestSnapshotVersion) = command.fetchState()
        // 2. Fetch the latest events, since the latest state snapshot
        val latestEvents = command.fetchEvents(Pair(latestSnapshotState, latestSnapshotVersion)).toList()
        // 3. Compute the current state, based on the latest state snapshot and the latest events
        val currentState = latestEvents.fold(latestSnapshotState ?: initialState) { s, e -> evolve(s, e.first) }
        // 4. Get the latest event version
        val latestEventVersion = latestEvents.map { it.second }.lastOrNull()
        // 5. Compute the new events, based on the latest events, latest snapshot state and the command, and save it
        val newEvents = latestEvents.asFlow()
            .map { it.first }
            .computeNewEventsByOrchestrating(command, latestSnapshotState) {
                it.fetchEvents(
                    Pair(
                        latestSnapshotState,
                        latestSnapshotVersion
                    )
                ).map { pair -> pair.first }
            }
            .save(latestEventVersion)
        // 6. Get the new snapshot version = the last/latest event version
        val newSnapshotVersion = newEvents.map { it.second }.lastOrNull()
        // 7. Compute the new state, based on the current state and the command and save it conditionally
        with(currentState.computeNewState(command)) {
            if (shouldCreateNewSnapshot(
                    latestSnapshotState,
                    latestSnapshotVersion,
                    newSnapshotVersion
                )
            )
                save(latestSnapshotVersion, newSnapshotVersion)
        }
        emitAll(newEvents)
    }