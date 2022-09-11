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
import arrow.core.nonFatalOrThrow
import com.fraktalio.fmodel.application.Error.*
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

/**
 * Extension function - Handles the command message of type [C]
 *
 * @param command Command message of type [C]
 * @return [Effect] (either [Error] or State of type [S])
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
@FlowPreview
suspend fun <C, S, E, I> I.handleWithEffect(command: C): Effect<Error, S> where I : StateComputation<C, S, E>,
                                                                                I : StateRepository<C, S> {
    /**
     * Inner function - Computes new State based on the previous State and the [command] or fails.
     *
     * @param command of type [C]
     * @return [Effect] (either the newly computed state of type [S] or [Error])
     */
    suspend fun S?.computeNewStateWithEffect(command: C): Effect<Error, S> =
        effect {
            try {
                computeNewState(command)
            } catch (t: Throwable) {
                shift(CalculatingNewStateFailed(this@computeNewStateWithEffect, command, t.nonFatalOrThrow()))
            }
        }

    /**
     * Inner function - Fetch state - either version
     *
     * @receiver Command of type [C]
     * @return [Effect] (either [Error] or the State of type [S]?)
     */
    suspend fun C.fetchStateWithEffect(): Effect<Error, S?> =
        effect {
            try {
                fetchState()
            } catch (t: Throwable) {
                shift(FetchingStateFailed(this@fetchStateWithEffect, t.nonFatalOrThrow()))
            }
        }

    /**
     * Inner function - Save state - either version
     *
     * @receiver State of type [S]
     * @return [Effect] (either [Error] or the newly saved State of type [S])
     */
    suspend fun S.saveWithEffect(): Effect<Error, S> =
        effect {
            try {
                save()
            } catch (t: Throwable) {
                shift(StoringStateFailed(this@saveWithEffect, t.nonFatalOrThrow()))
            }
        }

    return effect {
        command
            .fetchStateWithEffect().bind()
            .computeNewStateWithEffect(command).bind()
            .saveWithEffect().bind()
    }
}

/**
 * Extension function - Handles the command message of type [C] to the locking state stored aggregate, optimistically
 *
 * @param command Command message of type [C]
 * @return [Effect] (either [Error] or State of type [Pair]<[S], [V]>), in which [V] represents Version
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
@FlowPreview
suspend fun <C, S, E, V, I> I.handleOptimisticallyWithEffect(command: C): Effect<Error, Pair<S, V>> where I : StateComputation<C, S, E>,
                                                                                                          I : StateLockingRepository<C, S, V> {
    /**
     * Inner function - Computes new State based on the previous State and the [command] or fails.
     *
     * @param command of type [C]
     * @return [Effect] (either the newly computed state of type [S] or [Error])
     */
    suspend fun S?.computeNewStateWithEffect(command: C): Effect<Error, S> =
        effect {
            try {
                computeNewState(command)
            } catch (t: Throwable) {
                shift(CalculatingNewStateFailed(this@computeNewStateWithEffect, command, t.nonFatalOrThrow()))
            }
        }

    /**
     * Inner function - Fetch state - either version
     *
     * @receiver Command of type [C]
     * @return [Effect] (either [Error] or the State of type [S]?)
     */
    suspend fun C.fetchStateWithEffect(): Effect<Error, Pair<S?, V?>> =
        effect {
            try {
                fetchState()
            } catch (t: Throwable) {
                shift(FetchingStateFailed(this@fetchStateWithEffect, t.nonFatalOrThrow()))
            }
        }

    /**
     * Inner function - Save state - either version
     *
     * @receiver State of type [S]
     * @return [Effect] (either [Error] or the newly saved State of type [S])
     */
    suspend fun S.saveWithEffect(currentStateVersion: V?): Effect<Error, Pair<S, V>> =
        effect {
            try {
                save(currentStateVersion)
            } catch (t: Throwable) {
                shift(StoringStateFailed(this@saveWithEffect, t.nonFatalOrThrow()))
            }
        }

    return effect {
        val (state, version) = command.fetchStateWithEffect().bind()
        state
            .computeNewStateWithEffect(command).bind()
            .saveWithEffect(version).bind()
    }
}


