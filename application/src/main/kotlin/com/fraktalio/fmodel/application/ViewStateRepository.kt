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
import com.fraktalio.fmodel.domain.View
import kotlinx.coroutines.flow.Flow

/**
 * View state repository interface
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
 * Dependency injection
 * ____________________
 * Internally, the [materializedView] is used to create the object/instance of [MaterializedView]<[S], [E]>.
 * The Delegation pattern has proven to be a good alternative to implementation inheritance, and Kotlin supports it natively requiring zero boilerplate code.
 * Kotlin natively supports dependency injection with receivers.
 * -------------------
 *
 * @param R The type of the repository - [R] : [ViewStateRepository]<[E], [S]>
 * @param S State of type [S]
 * @param E Events of type [E] that are used internally by [view] to build/fold new state
 * @param event The event being handled
 * @return The persisted, new state
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
suspend fun <R, S, E> R.handle(
    event: E,
    view: View<S, E>
): S where R : ViewStateRepository<E, S> =
    event.publishTo(materializedView(view, this))

/**
 * Handle events of type [E],
 * compute the new state of type [S] based on the current/fetched state and the event being handled,
 * and save new state.
 *
 * Dependency injection
 * ____________________
 * Internally, the [materializedView] is used to create the object/instance of [MaterializedView]<[S], [E]>.
 * The Delegation pattern has proven to be a good alternative to implementation inheritance, and Kotlin supports it natively requiring zero boilerplate code.
 * Kotlin natively supports dependency injection with receivers.
 * -------------------
 *
 * @param R The type of the repository - [R] : [ViewStateRepository]<[E], [S]>
 * @param S State of type [S]
 * @param E Events of type [E] that are used internally by [view] to build/fold new state
 * @param events The events being handled
 * @return The persisted, new state
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
fun <R, S, E> R.handle(
    events: Flow<E>,
    view: View<S, E>
): Flow<S> where R : ViewStateRepository<E, S> =
    events.publishTo(materializedView(view, this))

/**
 * Handle events of type [E],
 * compute the new state of type [S] based on the current/fetched state and the event being handled,
 * and save new state / [Either.isRight], or fail transparently by returning [Error] / [Either.isLeft].
 *
 * Dependency injection
 * ____________________
 * Internally, the [materializedView] is used to create the object/instance of [MaterializedView]<[S], [E]>.
 * The Delegation pattern has proven to be a good alternative to implementation inheritance, and Kotlin supports it natively requiring zero boilerplate code.
 * Kotlin natively supports dependency injection with receivers.
 * -------------------
 *
 * @param R The type of the repository - [R] : [ViewStateRepository]<[E], [S]>
 * @param S State of type [S]
 * @param E Events of type [E] that are used internally by [view] to build/fold new state
 * @param event The event being handled
 * @return The persisted, new state
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
suspend fun <R, S, E> R.eitherHandleOrFail(
    event: E,
    view: View<S, E>
): Either<Error, S> where R : ViewStateRepository<E, S> =
    event.publishEitherTo(materializedView(view, this))

/**
 * Handle events of type [E],
 * compute the new state of type [S] based on the current/fetched state and the event being handled,
 * and save new state.
 *
 * Dependency injection
 * ____________________
 * Internally, the [materializedView] is used to create the object/instance of [MaterializedView]<[S], [E]>.
 * The Delegation pattern has proven to be a good alternative to implementation inheritance, and Kotlin supports it natively requiring zero boilerplate code.
 * Kotlin natively supports dependency injection with receivers.
 * -------------------
 *
 * @param R The type of the repository - [R] : [ViewStateRepository]<[E], [S]>
 * @param S State of type [S]
 * @param E Events of type [E] that are used internally by [view] to build/fold new state
 * @param events The events being handled
 * @return The persisted, new state
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
fun <R, S, E> R.eitherHandleOrFail(
    events: Flow<E>,
    view: View<S, E>
): Flow<S> where R : ViewStateRepository<E, S> =
    events.publishTo(materializedView(view, this))
