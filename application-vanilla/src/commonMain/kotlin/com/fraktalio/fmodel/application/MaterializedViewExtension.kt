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
 * Extension function - Handles the event of type [E]
 *
 * @param event Event of type [E] to be handled
 * @return State of type [S]
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
suspend fun <S, E, I> I.handle(event: E): S where I : ViewStateComputation<S, E>, I : ViewStateRepository<E, S> =
    event.fetchState().computeNewState(event).save()

suspend fun <S, E, I> I.handle(
    event: E,
    metadata: Map<String, Any>
): Pair<S, Map<String, Any>> where I : ViewStateComputation<S, E>, I : ViewStateRepository<E, S> =
    event.fetchStateWithMetadata().first.computeNewState(event).saveWithMetadata(metadata)

/**
 * Extension function - Handles the event of type [E]
 *
 * @param event Event of type [E] to be handled
 * @return State of type [Pair]<[S], [V]>
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
suspend fun <S, E, V, I> I.handleOptimistically(event: E): Pair<S, V> where I : ViewStateComputation<S, E>, I : ViewStateLockingRepository<E, S, V> {
    val (state, version) = event.fetchState()
    return state
        .computeNewState(event)
        .save(version)
}

suspend fun <S, E, V, I> I.handleOptimistically(
    event: E,
    metadata: Map<String, Any>
): Triple<S, V, Map<String, Any>> where I : ViewStateComputation<S, E>, I : ViewStateLockingRepository<E, S, V> {
    val (state, version, _) = event.fetchStateAndMetadata()
    return state
        .computeNewState(event)
        .saveWithMetadata(version, metadata)
}

/**
 * Extension function - Handles the event of type [E]
 *
 * @param eventAndVersion Event of type [Pair]<[E], [EV]> to be handled
 * @return State of type [Pair]<[S], [SV]>
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
suspend fun <S, E, EV, SV, I> I.handleOptimisticallyWithDeduplication(eventAndVersion: Pair<E, EV>): Pair<S, SV> where I : ViewStateComputation<S, E>, I : ViewStateLockingDeduplicationRepository<E, S, EV, SV> {
    val (event, eventVersion) = eventAndVersion
    val (state, currentStateVersion) = event.fetchState()
    return state
        .computeNewState(event)
        .save(eventVersion, currentStateVersion)
}

suspend fun <S, E, EV, SV, I> I.handleOptimisticallyWithDeduplication(
    eventAndVersion: Pair<E, EV>,
    metadata: Map<String, Any>
): Triple<S, SV, Map<String, Any>> where I : ViewStateComputation<S, E>, I : ViewStateLockingDeduplicationRepository<E, S, EV, SV> {
    val (event, eventVersion) = eventAndVersion
    val (state, currentStateVersion, _) = event.fetchStateAndMetadata()
    return state
        .computeNewState(event)
        .saveWithMetadata(eventVersion, currentStateVersion, metadata)
}

/**
 * Extension function - Handles the flow of events of type [E]
 *
 * @param events Flow of Events of type [E] to be handled
 * @return [Flow] of State of type [S]
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
fun <S, E, I> I.handle(events: Flow<E>): Flow<S> where I : ViewStateComputation<S, E>, I : ViewStateRepository<E, S> =
    events.map { handle(it) }

fun <S, E, I> I.handleWithMetadata(events: Flow<Pair<E, Map<String, Any>>>): Flow<Pair<S, Map<String, Any>>> where I : ViewStateComputation<S, E>, I : ViewStateRepository<E, S> =
    events.map { handle(it.first, it.second) }

/**
 * Extension function - Handles the flow of events of type [E]
 *
 * @param events Flow of Events of type [E] to be handled
 * @return [Flow] of State of type [Pair]<[S], [V]>
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
fun <S, E, V, I> I.handleOptimistically(events: Flow<E>): Flow<Pair<S, V>> where I : ViewStateComputation<S, E>, I : ViewStateLockingRepository<E, S, V> =
    events.map { handleOptimistically(it) }

fun <S, E, V, I> I.handleOptimisticallyWithMetadata(events: Flow<Pair<E, Map<String, Any>>>): Flow<Triple<S, V, Map<String, Any>>> where I : ViewStateComputation<S, E>, I : ViewStateLockingRepository<E, S, V> =
    events.map { handleOptimistically(it.first, it.second) }

/**
 * Extension function - Handles the flow of events of type [E]
 *
 * @param eventsAndVersions Flow of Events of type [Pair]<[E], [EV]> to be handled
 * @return [Flow] of State of type [Pair]<[S], [SV]>
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
fun <S, E, EV, SV, I> I.handleOptimisticallyWithDeduplication(eventsAndVersions: Flow<Pair<E, EV>>): Flow<Pair<S, SV>> where I : ViewStateComputation<S, E>, I : ViewStateLockingDeduplicationRepository<E, S, EV, SV> =
    eventsAndVersions.map { handleOptimisticallyWithDeduplication(it) }

fun <S, E, EV, SV, I> I.handleOptimisticallyWithDeduplicationAndMetadata(eventsAndVersions: Flow<Triple<E, EV, Map<String, Any>>>): Flow<Triple<S, SV, Map<String, Any>>> where I : ViewStateComputation<S, E>, I : ViewStateLockingDeduplicationRepository<E, S, EV, SV> =
    eventsAndVersions.map { handleOptimisticallyWithDeduplication(Pair(it.first, it.second), it.third) }

/**
 * Extension function - Publishes the event of type [E] to the materialized view
 * @receiver event of type [E]
 * @param materializedView of type [ViewStateComputation]<[S], [E]>, [ViewStateRepository]<[E], [S]>
 * @return the stored State of type [S]
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
suspend fun <S, E, M> E.publishTo(materializedView: M): S where M : ViewStateComputation<S, E>, M : ViewStateRepository<E, S> =
    materializedView.handle(this)

suspend fun <S, E, M> E.publishTo(
    materializedView: M,
    withMetadata: Map<String, Any>
): Pair<S, Map<String, Any>> where M : ViewStateComputation<S, E>, M : ViewStateRepository<E, S> =
    materializedView.handle(this, withMetadata)

/**
 * Extension function - Publishes the event of type [E] to the materialized view
 * @receiver event of type [E]
 * @param materializedView of type [ViewStateComputation]<[S], [E]>, [ViewStateLockingRepository]<[E], [S], [V]>
 * @return the stored State of type [Pair]<[S], [V]>
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
suspend fun <S, E, V, M> E.publishOptimisticallyTo(materializedView: M): Pair<S, V> where M : ViewStateComputation<S, E>, M : ViewStateLockingRepository<E, S, V> =
    materializedView.handleOptimistically(this)

suspend fun <S, E, V, M> E.publishOptimisticallyTo(
    materializedView: M,
    withMetadata: Map<String, Any>
): Triple<S, V, Map<String, Any>> where M : ViewStateComputation<S, E>, M : ViewStateLockingRepository<E, S, V> =
    materializedView.handleOptimistically(this, withMetadata)

/**
 * Extension function - Publishes the event of type [Pair]<[E], [EV]> to the materialized view
 * @receiver event of type [Pair]<[E], [EV]>
 * @param materializedView of type [ViewStateComputation]<[S], [E]>, [ViewStateLockingDeduplicationRepository]<[E], [S], [EV], [SV]>
 * @return the stored State of type [Pair]<[S], [SV]>
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
suspend fun <S, E, EV, SV, M> Pair<E, EV>.publishOptimisticallyWithDeduplicationTo(materializedView: M): Pair<S, SV> where M : ViewStateComputation<S, E>, M : ViewStateLockingDeduplicationRepository<E, S, EV, SV> =
    materializedView.handleOptimisticallyWithDeduplication(this)

suspend fun <S, E, EV, SV, M> Pair<E, EV>.publishOptimisticallyWithDeduplicationTo(
    materializedView: M,
    withMetadata: Map<String, Any>
): Triple<S, SV, Map<String, Any>> where M : ViewStateComputation<S, E>, M : ViewStateLockingDeduplicationRepository<E, S, EV, SV> =
    materializedView.handleOptimisticallyWithDeduplication(this, withMetadata)

/**
 * Extension function - Publishes the event of type [E] to the materialized view of type
 * @receiver [Flow] of events of type [E]
 * @param materializedView of type  [ViewStateComputation]<[S], [E]>, [ViewStateRepository]<[E], [S]>
 * @return the [Flow] of stored State of type [S]
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
fun <S, E, M> Flow<E>.publishTo(materializedView: M): Flow<S> where M : ViewStateComputation<S, E>, M : ViewStateRepository<E, S> =
    materializedView.handle(this)

fun <S, E, M> Flow<Pair<E, Map<String, Any>>>.publishWithMetadataTo(materializedView: M): Flow<Pair<S, Map<String, Any>>> where M : ViewStateComputation<S, E>, M : ViewStateRepository<E, S> =
    materializedView.handleWithMetadata(this)

/**
 * Extension function - Publishes the event of type [E] to the materialized view
 * @receiver [Flow] of events of type [E]
 * @param materializedView of type [ViewStateComputation]<[S], [E]>, [ViewStateLockingRepository]<[E], [S], [V]>
 * @return the [Flow] of stored State of type [Pair]<[S], [V]>
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
fun <S, E, V, M> Flow<E>.publishOptimisticallyTo(materializedView: M): Flow<Pair<S, V>> where M : ViewStateComputation<S, E>, M : ViewStateLockingRepository<E, S, V> =
    materializedView.handleOptimistically(this)

fun <S, E, V, M> Flow<Pair<E, Map<String, Any>>>.publishOptimisticallyWithMetadataTo(materializedView: M): Flow<Triple<S, V, Map<String, Any>>> where M : ViewStateComputation<S, E>, M : ViewStateLockingRepository<E, S, V> =
    materializedView.handleOptimisticallyWithMetadata(this)

/**
 * Extension function - Publishes the event of type [Pair]<[E], [EV]> to the materialized view
 * @receiver [Flow] of events of type [Pair]<[E], [EV]>
 * @param materializedView of type [ViewStateComputation]<[S], [E]>, [ViewStateLockingDeduplicationRepository]<[E], [S], [EV], [SV]>
 * @return the [Flow] of stored State of type [Pair]<[S], [SV]>
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
fun <S, E, EV, SV, M> Flow<Pair<E, EV>>.publishOptimisticallyWithDeduplicationTo(materializedView: M): Flow<Pair<S, SV>> where M : ViewStateComputation<S, E>, M : ViewStateLockingDeduplicationRepository<E, S, EV, SV> =
    materializedView.handleOptimisticallyWithDeduplication(this)

fun <S, E, EV, SV, M> Flow<Triple<E, EV, Map<String, Any>>>.publishOptimisticallyWithDeduplicationAndMetadataTo(
    materializedView: M
): Flow<Triple<S, SV, Map<String, Any>>> where M : ViewStateComputation<S, E>, M : ViewStateLockingDeduplicationRepository<E, S, EV, SV> =
    materializedView.handleOptimisticallyWithDeduplicationAndMetadata(this)