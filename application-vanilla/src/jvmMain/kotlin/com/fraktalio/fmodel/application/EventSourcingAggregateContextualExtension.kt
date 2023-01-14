package com.fraktalio.fmodel.application

import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*


/**
 * Handle command - Event-sourced aggregate/decider
 * @receiver [EventComputation] - context receiver
 * @receiver [EventRepository] - context receiver
 * @receiver command of type C to be handled
 */
context (EventComputation<C, S, E>, EventRepository<C, E>)
fun <C, S, E> C.handle(): Flow<E> = fetchEvents().computeNewEvents(this).save()


/**
 * Handle command optimistically - Event-sourced locking aggregate/decider
 * @receiver [EventComputation] - context receiver
 * @receiver [EventLockingRepository] - context receiver
 * @receiver command of type C to be handled
 */
context (EventComputation<C, S, E>, EventLockingRepository<C, E, V>)
@FlowPreview
fun <C, S, E, V> C.handleOptimistically(): Flow<Pair<E, V>> = flow {
    val events = this@handleOptimistically.fetchEvents()
    emitAll(
        events.map { it.first }
            .computeNewEvents(this@handleOptimistically)
            .save(events.lastOrNull())
    )
}

/**
 * Handle command - Event-sourced orchestrating aggregate/decider
 * @receiver [EventOrchestratingComputation] - context receiver
 * @receiver [EventRepository] - context receiver
 * @receiver command of type C to be handled
 */
context (EventOrchestratingComputation<C, S, E>, EventRepository<C, E>)
@FlowPreview
fun <C, S, E> C.handle(): Flow<E> = fetchEvents().computeNewEventsByOrchestrating(this) { it.fetchEvents() }.save()

/**
 * Handle command optimistically - Event-sourced orchestrating locking aggregate/decider
 * @receiver [EventOrchestratingComputation] - context receiver
 * @receiver [EventLockingRepository] - context receiver
 * @receiver command of type C to be handled
 */
context (EventOrchestratingComputation<C, S, E>, EventLockingRepository<C, E, V>)
@FlowPreview
fun <C, S, E, V> C.handleOptimistically(): Flow<Pair<E, V>> =
    this
        .fetchEvents().map { it.first }
        .computeNewEventsByOrchestrating(this) { it.fetchEvents().map { pair -> pair.first } }
        .save(latestVersionProvider)

/**
 * Handle command(s) - Event-sourced aggregate/decider
 * @receiver [EventComputation] - context receiver
 * @receiver [EventRepository] - context receiver
 * @receiver commands of type Flow<C> to be handled
 */
context (EventComputation<C, S, E>, EventRepository<C, E>)
@FlowPreview
fun <C, S, E> Flow<C>.handle(): Flow<E> = flatMapConcat { it.handle() }

/**
 * Handle command(s) optimistically - Event-sourced locking aggregate/decider
 * @receiver [EventComputation] - context receiver
 * @receiver [EventLockingRepository] - context receiver
 * @receiver commands of type Flow<C> to be handled
 */
context (EventComputation<C, S, E>, EventLockingRepository<C, E, V>)
@FlowPreview
fun <C, S, E, V> Flow<C>.handleOptimistically(): Flow<Pair<E, V>> = flatMapConcat { it.handleOptimistically() }

/**
 * Handle command(s) - Event-sourced orchestrating aggregate/decider
 * @receiver [EventOrchestratingComputation] - context receiver
 * @receiver [EventRepository] - context receiver
 * @receiver commands of type Flow<C> to be handled
 */
context (EventOrchestratingComputation<C, S, E>, EventRepository<C, E>)
@FlowPreview
fun <C, S, E> Flow<C>.handle(): Flow<E> = flatMapConcat { it.handle() }

/**
 * Handle command(s) optimistically - Event-sourced orchestrating locking aggregate/decider
 * @receiver [EventOrchestratingComputation] - context receiver
 * @receiver [EventLockingRepository] - context receiver
 * @receiver commands of type Flow<C> to be handled
 */
context (EventOrchestratingComputation<C, S, E>, EventLockingRepository<C, E, V>)
@FlowPreview
fun <C, S, E, V> Flow<C>.handleOptimistically(): Flow<Pair<E, V>> = flatMapConcat { it.handleOptimistically() }
