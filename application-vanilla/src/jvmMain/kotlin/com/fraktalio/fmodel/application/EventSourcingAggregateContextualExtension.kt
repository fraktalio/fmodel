package com.fraktalio.fmodel.application

import com.fraktalio.fmodel.domain.IDecider
import com.fraktalio.fmodel.domain.ISaga
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*

/**
 * Event-sourced aggregate/decider algorithm
 * Computes new Events based on the previous Events and the Command.
 */
context (IDecider<C, S, E>, C)
internal fun <C, S, E> Flow<E>.computeNewEvents(): Flow<E> = flow {
    val currentState = fold(initialState) { s, e -> evolve(s, e) }
    val resultingEvents = decide(this@C, currentState)
    emitAll(resultingEvents)
}

/**
 * Event-sourced orchestrating aggregate/decider algorithm
 * Computes new Events based on the previous Events and the Command.
 * Saga might react on Events and send new Commands to the Decider.
 */
context (IDecider<C, S, E>, ISaga<E, C>, C)
@FlowPreview
internal fun <C, S, E> Flow<E>.computeNewEvents(): Flow<E> = flow {
    val currentState = fold(initialState) { s, e -> evolve(s, e) }
    var resultingEvents = decide(this@C, currentState)

    resultingEvents.flatMapConcat { react(it) }.onEach {
        val newEvents = flowOf(this@computeNewEvents, resultingEvents).flattenConcat().computeNewEvents()
        resultingEvents = flowOf(resultingEvents, newEvents).flattenConcat()
    }.collect()

    emitAll(resultingEvents)
}


/**
 * Handle command - Event-sourced aggregate/decider
 * @receiver [IDecider] - context receiver
 * @receiver [EventRepository] - context receiver
 * @receiver command of type C to be handled
 */
context (IDecider<C, S, E>, EventRepository<C, E>)
fun <C, S, E> C.handle(): Flow<E> = fetchEvents().computeNewEvents().save()

/**
 * Handle command - Event-sourced aggregate/decider
 * @receiver [EventSourcingAggregate] - context receiver
 * @receiver command of type C to be handled
 *
 * Alternative function to `context (IDecider<C, S, E>, EventRepository<C, E>) C.handle()`, which combines multiple contexts ([IDecider], [EventRepository]) into a single meaningful interface/context [EventSourcingAggregate]
 */
context (EventSourcingAggregate<C, S, E>)
@FlowPreview
fun <C, S, E> C.handleIt(): Flow<E> = fetchEvents().computeNewEvents(this).save()

/**
 * Handle command - Event-sourced orchestrating aggregate/decider
 * @receiver [IDecider] - context receiver
 * @receiver [ISaga] - context receiver
 * @receiver [EventRepository] - context receiver
 * @receiver command of type C to be handled
 */
context (IDecider<C, S, E>, ISaga<E, C>, EventRepository<C, E>)
@FlowPreview
fun <C, S, E> C.handle(): Flow<E> = fetchEvents().computeNewEvents().save()

/**
 * Handle command - Event-sourced orchestrating aggregate/decider
 * @receiver [EventSourcingOrchestratingAggregate] - context receiver
 * @receiver command of type C to be handled
 *
 * Alternative function to `context (IDecider<C, S, E>, ISaga<E, C>, EventRepository<C, E>) C.handle()`, which combines multiple contexts ([IDecider], [ISaga], [EventRepository]) into a single meaningful interface/context [EventSourcingOrchestratingAggregate]
 */
context (EventSourcingOrchestratingAggregate<C, S, E>)
@FlowPreview
fun <C, S, E> C.handleIt(): Flow<E> = fetchEvents().computeNewEvents(this).save()


/**
 * Handle command(s) - Event-sourced aggregate/decider
 * @receiver [IDecider] - context receiver
 * @receiver [EventRepository] - context receiver
 * @receiver commands of type Flow<C> to be handled
 */
context (IDecider<C, S, E>, EventRepository<C, E>)
@FlowPreview
fun <C, S, E> Flow<C>.handle(): Flow<E> = flatMapConcat { it.handle() }

/**
 * Handle command(s) - Event-sourced aggregate/decider
 * @receiver [EventSourcingAggregate] - context receiver
 * @receiver commands of type Flow<C> to be handled
 *
 * Alternative function to `context (IDecider<C, S, E>, EventRepository<C, E>) Flow<C>.handle()`, which combines multiple contexts ([IDecider], [EventRepository]) into a single meaningful interface/context [EventSourcingAggregate]
 */
context (EventSourcingAggregate<C, S, E>)
@FlowPreview
fun <C, S, E> Flow<C>.handleIt(): Flow<E> = flatMapConcat { it.handleIt() }

/**
 * Handle command(s) - Event-sourced orchestrating aggregate/decider
 * @receiver [IDecider] - context receiver
 * @receiver [ISaga] - context receiver
 * @receiver [EventRepository] - context receiver
 * @receiver commands of type Flow<C> to be handled
 */
context (IDecider<C, S, E>, ISaga<E, C>, EventRepository<C, E>)
@FlowPreview
fun <C, S, E> Flow<C>.handle(): Flow<E> = flatMapConcat { it.handle() }

/**
 * Handle command(s) - Event-sourced orchestrating aggregate/decider
 * @receiver [EventSourcingOrchestratingAggregate] - context receiver
 * @receiver commands of type Flow<C> to be handled
 *
 * Alternative function to `context (IDecider<C, S, E>, ISaga<E, C>, EventRepository<C, E>) Flow<C>.handle()`, which combines multiple contexts ([IDecider], [ISaga], [EventRepository]) into a single meaningful interface/context [EventSourcingOrchestratingAggregate]
 */
context (EventSourcingOrchestratingAggregate<C, S, E>)
@FlowPreview
fun <C, S, E> Flow<C>.handleIt(): Flow<E> = flatMapConcat { it.handleIt() }
