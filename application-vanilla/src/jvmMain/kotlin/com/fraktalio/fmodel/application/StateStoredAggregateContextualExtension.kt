package com.fraktalio.fmodel.application

import com.fraktalio.fmodel.domain.IDecider
import com.fraktalio.fmodel.domain.ISaga
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlin.contracts.ExperimentalContracts
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext


/**
 * State-stored aggregate/decider algorithm
 * Computes new State based on the previous State and the Command.
 */
context (IDecider<C, S, E>, C)
internal suspend fun <C, S, E> S?.computeNewState(): S {
    val currentState = this ?: initialState
    val events = decide(this@C, currentState)
    return events.fold(currentState) { s, e -> evolve(s, e) }
}

/**
 * State-stored orchestrating aggregate/decider algorithm
 * Computes new State based on the previous State and the Command.
 * Saga might react on Events and send new Commands to the Decider.
 */
context (IDecider<C, S, E>, ISaga<E, C>, C)
@FlowPreview
internal suspend fun <C, S, E> S?.computeNewState(): S {
    val currentState = this ?: initialState
    val events = decide(this@C, currentState)
    val newState = events.fold(currentState) { s, e -> evolve(s, e) }
    events.flatMapConcat { react(it) }.onEach { newState.computeNewState() }.collect()
    return newState
}

/**
 * Handle command - State-stored aggregate/decider
 * @receiver [IDecider] - context receiver
 * @receiver [StateRepository] - context receiver
 * @receiver command of type C to be handled
 */
context (IDecider<C, S, E>, StateRepository<C, S>)
suspend fun <C, S, E> C.handle(): S = fetchState().computeNewState().save()

/**
 * Handle command - State-stored aggregate/decider
 * @receiver [StateStoredAggregate] - context receiver
 * @receiver command of type C to be handled
 *
 * Alternative function to `context (IDecider<C, S, E>, StateRepository<C, S>) C.handle()`, which combines multiple contexts ([IDecider], [StateRepository]) into a single meaningful interface/context [StateStoredAggregate]
 */
context (StateStoredAggregate<C, S, E>)
@FlowPreview
suspend fun <C, S, E> C.handleIt(): S = fetchState().computeNewState(this).save()

/**
 * Handle command - State-stored orchestrating aggregate/decider
 * @receiver [IDecider] - context receiver
 * @receiver [ISaga] - context receiver
 * @receiver [StateRepository] - context receiver
 * @receiver command of type C to be handled
 */
context (IDecider<C, S, E>, ISaga<E, C>, StateRepository<C, S>)
@FlowPreview
suspend fun <C, S, E> C.handle(): S = fetchState().computeNewState().save()

/**
 * Handle command - State-stored orchestrating aggregate/decider
 * @receiver [StateStoredOrchestratingAggregate] - context receiver
 * @receiver command of type C to be handled
 *
 * Alternative function to `context (IDecider<C, S, E>, ISaga<E, C>, StateRepository<C, S>) C.handle()`, which combines multiple contexts ([IDecider], [ISaga], [StateRepository]) into a single meaningful interface/context [StateStoredOrchestratingAggregate]
 */
context (StateStoredOrchestratingAggregate<C, S, E>)
@FlowPreview
suspend fun <C, S, E> C.handleIt(): S = fetchState().computeNewState(this).save()


/**
 * Handle command(s) - State-stored aggregate/decider
 * @receiver [IDecider] - context receiver
 * @receiver [StateRepository] - context receiver
 * @receiver commands of type `Flow<C>` to be handled
 */
context (IDecider<C, S, E>, StateRepository<C, S>)
fun <C, S, E> Flow<C>.handle(): Flow<S> = map { it.handle() }

/**
 * Handle command(s) concurrently by the finite number of actors - State-stored aggregate/decider
 * @receiver [IDecider] - context receiver
 * @receiver [StateRepository] - context receiver
 * @receiver commands of type `Flow<C>` to be handled
 */
context (IDecider<C, S, E>, StateRepository<C, S>)
@ExperimentalContracts
fun <C, S, E> Flow<C>.handleConcurrently(
    numberOfActors: Int = 100,
    actorsCapacity: Int = Channel.BUFFERED,
    actorsStart: CoroutineStart = CoroutineStart.LAZY,
    actorsContext: CoroutineContext = EmptyCoroutineContext,
    partitionKey: (C) -> Int
): Flow<S> =  publishConcurrentlyTo(
    stateStoredAggregate(this@IDecider, this@StateRepository),
    numberOfActors,
    actorsCapacity,
    actorsStart,
    actorsContext,
    partitionKey
)

