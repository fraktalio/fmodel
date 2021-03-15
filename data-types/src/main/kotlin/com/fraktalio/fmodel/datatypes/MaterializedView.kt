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

package com.fraktalio.fmodel.datatypes

import arrow.core.Either
import arrow.core.computations.either
import arrow.higherkind

/**
 * Materialized view is using/delegating a [View] to handle events of type [E] and to maintain a state of denormalized projection(s) as a result.
 * Essentially, it represents the query/view side of the CQRS pattern.
 *
 * @param S Materialized View state of type [S]
 * @param E Events of type [E] that are handled by this Materialized View
 * @property view A view component of type [View]<[S], [E]>
 * @property storeState A suspending function that takes the newly produced state by [View] and stores it (produces side effect by modifying object/data outside its own scope) by resulting with [either] error [Error.StoringStateFailed] or success [Success.StateStoredSuccessfully]
 * @property fetchState A suspending function that takes the event of type [E] and results with [either] error [Error.FetchingStateFailed] or success [S]?
 * @constructor Creates [MaterializedView]
 */
@higherkind
data class MaterializedView<S, E>(
    val view: View<S, E>,
    val storeState: suspend (S) -> Either<Error.StoringStateFailed<S>, Success.StateStoredSuccessfully<S>>,
    val fetchState: suspend (E) -> Either<Error.FetchingStateFailed, S?>
) : MaterializedViewOf<S, E> {

    suspend fun handle(event: E): Either<Error, Success> =
        // Arrow provides a Monad instance for Either. Except for the types signatures, our program remains unchanged when we compute over Either. All values on the left side assume to be Right biased and, whenever a Left value is found, the computation short-circuits, producing a result that is compatible with the function type signature.
        either {
            val oldState = fetchState(event).bind() ?: view.initialState
            val newState = view.evolve(oldState, event)
            storeState(newState).bind()
        }

    companion object
}

