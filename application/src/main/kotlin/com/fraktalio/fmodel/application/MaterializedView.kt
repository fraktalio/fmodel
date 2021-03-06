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
import arrow.core.computations.either
import com.fraktalio.fmodel.domain.View
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Materialized view is using/delegating a [View] to handle events of type [E] and to maintain a state of denormalized projection(s) as a result.
 * Essentially, it represents the query/view side of the CQRS pattern.
 *
 * @param S Materialized View state of type [S]
 * @param E Events of type [E] that are handled by this Materialized View
 * @property view A view component of type [View]<[S], [E]>
 * @property viewStateRepository Interface for [S]tate management/persistence - dependencies by delegation
 * @constructor Creates [MaterializedView]
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
data class MaterializedView<S, E>(
    private val view: View<S, E>,
    private val viewStateRepository: ViewStateRepository<E, S>,
) : ViewStateRepository<E, S> by viewStateRepository {

    /**
     * Handles the event of type [E]
     *
     * @param event Event of type [E] to be handled
     * @return [Either] [Error] or State of type [S]
     */
    suspend fun handleEither(event: E): Either<Error, S> =
        // Arrow provides a Monad instance for Either. Except for the types signatures, our program remains unchanged when we compute over Either. All values on the left side assume to be Right biased and, whenever a Left value is found, the computation short-circuits, producing a result that is compatible with the function type signature.
        either {
            (event.fetchStateEither().bind() ?: view.initialState)
                .calculateNewStateEither(event).bind()
                .saveEither().bind()
        }

    /**
     * Handles the flow of events of type [E]
     *
     * @param events Flow of Events of type [E] to be handled
     * @return [Flow] of [Either] [Error] or State of type [S]
     */
    fun handleEither(events: Flow<E>): Flow<Either<Error, S>> =
        events.map { handleEither(it) }

    /**
     * Handles the event of type [E]
     *
     * @param event Event of type [E] to be handled
     * @return State of type [S]
     */
    suspend fun handle(event: E): S =
        (event.fetchState() ?: view.initialState)
            .calculateNewState(event)
            .save()

    /**
     * Handles the flow of events of type [E]
     *
     * @param events Flow of Events of type [E] to be handled
     * @return [Flow] of State of type [S]
     */
    fun handle(events: Flow<E>): Flow<S> =
        events.map { handle(it) }


    private suspend fun S.calculateNewStateEither(event: E): Either<Error, S> =
        Either.catch {
            calculateNewState(event)
        }.mapLeft { throwable -> Error.CalculatingNewViewStateFailed(this, event, throwable) }


    private suspend fun S.calculateNewState(event: E): S = view.evolve(this, event)
}

/**
 * Extension function - Publishes the event of type [E] to the materialized view of type  [MaterializedView]<[S], [E]>
 * @receiver event of type [E]
 * @param materializedView of type  [MaterializedView]<[S], [E]>
 * @return the stored State of type [S]
 */
suspend fun <S, E> E.publishTo(materializedView: MaterializedView<S, E>): S = materializedView.handle(this)

/**
 * Extension function - Publishes the event of type [E] to the materialized view of type  [MaterializedView]<[S], [E]>
 * @receiver [Flow] of events of type [E]
 * @param materializedView of type  [MaterializedView]<[S], [E]>
 * @return the stored State of type [S]
 */
fun <S, E> Flow<E>.publishTo(materializedView: MaterializedView<S, E>): Flow<S> = materializedView.handle(this)

/**
 * Extension function - Publishes the event of type [E] to the materialized view of type  [MaterializedView]<[S], [E]>
 * @receiver event of type [E]
 * @param materializedView of type  [MaterializedView]<[S], [E]>
 * @return [Either] [Error] or the successfully stored State of type [S]
 */
suspend fun <S, E> E.publishEitherTo(materializedView: MaterializedView<S, E>): Either<Error, S> =
    materializedView.handleEither(this)

/**
 * Extension function - Publishes the event of type [E] to the materialized view of type  [MaterializedView]<[S], [E]>
 * @receiver [Flow] of events of type [E]
 * @param materializedView of type  [MaterializedView]<[S], [E]>
 * @return [Flow] of [Either] [Error] or the successfully stored State of type [S]
 */
fun <S, E> Flow<E>.publishEitherTo(materializedView: MaterializedView<S, E>): Flow<Either<Error, S>> =
    materializedView.handleEither(this)