/**
 * Handle command(s) - State-stored aggregate/decider
 * @receiver [StateStoredAggregate] - context receiver
 * @receiver commands of type `Flow<C>` to be handled
 *
 * Alternative function to `context (IDecider<C, S, E>, StateRepository<C, S>) Flow<C>.handle()`, which combines multiple contexts ([IDecider], [StateRepository]) into a single meaningful interface/context [StateStoredAggregate]
 */
context (StateStoredAggregate<C, S, E>)
@FlowPreview
fun <C, S, E> Flow<C>.handleIt(): Flow<S> = map { it.handleIt() }

/**
 * Handle command(s) concurrently by the finite number of actors - State-stored aggregate/decider
 * @receiver [StateStoredAggregate] - context receiver
 * @receiver commands of type `Flow<C>` to be handled
 *
 * Alternative function to `context (IDecider<C, S, E>, StateRepository<C, S>) Flow<C>.handleConcurrently(...)`, which combines multiple contexts ([IDecider], [StateRepository]) into a single meaningful interface/context [StateStoredAggregate]
 */
context (StateStoredAggregate<C, S, E>)
@ExperimentalContracts
fun <C, S, E> Flow<C>.handleItConcurrently(
    numberOfActors: Int = 100,
    actorsCapacity: Int = Channel.BUFFERED,
    actorsStart: CoroutineStart = CoroutineStart.LAZY,
    actorsContext: CoroutineContext = EmptyCoroutineContext,
    partitionKey: (C) -> Int
): Flow<S> =  publishConcurrentlyTo(
    this@StateStoredAggregate,
    numberOfActors,
    actorsCapacity,
    actorsStart,
    actorsContext,
    partitionKey
)

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
 * Handle command(s) concurrently by the finite number of actors - State-stored orchestrating aggregate/decider
 * @receiver [IDecider] - context receiver
 * @receiver [ISaga] - context receiver
 * @receiver [StateRepository] - context receiver
 * @receiver commands of type `Flow<C>` to be handled
 */
context (IDecider<C, S, E>, ISaga<E, C>, StateRepository<C, S>)
@ExperimentalContracts
fun <C, S, E> Flow<C>.handleConcurrently(
    numberOfActors: Int = 100,
    actorsCapacity: Int = Channel.BUFFERED,
    actorsStart: CoroutineStart = CoroutineStart.LAZY,
    actorsContext: CoroutineContext = EmptyCoroutineContext,
    partitionKey: (C) -> Int
): Flow<S> =  publishConcurrentlyTo(
    stateStoredOrchestratingAggregate(this@IDecider, this@StateRepository, this@ISaga),
    numberOfActors,
    actorsCapacity,
    actorsStart,
    actorsContext,
    partitionKey
)

/**
 * Handle command(s) - State-stored orchestrating aggregate/decider
 * @receiver [StateStoredOrchestratingAggregate] - context receiver
 * @receiver commands of type `Flow<C>` to be handled
 *
 * Alternative function to `context (IDecider<C, S, E>, ISaga<E, C>, StateRepository<C, S>) Flow<C>.handle()`, which combines multiple contexts ([IDecider], [ISaga], [StateRepository]) into a single meaningful interface/context [StateStoredOrchestratingAggregate]
 */
context (StateStoredOrchestratingAggregate<C, S, E>)
@FlowPreview
suspend fun <C, S, E> Flow<C>.handleIt(): Flow<S> = map { it.handleIt() }

/**
 * Handle command(s) concurrently by the finite number of actors - State-stored orchestrating aggregate/decider
 * @receiver [StateStoredOrchestratingAggregate] - context receiver
 * @receiver commands of type `Flow<C>` to be handled
 *
 * Alternative function to `context (IDecider<C, S, E>, ISaga<E, C>, StateRepository<C, S>) Flow<C>.handleConcurrently(...)`, which combines multiple contexts ([IDecider], [ISaga], [StateRepository]) into a single meaningful interface/context [StateStoredOrchestratingAggregate]
 */
context (StateStoredOrchestratingAggregate<C, S, E>)
@ExperimentalContracts
fun <C, S, E> Flow<C>.handleItConcurrently(
    numberOfActors: Int = 100,
    actorsCapacity: Int = Channel.BUFFERED,
    actorsStart: CoroutineStart = CoroutineStart.LAZY,
    actorsContext: CoroutineContext = EmptyCoroutineContext,
    partitionKey: (C) -> Int
): Flow<S> =  publishConcurrentlyTo(
    this@StateStoredOrchestratingAggregate,
    numberOfActors,
    actorsCapacity,
    actorsStart,
    actorsContext,
    partitionKey
)
