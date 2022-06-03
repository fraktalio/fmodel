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
suspend fun <C, S, E> StateStoredAggregate<C, S, E>.handleWithEffect(command: C): Effect<Error, S> {
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
 * Extension function - Handles the [Flow] of command messages of type [C]
 *
 * @param commands [Flow] of Command messages of type [C]
 * @return [Flow] of [Effect] (either [Error] or State of type [S])
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
@FlowPreview
fun <C, S, E> StateStoredAggregate<C, S, E>.handleWithEffect(commands: Flow<C>): Flow<Effect<Error, S>> =
    commands
        .map { handleWithEffect(it) }
        .catch { emit(effect { shift(CommandPublishingFailed(it)) }) }

/**
 * Extension function - Publishes the command of type [C] to the state stored aggregate of type  [StateStoredAggregate]<[C], [S], *>
 * @receiver command of type [C]
 * @param aggregate of type [StateStoredAggregate]<[C], [S], *>
 * @return [Effect] (either [Error] or successfully stored State of type [S])
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
@FlowPreview
suspend fun <C, S> C.publishWithEffect(aggregate: StateStoredAggregate<C, S, *>): Effect<Error, S> =
    aggregate.handleWithEffect(this)

/**
 * Extension function - Publishes the command of type [C] to the state stored aggregate of type  [StateStoredAggregate]<[C], [S], *>
 * @receiver [Flow] of commands of type [C]
 * @param aggregate of type [StateStoredAggregate]<[C], [S], *>
 * @return the [Flow] of [Effect] (either [Error] or successfully  stored State of type [S])
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
@FlowPreview
fun <C, S> Flow<C>.publishWithEffect(aggregate: StateStoredAggregate<C, S, *>): Flow<Effect<Error, S>> =
    aggregate.handleWithEffect(this)
