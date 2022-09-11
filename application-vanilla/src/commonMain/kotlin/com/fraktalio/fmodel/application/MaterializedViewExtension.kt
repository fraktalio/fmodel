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
        .pairWith(version)
        .save()
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

/**
 * Extension function - Publishes the event of type [E] to the materialized view
 * @receiver event of type [E]
 * @param materializedView of type [ViewStateComputation]<[S], [E]>, [ViewStateLockingRepository]<[E], [S], [V]>
 * @return the stored State of type [S]
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
suspend fun <S, E, V, M> E.publishOptimisticallyTo(materializedView: M): Pair<S, V> where M : ViewStateComputation<S, E>, M : ViewStateLockingRepository<E, S, V> =
    materializedView.handleOptimistically(this)

/**
 * Extension function - Publishes the event of type [E] to the materialized view of type
 * @receiver [Flow] of events of type [E]
 * @param materializedView of type  [ViewStateComputation]<[S], [E]>, [ViewStateRepository]<[E], [S]>
 * @return the stored State of type [S]
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
fun <S, E, M> Flow<E>.publishTo(materializedView: M): Flow<S> where M : ViewStateComputation<S, E>, M : ViewStateRepository<E, S> =
    materializedView.handle(this)

/**
 * Extension function - Publishes the event of type [E] to the materialized view
 * @receiver [Flow] of events of type [E]
 * @param materializedView of type [ViewStateComputation]<[S], [E]>, [ViewStateLockingRepository]<[E], [S], [V]>
 * @return the stored State of type [S]
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
fun <S, E, V, M> Flow<E>.publishOptimisticallyTo(materializedView: M): Flow<Pair<S, V>> where M : ViewStateComputation<S, E>, M : ViewStateLockingRepository<E, S, V> =
    materializedView.handleOptimistically(this)

private fun <S, V> S.pairWith(version: V): Pair<S, V> = Pair(this, version)