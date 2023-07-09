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
 * Extension function - Handles the flow of events of type [E] by concurrently distributing the load across finite number of actors/handlers
 *
 * @param events Flow of Events of type [E] to be handled
 * @param numberOfActors total number of actors/workers available for distributing the load. Minimum one.
 * @param actorsCapacity capacity of the actors channel's buffer
 * @param actorsStart actors coroutine start option
 * @param actorsContext additional to [CoroutineScope.coroutineContext] context of the actor coroutines.
 * @param partitionKey a function that calculates the partition key/routing key of event - events with the same partition key will be handled with the same 'actor' to keep the ordering
 * @return [Flow] of State of type [S]
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
@ObsoleteCoroutinesApi
fun <S, E, I> I.handleConcurrently(
    events: Flow<E>,
    numberOfActors: Int = 100,
    actorsCapacity: Int = Channel.BUFFERED,
    actorsStart: CoroutineStart = CoroutineStart.LAZY,
    actorsContext: CoroutineContext = EmptyCoroutineContext,
    partitionKey: (E) -> Int
): Flow<S> where I : ViewStateComputation<S, E>, I : ViewStateRepository<E, S> = channelFlow {
    val actors: List<SendChannel<E>> = (1..numberOfActors).map {
        eventActor(channel, actorsCapacity, actorsStart, actorsContext) { handle(it) }
    }
    events
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
 * Extension function - Handles the flow of events of type [E] by concurrently distributing the load across finite number of actors/handlers
 *
 * @param events Flow of Events of type [E] to be handled
 * @param numberOfActors total number of actors/workers available for distributing the load. Minimum one.
 * @param actorsCapacity capacity of the actors channel's buffer
 * @param actorsStart actors coroutine start option
 * @param actorsContext additional to [CoroutineScope.coroutineContext] context of the actor coroutines.
 * @param partitionKey a function that calculates the partition key/routing key of event - events with the same partition key will be handled with the same 'actor' to keep the ordering
 * @return [Flow] of State of type `Pair<S, V>`
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
@ObsoleteCoroutinesApi
fun <S, E, V, I> I.handleConcurrentlyAndOptimistically(
    events: Flow<E>,
    numberOfActors: Int = 100,
    actorsCapacity: Int = Channel.BUFFERED,
    actorsStart: CoroutineStart = CoroutineStart.LAZY,
    actorsContext: CoroutineContext = EmptyCoroutineContext,
    partitionKey: (E) -> Int
): Flow<Pair<S, V>> where I : ViewStateComputation<S, E>, I : ViewStateLockingRepository<E, S, V> = channelFlow {
    val actors: List<SendChannel<E>> = (1..numberOfActors).map {
        eventActor(channel, actorsCapacity, actorsStart, actorsContext) { handleOptimistically(it) }
    }
    events
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
 * Extension function - Publishes the event of type [E] to the materialized view of type  [MaterializedView]<[S], [E]> by concurrently distributing the load across finite number of actors/handlers
 *
 * @receiver [Flow] of events of type [E]
 * @param materializedView of type  [MaterializedView]<[S], [E]>
 * @param numberOfActors total number of actors/workers available for distributing the load. Minimum one.
 * @param actorsCapacity capacity of the actors channel's buffer
 * @param actorsStart actors coroutine start option
 * @param actorsContext additional to [CoroutineScope.coroutineContext] context of the actor coroutines.
 * @param partitionKey a function that calculates the partition key/routing key of event - events with the same partition key will be handled with the same 'actor' to keep the ordering
 * @return the [Flow] of stored State of type [S]
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
@ObsoleteCoroutinesApi
fun <S, E, M> Flow<E>.publishConcurrentlyTo(
    materializedView: M,
    numberOfActors: Int = 100,
    actorsCapacity: Int = Channel.BUFFERED,
    actorsStart: CoroutineStart = CoroutineStart.LAZY,
    actorsContext: CoroutineContext = EmptyCoroutineContext,
    partitionKey: (E) -> Int
): Flow<S> where M : ViewStateComputation<S, E>, M : ViewStateRepository<E, S> = materializedView.handleConcurrently(
    this,
    numberOfActors,
    actorsCapacity,
    actorsStart,
    actorsContext
) { partitionKey(it) }

/**
 * Extension function - Publishes the event of type [E] to the materialized view of type  [MaterializedView]<[S], [E]> by concurrently distributing the load across finite number of actors/handlers
 *
 * @receiver [Flow] of events of type [E]
 * @param materializedView of type  [MaterializedView]<[S], [E]>
 * @param numberOfActors total number of actors/workers available for distributing the load. Minimum one.
 * @param actorsCapacity capacity of the actors channel's buffer
 * @param actorsStart actors coroutine start option
 * @param actorsContext additional to [CoroutineScope.coroutineContext] context of the actor coroutines.
 * @param partitionKey a function that calculates the partition key/routing key of event - events with the same partition key will be handled with the same 'actor' to keep the ordering
 * @return [Flow] of State of type `Pair<S, V>`
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
@ObsoleteCoroutinesApi
fun <S, E, V, M> Flow<E>.publishConcurrentlyAndOptimisticallyTo(
    materializedView: M,
    numberOfActors: Int = 100,
    actorsCapacity: Int = Channel.BUFFERED,
    actorsStart: CoroutineStart = CoroutineStart.LAZY,
    actorsContext: CoroutineContext = EmptyCoroutineContext,
    partitionKey: (E) -> Int
): Flow<Pair<S, V>> where M : ViewStateComputation<S, E>, M : ViewStateLockingRepository<E, S, V> =
    materializedView.handleConcurrentlyAndOptimistically(
        this,
        numberOfActors,
        actorsCapacity,
        actorsStart,
        actorsContext
    ) { partitionKey(it) }

/**
 * Event Actor - Materialized View
 *
 * @param fanInChannel A reference to the channel this coroutine/actor sends state to
 * @param handle A function that handles events and returns new state.
 * @receiver CoroutineScope
 */
@ObsoleteCoroutinesApi
private fun <E, S> CoroutineScope.eventActor(
    fanInChannel: SendChannel<S>,
    capacity: Int = Channel.RENDEZVOUS,
    start: CoroutineStart = CoroutineStart.DEFAULT,
    context: CoroutineContext = EmptyCoroutineContext,
    handle: suspend (E) -> S
) = actor<E>(context, capacity, start) {
    for (msg in channel) {
        fanInChannel.send(handle(msg))
    }
}
