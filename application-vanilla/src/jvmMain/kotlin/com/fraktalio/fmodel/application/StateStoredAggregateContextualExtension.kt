package com.fraktalio.fmodel.application

import com.fraktalio.fmodel.domain.IDecider
import com.fraktalio.fmodel.domain.ISaga
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Handle command - State-stored aggregate/decider
 * @receiver [IDecider] - context receiver
 * @receiver [StateRepository] - context receiver
 * @receiver command of type C to be handled
 */
context (IDecider<C, S, E>, StateRepository<C, S>)
suspend fun <C, S, E> C.handle(): S =
    with(object : StateComputation<C, S, E>, IDecider<C, S, E> by this@IDecider {}) {
        this@handle.handleIt()
    }

/**
 * Handle command - State-stored aggregate/decider
 * @receiver [IDecider] - context receiver
 * @receiver [StateLockingRepository] - context receiver
 * @receiver command of type C to be handled
 */
context (IDecider<C, S, E>, StateLockingRepository<C, S, V>)
suspend fun <C, S, E, V> C.handleOptimistically(): Pair<S, V> =
    with(object : StateComputation<C, S, E>, IDecider<C, S, E> by this@IDecider {}) {
        this@handleOptimistically.handleItOptimistically()
    }

/**
 * Handle command - State-stored orchestrating aggregate/decider
 * @receiver [IDecider] - context receiver
 * @receiver [ISaga] - context receiver
 * @receiver [StateRepository] - context receiver
 * @receiver command of type C to be handled
 */
context (IDecider<C, S, E>, ISaga<E, C>, StateRepository<C, S>)
@FlowPreview
suspend fun <C, S, E> C.handle(): S =
    with(object : StateOrchestratingComputation<C, S, E>,
        IDecider<C, S, E> by this@IDecider,
        ISaga<E, C> by this@ISaga {}) {
        this@handle.handleIt()
    }

/**
 * Handle command - State-stored orchestrating aggregate/decider
 * @receiver [IDecider] - context receiver
 * @receiver [ISaga] - context receiver
 * @receiver [StateLockingRepository] - context receiver
 * @receiver command of type C to be handled
 */
context (IDecider<C, S, E>, ISaga<E, C>, StateLockingRepository<C, S, V>)
@FlowPreview
suspend fun <C, S, E, V> C.handleOptimistically(): Pair<S, V> =
    with(object : StateOrchestratingComputation<C, S, E>,
        IDecider<C, S, E> by this@IDecider,
        ISaga<E, C> by this@ISaga {}) {
        this@handleOptimistically.handleItOptimistically()
    }

/**
 * Handle command - State-stored aggregate/decider
 * @receiver [StateComputation] - context receiver
 * @receiver [StateRepository] - context receiver
 * @receiver command of type C to be handled
 *
 * Alternative function to `context (IDecider<C, S, E>, StateRepository<C, S>) C.handle()`
 */
context (StateComputation<C, S, E>, StateRepository<C, S>)
suspend fun <C, S, E> C.handleIt(): S = fetchState().computeNewState(this).save()

/**
 * Handle command - State-stored aggregate/decider
 * @receiver [StateComputation] - context receiver
 * @receiver [StateLockingRepository] - context receiver
 * @receiver command of type C to be handled
 *
 * Alternative function to `context (IDecider<C, S, E>, StateLockingRepository<C, S, V>) C.handleOptimistically()`
 */
context (StateComputation<C, S, E>, StateLockingRepository<C, S, V>)
suspend fun <C, S, E, V> C.handleItOptimistically(): Pair<S, V> {
    val (state, version) = this@handleItOptimistically.fetchState()
    return state
        .computeNewState(this@handleItOptimistically)
        .save(version)
}

/**
 * Handle command - State-stored orchestrating aggregate/decider
 * @receiver [StateOrchestratingComputation] - context receiver
 * @receiver [StateRepository] - context receiver
 * @receiver command of type C to be handled
 *
 * Alternative function to `context (IDecider<C, S, E>, ISaga<E, C>, StateRepository<C, S>) C.handle()`
 */
context (StateOrchestratingComputation<C, S, E>, StateRepository<C, S>)
@FlowPreview
suspend fun <C, S, E> C.handleIt(): S = fetchState().computeNewState(this).save()

/**
 * Handle command - State-stored orchestrating aggregate/decider
 * @receiver [StateOrchestratingComputation] - context receiver
 * @receiver [StateLockingRepository] - context receiver
 * @receiver command of type C to be handled
 *
 * Alternative function to `context (IDecider<C, S, E>, ISaga<E, C>, StateLockingRepository<C, S, V>) C.handleOptimistically()`
 */
