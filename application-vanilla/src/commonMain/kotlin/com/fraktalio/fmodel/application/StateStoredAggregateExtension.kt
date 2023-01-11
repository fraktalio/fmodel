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
suspend fun <C, S, E, I> I.handle(command: C): S where I : StateComputation<C, S, E>,
                                                       I : StateRepository<C, S> =
    command.fetchState().computeNewState(command).save()

/**
 * Extension function - Handles the command message of type [C] to the locking state stored aggregate, optimistically
 *
 * @param command Command message of type [C]
 * @return State of type [Pair]<[S], [V]>, in which [V] is the type of the Version (optimistic locking)
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
@FlowPreview
suspend fun <C, S, E, V, I> I.handleOptimistically(command: C): Pair<S, V> where I : StateComputation<C, S, E>,
                                                                                 I : StateLockingRepository<C, S, V> {
    val (state, version) = command.fetchState()
    return state
        .computeNewState(command)
        .save(version)
}

/**
 * Extension function - Handles the [Flow] of command messages of type [C]
 *
 * @param commands [Flow] of Command messages of type [C]
 * @return [Flow] of State of type [S]
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
@FlowPreview
fun <C, S, E, I> I.handle(commands: Flow<C>): Flow<S> where I : StateComputation<C, S, E>,
                                                            I : StateRepository<C, S> =
    commands.map { handle(it) }

/**
 * Extension function - Handles the [Flow] of command messages of type [C] to the locking state stored aggregate, optimistically
 *
 * @param commands [Flow] of Command messages of type [C]
 * @return [Flow] of State of type [Pair]<[S], [V]>, in which [V] is the type of the Version (optimistic locking)
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
@FlowPreview
fun <C, S, E, V, I> I.handleOptimistically(commands: Flow<C>): Flow<Pair<S, V>> where I : StateComputation<C, S, E>,
                                                                                      I : StateLockingRepository<C, S, V> =
    commands.map { handleOptimistically(it) }


/**
 * Extension function - Publishes the command of type [C] to the state stored aggregate
 * @receiver command of type [C]
 * @param aggregate of type [StateComputation]<[C], [S], [E]>, [StateRepository]<[C], [S]>
 * @return the stored State of type [S]
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
@FlowPreview
suspend fun <C, S, E, A> C.publishTo(aggregate: A): S where A : StateComputation<C, S, E>,
                                                            A : StateRepository<C, S> =
    aggregate.handle(this)

/**
 * Extension function - Publishes the command of type [C] to the locking state stored aggregate, optimistically
 * @receiver command of type [C]
 * @param aggregate of type [StateComputation]<[C], [S], [E]>, [StateLockingRepository]<[C], [S] ,[V]>
 * @return the stored State of type [Pair]<[S], [V]>
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
@FlowPreview
suspend fun <C, S, E, V, A> C.publishOptimisticallyTo(aggregate: A): Pair<S, V> where A : StateComputation<C, S, E>,
                                                                                      A : StateLockingRepository<C, S, V> =
    aggregate.handleOptimistically(this)

/**
 * Extension function - Publishes the command of type [C] to the state stored aggregate
 * @receiver [Flow] of commands of type [C]
 * @param aggregate of type [StateComputation]<[C], [S], [E]>, [StateRepository]<[C], [S]>
 * @return the [Flow] of State of type [S]
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
@FlowPreview
fun <C, S, E, A> Flow<C>.publishTo(aggregate: A): Flow<S> where A : StateComputation<C, S, E>,
                                                                A : StateRepository<C, S> =
    aggregate.handle(this)

/**
 * Extension function - Publishes the command of type [C] to the locking state stored aggregate, optimistically
 * @receiver [Flow] of commands of type [C]
 * @param aggregate of type [StateComputation]<[C], [S], [E]>, [StateLockingRepository]<[C], [S] ,[V]>
 * @return the [Flow] of State of type [Pair]<[S], [V]>
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
@FlowPreview
fun <C, S, E, V, A> Flow<C>.publishOptimisticallyTo(aggregate: A): Flow<Pair<S, V>> where A : StateComputation<C, S, E>,
                                                                                          A : StateLockingRepository<C, S, V> =
    aggregate.handleOptimistically(this)