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
     * Save state
     *
     * @receiver State of type [S]
     * @return newly saved State of type [S]
     */
    suspend fun S.save(): S
}

/**
 * View state locking repository interface
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
     * Save state
     *
     * You can update/save the item/state, but only if the `version` number in the storage has not changed.
     *
     * @receiver State/[S] to be saved
     * @return newly saved State of type [Pair]<[S], [V]>
     */
    suspend fun S.save(currentStateVersion: V?): Pair<S, V>
}

/**
 * View state locking deduplication repository interface
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
     * Save state and version
     *
     * You can update/save the item/state, but only if the `version` number in the storage has not changed.
     *
     * @receiver State/[S] to be saved
     * @return newly saved State of type [Pair]<[S], [SV]>
     */
    suspend fun S.save(eventVersion: EV, currentStateVersion: SV?): Pair<S, SV>
}
