package com.fraktalio.fmodel.application

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.actor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.onCompletion
import kotlin.contracts.ExperimentalContracts
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

/**
 * Extension function - Handles the flow of command messages of type [C] by concurrently distributing the load across finite number of actors/handlers
 *
 * @param commands [Flow] of Command messages of type [C]
 * @param numberOfActors total number of actors/workers available for distributing the load
 * @param actorsCapacity capacity of the actors channel's buffer
 * @param actorsStart actors coroutine start option
 * @param actorsContext additional to [CoroutineScope.coroutineContext] context of the actor coroutines.
 * @param partitionKey a function that calculates the partition key/routing key of command - commands with the same partition key will be handled wit the same actor to keep the ordering
 * @return [Flow] of stored Events of type [E]
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
@ExperimentalContracts
@FlowPreview
fun <C, S, E> EventSourcingAggregate<C, S, E>.handleConcurrently(
    commands: Flow<C>,
    numberOfActors: Int = 100,
    actorsCapacity: Int = Channel.BUFFERED,
    actorsStart: CoroutineStart = CoroutineStart.LAZY,
    actorsContext: CoroutineContext = EmptyCoroutineContext,
    partitionKey: (C) -> Int
): Flow<E> = channelFlow {
    val actors: List<SendChannel<C>> = (1..numberOfActors).map {
        commandActor(channel, actorsCapacity, actorsStart, actorsContext) { command, channel ->
            handle(command).collect { channel.send(it) }
        }
    }
    commands
        .onCompletion {
            actors.forEach {
                it.close()
            }
        }
        .collect {
            val partitionKeyVal = partitionKey(it)
            val partition = (if (partitionKeyVal < 0) partitionKeyVal * (-1) else partitionKeyVal) % numberOfActors
            actors[partition].send(it)
        }
}

/**
 * Extension function - Publishes [Flow] of commands of type [C] to the event sourcing aggregate of type  [EventSourcingAggregate]<[C], *, [E]> by concurrently distributing the load across finite number of actors
 *
 * @receiver [Flow] of commands of type [C]
 * @param aggregate of type [EventSourcingAggregate]<[C], *, [E]>
 * @param numberOfActors total number of actors/workers available for distributing the load
 * @param actorsCapacity capacity of the actors channel's buffer
 * @param actorsStart actors coroutine start option
 * @param actorsContext additional to [CoroutineScope.coroutineContext] context of the actor coroutines.
 * @param partitionKey a function that calculates the partition key/routing key of command - commands with the same partition key will be handled wit the same actor to keep the ordering
 * @return the [Flow] of stored Events of type [E]
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
@ExperimentalContracts
@FlowPreview
fun <C, E> Flow<C>.publishConcurrentlyTo(
    aggregate: EventSourcingAggregate<C, *, E>,
    numberOfActors: Int = 100,
    actorsCapacity: Int = Channel.BUFFERED,
    actorsStart: CoroutineStart = CoroutineStart.LAZY,
    actorsContext: CoroutineContext = EmptyCoroutineContext,
    partitionKey: (C) -> Int
): Flow<E> =
    aggregate.handleConcurrently(this, numberOfActors, actorsCapacity, actorsStart, actorsContext) { partitionKey(it) }


/**
 * Command Actor - Event Sourced Aggregate
 *
 * @param fanInChannel A reference to the channel this coroutine/actor sends elements/events to
 * @param handle A function that handles command and sends responses/events to [fanInChannel]
 * @receiver CoroutineScope
 */
@ObsoleteCoroutinesApi
private fun <C, E> CoroutineScope.commandActor(
    fanInChannel: SendChannel<E>,
    capacity: Int = Channel.RENDEZVOUS,
    start: CoroutineStart = CoroutineStart.DEFAULT,
    context: CoroutineContext = EmptyCoroutineContext,
    handle: suspend (C, SendChannel<E>) -> Unit
) = actor<C>(context, capacity, start) {
    for (msg in channel) {
        handle(msg, fanInChannel)
    }
}
