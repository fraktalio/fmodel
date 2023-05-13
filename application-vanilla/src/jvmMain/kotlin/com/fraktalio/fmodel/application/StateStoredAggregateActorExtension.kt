/*
 * Copyright (c) 2023 Fraktalio D.O.O. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
import kotlin.math.absoluteValue

/**
 * Extension function - Handles the [Flow] of command messages of type [C] by concurrently distributing the load across finite number of actors/handlers
 *
 * @param commands [Flow] of Command messages of type [C]
 * @param numberOfActors total number of actors/workers available for distributing the load. Minimum one.
 * @param actorsCapacity capacity of the actors channel's buffer
 * @param actorsStart actors coroutine start option
 * @param actorsContext additional to [CoroutineScope.coroutineContext] context of the actor coroutines.
 * @param partitionKey a function that calculates the partition key/routing key of command - commands with the same partition key will be handled with the same 'actor' to keep the ordering
 * @return [Flow] of State of type [S]
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
@ObsoleteCoroutinesApi
@ExperimentalContracts
@FlowPreview
fun <C, S, E> StateStoredAggregate<C, S, E>.handleConcurrently(
    commands: Flow<C>,
    numberOfActors: Int = 100,
    actorsCapacity: Int = Channel.BUFFERED,
    actorsStart: CoroutineStart = CoroutineStart.LAZY,
    actorsContext: CoroutineContext = EmptyCoroutineContext,
    partitionKey: (C) -> Int
): Flow<S> = channelFlow {
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

/**
 * Extension function - Publishes the command of type [C] to the state stored aggregate of type  [StateStoredAggregate]<[C], [S], *> by concurrently distributing the load across finite number of actors/handlers
 * @receiver [Flow] of commands of type [C]
 * @param aggregate of type [StateStoredAggregate]<[C], [S], *>
 * @param numberOfActors total number of actors/workers available for distributing the load. Minimum one.
 * @param actorsCapacity capacity of the actors channel's buffer
 * @param actorsStart actors coroutine start option
 * @param actorsContext additional to [CoroutineScope.coroutineContext] context of the actor coroutines.
 * @param partitionKey a function that calculates the partition key/routing key of command - commands with the same partition key will be handled with the same 'actor' to keep the ordering
 * @return the [Flow] of State of type [S]
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
@ObsoleteCoroutinesApi
@ExperimentalContracts
@FlowPreview
fun <C, S> Flow<C>.publishConcurrentlyTo(
    aggregate: StateStoredAggregate<C, S, *>,
    numberOfActors: Int = 100,
    actorsCapacity: Int = Channel.BUFFERED,
    actorsStart: CoroutineStart = CoroutineStart.LAZY,
    actorsContext: CoroutineContext = EmptyCoroutineContext,
    partitionKey: (C) -> Int
): Flow<S> =
    aggregate.handleConcurrently(this, numberOfActors, actorsCapacity, actorsStart, actorsContext) { partitionKey(it) }


/**
 * Command Actor - State Stored Aggregate
 *
 * @param fanInChannel A reference to the channel this coroutine/actor sends state to
 * @param handle A function that handles commands and returns new state.
 * @receiver CoroutineScope
 */
@ObsoleteCoroutinesApi
private fun <C, S> CoroutineScope.commandActor(
    fanInChannel: SendChannel<S>,
    capacity: Int = Channel.RENDEZVOUS,
    start: CoroutineStart = CoroutineStart.DEFAULT,
    context: CoroutineContext = EmptyCoroutineContext,
    handle: suspend (C) -> S
) = actor(context, capacity, start) {
    for (msg in channel) {
        fanInChannel.send(handle(msg))
    }
}
