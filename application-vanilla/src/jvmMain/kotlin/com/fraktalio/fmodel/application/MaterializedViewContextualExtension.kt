package com.fraktalio.fmodel.application

import com.fraktalio.fmodel.domain.IView
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map


/**
 * Materialized View algorithm
 * Computes new State based on the previous State and the Event.
 */
context (IView<S, E>, E)
internal fun <S, E> S?.computeNewState(): S = evolve(this ?: initialState, this@E)


/**
 * Handle event - Materialized View
 * @receiver [IView] - context receiver
 * @receiver [ViewStateRepository] - context receiver
 * @receiver event of type E to be handled
 */
context (IView<S, E>, ViewStateRepository<E, S>)
suspend fun <S, E> E.handle(): S = fetchState().computeNewState().save()

/**
 * Handle event - Materialized View
 * @receiver [MaterializedView] - context receiver
 * @receiver event of type E to be handled
 *
 * Alternative function to `context (IView<S, E>, ViewStateRepository<E, S>) E.handle()`, which combines multiple contexts ([IView], [ViewStateRepository]) into a single meaningful interface/context [MaterializedView]
 */
context (MaterializedView<S, E>)
suspend fun <S, E> E.handleIt(): S = fetchState().computeNewState(this).save()


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
 * @receiver [MaterializedView] - context receiver
 * @receiver events of type Flow<E> to be handled
 *
 * Alternative function to `context (IView<S, E>, ViewStateRepository<E, S>) Flow<E>.handle()`, which combines multiple contexts ([IView], [ViewStateRepository]) into a single meaningful interface/context [MaterializedView]
 */
context (MaterializedView<S, E>)
fun <S, E> Flow<E>.handleIt(): Flow<S> = map { it.handleIt() }
