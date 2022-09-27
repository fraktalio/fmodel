package com.fraktalio.fmodel.application

import com.fraktalio.fmodel.domain.IView
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map


/**
 * Handle event - Materialized View
 * @receiver [IView] - context receiver
 * @receiver [ViewStateRepository] - context receiver
 * @receiver event of type E to be handled
 */
context (IView<S, E>, ViewStateRepository<E, S>)
suspend fun <S, E> E.handle(): S =
    with(object : ViewStateComputation<S, E>, IView<S, E> by this@IView {}) {
        this@handle.handleIt()
    }

/**
 * Handle event - Materialized View
 * @receiver [IView] - context receiver
 * @receiver [ViewStateLockingRepository] - context receiver
 * @receiver event of type E to be handled
 */
context (IView<S, E>, ViewStateLockingRepository<E, S, V>)
suspend fun <S, E, V> E.handleOptimistically(): Pair<S, V> =
    with(object : ViewStateComputation<S, E>, IView<S, E> by this@IView {}) {
        this@handleOptimistically.handleItOptimistically()
    }

/**
 * Handle event - Materialized View
 * @receiver [IView] - context receiver
 * @receiver [ViewStateLockingDeduplicationRepository] - context receiver
 * @receiver event of type E to be handled
 */
context (IView<S, E>, ViewStateLockingDeduplicationRepository<E, S, EV, SV>)
suspend fun <S, E, EV, SV> E.handleOptimisticallyWithDeduplication(eventAndVersion: Pair<E, EV>): Pair<S, SV> =
    with(object : ViewStateComputation<S, E>, IView<S, E> by this@IView {}) {
        this@handleOptimisticallyWithDeduplication.handleItOptimisticallyWithDeduplication(eventAndVersion)
    }
/**
 * Handle event - Materialized View
 * @receiver [ViewStateComputation] - context receiver
 * @receiver [ViewStateRepository] - context receiver
 * @receiver event of type E to be handled
 *
 * Alternative function to `context (IView<S, E>, ViewStateRepository<E, S>) E.handle()`
 */
context (ViewStateComputation<S, E>, ViewStateRepository<E, S>)
suspend fun <S, E> E.handleIt(): S = fetchState().computeNewState(this).save()


/**
 * Handle event - Materialized View
 * @receiver [ViewStateComputation] - context receiver
 * @receiver [ViewStateLockingRepository] - context receiver
 * @receiver event of type E to be handled
 *
 * Alternative function to `context (IView<S, E>, ViewStateLockingRepository<E, S, V>) E.handleOptimistically()`
 */
context (ViewStateComputation<S, E>, ViewStateLockingRepository<E, S, V>)
suspend fun <S, E, V> E.handleItOptimistically(): Pair<S, V> {
    val (state, version) = this@handleItOptimistically.fetchState()
    return state
        .computeNewState(this@handleItOptimistically)
        .save(version)
}

/**
 * Handle event - Materialized View
 * @receiver [ViewStateComputation] - context receiver
 * @receiver [ViewStateLockingDeduplicationRepository] - context receiver
 * @receiver event of type E to be handled
 *
 * Alternative function to `context (IView<S, E>, ViewStateLockingDeduplicationRepository<E, S, EV, SV>) E.handleOptimisticallyWithDeduplication()`
 */
context (ViewStateComputation<S, E>, ViewStateLockingDeduplicationRepository<E, S, EV, SV>)
suspend fun <S, E, EV, SV> E.handleItOptimisticallyWithDeduplication(eventAndVersion: Pair<E, EV>): Pair<S, SV> {
    val (event, eventVersion) = eventAndVersion
    val (state, currentStateVersion) = event.fetchState()
    return state
        .computeNewState(event)
        .save(eventVersion, currentStateVersion)
}

/**
 * Handle event(s) - Materialized View
 * @receiver [IView] - context receiver
 * @receiver [ViewStateRepository] - context receiver
 * @receiver events of type Flow<E> to be handled
 */
context (IView<S, E>, ViewStateRepository<E, S>)
fun <S, E> Flow<E>.handle(): Flow<S> = map { it.handle() }

/**
 * Handle event(s) - Materialized View
 * @receiver [IView] - context receiver
 * @receiver [ViewStateLockingRepository] - context receiver
 * @receiver events of type Flow<E> to be handled
 */
context (IView<S, E>, ViewStateLockingRepository<E, S, V>)
fun <S, E, V> Flow<E>.handleOptimistically(): Flow<Pair<S, V>> = map { it.handleOptimistically() }

/**
 * Handle event(s) - Materialized View
 * @receiver [IView] - context receiver
 * @receiver [ViewStateLockingDeduplicationRepository] - context receiver
 * @receiver events of type Flow<E> to be handled
 */
context (IView<S, E>, ViewStateLockingDeduplicationRepository<E, S, EV, SV>)
fun <S, E, EV, SV> Flow<E>.handleOptimisticallyWithDeduplication(eventAndVersion: Pair<E, EV>): Flow<Pair<S, SV>> = map { it.handleOptimisticallyWithDeduplication(eventAndVersion) }

/**
 * Handle event(s) - Materialized View
 * @receiver [ViewStateComputation] - context receiver
 * @receiver [ViewStateRepository] - context receiver
 * @receiver events of type Flow<E> to be handled
 *
 * Alternative function to `context (IView<S, E>, ViewStateRepository<E, S>) Flow<E>.handle()`
 */
context (ViewStateComputation<S, E>, ViewStateRepository<E, S>)
fun <S, E> Flow<E>.handleIt(): Flow<S> = map { it.handleIt() }

/**
 * Handle event(s) - Materialized View
 * @receiver [ViewStateComputation] - context receiver
 * @receiver [ViewStateLockingRepository] - context receiver
 * @receiver events of type Flow<E> to be handled
 *
 * Alternative function to `context (IView<S, E>, ViewStateLockingRepository<E, S, V>) Flow<E>.handleOptimistically()`
 */
context (ViewStateComputation<S, E>, ViewStateLockingRepository<E, S, V>)
fun <S, E, V> Flow<E>.handleItOptimistically(): Flow<Pair<S, V>> = map { it.handleItOptimistically() }

/**
 * Handle event(s) - Materialized View
 * @receiver [ViewStateComputation] - context receiver
 * @receiver [ViewStateLockingDeduplicationRepository] - context receiver
 * @receiver events of type Flow<E> to be handled
 *
 * Alternative function to `context (IView<S, E>, ViewStateLockingDeduplicationRepository<E, S, EV, SV>) Flow<E>.handleOptimisticallyWithDeduplication(eventAndVersion)`
 */
context (ViewStateComputation<S, E>, ViewStateLockingDeduplicationRepository<E, S, EV, SV>)
fun <S, E, EV, SV> Flow<E>.handleItOptimisticallyWithDeduplication(eventAndVersion: Pair<E, EV>): Flow<Pair<S, SV>> = map { it.handleItOptimisticallyWithDeduplication(eventAndVersion) }
