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
 * State repository interface.
 *
 * @param C Command
 * @param S State
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
interface StateRepository<C, S> {
    /**
     * Fetch state
     *
     * @receiver Command of type [C]
     * @return the State of type [S] or null
     */
    suspend fun C.fetchState(): S?

    /**
     * Fetch state and metadata
     *
     * @receiver Command of type [C]
     * @return the [Pair] of current State/[S] and metadata of type [Map]<[String], [Any]>
     */
    suspend fun C.fetchStateAndMetadata(): Pair<S?, Map<String, Any>> = Pair(fetchState(), emptyMap())

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
    suspend fun S.saveWithMetadata(metaData: Map<String, Any>): Pair<S, Map<String, Any>> = Pair(save(), emptyMap())
}

/**
 * State Locking repository interface.
 *
 * Explicitly enables `optimistic locking` mechanism.
 *
 * If you fetch an item/state from a storage, the application records the `version` number of that item.
 * You can update/save the item/state, but only if the `version` number in the storage has not changed.
 * If there is a `version` mismatch, it means that someone else has modified the item/state before you did.
 *
 * @param C Command
 * @param S State
 * @param V Version
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
interface StateLockingRepository<C, S, V> {
    /**
     * Fetch state and version
     *
     * @receiver Command of type [C]
     * @return the [Pair] of current State/[S] and current Version/[V]
     */
    suspend fun C.fetchState(): Pair<S?, V?>

    /**
     * Fetch state, version and metadata
     *
     * @receiver Command of type [C]
     * @return the [Triple] of current State/[S], current Version/[V] and metadata of type [Map]<[String], [Any]>
     */
    suspend fun C.fetchStateAndMetadata(): Triple<S?, V?, Map<String, Any>> {
        val (state, version) = fetchState()
        return Triple(state, version, emptyMap())
    }

    /**
     * Save state
     *
     * You can update/save the item/state, but only if the `version` number in the storage has not changed.
     *
     * @receiver State/[S]
     * @param currentStateVersion The current version of the state
     * @return newly saved State of type [Pair]<[S], [V]>
     */
    suspend fun S.save(currentStateVersion: V?): Pair<S, V>

    /**
     * Save state with metadata
     *
     * You can update/save the item/state, but only if the `version` number in the storage has not changed.
     *
     * @receiver State/[S]
     * @param currentStateVersion The current version of the state
     * @param metaData metadata of type [Map]<[String], [Any]>
     * @return newly saved State of type [Triple]<[S], [V], [Map]<[String], [Any]>>
     */
    suspend fun S.saveWithMetadata(
        currentStateVersion: V?,
        metaData: Map<String, Any>
    ): Triple<S, V, Map<String, Any>> {
        val (state, version) = save(currentStateVersion)
        return Triple(state, version, emptyMap())
    }

}
