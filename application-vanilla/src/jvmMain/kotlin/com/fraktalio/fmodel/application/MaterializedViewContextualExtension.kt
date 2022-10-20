package com.fraktalio.fmodel.application

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map


/**
 * Handle event - Materialized View
 * @receiver [ViewStateComputation] - context receiver
 * @receiver [ViewStateRepository] - context receiver
 * @receiver event of type E to be handled
 */
context (ViewStateComputation<S, E>, ViewStateRepository<E, S>)
suspend fun <S, E> E.handle(): S = fetchState().computeNewState(this).save()


/**
 * Handle event - Materialized View
 * @receiver [ViewStateComputation] - context receiver
 * @receiver [ViewStateLockingRepository] - context receiver
 * @receiver event of type E to be handled
 */
context (ViewStateComputation<S, E>, ViewStateLockingRepository<E, S, V>)
suspend fun <S, E, V> E.handleOptimistically(): Pair<S, V> {
    val (state, version) = this@handleOptimistically.fetchState()
    return state
        .computeNewState(this@handleOptimistically)
        .save(version)
}

/**
 * Handle event - Materialized View
 * @receiver [ViewStateComputation] - context receiver
 * @receiver [ViewStateLockingDeduplicationRepository] - context receiver
 * @receiver event of type E to be handled
 */
context (ViewStateComputation<S, E>, ViewStateLockingDeduplicationRepository<E, S, EV, SV>)
suspend fun <S, E, EV, SV> E.handleOptimisticallyWithDeduplication(eventAndVersion: Pair<E, EV>): Pair<S, SV> {
    val (event, eventVersion) = eventAndVersion
    val (state, currentStateVersion) = event.fetchState()
    return state
        .computeNewState(event)
        .save(eventVersion, currentStateVersion)
}

/**
 * Handle event(s) - Materialized View
 * @receiver [ViewStateComputation] - context receiver
 * @receiver [ViewStateRepository] - context receiver
 * @receiver events of type Flow<E> to be handled
 */
context (ViewStateComputation<S, E>, ViewStateRepository<E, S>)
fun <S, E> Flow<E>.handle(): Flow<S> = map { it.handle() }

/**
 * Handle event(s) - Materialized View
 * @receiver [ViewStateComputation] - context receiver
 * @receiver [ViewStateLockingRepository] - context receiver
 * @receiver events of type Flow<E> to be handled
 */
context (ViewStateComputation<S, E>, ViewStateLockingRepository<E, S, V>)
fun <S, E, V> Flow<E>.handleOptimistically(): Flow<Pair<S, V>> = map { it.handleOptimistically() }

/**
 * Handle event(s) - Materialized View
 * @receiver [ViewStateComputation] - context receiver
 * @receiver [ViewStateLockingDeduplicationRepository] - context receiver
 * @receiver events of type Flow<E> to be handled
 */
context (ViewStateComputation<S, E>, ViewStateLockingDeduplicationRepository<E, S, EV, SV>)
fun <S, E, EV, SV> Flow<E>.handleOptimisticallyWithDeduplication(eventAndVersion: Pair<E, EV>): Flow<Pair<S, SV>> = map { it.handleOptimisticallyWithDeduplication(eventAndVersion) }
