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
import com.fraktalio.fmodel.domain.IView
import kotlinx.coroutines.flow.Flow

/**
 * IView state repository interface
 *
 * Used by [MaterializedView]
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
     * Fetch state - either version
     *
     * @receiver Event of type [E]
     * @return [Either] [Error] or the State of type [S]?
     */
    suspend fun E.eitherFetchStateOrFail(): Either<Error.FetchingViewStateFailed<E>, S?> =
        Either.catch {
            fetchState()
        }.mapLeft { throwable -> Error.FetchingViewStateFailed(this, throwable) }

    /**
     * Save state
     *
     * @receiver State of type [S]
     * @return newly saved State of type [S]
     */
    suspend fun S.save(): S

    /**
     * Save state - either version
     *
     * @receiver State of type [S]
     * @return [Either] [Error] or the newly saved State of type [S]
     */
    suspend fun S.eitherSaveOrFail(): Either<Error.StoringStateFailed<S>, S> =
        Either.catch {
            this.save()
        }.mapLeft { throwable -> Error.StoringStateFailed(this, throwable) }
}

/**
 * Handle events of type [E],
 * compute the new state of type [S] based on the current/fetched state and the event being handled,
 * and save new state.
 *
 * @param S State of type [S]
 * @param E Events of type [E] that are used internally by [view] to build/fold new state
 * @param event The event being handled
 * @return The persisted, new state
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
suspend fun <S, E> ViewStateRepository<E, S>.handle(
    event: E,
    view: IView<S, E>
): S = event.publishTo(materializedView(view, this))

/**
 * Handle events of type [E],
 * compute the new state of type [S] based on the current/fetched state and the event being handled,
 * and save new state.
 *
 * @param S State of type [S]
 * @param E Events of type [E] that are used internally by [view] to build/fold new state
 * @param events The events being handled
 * @return The persisted, new state
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
fun <S, E> ViewStateRepository<E, S>.handle(
    events: Flow<E>,
    view: IView<S, E>
): Flow<S> = events.publishTo(materializedView(view, this))

/**
 * Handle events of type [E],
 * compute the new state of type [S] based on the current/fetched state and the event being handled,
 * and save new state / [Either.isRight], or fail transparently by returning [Error] / [Either.isLeft].
 *
 * @param S State of type [S]
 * @param E Events of type [E] that are used internally by [view] to build/fold new state
 * @param event The event being handled
 * @return The persisted, new state
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
suspend fun <S, E> ViewStateRepository<E, S>.eitherHandleOrFail(
    event: E,
    view: IView<S, E>
): Either<Error, S> = event.publishEitherTo(materializedView(view, this))

/**
 * Handle events of type [E],
 * compute the new state of type [S] based on the current/fetched state and the event being handled,
 * and save new state.
 *
 * @param S State of type [S]
 * @param E Events of type [E] that are used internally by [view] to build/fold new state
 * @param events The events being handled
 * @return The persisted, new state
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
fun <S, E> ViewStateRepository<E, S>.eitherHandleOrFail(
    events: Flow<E>,
    view: IView<S, E>
): Flow<S> = events.publishTo(materializedView(view, this))
