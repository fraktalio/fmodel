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

/**
 * View state repository interface
 *
 * @param E Event
 * @param S State
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
interface ViewStateRepository<E, S> {
    /**
     * Fetch state
     *
     * @receiver Event of type [E]
     * @return the State of type [S] or null
     */
    suspend fun E.fetchState(): S?

    /**
     * Fetch state and metadata
     *
     * @receiver Event of type [E]
     * @return the [Pair] of current State/[S] and metadata of type [Map]<[String], [Any]>
     */
    suspend fun E.fetchStateWithMetaData(): Pair<S?, Map<String, Any>> = Pair(fetchState(), emptyMap())

    /**
     * Save state
     *
     * @receiver State of type [S]
     * @return newly saved State of type [S]
     */
    suspend fun S.save(): S

    /**
     * Save state with metadata
     *
     * @receiver State of type [S]
     * @param metaData metadata of type [Map]<[String], [Any]>
     * @return newly saved State of type [Pair]<[S], [Map]<[String], [Any]>>
     */
    suspend fun S.saveWithMetaData(metaData: Map<String, Any>): Pair<S, Map<String, Any>> = Pair(save(), emptyMap())
}

/**
 * View state locking repository interface
 *
 * Explicitly enables `optimistic locking` mechanism.
 *
 * If you fetch an item/state from a storage, the application records the `version` number of that item.
 * You can update/save the item/state, but only if the `version` number in the storage has not changed.
 * If there is a `version` mismatch, it means that someone else has modified the item/state before you did.
 *
 * @param E Event
 * @param S State
 * @param V Version
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
interface ViewStateLockingRepository<E, S, V> {
    /**
     * Fetch state and version
     *
     * @receiver Event of type [E]
     * @return the [Pair] of current State/[S] and current Version/[V]
     */
    suspend fun E.fetchState(): Pair<S?, V?>

    /**
     * Fetch state, version and metadata
     *
     * @receiver Event of type [E]
     * @return the [Triple] of current State/[S], current Version/[V] and metadata of type [Map]<[String], [Any]>
     */
    suspend fun E.fetchStateAndMetaData(): Triple<S?, V?, Map<String, Any>> {
        val (state, version) = fetchState()
        return Triple(state, version, emptyMap())
    }

    /**
     * Save state
     *
     * You can update/save the item/state, but only if the `version` number in the storage has not changed.
     *
     * @receiver State/[S] to be saved
     * @param currentStateVersion Current State version of type [V]?
     * @return newly saved State of type [Pair]<[S], [V]>
     */
    suspend fun S.save(currentStateVersion: V?): Pair<S, V>

    /**
     * Save state and metadata
     *
     * You can update/save the item/state, but only if the `version` number in the storage has not changed.
     *
     * @receiver State/[S] to be saved
     * @param currentStateVersion Current State version of type [V]?
     * @param metaData metadata of type [Map]<[String], [Any]>
     * @return newly saved State of type [Triple]<[S], [V], [Map]<[String], [Any]>>
     */
    suspend fun S.saveWithMetaData(
        currentStateVersion: V?,
        metaData: Map<String, Any>
    ): Triple<S, V, Map<String, Any>> {
        val (state, version) = save(currentStateVersion)
        return Triple(state, version, emptyMap())
    }
}

/**
 * View state locking deduplication repository interface
 *
 * Explicitly enables `optimistic locking` mechanism.
 *
 * If you fetch an item/state from a storage, the application records the `version` number of that item.
 * You can update/save the item/state, but only if the `version` number in the storage has not changed.
 * If there is a `version` mismatch, it means that someone else has modified the item/state before you did.
 *
 * Use Event Version to implement `deduplication` and `optimistic locking` together.
 * 'At Least Once` delivery guaranty is a reality in distributed systems, and you can consider `deduplication` or `idempotency`
 *
 * @param E Event
 * @param S State
 * @param EV Event Version
 * @param SV State Version
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
interface ViewStateLockingDeduplicationRepository<E, S, EV, SV> {
    /**
     * Fetch state and version
     *
     * @receiver Event of type [E]
     * @return the [Pair] of current State/[S] and current state Version/[SV]
     */
    suspend fun E.fetchState(): Pair<S?, SV?>

    /**
     * Fetch state, version and metadata
     *
     * @receiver Event of type [E]
     * @return the [Triple] of current State/[S], current state Version/[SV] and metadata of type [Map]<[String], [Any]>
     */
    suspend fun E.fetchStateAndMetadata(): Triple<S?, SV?, Map<String, Any>> {
        val (state, stateVersion) = fetchState()
        return Triple(state, stateVersion, emptyMap())
    }

    /**
     * Save state and version
     *
     * You can update/save the item/state,
     * but only if the `version` number in the storage has not changed,
     * or you have not seen the `eventVersion` so far.
     *
     * @receiver State/[S] to be saved
     * @param eventVersion Event version
     * @param currentStateVersion Current State version of type [SV]?
     * @return newly saved State of type [Pair]<[S], [SV]>
     */
    suspend fun S.save(eventVersion: EV, currentStateVersion: SV?): Pair<S, SV>

    /**
     * Save state, version and metadata
     *
     * You can update/save the item/state,
     * but only if the `version` number in the storage has not changed,
     * or you have not seen the `eventVersion` so far.
     *
     * @receiver State/[S] to be saved
     * @param eventVersion Event version
     * @param currentStateVersion Current State version of type [SV]?
     * @param metaData metadata of type [Map]<[String], [Any]>
     * @return newly saved State of type [Triple]<[S], [SV], [Map]<[String], [Any]>>
     */
    suspend fun S.saveWithMetadata(
        eventVersion: EV,
        currentStateVersion: SV?,
        metaData: Map<String, Any>
    ): Triple<S, SV, Map<String, Any>> {
        val (state, stateVersion) = save(eventVersion, currentStateVersion)
        return Triple(state, stateVersion, emptyMap())
    }
}
