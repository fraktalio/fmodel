package com.fraktalio.fmodel.application

import kotlinx.coroutines.flow.fold

/**
 * Extension function - Handles the query of type [Q]
 *
 * @param query Query of type [Q] to be handled
 * @return State of type [S]
 *
 * @author Domenic Cassisi
 */
suspend fun <S, E, Q, EV> EV.handle(query: Q): S where EV : ViewStateComputation<S, E>, EV : EphemeralViewRepository<E, Q> =
    query.fetchEvents().fold(initialState) { s, e -> evolve(s, e) }
