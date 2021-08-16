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

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Event repository interface
 *
 * Used by [EventSourcingAggregate]
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
     * Save event
     *
     * @receiver Event of type [E]
     * @return newly saved Event of type [E]
     */
    suspend fun E.save(): E

    /**
     * Save events
     *
     * @receiver [Flow] of Events of type [E]
     * @return newly saved [Flow] of Events of type [E]
     */
    fun Flow<E>.save(): Flow<E> = map { it.save() }
}
