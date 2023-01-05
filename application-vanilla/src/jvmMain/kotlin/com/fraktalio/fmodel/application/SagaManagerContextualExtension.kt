package com.fraktalio.fmodel.application

import com.fraktalio.fmodel.domain.ISaga
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapConcat
import kotlin.contracts.ExperimentalContracts
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext


/**
 * Materialized View algorithm
 * Computes new State based on the previous State and the Event.
 */
context (ISaga<AR, A>, ActionPublisher<A>)
internal fun <AR, A> AR.computeNewActions(): Flow<A> = react(this)

/**
 * Handle event / action result - Saga Manager
 * @receiver [ISaga] - context receiver
 * @receiver [ActionPublisher] - context receiver
 * @receiver event/action result of type AR to be handled
 */
context (ISaga<AR, A>, ActionPublisher<A>)
fun <AR, A> AR.handle(): Flow<A> = computeNewActions().publish()

/**
 * Handle event / action result - Saga Manager
 * @receiver [SagaManager] - context receiver
 * @receiver event/action result of type AR to be handled
 *
 * Alternative function to `context (ISaga<AR, A>, ActionPublisher<A>) AR.handle()`, which combines multiple contexts ([ISaga], [ActionPublisher]) into a single meaningful interface/context [SagaManager]
 */
context (SagaManager<AR, A>)
fun <AR, A> AR.handleIt(): Flow<A> = computeNewActions().publish()

/**
 * Handle event / action result - Saga Manager
 * @receiver [ISaga] - context receiver
 * @receiver [ActionPublisher] - context receiver
 * @receiver events/actions result of type Flow<AR> to be handled
 */
context (ISaga<AR, A>, ActionPublisher<A>)
@FlowPreview
fun <AR, A> Flow<AR>.handle(): Flow<A> = flatMapConcat { it.handle() }

/**
 * Handle event / action result concurrently by the finite number of actors - Saga Manager
 * @receiver [ISaga] - context receiver
 * @receiver [ActionPublisher] - context receiver
 * @receiver events/actions result of type Flow<AR> to be handled
 */
context (ISaga<AR, A>, ActionPublisher<A>)
@ExperimentalContracts
@FlowPreview
fun <AR, A> Flow<AR>.handleConcurrently(
    numberOfActors: Int = 100,
    actorsCapacity: Int = Channel.BUFFERED,
    actorsStart: CoroutineStart = CoroutineStart.LAZY,
    actorsContext: CoroutineContext = EmptyCoroutineContext,
    partitionKey: (AR) -> Int
): Flow<A> = publishConcurrentlyTo(
    sagaManager(this@ISaga, this@ActionPublisher),
    numberOfActors,
    actorsCapacity,
    actorsStart,
    actorsContext,
    partitionKey
)

/**
 * Handle event / action result - Saga Manager
 * @receiver [SagaManager] - context receiver
 * @receiver events/actions result of type Flow<AR> to be handled
 *
 * Alternative function to `context (ISaga<AR, A>, ActionPublisher<A>) Flow<AR>.handle()`, which combines multiple contexts ([ISaga], [ActionPublisher]) into a single meaningful interface/context [SagaManager]
 */
context (SagaManager<AR, A>)
@FlowPreview
fun <AR, A> Flow<AR>.handleIt(): Flow<A> = flatMapConcat { it.handleIt() }

/**
 * Handle event / action result concurrently by the finite number of actors - Saga Manager
 * @receiver [SagaManager] - context receiver
 * @receiver events/actions result of type Flow<AR> to be handled
 *
 * Alternative function to `context (ISaga<AR, A>, ActionPublisher<A>) Flow<AR>.handleConcurrently(...)`, which combines multiple contexts ([ISaga], [ActionPublisher]) into a single meaningful interface/context [SagaManager]
 */
context (SagaManager<AR, A>)
@ExperimentalContracts
@FlowPreview
fun <AR, A> Flow<AR>.handleItConcurrently(
    numberOfActors: Int = 100,
    actorsCapacity: Int = Channel.BUFFERED,
    actorsStart: CoroutineStart = CoroutineStart.LAZY,
    actorsContext: CoroutineContext = EmptyCoroutineContext,
    partitionKey: (AR) -> Int
): Flow<A> = publishConcurrentlyTo(
    this@SagaManager,
    numberOfActors,
    actorsCapacity,
    actorsStart,
    actorsContext,
    partitionKey
)
