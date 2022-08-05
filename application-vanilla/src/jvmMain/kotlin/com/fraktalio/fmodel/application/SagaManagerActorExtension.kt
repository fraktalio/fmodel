/*
 * Copyright (c) 2021 Fraktalio D.O.O. All rights reserved.
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
 * Extension function - Handles the the [Flow] of action results of type [AR] by concurrently distributing the load across finite number of actors/handlers
 *
 * @param actionResults Action Results represent the outcome of some action you want to handle in some way
 * @param numberOfActors total number of actors/workers available for distributing the load. Minimum one.
 * @param actorsCapacity capacity of the actors channel's buffer
 * @param actorsStart actors coroutine start option
 * @param actorsContext additional to [CoroutineScope.coroutineContext] context of the actor coroutines.
 * @param partitionKey a function that calculates the partition key/routing key of the Action Result - Action Results with the same partition key will be handled with the same 'actor' to keep the ordering
 * @return [Flow] of Actions of type [A]
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
@ExperimentalContracts
@FlowPreview
fun <AR, A> SagaManager<AR, A>.handleConcurrently(
    actionResults: Flow<AR>,
    numberOfActors: Int = 100,
    actorsCapacity: Int = Channel.BUFFERED,
    actorsStart: CoroutineStart = CoroutineStart.LAZY,
    actorsContext: CoroutineContext = EmptyCoroutineContext,
    partitionKey: (AR) -> Int
): Flow<A> = channelFlow {
    val actors: List<SendChannel<AR>> = (1..numberOfActors).map {
        sagaActor(channel, actorsCapacity, actorsStart, actorsContext) { handle(it) }
    }
    actionResults
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
 * Extension function - Publishes the action result of type [AR] to the saga manager of type  [SagaManager]<[AR], [A]> by concurrently distributing the load across finite number of actors/handlers
 * @receiver [Flow] of action results of type [AR]
 * @param sagaManager of type [SagaManager]<[AR], [A]>
 * @param numberOfActors total number of actors/workers available for distributing the load. Minimum one.
 * @param actorsCapacity capacity of the actors channel's buffer
 * @param actorsStart actors coroutine start option
 * @param actorsContext additional to [CoroutineScope.coroutineContext] context of the actor coroutines.
 * @param partitionKey a function that calculates the partition key/routing key of the Action Result - Action Results with the same partition key will be handled with the same 'actor' to keep the ordering
 * @return the [Flow] of published Actions of type [A]
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
@ExperimentalContracts
@FlowPreview
fun <AR, A> Flow<AR>.publishConcurrentlyTo(
    sagaManager: SagaManager<AR, A>,
    numberOfActors: Int = 100,
    actorsCapacity: Int = Channel.BUFFERED,
    actorsStart: CoroutineStart = CoroutineStart.LAZY,
    actorsContext: CoroutineContext = EmptyCoroutineContext,
    partitionKey: (AR) -> Int
): Flow<A> = sagaManager.handleConcurrently(
    this,
    numberOfActors,
    actorsCapacity,
    actorsStart,
    actorsContext
) { partitionKey(it) }

/**
 * Saga Actor
 *
 * @param fanInChannel A reference to the channel this coroutine/actor sends actions to
 * @param handle A function that handles action-result and returns actions.
 * @receiver CoroutineScope
 */
@ObsoleteCoroutinesApi
private fun <AR, A> CoroutineScope.sagaActor(
    fanInChannel: SendChannel<A>,
    capacity: Int = Channel.RENDEZVOUS,
    start: CoroutineStart = CoroutineStart.DEFAULT,
    context: CoroutineContext = EmptyCoroutineContext,
    handle: (AR) -> Flow<A>
) = actor<AR>(context, capacity, start) {
    for (msg in channel) {
        handle(msg).collect { fanInChannel.send(it) }
    }
}