/**
 * Extension function - Handles the [Flow] of command messages of type [C]
 *
 * @param commands [Flow] of Command messages of type [C]
 * @return [Flow] of [Effect] (either [Error] or State of type [S])
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
@FlowPreview
fun <C, S, E, I> I.handleWithEffect(commands: Flow<C>): Flow<Effect<Error, S>> where I : StateComputation<C, S, E>,
                                                                                     I : StateRepository<C, S> =
    commands
        .map { handleWithEffect(it) }
        .catch { emit(effect { shift(CommandPublishingFailed(it)) }) }

/**
 * Extension function - Handles the [Flow] of command messages of type [C] to the locking state stored aggregate, optimistically
 *
 * @param commands [Flow] of Command messages of type [C]
 * @return [Flow] of [Effect] (either [Error] or State of type [Pair]<[S], [V]>)
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
@FlowPreview
fun <C, S, E, V, I> I.handleOptimisticallyWithEffect(commands: Flow<C>): Flow<Effect<Error, Pair<S, V>>> where I : StateComputation<C, S, E>,
                                                                                                               I : StateLockingRepository<C, S, V> =
    commands
        .map { handleOptimisticallyWithEffect(it) }
        .catch { emit(effect { shift(CommandPublishingFailed(it)) }) }

/**
 * Extension function - Publishes the command of type [C] to the state stored aggregate
 * @receiver command of type [C]
 * @param aggregate of type [StateComputation]<[C], [S], [E]>, [StateRepository]<[C], [S]>
 * @return [Effect] (either [Error] or successfully stored State of type [S])
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
@FlowPreview
suspend fun <C, S, E, A> C.publishWithEffect(aggregate: A): Effect<Error, S> where A : StateComputation<C, S, E>,
                                                                                   A : StateRepository<C, S> =
    aggregate.handleWithEffect(this)

/**
 * Extension function - Publishes the command of type [C] to the locking state stored aggregate, optimistically
 * @receiver command of type [C]
 * @param aggregate of type [StateComputation]<[C], [S], [E]>, [StateLockingRepository]<[C], [S], [V]>
 * @return [Effect] (either [Error] or successfully stored State of type [Pair]<[S], [V]>)
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
@FlowPreview
suspend fun <C, S, E, V, A> C.publishOptimisticallyWithEffect(aggregate: A): Effect<Error, Pair<S, V>> where A : StateComputation<C, S, E>,
                                                                                                             A : StateLockingRepository<C, S, V> =
    aggregate.handleOptimisticallyWithEffect(this)

/**
 * Extension function - Publishes the command of type [C] to the state stored aggregate
 * @receiver [Flow] of commands of type [C]
 * @param aggregate of type [StateComputation]<[C], [S], [E]>, [StateRepository]<[C], [S]>
 * @return the [Flow] of [Effect] (either [Error] or successfully  stored State of type [S])
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
@FlowPreview
fun <C, S, E, A> Flow<C>.publishWithEffect(aggregate: A): Flow<Effect<Error, S>> where A : StateComputation<C, S, E>,
                                                                                       A : StateRepository<C, S> =
    aggregate.handleWithEffect(this)

/**
 * Extension function - Publishes the command of type [C] to the locking state stored aggregate, optimistically
 * @receiver [Flow] of commands of type [C]
 * @param aggregate of type [StateComputation]<[C], [S], [E]>, [StateLockingRepository]<[C], [S], [V]>
 * @return the [Flow] of [Effect] (either [Error] or successfully  stored State of type [Pair]<[S], [V]>)
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
@FlowPreview
fun <C, S, E, V, A> Flow<C>.publishOptimisticallyWithEffect(aggregate: A): Flow<Effect<Error, Pair<S, V>>> where A : StateComputation<C, S, E>,
                                                                                                                 A : StateLockingRepository<C, S, V> =
    aggregate.handleOptimisticallyWithEffect(this)

private fun <S, V> S.pairWith(version: V): Pair<S, V> = Pair(this, version)