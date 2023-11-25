package com.fraktalio.fmodel.application

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.actor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.onCompletion
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.math.absoluteValue

/**
 * Extension function - Handles the flow of command messages of type [C] by concurrently distributing the load across finite number of actors/handlers
 *
 * @param commands [Flow] of Command messages of type [C]
 * @param numberOfActors total number of actors/workers available for distributing the load. Minimum one.
 * @param actorsCapacity capacity of the actors channel's buffer
 * @param actorsStart actors coroutine start option
 * @param actorsContext additional to [CoroutineScope.coroutineContext] context of the actor coroutines.
 * @param partitionKey a function that calculates the partition key/routing key of command - commands with the same partition key will be handled with the same 'actor' to keep the ordering
 * @return [Flow] of stored Events of type [E]
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
@ObsoleteCoroutinesApi
fun <C, S, E> EventSourcingAggregate<C, S, E>.handleConcurrently(
    commands: Flow<C>,
    numberOfActors: Int = 100,
    actorsCapacity: Int = Channel.BUFFERED,
    actorsStart: CoroutineStart = CoroutineStart.LAZY,
    actorsContext: CoroutineContext = EmptyCoroutineContext,
    partitionKey: (C) -> Int
): Flow<E> = channelFlow {
    val actors: List<SendChannel<C>> = (1..numberOfActors).map {
        commandActor(channel, actorsCapacity, actorsStart, actorsContext) { handle(it) }
    }
    commands
        .onCompletion {
            actors.forEach {
                it.close()
            }
        }
        .collect {
            val partition = partitionKey(it).absoluteValue % numberOfActors.coerceAtLeast(1)
            actors[partition].send(it)
        }
}

@ObsoleteCoroutinesApi
fun <C, S, E> EventSourcingAggregate<C, S, E>.handleConcurrently(
    commands: Flow<Pair<C, Map<String, Any>>>,
    numberOfActors: Int = 100,
    actorsCapacity: Int = Channel.BUFFERED,
    actorsStart: CoroutineStart = CoroutineStart.LAZY,
    actorsContext: CoroutineContext = EmptyCoroutineContext,
    partitionKey: (C, Map<String, Any>) -> Int
): Flow<Pair<E, Map<String, Any>>> = channelFlow {
    val actors: List<SendChannel<Pair<C, Map<String, Any>>>> = (1..numberOfActors).map {
        commandWithMetadataActor(channel, actorsCapacity, actorsStart, actorsContext) { cmd, meta -> handle(cmd, meta) }
    }
    commands
        .onCompletion {
            actors.forEach {
                it.close()
            }
        }
        .collect {
            val partition = partitionKey(it.first, it.second).absoluteValue % numberOfActors.coerceAtLeast(1)
            actors[partition].send(it)
        }
}

/**
 * Extension function - Publishes [Flow] of commands of type [C] to the event sourcing aggregate of type  [EventSourcingAggregate]<[C], *, [E]> by concurrently distributing the load across finite number of actors
 *
 * @receiver [Flow] of commands of type [C]
 * @param aggregate of type [EventSourcingAggregate]<[C], *, [E]>
 * @param numberOfActors total number of actors/workers available for distributing the load. Minimum one.
 * @param actorsCapacity capacity of the actors channel's buffer
 * @param actorsStart actors coroutine start option
 * @param actorsContext additional to [CoroutineScope.coroutineContext] context of the actor coroutines.
 * @param partitionKey a function that calculates the partition key/routing key of command - commands with the same partition key will be handled with the same 'actor' to keep the ordering
 * @return the [Flow] of stored Events of type [E]
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
@ObsoleteCoroutinesApi
fun <C, E> Flow<C>.publishConcurrentlyTo(
    aggregate: EventSourcingAggregate<C, *, E>,
    numberOfActors: Int = 100,
    actorsCapacity: Int = Channel.BUFFERED,
    actorsStart: CoroutineStart = CoroutineStart.LAZY,
    actorsContext: CoroutineContext = EmptyCoroutineContext,
    partitionKey: (C) -> Int
): Flow<E> =
    aggregate.handleConcurrently(this, numberOfActors, actorsCapacity, actorsStart, actorsContext) { partitionKey(it) }

@ObsoleteCoroutinesApi
fun <C, E> Flow<Pair<C, Map<String, Any>>>.publishConcurrentlyTo(
    aggregate: EventSourcingAggregate<C, *, E>,
    numberOfActors: Int = 100,
    actorsCapacity: Int = Channel.BUFFERED,
    actorsStart: CoroutineStart = CoroutineStart.LAZY,
    actorsContext: CoroutineContext = EmptyCoroutineContext,
    partitionKey: (C, Map<String, Any>) -> Int
): Flow<Pair<E, Map<String, Any>>> =
    aggregate.handleConcurrently(
        this,
        numberOfActors,
        actorsCapacity,
        actorsStart,
        actorsContext
    ) { cmd, meta -> partitionKey(cmd, meta) }

/**
 * Extension function - Handles the flow of command messages of type [C] by concurrently distributing the load across finite number of actors/handlers
 *
 * @param commands [Flow] of Command messages of type [C]
 * @param numberOfActors total number of actors/workers available for distributing the load. Minimum one.
 * @param actorsCapacity capacity of the actors channel's buffer
 * @param actorsStart actors coroutine start option
 * @param actorsContext additional to [CoroutineScope.coroutineContext] context of the actor coroutines.
 * @param partitionKey a function that calculates the partition key/routing key of command - commands with the same partition key will be handled with the same 'actor' to keep the ordering
 * @return [Flow] of stored Events of type [E]
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
@ExperimentalCoroutinesApi
@ObsoleteCoroutinesApi
fun <C, S, E> EventSourcingOrchestratingAggregate<C, S, E>.handleConcurrently(
    commands: Flow<C>,
    numberOfActors: Int = 100,
    actorsCapacity: Int = Channel.BUFFERED,
    actorsStart: CoroutineStart = CoroutineStart.LAZY,
    actorsContext: CoroutineContext = EmptyCoroutineContext,
    partitionKey: (C) -> Int
): Flow<E> = channelFlow {
    val actors: List<SendChannel<C>> = (1..numberOfActors).map {
        commandActor(channel, actorsCapacity, actorsStart, actorsContext) { handle(it) }
    }
    commands
        .onCompletion {
            actors.forEach {
                it.close()
            }
        }
        .collect {
            val partition = partitionKey(it).absoluteValue % numberOfActors.coerceAtLeast(1)
            actors[partition].send(it)
        }
}

@ExperimentalCoroutinesApi
@ObsoleteCoroutinesApi
fun <C, S, E> EventSourcingOrchestratingAggregate<C, S, E>.handleConcurrently(
    commands: Flow<Pair<C, Map<String, Any>>>,
    numberOfActors: Int = 100,
    actorsCapacity: Int = Channel.BUFFERED,
    actorsStart: CoroutineStart = CoroutineStart.LAZY,
    actorsContext: CoroutineContext = EmptyCoroutineContext,
    partitionKey: (C, Map<String, Any>) -> Int
): Flow<Pair<E, Map<String, Any>>> = channelFlow {
    val actors: List<SendChannel<Pair<C, Map<String, Any>>>> = (1..numberOfActors).map {
        commandWithMetadataActor(channel, actorsCapacity, actorsStart, actorsContext) { cmd, meta -> handle(cmd, meta) }
    }
    commands
        .onCompletion {
            actors.forEach {
                it.close()
            }
        }
        .collect {
            val partition = partitionKey(it.first, it.second).absoluteValue % numberOfActors.coerceAtLeast(1)
            actors[partition].send(it)
        }
}

/**
 * Extension function - Publishes [Flow] of commands of type [C] to the event sourcing aggregate of type  [EventSourcingAggregate]<[C], *, [E]> by concurrently distributing the load across finite number of actors
 *
 * @receiver [Flow] of commands of type [C]
 * @param aggregate of type [EventSourcingAggregate]<[C], *, [E]>
 * @param numberOfActors total number of actors/workers available for distributing the load. Minimum one.
 * @param actorsCapacity capacity of the actors channel's buffer
 * @param actorsStart actors coroutine start option
 * @param actorsContext additional to [CoroutineScope.coroutineContext] context of the actor coroutines.
 * @param partitionKey a function that calculates the partition key/routing key of command - commands with the same partition key will be handled with the same 'actor' to keep the ordering
 * @return the [Flow] of stored Events of type [E]
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
@ExperimentalCoroutinesApi
@ObsoleteCoroutinesApi
fun <C, E> Flow<C>.publishConcurrentlyTo(
    aggregate: EventSourcingOrchestratingAggregate<C, *, E>,
    numberOfActors: Int = 100,
    actorsCapacity: Int = Channel.BUFFERED,
    actorsStart: CoroutineStart = CoroutineStart.LAZY,
    actorsContext: CoroutineContext = EmptyCoroutineContext,
    partitionKey: (C) -> Int
): Flow<E> =
    aggregate.handleConcurrently(this, numberOfActors, actorsCapacity, actorsStart, actorsContext) { partitionKey(it) }

@ExperimentalCoroutinesApi
@ObsoleteCoroutinesApi
fun <C, E> Flow<Pair<C, Map<String, Any>>>.publishConcurrentlyTo(
    aggregate: EventSourcingOrchestratingAggregate<C, *, E>,
    numberOfActors: Int = 100,
    actorsCapacity: Int = Channel.BUFFERED,
    actorsStart: CoroutineStart = CoroutineStart.LAZY,
    actorsContext: CoroutineContext = EmptyCoroutineContext,
    partitionKey: (C, Map<String, Any>) -> Int
): Flow<Pair<E, Map<String, Any>>> =
    aggregate.handleConcurrently(
        this,
        numberOfActors,
        actorsCapacity,
        actorsStart,
        actorsContext
    ) { cmd, meta -> partitionKey(cmd, meta) }

/**
 * Extension function - Handles the flow of command messages of type [C] by concurrently distributing the load across finite number of actors/handlers
 *
 * @param commands [Flow] of Command messages of type [C]
 * @param numberOfActors total number of actors/workers available for distributing the load. Minimum one.
 * @param actorsCapacity capacity of the actors channel's buffer
 * @param actorsStart actors coroutine start option
 * @param actorsContext additional to [CoroutineScope.coroutineContext] context of the actor coroutines.
 * @param partitionKey a function that calculates the partition key/routing key of command - commands with the same partition key will be handled with the same 'actor' to keep the ordering
 * @return [Flow] of stored Events of type [E] with version [V]
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
@ObsoleteCoroutinesApi
fun <C, S, E, V> EventSourcingLockingAggregate<C, S, E, V>.handleConcurrentlyAndOptimistically(
    commands: Flow<C>,
    numberOfActors: Int = 100,
    actorsCapacity: Int = Channel.BUFFERED,
    actorsStart: CoroutineStart = CoroutineStart.LAZY,
    actorsContext: CoroutineContext = EmptyCoroutineContext,
    partitionKey: (C) -> Int
): Flow<Pair<E, V>> = channelFlow {
    val actors: List<SendChannel<C>> = (1..numberOfActors).map {
        commandActor(channel, actorsCapacity, actorsStart, actorsContext) { handleOptimistically(it) }
    }
    commands
        .onCompletion {
            actors.forEach {
                it.close()
            }
        }
        .collect {
            val partition = partitionKey(it).absoluteValue % numberOfActors.coerceAtLeast(1)
            actors[partition].send(it)
        }
}

@ObsoleteCoroutinesApi
fun <C, S, E, V> EventSourcingLockingAggregate<C, S, E, V>.handleConcurrentlyAndOptimistically(
    commands: Flow<Pair<C, Map<String, Any>>>,
    numberOfActors: Int = 100,
    actorsCapacity: Int = Channel.BUFFERED,
    actorsStart: CoroutineStart = CoroutineStart.LAZY,
    actorsContext: CoroutineContext = EmptyCoroutineContext,
    partitionKey: (C, Map<String, Any>) -> Int
): Flow<Triple<E, V, Map<String, Any>>> = channelFlow {
    val actors: List<SendChannel<Pair<C, Map<String, Any>>>> = (1..numberOfActors).map {
        commandWithMetadataAndVersionActor(
            channel,
            actorsCapacity,
            actorsStart,
            actorsContext
        ) { cmd, meta -> handleOptimistically(cmd, meta) }
    }
    commands
        .onCompletion {
            actors.forEach {
                it.close()
            }
        }
        .collect {
            val partition = partitionKey(it.first, it.second).absoluteValue % numberOfActors.coerceAtLeast(1)
            actors[partition].send(it)
        }
}

/**
 * Extension function - Publishes [Flow] of commands of type [C] to the event sourcing aggregate of type  [EventSourcingAggregate]<[C], *, [E]> by concurrently distributing the load across finite number of actors
 *
 * @receiver [Flow] of commands of type [C]
 * @param aggregate of type [EventSourcingAggregate]<[C], *, [E]>
 * @param numberOfActors total number of actors/workers available for distributing the load. Minimum one.
 * @param actorsCapacity capacity of the actors channel's buffer
 * @param actorsStart actors coroutine start option
 * @param actorsContext additional to [CoroutineScope.coroutineContext] context of the actor coroutines.
 * @param partitionKey a function that calculates the partition key/routing key of command - commands with the same partition key will be handled with the same 'actor' to keep the ordering
 * @return the [Flow] of stored Events of type [E] and version [V]
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
@ObsoleteCoroutinesApi
fun <C, E, V> Flow<C>.publishConcurrentlyAndOptimisticallyTo(
    aggregate: EventSourcingLockingAggregate<C, *, E, V>,
    numberOfActors: Int = 100,
    actorsCapacity: Int = Channel.BUFFERED,
    actorsStart: CoroutineStart = CoroutineStart.LAZY,
    actorsContext: CoroutineContext = EmptyCoroutineContext,
    partitionKey: (C) -> Int
): Flow<Pair<E, V>> = aggregate.handleConcurrentlyAndOptimistically(
    this,
    numberOfActors,
    actorsCapacity,
    actorsStart,
    actorsContext
) { partitionKey(it) }

@ObsoleteCoroutinesApi
fun <C, E, V> Flow<Pair<C, Map<String, Any>>>.publishConcurrentlyAndOptimisticallyTo(
    aggregate: EventSourcingLockingAggregate<C, *, E, V>,
    numberOfActors: Int = 100,
    actorsCapacity: Int = Channel.BUFFERED,
    actorsStart: CoroutineStart = CoroutineStart.LAZY,
    actorsContext: CoroutineContext = EmptyCoroutineContext,
    partitionKey: (C, Map<String, Any>) -> Int
): Flow<Triple<E, V, Map<String, Any>>> = aggregate.handleConcurrentlyAndOptimistically(
    this,
    numberOfActors,
    actorsCapacity,
    actorsStart,
    actorsContext
) { cmd, meta -> partitionKey(cmd, meta) }

/**
 * Extension function - Handles the flow of command messages of type [C] by concurrently distributing the load across finite number of actors/handlers
 *
 * @param commands [Flow] of Command messages of type [C]
 * @param numberOfActors total number of actors/workers available for distributing the load. Minimum one.
 * @param actorsCapacity capacity of the actors channel's buffer
 * @param actorsStart actors coroutine start option
 * @param actorsContext additional to [CoroutineScope.coroutineContext] context of the actor coroutines.
 * @param partitionKey a function that calculates the partition key/routing key of command - commands with the same partition key will be handled with the same 'actor' to keep the ordering
 * @return [Flow] of stored Events of type [E] with version [V]
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
@ExperimentalCoroutinesApi
@ObsoleteCoroutinesApi
fun <C, S, E, V> EventSourcingLockingOrchestratingAggregate<C, S, E, V>.handleConcurrentlyAndOptimistically(
    commands: Flow<C>,
    numberOfActors: Int = 100,
    actorsCapacity: Int = Channel.BUFFERED,
    actorsStart: CoroutineStart = CoroutineStart.LAZY,
    actorsContext: CoroutineContext = EmptyCoroutineContext,
    partitionKey: (C) -> Int
): Flow<Pair<E, V>> = channelFlow {
    val actors: List<SendChannel<C>> = (1..numberOfActors).map {
        commandActor(channel, actorsCapacity, actorsStart, actorsContext) { handleOptimistically(it) }
    }
    commands
        .onCompletion {
            actors.forEach {
                it.close()
            }
        }
        .collect {
            val partition = partitionKey(it).absoluteValue % numberOfActors.coerceAtLeast(1)
            actors[partition].send(it)
        }
}

@ExperimentalCoroutinesApi
@ObsoleteCoroutinesApi
fun <C, S, E, V> EventSourcingLockingOrchestratingAggregate<C, S, E, V>.handleConcurrentlyAndOptimistically(
    commands: Flow<Pair<C, Map<String, Any>>>,
    numberOfActors: Int = 100,
    actorsCapacity: Int = Channel.BUFFERED,
    actorsStart: CoroutineStart = CoroutineStart.LAZY,
    actorsContext: CoroutineContext = EmptyCoroutineContext,
    partitionKey: (C, Map<String, Any>) -> Int
): Flow<Triple<E, V, Map<String, Any>>> = channelFlow {
    val actors: List<SendChannel<Pair<C, Map<String, Any>>>> = (1..numberOfActors).map {
        commandWithMetadataAndVersionActor(
            channel,
            actorsCapacity,
            actorsStart,
            actorsContext
        ) { cmd, meta -> handleOptimistically(cmd, meta) }
    }
    commands
        .onCompletion {
            actors.forEach {
                it.close()
            }
        }
        .collect {
            val partition = partitionKey(it.first, it.second).absoluteValue % numberOfActors.coerceAtLeast(1)
            actors[partition].send(it)
        }
}

/**
 * Extension function - Publishes [Flow] of commands of type [C] to the event sourcing aggregate of type  [EventSourcingAggregate]<[C], *, [E]> by concurrently distributing the load across finite number of actors
 *
 * @receiver [Flow] of commands of type [C]
 * @param aggregate of type [EventSourcingAggregate]<[C], *, [E]>
 * @param numberOfActors total number of actors/workers available for distributing the load. Minimum one.
 * @param actorsCapacity capacity of the actors channel's buffer
 * @param actorsStart actors coroutine start option
 * @param actorsContext additional to [CoroutineScope.coroutineContext] context of the actor coroutines.
 * @param partitionKey a function that calculates the partition key/routing key of command - commands with the same partition key will be handled with the same 'actor' to keep the ordering
 * @return the [Flow] of stored Events of type [E] and version [V]
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
@ExperimentalCoroutinesApi
@ObsoleteCoroutinesApi
fun <C, E, V> Flow<C>.publishConcurrentlyAndOptimisticallyTo(
    aggregate: EventSourcingLockingOrchestratingAggregate<C, *, E, V>,
    numberOfActors: Int = 100,
    actorsCapacity: Int = Channel.BUFFERED,
    actorsStart: CoroutineStart = CoroutineStart.LAZY,
    actorsContext: CoroutineContext = EmptyCoroutineContext,
    partitionKey: (C) -> Int
): Flow<Pair<E, V>> = aggregate.handleConcurrentlyAndOptimistically(
    this,
    numberOfActors,
    actorsCapacity,
    actorsStart,
    actorsContext
) { partitionKey(it) }

@ExperimentalCoroutinesApi
@ObsoleteCoroutinesApi
fun <C, E, V> Flow<Pair<C, Map<String, Any>>>.publishConcurrentlyAndOptimisticallyTo(
    aggregate: EventSourcingLockingOrchestratingAggregate<C, *, E, V>,
    numberOfActors: Int = 100,
    actorsCapacity: Int = Channel.BUFFERED,
    actorsStart: CoroutineStart = CoroutineStart.LAZY,
    actorsContext: CoroutineContext = EmptyCoroutineContext,
    partitionKey: (C, Map<String, Any>) -> Int
): Flow<Triple<E, V, Map<String, Any>>> = aggregate.handleConcurrentlyAndOptimistically(
    this,
    numberOfActors,
    actorsCapacity,
    actorsStart,
    actorsContext
) { cmd, meta -> partitionKey(cmd, meta) }

/**
 * Command Actor - Event Sourced Aggregate
 *
 * @param fanInChannel A reference to the channel this coroutine/actor sends events to
 * @param handle A function that handles command and returns new events.
 * @receiver CoroutineScope
 */
