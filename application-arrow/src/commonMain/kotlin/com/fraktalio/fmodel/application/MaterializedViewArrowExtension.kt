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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

/**
 * Extension function - Handles the event of type [E]
 *
 * @param event Event of type [E] to be handled
 * @return [Effect] (either [Error] or State of type [S])
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
suspend fun <S, E> MaterializedView<S, E>.handleWithEffect(event: E): Effect<Error, S> {
    /**
     * Inner function - Computes new State based on the Event or fails.
     *
     * @param event of type [E]
     * @return The newly computed state of type [S] or [Error]
     */
    fun S?.computeNewStateWithEffect(event: E): Effect<Error, S> =
        effect {
            try {
                computeNewState(event)
            } catch (t: Throwable) {
                shift(CalculatingNewViewStateFailed(this@computeNewStateWithEffect, event, t.nonFatalOrThrow()))
            }
        }

    /**
     * Inner function - Fetch state - either version
     *
     * @receiver Event of type [E]
     * @return [Effect] (either [Error] or the State of type [S]?)
     */
    suspend fun E.fetchStateWithEffect(): Effect<Error, S?> =
        effect {
            try {
                fetchState()
            } catch (t: Throwable) {
                shift(FetchingViewStateFailed(this@fetchStateWithEffect, t.nonFatalOrThrow()))
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
        event.fetchStateWithEffect().bind()
            .computeNewStateWithEffect(event).bind()
            .saveWithEffect().bind()
    }
}

/**
 * Extension function - Handles the flow of events of type [E]
 *
 * @param events Flow of Events of type [E] to be handled
 * @return [Flow] of [Effect] (either [Error] or State of type [S])
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
fun <S, E> MaterializedView<S, E>.handleWithEffect(events: Flow<E>): Flow<Effect<Error, S>> =
    events
        .map { handleWithEffect(it) }
        .catch { emit(effect { shift(EventPublishingFailed(it)) }) }


/**
 * Extension function - Publishes the event of type [E] to the materialized view of type  [MaterializedView]<[S], [E]>
 * @receiver event of type [E]
 * @param materializedView of type  [MaterializedView]<[S], [E]>
 * @return [Effect] (either [Error] or the successfully stored State of type [S])
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
suspend fun <S, E> E.publishWithEffect(materializedView: MaterializedView<S, E>): Effect<Error, S> =
    materializedView.handleWithEffect(this)

/**
 * Extension function - Publishes the event of type [E] to the materialized view of type  [MaterializedView]<[S], [E]>
 * @receiver [Flow] of events of type [E]
 * @param materializedView of type  [MaterializedView]<[S], [E]>
 * @return [Flow] of [Effect] (either [Error] or the successfully stored State of type [S])
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
fun <S, E> Flow<E>.publishWithEffect(materializedView: MaterializedView<S, E>): Flow<Effect<Error, S>> =
    materializedView.handleWithEffect(this)
