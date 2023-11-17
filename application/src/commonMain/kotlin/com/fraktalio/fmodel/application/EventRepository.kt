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
 * Event repository interface
 *
 * @param C Command
 * @param E Event
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
interface EventRepository<C, E> {
    /**
     * Fetch events
     *
     * @receiver Command of type [C]
     *
     * @return [Flow] of Events of type [E]
     */
    fun C.fetchEvents(): Flow<E>

    /**
     * Fetch events and metadata
     *
     * @receiver Command of type [C]
     *
     * @return [Flow] of Events with metadata of type [Pair]<[E], [Map]<[String], [Any]>>>
     *
     * Default implementation is to fetch events without metadata
     */
    fun C.fetchEventsAndMetaData(): Flow<Pair<E, Map<String, Any>>> = fetchEvents().map { Pair(it, emptyMap()) }

    /**
     * Save events
     *
     * @receiver [Flow] of Events of type [E]
     * @return newly saved [Flow] of Events of type [E]
     */
    fun Flow<E>.save(): Flow<E>

    /**
     * Save events
     *
     * @param metaData event metadata of type [Map]<[String], [Any]>
     * @receiver [Flow] of Events of type [E]
     * @return newly saved [Flow] of Events with metadata of type [Pair]<[E], [Map]<[String], [Any]>>>
     *
     * Default implementation is to save events without metadata
     */
    fun Flow<E>.saveWithMetaData(metaData: Map<String, Any>): Flow<Pair<E, Map<String, Any>>> =
        save().map { Pair(it, emptyMap()) }

}

/**
 * A type alias for the version provider/function.
 * It provides the Version of the last Event in the stream.
 */
typealias LatestVersionProvider <E, V> = (E) -> V?

/**
 * Event locking repository interface.
 * Explicitly enables `optimistic locking` mechanism.
 *
 * If you fetch an event from a storage, the application records the `version` number of that event.
 * You can update/save the event, but only if the `version` number in the storage has not changed.
 * If there is a `version` mismatch, it means that someone else has added another event before you did.
 *
 *
 * @param C Command
 * @param E Event
 * @param V Version
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */

interface EventLockingRepository<C, E, V> {
    /**
     * Fetch events
     *
     * @receiver Command of type [C]
     *
     * @return [Flow] of Events of type [Pair]<[E], [V]>
     */
    fun C.fetchEvents(): Flow<Pair<E, V>>

    /**
     * Fetch events and metadata
     *
     * @receiver Command of type [C]
     *
     * @return [Flow] of Events of type [Triple]<[E], [V], [Map]<[String], [Any]>>>
     *
     * Default implementation is to fetch events without metadata
     */
    fun C.fetchEventsAndMetaData(): Flow<Triple<E, V, Map<String, Any>>> =
        fetchEvents().map { Triple(it.first, it.second, emptyMap()) }


    /**
     * The latest event stream version provider
     */
    val latestVersionProvider: LatestVersionProvider<E, V>

    /**
     * Save events
     *
     * @param latestVersionProvider The latest event stream version provider / function that provides the latest known event stream version
     * @receiver [Flow] of Events of type [E]
     * @return newly saved [Flow] of Events of type [Pair]<[E], [V]>
     */
    fun Flow<E>.save(latestVersionProvider: LatestVersionProvider<E, V>): Flow<Pair<E, V>>

    /**
     * Save events
     *
     * @param latestVersionProvider The latest event stream version provider / function that provides the latest known event stream version
     * @param metaData event metadata of type [Map]<[String], [Any]>
     * @receiver [Flow] of Events of type [E]
     * @return newly saved [Flow] of Events with metadata of type [Triple]<[E], [V], [Map]<[String], [Any]>>>
     *
     * Default implementation is to save events without metadata
     */
    fun Flow<E>.saveWithMetaData(
        latestVersionProvider: LatestVersionProvider<E, V>,
        metaData: Map<String, Any>
    ): Flow<Triple<E, V, Map<String, Any>>> =
        save(latestVersionProvider).map { Triple(it.first, it.second, emptyMap()) }


    /** Save events
     *
     * @param latestVersion The latest known event stream version
     * @receiver [Flow] of Events of type [E]
     * @return newly saved [Flow] of Events of type [Pair]<[E], [V]>
     */
    fun Flow<E>.save(latestVersion: V?): Flow<Pair<E, V>>

    /**
     * Save events
     *
     * @param latestVersion The latest known event stream version
     * @param metaData event metadata of type [Map]<[String], [Any]>
     * @receiver [Flow] of Events of type [E]
     * @return newly saved [Flow] of Events with metadata of type [Triple]<[E], [V], [Map]<[String], [Any]>>>
     *
     * Default implementation is to save events without metadata
     */
    fun Flow<E>.saveWithMetaData(latestVersion: V?, metaData: Map<String, Any>): Flow<Triple<E, V, Map<String, Any>>> =
        save(latestVersion).map { Triple(it.first, it.second, emptyMap()) }

}