@ObsoleteCoroutinesApi
private fun <C, E> CoroutineScope.commandActor(
    fanInChannel: SendChannel<E>,
    capacity: Int = Channel.RENDEZVOUS,
    start: CoroutineStart = CoroutineStart.DEFAULT,
    context: CoroutineContext = EmptyCoroutineContext,
    handle: (C) -> Flow<E>
) = actor<C>(context, capacity, start) {
    for (msg in channel) {
        handle(msg).collect { fanInChannel.send(it) }
    }
}

@ObsoleteCoroutinesApi
private fun <C, E> CoroutineScope.commandWithMetadataActor(
    fanInChannel: SendChannel<Pair<E, Map<String, Any>>>,
    capacity: Int = Channel.RENDEZVOUS,
    start: CoroutineStart = CoroutineStart.DEFAULT,
    context: CoroutineContext = EmptyCoroutineContext,
    handle: (C, Map<String, Any>) -> Flow<Pair<E, Map<String, Any>>>
) = actor<Pair<C, Map<String, Any>>>(context, capacity, start) {
    for (msg in channel) {
        handle(msg.first, msg.second).collect { fanInChannel.send(it) }
    }
}

@ObsoleteCoroutinesApi
private fun <C, E, V> CoroutineScope.commandWithMetadataAndVersionActor(
    fanInChannel: SendChannel<Triple<E, V, Map<String, Any>>>,
    capacity: Int = Channel.RENDEZVOUS,
    start: CoroutineStart = CoroutineStart.DEFAULT,
    context: CoroutineContext = EmptyCoroutineContext,
    handle: (C, Map<String, Any>) -> Flow<Triple<E, V, Map<String, Any>>>
) = actor<Pair<C, Map<String, Any>>>(context, capacity, start) {
    for (msg in channel) {
        handle(msg.first, msg.second).collect { fanInChannel.send(it) }
    }
}