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

import arrow.core.Either
import arrow.core.raise.catch
import arrow.core.raise.either
import com.fraktalio.fmodel.application.Error.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

/**
 * Extension function - Handles the event of type [E]
 *
 * @param event Event of type [E] to be handled
 * @return [Either] (either [Error] or State of type [S])
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
suspend fun <S, E, I> I.handleWithEffect(event: E): Either<Error, S> where I : ViewStateComputation<S, E>, I : ViewStateRepository<E, S> {

    fun S?.computeNewStateWithEffect(event: E): Either<Error, S> =
        either {
            catch({
                computeNewState(event)
            }) {
                raise(CalculatingNewViewStateFailed(this@computeNewStateWithEffect, event, it))
            }
        }

    suspend fun E.fetchStateWithEffect(): Either<Error, S?> =
        either {
            catch({
                fetchState()
            }) {
                raise(FetchingViewStateFailed(this@fetchStateWithEffect, it))
            }
        }

    suspend fun S.saveWithEffect(): Either<Error, S> =
        either {
            catch({
                save()
            }) {
                raise(StoringStateFailed(this@saveWithEffect, it))
            }
        }

    return either {
        event.fetchStateWithEffect().bind()
            .computeNewStateWithEffect(event).bind()
            .saveWithEffect().bind()
    }
}

suspend fun <S, E, I> I.handleWithEffect(
    event: E,
    metadata: Map<String, Any>
): Either<Error, Pair<S, Map<String, Any>>> where I : ViewStateComputation<S, E>, I : ViewStateRepository<E, S> {

    fun S?.computeNewStateWithEffect(event: E): Either<Error, S> =
        either {
            catch({
                computeNewState(event)
            }) {
                raise(CalculatingNewViewStateFailed(this@computeNewStateWithEffect, event, it))
            }
        }

    suspend fun E.fetchStateWithEffectAndMetadata(): Either<Error, Pair<S?, Map<String, Any>>> =
        either {
            catch({
                fetchStateWithMetadata()
            }) {
                raise(FetchingViewStateFailed(this@fetchStateWithEffectAndMetadata, it))
            }
        }

    suspend fun S.saveWithEffectAndMetadata(metadata: Map<String, Any>): Either<Error, Pair<S, Map<String, Any>>> =
        either {
            catch({
                saveWithMetadata(metadata)
            }) {
                raise(StoringStateFailed(this@saveWithEffectAndMetadata, it))
            }
        }

    return either {
        event.fetchStateWithEffectAndMetadata().bind().first
            .computeNewStateWithEffect(event).bind()
            .saveWithEffectAndMetadata(metadata).bind()
    }
}

/**
 * Extension function - Handles the event of type [E]
 *
 * @param event Event of type [E] to be handled
 * @return [Either] (either [Error] or State of type [Pair]<[S], [V]>)
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
suspend fun <S, E, V, I> I.handleOptimisticallyWithEffect(event: E): Either<Error, Pair<S, V>> where I : ViewStateComputation<S, E>, I : ViewStateLockingRepository<E, S, V> {
    fun S?.computeNewStateWithEffect(event: E): Either<Error, S> =
        either {
            catch({
                computeNewState(event)
            }) {
                raise(CalculatingNewViewStateFailed(this@computeNewStateWithEffect, event, it))
            }
        }

    suspend fun E.fetchStateWithEffect(): Either<Error, Pair<S?, V?>> =
        either {
            catch({
                fetchState()
            }) {
                raise(FetchingViewStateFailed(this@fetchStateWithEffect, it))
            }
        }

    suspend fun S.saveWithEffect(currentVersion: V?): Either<Error, Pair<S, V>> =
        either {
            catch({
                save(currentVersion)
            }) {
                raise(StoringStateFailed(this@saveWithEffect, it))
            }
        }

    return either {
        val (state, version) = event.fetchStateWithEffect().bind()
        state
            .computeNewStateWithEffect(event).bind()
            .saveWithEffect(version).bind()
    }
}

suspend fun <S, E, V, I> I.handleOptimisticallyWithEffect(
    event: E,
    metadata: Map<String, Any>
): Either<Error, Triple<S, V, Map<String, Any>>> where I : ViewStateComputation<S, E>, I : ViewStateLockingRepository<E, S, V> {
    fun S?.computeNewStateWithEffect(event: E): Either<Error, S> =
        either {
            catch({
                computeNewState(event)
            }) {
                raise(CalculatingNewViewStateFailed(this@computeNewStateWithEffect, event, it))
            }
        }

    suspend fun E.fetchStateWithEffectAndMetadata(): Either<Error, Triple<S?, V?, Map<String, Any>>> =
        either {
            catch({
                fetchStateAndMetadata()
            }) {
                raise(FetchingViewStateFailed(this@fetchStateWithEffectAndMetadata, it))
            }
        }

    suspend fun S.saveWithEffectAndMetadata(
        currentVersion: V?,
        metadata: Map<String, Any>
    ): Either<Error, Triple<S, V, Map<String, Any>>> =
        either {
            catch({
                saveWithMetadata(currentVersion, metadata)
            }) {
                raise(StoringStateFailed(this@saveWithEffectAndMetadata, it))
            }
        }

    return either {
        val (state, version, _) = event.fetchStateWithEffectAndMetadata().bind()
        state
            .computeNewStateWithEffect(event).bind()
            .saveWithEffectAndMetadata(version, metadata).bind()
    }
}

/**
 * Extension function - Handles the event of type [E]
 *
 * @param EV Event Version
 * @param SV State Version
 *
 * @param eventAndVersion Event of type [Pair]<[E], [EV]> to be handled
 * @return [Either] (either [Error] or State of type [Pair]<[S], [SV]>)
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
suspend fun <S, E, EV, SV, I> I.handleOptimisticallyWithDeduplicationWithEffect(eventAndVersion: Pair<E, EV>): Either<Error, Pair<S, SV>> where I : ViewStateComputation<S, E>, I : ViewStateLockingDeduplicationRepository<E, S, EV, SV> {
    fun S?.computeNewStateWithEffect(event: E): Either<Error, S> =
        either {
            catch({
                computeNewState(event)
            }) {
                raise(CalculatingNewViewStateFailed(this@computeNewStateWithEffect, event, it))
            }
        }

    suspend fun E.fetchStateWithEffect(): Either<Error, Pair<S?, SV?>> =
        either {
            catch({
                fetchState()
            }) {
                raise(FetchingViewStateFailed(this@fetchStateWithEffect, it))
            }
        }

    suspend fun S.saveWithEffect(entityVersion: EV, currentStateVersion: SV?): Either<Error, Pair<S, SV>> =
        either {
            catch({
                save(entityVersion, currentStateVersion)
            }) {
                raise(StoringStateFailed(this@saveWithEffect, it))
            }
        }

    return either {
        val (event, eventVersion) = eventAndVersion
        val (state, currentStateVersion) = event.fetchStateWithEffect().bind()
        state
            .computeNewStateWithEffect(event).bind()
            .saveWithEffect(eventVersion, currentStateVersion).bind()
    }
}


suspend fun <S, E, EV, SV, I> I.handleOptimisticallyWithDeduplicationWithEffect(
    eventAndVersionAndMetadata: Triple<E, EV, Map<String, Any>>
): Either<Error, Triple<S, SV, Map<String, Any>>> where I : ViewStateComputation<S, E>, I : ViewStateLockingDeduplicationRepository<E, S, EV, SV> {
    fun S?.computeNewStateWithEffect(event: E): Either<Error, S> =
        either {
            catch({
                computeNewState(event)
            }) {
                raise(CalculatingNewViewStateFailed(this@computeNewStateWithEffect, event, it))
            }
        }

    suspend fun E.fetchStateWithEffectAndMetadata(): Either<Error, Triple<S?, SV?, Map<String, Any>>> =
        either {
            catch({
                fetchStateAndMetadata()
            }) {
                raise(FetchingViewStateFailed(this@fetchStateWithEffectAndMetadata, it))
            }
        }

    suspend fun S.saveWithEffectAndMetadata(
        entityVersion: EV,
        currentStateVersion: SV?,
        metadata: Map<String, Any>
    ): Either<Error, Triple<S, SV, Map<String, Any>>> =
        either {
            catch({
                saveWithMetadata(entityVersion, currentStateVersion, metadata)
            }) {
                raise(StoringStateFailed(this@saveWithEffectAndMetadata, it))
            }
        }

    return either {
        val (event, eventVersion, metadata) = eventAndVersionAndMetadata
        val (state, currentStateVersion, _) = event.fetchStateWithEffectAndMetadata().bind()
        state
            .computeNewStateWithEffect(event).bind()
            .saveWithEffectAndMetadata(eventVersion, currentStateVersion, metadata).bind()
    }
}

/**
 * Extension function - Handles the flow of events of type [E]
 *
 * @param events Flow of Events of type [E] to be handled
 * @return [Flow] of [Either] (either [Error] or State of type [S])
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
fun <S, E, I> I.handleWithEffect(events: Flow<E>): Flow<Either<Error, S>> where I : ViewStateComputation<S, E>, I : ViewStateRepository<E, S> =
    events
        .map { handleWithEffect(it) }
        .catch { emit(either { raise(EventPublishingFailed(it)) }) }

fun <S, E, I> I.handleWithEffectAndMetadata(events: Flow<Pair<E, Map<String, Any>>>): Flow<Either<Error, Pair<S, Map<String, Any>>>> where I : ViewStateComputation<S, E>, I : ViewStateRepository<E, S> =
    events
        .map { handleWithEffect(it.first, it.second) }
        .catch { emit(either { raise(EventPublishingFailed(it)) }) }

/**
 * Extension function - Handles the flow of events of type [E]
 *
 * @param events Flow of Events of type [E] to be handled
 * @return [Flow] of [Either] (either [Error] or State of type [Pair]<[S], [V]>)
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
fun <S, E, V, I> I.handleOptimisticallyWithEffect(events: Flow<E>): Flow<Either<Error, Pair<S, V>>> where I : ViewStateComputation<S, E>, I : ViewStateLockingRepository<E, S, V> =
    events
        .map { handleOptimisticallyWithEffect(it) }
        .catch { emit(either { raise(EventPublishingFailed(it)) }) }

fun <S, E, V, I> I.handleOptimisticallyWithEffectAndMetadata(events: Flow<Pair<E, Map<String, Any>>>): Flow<Either<Error, Triple<S, V, Map<String, Any>>>> where I : ViewStateComputation<S, E>, I : ViewStateLockingRepository<E, S, V> =
    events
        .map { handleOptimisticallyWithEffect(it.first, it.second) }
        .catch { emit(either { raise(EventPublishingFailed(it)) }) }

/**
 * Extension function - Handles the flow of events of type [Pair]<[E], [EV]>
 *
 * @param eventsAndVersions Flow of Events of type [E] to be handled
 * @return [Flow] of [Either] (either [Error] or State of type [Pair]<[S], [SV]>)
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
fun <S, E, EV, SV, I> I.handleOptimisticallyWithDeduplicationWithEffect(eventsAndVersions: Flow<Pair<E, EV>>): Flow<Either<Error, Pair<S, SV>>> where I : ViewStateComputation<S, E>, I : ViewStateLockingDeduplicationRepository<E, S, EV, SV> =
    eventsAndVersions
        .map { handleOptimisticallyWithDeduplicationWithEffect(it) }
        .catch { emit(either { raise(EventPublishingFailed(it)) }) }

fun <S, E, EV, SV, I> I.handleOptimisticallyWithDeduplicationWithEffectAndMetadata(eventsAndVersionsAndMetadata: Flow<Triple<E, EV, Map<String, Any>>>): Flow<Either<Error, Triple<S, SV, Map<String, Any>>>> where I : ViewStateComputation<S, E>, I : ViewStateLockingDeduplicationRepository<E, S, EV, SV> =
    eventsAndVersionsAndMetadata
        .map { handleOptimisticallyWithDeduplicationWithEffect(Triple(it.first, it.second, it.third)) }
        .catch { emit(either { raise(EventPublishingFailed(it)) }) }

/**
 * Extension function - Publishes the event of type [E] to the materialized view
 * @receiver event of type [E]
 * @param materializedView of type [ViewStateComputation]<[S], [E]>, [ViewStateRepository]<[E], [S]>
 * @return [Either] (either [Error] or the successfully stored State of type [S])
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
suspend fun <S, E, M> E.publishWithEffect(materializedView: M): Either<Error, S> where M : ViewStateComputation<S, E>, M : ViewStateRepository<E, S> =
    materializedView.handleWithEffect(this)

suspend fun <S, E, M> E.publishWithEffect(
    materializedView: M,
    withMetadata: Map<String, Any>
): Either<Error, Pair<S, Map<String, Any>>> where M : ViewStateComputation<S, E>, M : ViewStateRepository<E, S> =
    materializedView.handleWithEffect(this, withMetadata)

/**
 * Extension function - Publishes the event of type [E] to the materialized view
 * @receiver event of type [E]
 * @param materializedView of type [ViewStateComputation]<[S], [E]>, [ViewStateLockingRepository]<[E], [S], [V]>
 * @return [Either] (either [Error] or the successfully stored State of type [Pair]<[S], [V]>)
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
suspend fun <S, E, V, M> E.publishOptimisticallyWithEffect(materializedView: M): Either<Error, Pair<S, V>> where M : ViewStateComputation<S, E>, M : ViewStateLockingRepository<E, S, V> =
    materializedView.handleOptimisticallyWithEffect(this)

suspend fun <S, E, V, M> E.publishOptimisticallyWithEffect(
    materializedView: M,
    withMetadata: Map<String, Any>
): Either<Error, Triple<S, V, Map<String, Any>>> where M : ViewStateComputation<S, E>, M : ViewStateLockingRepository<E, S, V> =
    materializedView.handleOptimisticallyWithEffect(this, withMetadata)

/**
 * Extension function - Publishes the event of type [E] to the materialized view
 * @receiver event of type [E]
 * @param materializedView of type [ViewStateComputation]<[S], [E]>, [ViewStateLockingDeduplicationRepository]<[E], [S], [EV], [SV]>
 * @return [Either] (either [Error] or the successfully stored State of type [Pair]<[S], [SV]>)
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
suspend fun <S, E, EV, SV, M> Pair<E, EV>.publishOptimisticallyWithDeduplicationWithEffect(materializedView: M): Either<Error, Pair<S, SV>> where M : ViewStateComputation<S, E>, M : ViewStateLockingDeduplicationRepository<E, S, EV, SV> =
    materializedView.handleOptimisticallyWithDeduplicationWithEffect(this)

suspend fun <S, E, EV, SV, M> Triple<E, EV, Map<String, Any>>.publishOptimisticallyWithDeduplicationWithEffect(
    materializedView: M
): Either<Error, Triple<S, SV, Map<String, Any>>> where M : ViewStateComputation<S, E>, M : ViewStateLockingDeduplicationRepository<E, S, EV, SV> =
    materializedView.handleOptimisticallyWithDeduplicationWithEffect(this)

/**
 * Extension function - Publishes the event of type [E] to the materialized view
 * @receiver [Flow] of events of type [E]
 * @param materializedView of type [ViewStateComputation]<[S], [E]>, [ViewStateRepository]<[E], [S]>
 * @return [Flow] of [Either] (either [Error] or the successfully stored State of type [S])
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
fun <S, E, M> Flow<E>.publishWithEffect(materializedView: M): Flow<Either<Error, S>> where M : ViewStateComputation<S, E>, M : ViewStateRepository<E, S> =
    materializedView.handleWithEffect(this)

fun <S, E, M> Flow<Pair<E, Map<String, Any>>>.publishWithEffectAndMetadata(materializedView: M): Flow<Either<Error, Pair<S, Map<String, Any>>>> where M : ViewStateComputation<S, E>, M : ViewStateRepository<E, S> =
    materializedView.handleWithEffectAndMetadata(this)

/**
 * Extension function - Publishes the event of type [E] to the materialized view
 * @receiver [Flow] of events of type [E]
 * @param materializedView of type [ViewStateComputation]<[S], [E]>, [ViewStateLockingRepository]<[E], [S], [V]>
 * @return [Flow] of [Either] (either [Error] or the successfully stored State of type [Pair]<[S], [V]>)
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
fun <S, E, V, M> Flow<E>.publishOptimisticallyWithEffect(materializedView: M): Flow<Either<Error, Pair<S, V>>> where M : ViewStateComputation<S, E>, M : ViewStateLockingRepository<E, S, V> =
    materializedView.handleOptimisticallyWithEffect(this)

fun <S, E, V, M> Flow<Pair<E, Map<String, Any>>>.publishOptimisticallyWithEffectAndMetadata(materializedView: M): Flow<Either<Error, Triple<S, V, Map<String, Any>>>> where M : ViewStateComputation<S, E>, M : ViewStateLockingRepository<E, S, V> =
    materializedView.handleOptimisticallyWithEffectAndMetadata(this)

/**
 * Extension function - Publishes the event of type [E] to the materialized view
 * @receiver [Flow] of events of type [E]
 * @param materializedView of type [ViewStateComputation]<[S], [E]>, [ViewStateLockingDeduplicationRepository]<[E], [S], [EV], [SV]>
 * @return [Flow] of [Either] (either [Error] or the successfully stored State of type [Pair]<[S], [SV]>)
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
fun <S, E, EV, SV, M> Flow<Pair<E, EV>>.publishOptimisticallyWithDeduplicationWithEffect(materializedView: M): Flow<Either<Error, Pair<S, SV>>> where M : ViewStateComputation<S, E>, M : ViewStateLockingDeduplicationRepository<E, S, EV, SV> =
    materializedView.handleOptimisticallyWithDeduplicationWithEffect(this)

fun <S, E, EV, SV, M> Flow<Triple<E, EV, Map<String, Any>>>.publishOptimisticallyWithDeduplicationWithEffectAndMetadata(
    materializedView: M
): Flow<Either<Error, Triple<S, SV, Map<String, Any>>>> where M : ViewStateComputation<S, E>, M : ViewStateLockingDeduplicationRepository<E, S, EV, SV> =
    materializedView.handleOptimisticallyWithDeduplicationWithEffectAndMetadata(this)