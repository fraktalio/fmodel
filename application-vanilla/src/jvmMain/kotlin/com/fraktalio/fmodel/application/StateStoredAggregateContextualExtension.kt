package com.fraktalio.fmodel.application

import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Handle command - State-stored aggregate/decider
 * @receiver [StateComputation] - context receiver
 * @receiver [StateRepository] - context receiver
 * @receiver command of type C to be handled
 */
context (StateComputation<C, S, E>, StateRepository<C, S>)
suspend fun <C, S, E> C.handle(): S = fetchState().computeNewState(this).save()

/**
 * Handle command - State-stored aggregate/decider
 * @receiver [StateComputation] - context receiver
 * @receiver [StateLockingRepository] - context receiver
 * @receiver command of type C to be handled
 */
context (StateComputation<C, S, E>, StateLockingRepository<C, S, V>)
suspend fun <C, S, E, V> C.handleOptimistically(): Pair<S, V> {
    val (state, version) = this@handleOptimistically.fetchState()
    return state
        .computeNewState(this@handleOptimistically)
        .save(version)
}

/**
 * Handle command - State-stored orchestrating aggregate/decider
 * @receiver [StateOrchestratingComputation] - context receiver
 * @receiver [StateRepository] - context receiver
 * @receiver command of type C to be handled
 */
context (StateOrchestratingComputation<C, S, E>, StateRepository<C, S>)
@FlowPreview
suspend fun <C, S, E> C.handle(): S = fetchState().computeNewState(this).save()

/**
 * Handle command - State-stored orchestrating aggregate/decider
 * @receiver [StateOrchestratingComputation] - context receiver
 * @receiver [StateLockingRepository] - context receiver
 * @receiver command of type C to be handled
 */
context (StateOrchestratingComputation<C, S, E>, StateLockingRepository<C, S, V>)
@FlowPreview
suspend fun <C, S, E, V> C.handleOptimistically(): Pair<S, V> {
    val (state, version) = this@handleOptimistically.fetchState()
    return state
        .computeNewState(this@handleOptimistically)
        .save(version)
}

/**
 * Handle command(s) - State-stored aggregate/decider
 * @receiver [StateComputation] - context receiver
 * @receiver [StateRepository] - context receiver
 * @receiver commands of type `Flow<C>` to be handled
 */
context (StateComputation<C, S, E>, StateRepository<C, S>)
fun <C, S, E> Flow<C>.handle(): Flow<S> = map { it.handle() }

/**
 * Handle command(s) - State-stored aggregate/decider
 * @receiver [StateComputation] - context receiver
 * @receiver [StateLockingRepository] - context receiver
 * @receiver commands of type `Flow<C>` to be handled
 */
context (StateComputation<C, S, E>, StateLockingRepository<C, S, V>)
fun <C, S, E, V> Flow<C>.handleOptimistically(): Flow<Pair<S, V>> = map { it.handleOptimistically() }


/**
 * Handle command(s) - State-stored orchestrating aggregate/decider
 * @receiver [StateOrchestratingComputation] - context receiver
 * @receiver [StateRepository] - context receiver
 * @receiver commands of type `Flow<C>` to be handled
 */
context (StateOrchestratingComputation<C, S, E>, StateRepository<C, S>)
@FlowPreview
suspend fun <C, S, E> Flow<C>.handle(): Flow<S> = map { it.handle() }

/**
 * Handle command(s) - State-stored orchestrating aggregate/decider
 * @receiver [StateOrchestratingComputation] - context receiver
 * @receiver [StateLockingRepository] - context receiver
 * @receiver commands of type `Flow<C>` to be handled
 */
context (StateOrchestratingComputation<C, S, E>, StateLockingRepository<C, S, V>)
@FlowPreview
suspend fun <C, S, E, V> Flow<C>.handleOptimistically(): Flow<Pair<S, V>> =
    map { it.handleOptimistically() }