context (StateOrchestratingComputation<C, S, E>, StateLockingRepository<C, S, V>)
@FlowPreview
suspend fun <C, S, E, V> C.handleItOptimistically(): Pair<S, V> {
    val (state, version) = this@handleItOptimistically.fetchState()
    return state
        .computeNewState(this@handleItOptimistically)
        .save(version)
}


/**
 * Handle command(s) - State-stored aggregate/decider
 * @receiver [IDecider] - context receiver
 * @receiver [StateRepository] - context receiver
 * @receiver commands of type `Flow<C>` to be handled
 */
context (IDecider<C, S, E>, StateRepository<C, S>)
fun <C, S, E> Flow<C>.handle(): Flow<S> = map { it.handle() }

/**
 * Handle command(s) - State-stored aggregate/decider
 * @receiver [IDecider] - context receiver
 * @receiver [StateLockingRepository] - context receiver
 * @receiver commands of type `Flow<C>` to be handled
 */
context (IDecider<C, S, E>, StateLockingRepository<C, S, V>)
fun <C, S, E, V> Flow<C>.handleOptimistically(): Flow<Pair<S, V>> = map { it.handleOptimistically() }


/**
 * Handle command(s) - State-stored orchestrating aggregate/decider
 * @receiver [IDecider] - context receiver
 * @receiver [ISaga] - context receiver
 * @receiver [StateRepository] - context receiver
 * @receiver commands of type `Flow<C>` to be handled
 */
context (IDecider<C, S, E>, ISaga<E, C>, StateRepository<C, S>)
@FlowPreview
suspend fun <C, S, E> Flow<C>.handle(): Flow<S> = map { it.handle() }

/**
 * Handle command(s) - State-stored orchestrating aggregate/decider
 * @receiver [IDecider] - context receiver
 * @receiver [ISaga] - context receiver
 * @receiver [StateLockingRepository] - context receiver
 * @receiver commands of type `Flow<C>` to be handled
 */
context (IDecider<C, S, E>, ISaga<E, C>, StateLockingRepository<C, S, V>)
@FlowPreview
suspend fun <C, S, E, V> Flow<C>.handleOptimistically(): Flow<Pair<S, V>> = map { it.handleOptimistically() }

/**
 * Handle command(s) - State-stored aggregate/decider
 * @receiver [StateComputation] - context receiver
 * @receiver [StateRepository] - context receiver
 * @receiver commands of type `Flow<C>` to be handled
 *
 * Alternative function to `context (IDecider<C, S, E>, StateRepository<C, S>) Flow<C>.handle()`
 */
context (StateComputation<C, S, E>, StateRepository<C, S>)
fun <C, S, E> Flow<C>.handleIt(): Flow<S> = map { it.handleIt() }

/**
 * Handle command(s) - State-stored aggregate/decider
 * @receiver [StateComputation] - context receiver
 * @receiver [StateLockingRepository] - context receiver
 * @receiver commands of type `Flow<C>` to be handled
 *
 * Alternative function to `context (IDecider<C, S, E>, StateLockingRepository<C, S, V>) Flow<C>.handleOptimistically()`
 */
context (StateComputation<C, S, E>, StateLockingRepository<C, S, V>)
fun <C, S, E, V> Flow<C>.handleItOptimistically(): Flow<Pair<S, V>> = map { it.handleItOptimistically() }


/**
 * Handle command(s) - State-stored orchestrating aggregate/decider
 * @receiver [StateOrchestratingComputation] - context receiver
 * @receiver [StateRepository] - context receiver
 * @receiver commands of type `Flow<C>` to be handled
 *
 * Alternative function to `context (IDecider<C, S, E>, ISaga<E, C>, StateRepository<C, S>) Flow<C>.handle()`
 */
context (StateOrchestratingComputation<C, S, E>, StateRepository<C, S>)
@FlowPreview
suspend fun <C, S, E> Flow<C>.handleIt(): Flow<S> = map { it.handleIt() }

/**
 * Handle command(s) - State-stored orchestrating aggregate/decider
 * @receiver [StateOrchestratingComputation] - context receiver
 * @receiver [StateLockingRepository] - context receiver
 * @receiver commands of type `Flow<C>` to be handled
 *
 * Alternative function to `context (IDecider<C, S, E>, ISaga<E, C>, StateLockingRepository<C, S, V>) Flow<C>.handleOptimistically()`
 */
context (StateOrchestratingComputation<C, S, E>, StateLockingRepository<C, S, V>)
@FlowPreview
suspend fun <C, S, E, V> Flow<C>.handleItOptimistically(): Flow<Pair<S, V>> =
    map { it.handleItOptimistically() }
