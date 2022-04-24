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

import arrow.core.Either
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
 * @return [Either] [Error] or State of type [S]
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
suspend fun <S, E> MaterializedView<S, E>.handleEither(event: E): Effect<Error, S> {
    /**
     * Inner function - Computes new State based on the Event or fails.
     *
     * @param event of type [E]
     * @return The newly computed state of type [S] or [Error]
     */
    fun S?.eitherComputeNewStateOrFail(event: E): Effect<Error, S> =
        effect {
            try {
                computeNewState(event)
            } catch (t: Throwable) {
                shift(CalculatingNewViewStateFailed(this@eitherComputeNewStateOrFail, event, t.nonFatalOrThrow()))
            }
        }

    /**
     * Inner function - Fetch state - either version
     *
     * @receiver Event of type [E]
     * @return [Either] [Error] or the State of type [S]?
     */
    suspend fun E.eitherFetchStateOrFail(): Effect<Error, S?> =
        effect {
            try {
                fetchState()
            } catch (t: Throwable) {
                shift(FetchingViewStateFailed(this@eitherFetchStateOrFail, t.nonFatalOrThrow()))
            }
        }

    /**
     * Inner function - Save state - either version
     *
     * @receiver State of type [S]
     * @return [Either] [Error] or the newly saved State of type [S]
     */
    suspend fun S.eitherSaveOrFail(): Effect<Error, S> =
        effect {
            try {
                this@eitherSaveOrFail.save()
            } catch (t: Throwable) {
                shift(StoringStateFailed(this@eitherSaveOrFail, t.nonFatalOrThrow()))
            }
        }

    return effect {
        event.eitherFetchStateOrFail().bind()
            .eitherComputeNewStateOrFail(event).bind()
            .eitherSaveOrFail().bind()
    }
}

/**
 * Extension function - Handles the flow of events of type [E]
 *
 * @param events Flow of Events of type [E] to be handled
 * @return [Flow] of [Either] [Error] or State of type [S]
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
fun <S, E> MaterializedView<S, E>.handleEither(events: Flow<E>): Flow<Effect<Error, S>> =
    events
        .map { handleEither(it) }
        .catch { emit(effect { shift(EventPublishingFailed(it)) }) }


/**
 * Extension function - Publishes the event of type [E] to the materialized view of type  [MaterializedView]<[S], [E]>
 * @receiver event of type [E]
 * @param materializedView of type  [MaterializedView]<[S], [E]>
 * @return [Either] [Error] or the successfully stored State of type [S]
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
suspend fun <S, E> E.publishEitherTo(materializedView: MaterializedView<S, E>): Effect<Error, S> =
    materializedView.handleEither(this)

/**
 * Extension function - Publishes the event of type [E] to the materialized view of type  [MaterializedView]<[S], [E]>
 * @receiver [Flow] of events of type [E]
 * @param materializedView of type  [MaterializedView]<[S], [E]>
 * @return [Flow] of [Either] [Error] or the successfully stored State of type [S]
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
fun <S, E> Flow<E>.publishEitherTo(materializedView: MaterializedView<S, E>): Flow<Effect<Error, S>> =
    materializedView.handleEither(this)
