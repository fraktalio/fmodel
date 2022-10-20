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
suspend fun <S, E, I> I.handleWithEffect(event: E): Effect<Error, S> where I : ViewStateComputation<S, E>, I : ViewStateRepository<E, S> =
    effect {
        try {
            event.fetchState().computeNewState(event).save()
        } catch (t: Throwable) {
            shift(EventHandlingFailed(event, t.nonFatalOrThrow()))
        }
    }

/**
 * Extension function - Handles the event of type [E]
 *
 * @param event Event of type [E] to be handled
 * @return [Effect] (either [Error] or State of type [Pair]<[S], [V]>)
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
suspend fun <S, E, V, I> I.handleOptimisticallyWithEffect(event: E): Effect<Error, Pair<S, V>> where I : ViewStateComputation<S, E>, I : ViewStateLockingRepository<E, S, V> =
    effect {
        try {
            val (state, version) = event.fetchState()
            state.computeNewState(event).save(version)
        } catch (t: Throwable) {
            shift(EventHandlingFailed(event, t.nonFatalOrThrow()))
        }
    }

/**
 * Extension function - Handles the event of type [E]
 *
 * @param EV Event Version
 * @param SV State Version
 *
 * @param eventAndVersion Event of type [Pair]<[E], [EV]> to be handled
 * @return [Effect] (either [Error] or State of type [Pair]<[S], [SV]>)
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
suspend fun <S, E, EV, SV, I> I.handleOptimisticallyWithDeduplicationWithEffect(eventAndVersion: Pair<E, EV>): Effect<Error, Pair<S, SV>> where I : ViewStateComputation<S, E>, I : ViewStateLockingDeduplicationRepository<E, S, EV, SV> {
    return effect {
        val (event, eventVersion) = eventAndVersion
        try {
            val (state, currentStateVersion) = event.fetchState()
            state.computeNewState(event).save(eventVersion, currentStateVersion)
        } catch (t: Throwable) {
            shift(EventHandlingFailed(event, t.nonFatalOrThrow()))
        }
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
fun <S, E, I> I.handleWithEffect(events: Flow<E>): Flow<Effect<Error, S>> where I : ViewStateComputation<S, E>, I : ViewStateRepository<E, S> =
    events
        .map { handleWithEffect(it) }
        .catch { emit(effect { shift(EventPublishingFailed(it)) }) }

/**
 * Extension function - Handles the flow of events of type [E]
 *
 * @param events Flow of Events of type [E] to be handled
 * @return [Flow] of [Effect] (either [Error] or State of type [Pair]<[S], [V]>)
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
fun <S, E, V, I> I.handleOptimisticallyWithEffect(events: Flow<E>): Flow<Effect<Error, Pair<S, V>>> where I : ViewStateComputation<S, E>, I : ViewStateLockingRepository<E, S, V> =
    events
        .map { handleOptimisticallyWithEffect(it) }
        .catch { emit(effect { shift(EventPublishingFailed(it)) }) }

/**
 * Extension function - Handles the flow of events of type [Pair]<[E], [EV]>
 *
 * @param eventsAndVersions Flow of Events of type [E] to be handled
 * @return [Flow] of [Effect] (either [Error] or State of type [Pair]<[S], [SV]>)
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
fun <S, E, EV, SV, I> I.handleOptimisticallyWithDeduplicationWithEffect(eventsAndVersions: Flow<Pair<E, EV>>): Flow<Effect<Error, Pair<S, SV>>> where I : ViewStateComputation<S, E>, I : ViewStateLockingDeduplicationRepository<E, S, EV, SV> =
    eventsAndVersions
        .map { handleOptimisticallyWithDeduplicationWithEffect(it) }
        .catch { emit(effect { shift(EventPublishingFailed(it)) }) }


/**
 * Extension function - Publishes the event of type [E] to the materialized view
 * @receiver event of type [E]
 * @param materializedView of type [ViewStateComputation]<[S], [E]>, [ViewStateRepository]<[E], [S]>
 * @return [Effect] (either [Error] or the successfully stored State of type [S])
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
suspend fun <S, E, M> E.publishWithEffect(materializedView: M): Effect<Error, S> where M : ViewStateComputation<S, E>, M : ViewStateRepository<E, S> =
    materializedView.handleWithEffect(this)

/**
 * Extension function - Publishes the event of type [E] to the materialized view
 * @receiver event of type [E]
 * @param materializedView of type [ViewStateComputation]<[S], [E]>, [ViewStateLockingRepository]<[E], [S], [V]>
 * @return [Effect] (either [Error] or the successfully stored State of type [Pair]<[S], [V]>)
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
suspend fun <S, E, V, M> E.publishOptimisticallyWithEffect(materializedView: M): Effect<Error, Pair<S, V>> where M : ViewStateComputation<S, E>, M : ViewStateLockingRepository<E, S, V> =
    materializedView.handleOptimisticallyWithEffect(this)

/**
 * Extension function - Publishes the event of type [E] to the materialized view
 * @receiver event of type [E]
 * @param materializedView of type [ViewStateComputation]<[S], [E]>, [ViewStateLockingDeduplicationRepository]<[E], [S], [EV], [SV]>
 * @return [Effect] (either [Error] or the successfully stored State of type [Pair]<[S], [SV]>)
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
suspend fun <S, E, EV, SV, M> Pair<E, EV>.publishOptimisticallyWithDeduplicationWithEffect(materializedView: M): Effect<Error, Pair<S, SV>> where M : ViewStateComputation<S, E>, M : ViewStateLockingDeduplicationRepository<E, S, EV, SV> =
    materializedView.handleOptimisticallyWithDeduplicationWithEffect(this)

/**
 * Extension function - Publishes the event of type [E] to the materialized view
 * @receiver [Flow] of events of type [E]
 * @param materializedView of type [ViewStateComputation]<[S], [E]>, [ViewStateRepository]<[E], [S]>
 * @return [Flow] of [Effect] (either [Error] or the successfully stored State of type [S])
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
fun <S, E, M> Flow<E>.publishWithEffect(materializedView: M): Flow<Effect<Error, S>> where M : ViewStateComputation<S, E>, M : ViewStateRepository<E, S> =
    materializedView.handleWithEffect(this)

/**
 * Extension function - Publishes the event of type [E] to the materialized view
 * @receiver [Flow] of events of type [E]
 * @param materializedView of type [ViewStateComputation]<[S], [E]>, [ViewStateLockingRepository]<[E], [S], [V]>
 * @return [Flow] of [Effect] (either [Error] or the successfully stored State of type [Pair]<[S], [V]>)
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
fun <S, E, V, M> Flow<E>.publishOptimisticallyWithEffect(materializedView: M): Flow<Effect<Error, Pair<S, V>>> where M : ViewStateComputation<S, E>, M : ViewStateLockingRepository<E, S, V> =
    materializedView.handleOptimisticallyWithEffect(this)

/**
 * Extension function - Publishes the event of type [E] to the materialized view
 * @receiver [Flow] of events of type [E]
 * @param materializedView of type [ViewStateComputation]<[S], [E]>, [ViewStateLockingDeduplicationRepository]<[E], [S], [EV], [SV]>
 * @return [Flow] of [Effect] (either [Error] or the successfully stored State of type [Pair]<[S], [SV]>)
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
fun <S, E, EV, SV, M> Flow<Pair<E, EV>>.publishOptimisticallyWithDeduplicationWithEffect(materializedView: M): Flow<Effect<Error, Pair<S, SV>>> where M : ViewStateComputation<S, E>, M : ViewStateLockingDeduplicationRepository<E, S, EV, SV> =
    materializedView.handleOptimisticallyWithDeduplicationWithEffect(this)
