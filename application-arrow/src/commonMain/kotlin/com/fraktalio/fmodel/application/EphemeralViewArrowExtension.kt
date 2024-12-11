package com.fraktalio.fmodel.application

import arrow.core.Either
import arrow.core.raise.catch
import arrow.core.raise.either
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.fold

/**
 * Extension function - Handles the query of type [Q]
 *
 * @param query Query of type [Q] to be handled
 * @return [Either] (either [Error] or State of type [S])
 *
 * @author Domenic Cassisi
 */
suspend fun <S, E, Q, EV> EV.handleWithEffect(query: Q): Either<Error, S>
        where EV : ViewStateComputation<S, E>, EV : EphemeralViewRepository<E, Q> {

    fun Q.fetchEventsWithEffect(): Either<Error, Flow<E>> =
        either {
            catch({
                fetchEvents()
            }) {
                raise(Error.FetchingEventsFailed(query, it))
            }
        }

    suspend fun Flow<E>.computeStateWithEffect(): Either<Error, S> =
        either {
            catch({
                fold(initialState) { s, e -> evolve(s, e) }
            }) {
                raise(Error.CalculatingNewViewStateFailed(this@computeStateWithEffect, it))
            }
        }

    return either {
        query.fetchEventsWithEffect().bind()
            .computeStateWithEffect().bind()
    }
}
