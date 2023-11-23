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
suspend fun <C, S, E, I> I.handle(command: C): S where I : StateComputation<C, S, E>,
                                                       I : StateRepository<C, S> =
    command.fetchState().computeNewState(command).save()

/**
 * Extension function - Handles the command message of type [C] and metadata of type [Map]<[String], [Any]>
 *
 * @param command Command message of type [C]
 * @param metaData metadata of type [Map]<[String], [Any]>
 * @return State with metadata of type [Pair]<[S], [Map]<[String], [Any]>>
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
suspend fun <C, S, E, I> I.handle(
    command: C,
    metaData: Map<String, Any>
): Pair<S, Map<String, Any>> where I : StateComputation<C, S, E>,
                                   I : StateRepository<C, S> =
    command.fetchStateAndMetadata().first.computeNewState(command).saveWithMetadata(metaData)

/**
 * Extension function - Handles the command message of type [C] to the locking state stored aggregate, optimistically
 *
 * @param command Command message of type [C]
 * @return State of type [Pair]<[S], [V]>, in which [V] is the type of the Version (optimistic locking)
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
suspend fun <C, S, E, V, I> I.handleOptimistically(command: C): Pair<S, V> where I : StateComputation<C, S, E>,
                                                                                 I : StateLockingRepository<C, S, V> {
    val (state, version) = command.fetchState()
    return state
        .computeNewState(command)
        .save(version)
}

/**
 * Extension function - Handles the command message of type [C] and metadata of type [Map]<[String], [Any]> to the locking state stored aggregate, optimistically
 *
 * @param command Command message of type [C]
 * @param metaData metadata of type [Map]<[String], [Any]>
 * @return State with metadata of type [Triple]<[S], [V], [Map]<[String], [Any]>>, in which [V] is the type of the Version (optimistic locking)
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
suspend fun <C, S, E, V, I> I.handleOptimistically(
    command: C,
    metaData: Map<String, Any>
): Triple<S, V, Map<String, Any>> where I : StateComputation<C, S, E>,
                                        I : StateLockingRepository<C, S, V> {
    val (state, version, _) = command.fetchStateAndMetadata()
    return state
        .computeNewState(command)
        .saveWithMetadata(version, metaData)
}

/**
 * Extension function - Handles the [Flow] of command messages of type [C]
 *
 * @param commands [Flow] of Command messages of type [C]
 * @return [Flow] of State of type [S]
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
fun <C, S, E, I> I.handle(commands: Flow<C>): Flow<S> where I : StateComputation<C, S, E>,
                                                            I : StateRepository<C, S> =
    commands.map { handle(it) }

/**
 * Extension function - Handles the [Flow] of command messages with metadata of type [Pair]<[C], [Map]<[String], [Any]>>
 *
 * @param commands [Flow] of Command messages with metadata of type [Pair]<[C], [Map]<[String], [Any]>>
 * @return [Flow] of State with metadata of type [Pair]<[S], [Map]<[String], [Any]>>
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
fun <C, S, E, I> I.handleWithMetadata(commands: Flow<Pair<C, Map<String, Any>>>): Flow<Pair<S, Map<String, Any>>> where I : StateComputation<C, S, E>,
                                                                                                                        I : StateRepository<C, S> =
    commands.map { handle(it.first, it.second) }

/**
 * Extension function - Handles the [Flow] of command messages of type [C] to the locking state stored aggregate, optimistically
 *
 * @param commands [Flow] of Command messages of type [C]
 * @return [Flow] of State of type [Pair]<[S], [V]>, in which [V] is the type of the Version (optimistic locking)
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
fun <C, S, E, V, I> I.handleOptimistically(commands: Flow<C>): Flow<Pair<S, V>> where I : StateComputation<C, S, E>,
                                                                                      I : StateLockingRepository<C, S, V> =
    commands.map { handleOptimistically(it) }

/**
 * Extension function - Handles the [Flow] of command messages with metadata of type [Pair]<[C], [Map]<[String], [Any]>> to the locking state stored aggregate, optimistically
 *
 * @param commands [Flow] of Command messages with metadata of type [Pair]<[C], [Map]<[String], [Any]>>
 * @return [Flow] of State with metadata of type [Triple]<[S], [V], [Map]<[String], [Any]>>>, in which [V] is the type of the Version (optimistic locking)
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
fun <C, S, E, V, I> I.handleOptimisticallyWithMetadata(commands: Flow<Pair<C, Map<String, Any>>>): Flow<Triple<S, V, Map<String, Any>>> where I : StateComputation<C, S, E>,
                                                                                                                                              I : StateLockingRepository<C, S, V> =
    commands.map { handleOptimistically(it.first, it.second) }

/**
 * Extension function - Publishes the command of type [C] to the state stored aggregate
 * @receiver command of type [C]
 * @param aggregate of type [StateComputation]<[C], [S], [E]>, [StateRepository]<[C], [S]>
 * @return the stored State of type [S]
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
suspend fun <C, S, E, A> C.publishTo(aggregate: A): S where A : StateComputation<C, S, E>,
                                                            A : StateRepository<C, S> =
    aggregate.handle(this)

/**
 * Extension function - Publishes the command of type [C] with metadata of type [Map]<[String], [Any]> to the state stored aggregate
 * @receiver command of type [C]
 * @param aggregate of type [StateComputation]<[C], [S], [E]>, [StateRepository]<[C], [S]>
 * @param withMetadata metadata of type [Map]<[String], [Any]>
 * @return the stored State with metadata of type [Pair]<[S], [Map]<[String], [Any]>>
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
suspend fun <C, S, E, A> C.publishTo(
    aggregate: A,
    withMetadata: Map<String, Any>
): Pair<S, Map<String, Any>> where A : StateComputation<C, S, E>,
                                   A : StateRepository<C, S> =
    aggregate.handle(this, withMetadata)

/**
 * Extension function - Publishes the command of type [C] to the locking state stored aggregate, optimistically
 * @receiver command of type [C]
 * @param aggregate of type [StateComputation]<[C], [S], [E]>, [StateLockingRepository]<[C], [S] ,[V]>
 * @return the stored State of type [Pair]<[S], [V]>
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
suspend fun <C, S, E, V, A> C.publishOptimisticallyTo(aggregate: A): Pair<S, V> where A : StateComputation<C, S, E>,
                                                                                      A : StateLockingRepository<C, S, V> =
    aggregate.handleOptimistically(this)

/**
 * Extension function - Publishes the command of type [C] with metadata of type [Map]<[String], [Any]> to the locking state stored aggregate, optimistically
 * @receiver command of type [C]
 * @param aggregate of type [StateComputation]<[C], [S], [E]>, [StateLockingRepository]<[C], [S] ,[V]>
 * @param withMetadata metadata of type [Map]<[String], [Any]>
 * @return the stored State with version and metadata of type [Triple]<[S], [V], [Map]<[String], [Any]>>
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
suspend fun <C, S, E, V, A> C.publishOptimisticallyTo(
    aggregate: A,
    withMetadata: Map<String, Any>
): Triple<S, V, Map<String, Any>> where A : StateComputation<C, S, E>,
                                        A : StateLockingRepository<C, S, V> =
    aggregate.handleOptimistically(this, withMetadata)

/**
 * Extension function - Publishes the [Flow] of commands of type [C] to the state stored aggregate
 * @receiver [Flow] of commands of type [C]
 * @param aggregate of type [StateComputation]<[C], [S], [E]>, [StateRepository]<[C], [S]>
 * @return the [Flow] of State of type [S]
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
fun <C, S, E, A> Flow<C>.publishTo(aggregate: A): Flow<S> where A : StateComputation<C, S, E>,
                                                                A : StateRepository<C, S> =
    aggregate.handle(this)

/**
 * Extension function - Publishes the [Flow] of commands of type [C] with metadata of type [Pair]<[C], [Map]<[String], [Any]>> to the state stored aggregate
 * @receiver [Flow] of commands of type [C]
 * @param aggregate of type [StateComputation]<[C], [S], [E]>, [StateRepository]<[C], [S]>
 * @return the [Flow] of State with metadata of type [Pair]<[S], [Map]<[String], [Any]>>
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
fun <C, S, E, A> Flow<Pair<C, Map<String, Any>>>.publishWithMetadataTo(aggregate: A): Flow<Pair<S, Map<String, Any>>> where A : StateComputation<C, S, E>,
                                                                                                                            A : StateRepository<C, S> =
    aggregate.handleWithMetadata(this)

/**
 * Extension function - Publishes the command of type [C] to the locking state stored aggregate, optimistically
 * @receiver [Flow] of commands of type [C]
 * @param aggregate of type [StateComputation]<[C], [S], [E]>, [StateLockingRepository]<[C], [S] ,[V]>
 * @return the [Flow] of State of type [Pair]<[S], [V]>
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
fun <C, S, E, V, A> Flow<C>.publishOptimisticallyTo(aggregate: A): Flow<Pair<S, V>> where A : StateComputation<C, S, E>,
                                                                                          A : StateLockingRepository<C, S, V> =
    aggregate.handleOptimistically(this)

/**
 * Extension function - Publishes the [Flow] of commands of type [C] with metadata of type [Pair]<[C], [Map]<[String], [Any]>> to the locking state stored aggregate, optimistically
 * @receiver [Flow] of commands with metadata of type [Pair]<[C], [Map]<[String], [Any]>>
 * @param aggregate of type [StateComputation]<[C], [S], [E]>, [StateLockingRepository]<[C], [S] ,[V]>
 * @return the [Flow] of State with version and metadata of type [Triple]<[S], [V], [Map]<[String], [Any]>>
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
fun <C, S, E, V, A> Flow<Pair<C, Map<String, Any>>>.publishOptimisticallyWithMetadataTo(aggregate: A): Flow<Triple<S, V, Map<String, Any>>> where A : StateComputation<C, S, E>,
                                                                                                                                                  A : StateLockingRepository<C, S, V> =
    aggregate.handleOptimisticallyWithMetadata(this)