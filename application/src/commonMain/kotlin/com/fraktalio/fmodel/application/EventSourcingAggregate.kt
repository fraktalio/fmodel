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

import com.fraktalio.fmodel.domain.IDecider
import com.fraktalio.fmodel.domain.ISaga
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*

/**
 * `EventComputation` interface formalizes the `Event Computation` algorithm / event sourced system by using a `decider` of type [IDecider]<[C], [S], [E]> to handle commands based on the current events, and produce new events.
 */
interface EventComputation<C, S, E> : IDecider<C, S, E> {
    @FlowPreview
    fun Flow<E>.computeNewEvents(command: C): Flow<E> = flow {
        val currentState = fold(initialState) { s, e -> evolve(s, e) }
        val resultingEvents = decide(command, currentState)
        emitAll(resultingEvents)
    }
}

/**
 * `EventOrchestratingComputation` interface formalizes the `Event Computation` algorithm / event sourced system by using a `decider` of type [IDecider]<[C], [S], [E]> to handle commands based on the current events, and produce new events.
 * Additionally, the computation is using the [ISaga]<[E], [C]> to further orchestrate the process in were Decider could be triggered by a new Command.
 */
interface EventOrchestratingComputation<C, S, E> : ISaga<E, C>, IDecider<C, S, E> {
    @FlowPreview
    fun Flow<E>.computeNewEventsByOrchestrating(command: C, fetchEvents: (C) -> Flow<E>): Flow<E> = flow {
        val currentState = fold(initialState) { s, e -> evolve(s, e) }
        var resultingEvents = decide(command, currentState)

        resultingEvents
            .flatMapConcat { react(it) }
            .onEach { c ->
                val newEvents = flowOf(fetchEvents(c), resultingEvents)
                    .flattenConcat()
                    .computeNewEventsByOrchestrating(c, fetchEvents)
                resultingEvents = flowOf(resultingEvents, newEvents).flattenConcat()
            }.collect()

        emitAll(resultingEvents)
    }
}

/**
 * Event sourcing aggregate is using/delegating a `decider` of type [IDecider]<[C], [S], [E]>/ [EventComputation]<[C], [S], [E]> to handle commands and produce events.
 * In order to handle the command, aggregate needs to fetch the current state (represented as a list of events) via [EventRepository.fetchEvents] function, and then delegate the command to the `decider` which can produce new event(s) as a result.
 * Produced events are then stored via [EventRepository.save] suspending function.
 *
 * [EventSourcingAggregate] extends [EventComputation] and [EventRepository] interfaces,
 * clearly communicating that it is composed out of these two behaviours.
 *
 * @param C Commands of type [C] that this aggregate can handle
 * @param S Aggregate state of type [S]
 * @param E Events of type [E] that this aggregate can publish
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
interface EventSourcingAggregate<C, S, E> : EventComputation<C, S, E>, EventRepository<C, E>

/**
 * Locking Event sourcing aggregate is using/delegating a `decider` of type [IDecider]<[C], [S], [E]>/ [EventComputation]<[C], [S], [E]> to handle commands and produce events.
 * In order to handle the command, aggregate needs to fetch the current state (represented as a list of events) via [EventLockingRepository.fetchEvents] function, and then delegate the command to the `decider` which can produce new event(s) as a result.
 * Produced events are then stored via [EventLockingRepository.save] suspending function.
 *
 * Locking Event sourcing aggregate enables `optimistic locking` mechanism more explicitly.
 * If you fetch events from a storage, the application records the `version` number of that event stream.
 * You can append new events, but only if the `version` number in the storage has not changed.
 * If there is a `version` mismatch, it means that someone else has added the event(s) before you did.
 *
 * [EventSourcingLockingAggregate] extends [EventComputation] and [EventLockingRepository] interfaces,
 * clearly communicating that it is composed out of these two behaviours.
 *
 * @param C Commands of type [C] that this aggregate can handle
 * @param S Aggregate state of type [S]
 * @param E Events of type [E] that this aggregate can publish
 * @param V Version
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
interface EventSourcingLockingAggregate<C, S, E, V> : EventComputation<C, S, E>, EventLockingRepository<C, E, V>

/**
 * Orchestrating Event sourcing aggregate is using/delegating a `decider` of type [IDecider]<[C], [S], [E]> to handle commands and produce events.
 * In order to handle the command, aggregate needs to fetch the current state (represented as a list of events) via [EventRepository.fetchEvents] function, and then delegate the command to the `decider` which can produce new event(s) as a result.
 * If the `decider` is combined out of many deciders via `combine` function, an optional `saga` of type [ISaga] could be used to react on new events and send new commands to the 'decider` recursively, in single transaction.
 * This behaviour is formalized in [EventOrchestratingComputation].
 * Produced events are then stored via [EventRepository.save] suspending function.
 *
 * [EventSourcingOrchestratingAggregate] extends [EventOrchestratingComputation] and [EventRepository] interfaces,
 * clearly communicating that it is composed out of these two behaviours.
 *
 * @param C Commands of type [C] that this aggregate can handle
 * @param S Aggregate state of type [S]
 * @param E Events of type [E] that this aggregate can publish
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
interface EventSourcingOrchestratingAggregate<C, S, E> : EventOrchestratingComputation<C, S, E>, EventRepository<C, E>

/**
 * Orchestrating Locking Event sourcing aggregate is using/delegating a `decider` of type [IDecider]<[C], [S], [E]> to handle commands and produce events.
 * In order to handle the command, aggregate needs to fetch the current state (represented as a list of events) via [EventLockingRepository.fetchEvents] function, and then delegate the command to the `decider` which can produce new event(s) as a result.
 * If the `decider` is combined out of many deciders via `combine` function, an optional `saga` of type [ISaga] could be used to react on new events and send new commands to the 'decider` recursively, in single transaction.
 * This behaviour is formalized in [EventOrchestratingComputation].
 * Produced events are then stored via [EventLockingRepository.save] suspending function.
 *
 * Locking Orchestrating Event sourcing aggregate enables `optimistic locking` mechanism more explicitly.
 * If you fetch events from a storage, the application records the `version` number of that event stream.
 * You can append new events, but only if the `version` number in the storage has not changed.
 * If there is a `version` mismatch, it means that someone else has added the event(s) before you did.
 *
 * [EventSourcingLockingOrchestratingAggregate] extends [EventOrchestratingComputation] and [EventLockingRepository] interfaces,
 * clearly communicating that it is composed out of these two behaviours.
 *
 * @param C Commands of type [C] that this aggregate can handle
 * @param S Aggregate state of type [S]
 * @param E Events of type [E] that this aggregate can publish
 * @param V Version
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
interface EventSourcingLockingOrchestratingAggregate<C, S, E, V> : EventOrchestratingComputation<C, S, E>,
    EventLockingRepository<C, E, V>


/**
 * Event Sourced aggregate factory function.
 *
 * The Delegation pattern has proven to be a good alternative to implementation inheritance, and Kotlin supports it natively requiring zero boilerplate code.
 *
 * @param C Commands of type [C] that this aggregate can handle
 * @param S Aggregate state of type [S]
 * @param E Events of type [E] that are used internally to build/fold new state
 * @param decider A decider component of type [IDecider]<[C], [S], [E]>
 * @param eventRepository An aggregate event repository of type [EventRepository]<[C], [E]>
 * @return An object/instance of type [EventSourcingAggregate]<[C], [S], [E]>
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
@Deprecated(
    message = "Use EventSourcingAggregate constructor-like function instead",
    replaceWith = ReplaceWith("EventSourcingAggregate(decider, eventRepository)")
)
fun <C, S, E> eventSourcingAggregate(
    decider: IDecider<C, S, E>,
    eventRepository: EventRepository<C, E>
): EventSourcingAggregate<C, S, E> =
    object : EventSourcingAggregate<C, S, E>,
        EventRepository<C, E> by eventRepository,
        IDecider<C, S, E> by decider {}

/**
 * Event Sourced aggregate constructor-like function.
 *
 * The Delegation pattern has proven to be a good alternative to implementation inheritance, and Kotlin supports it natively requiring zero boilerplate code.
 *
 * @param C Commands of type [C] that this aggregate can handle
 * @param S Aggregate state of type [S]
 * @param E Events of type [E] that are used internally to build/fold new state
 * @param decider A decider component of type [IDecider]<[C], [S], [E]>
 * @param eventRepository An aggregate event repository of type [EventRepository]<[C], [E]>
 * @return An object/instance of type [EventSourcingAggregate]<[C], [S], [E]>
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
fun <C, S, E> EventSourcingAggregate(
    decider: IDecider<C, S, E>,
    eventRepository: EventRepository<C, E>
): EventSourcingAggregate<C, S, E> =
    object : EventSourcingAggregate<C, S, E>,
        EventRepository<C, E> by eventRepository,
        IDecider<C, S, E> by decider {}

/**
 * Event Sourced Locking aggregate factory function.
 *
 * The Delegation pattern has proven to be a good alternative to implementation inheritance, and Kotlin supports it natively requiring zero boilerplate code.
 *
 * @param C Commands of type [C] that this aggregate can handle
 * @param S Aggregate state of type [S]
 * @param E Events of type [E] that are used internally to build/fold new state
 * @param V Version
 * @param decider A decider component of type [IDecider]<[C], [S], [E]>
 * @param eventRepository An aggregate event repository of type [EventLockingRepository]<[C], [E], [V]>
 * @return An object/instance of type [EventSourcingLockingAggregate]<[C], [S], [E], [V]>
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
@Deprecated(
    message = "Use EventSourcingLockingAggregate constructor-like function instead",
    replaceWith = ReplaceWith("EventSourcingLockingAggregate(decider, eventRepository)")
)
fun <C, S, E, V> eventSourcingLockingAggregate(
    decider: IDecider<C, S, E>,
    eventRepository: EventLockingRepository<C, E, V>
): EventSourcingLockingAggregate<C, S, E, V> =
    object : EventSourcingLockingAggregate<C, S, E, V>,
        EventLockingRepository<C, E, V> by eventRepository,
        IDecider<C, S, E> by decider {}

/**
 * Event Sourced Locking aggregate constructor-like function.
 *
 * The Delegation pattern has proven to be a good alternative to implementation inheritance, and Kotlin supports it natively requiring zero boilerplate code.
 *
 * @param C Commands of type [C] that this aggregate can handle
 * @param S Aggregate state of type [S]
 * @param E Events of type [E] that are used internally to build/fold new state
 * @param V Version
 * @param decider A decider component of type [IDecider]<[C], [S], [E]>
 * @param eventRepository An aggregate event repository of type [EventLockingRepository]<[C], [E], [V]>
 * @return An object/instance of type [EventSourcingLockingAggregate]<[C], [S], [E], [V]>
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
fun <C, S, E, V> EventSourcingLockingAggregate(
    decider: IDecider<C, S, E>,
    eventRepository: EventLockingRepository<C, E, V>
): EventSourcingLockingAggregate<C, S, E, V> =
    object : EventSourcingLockingAggregate<C, S, E, V>,
        EventLockingRepository<C, E, V> by eventRepository,
        IDecider<C, S, E> by decider {}

/**
 * Event Sourced Orchestrating aggregate factory function.
 *
 * The Delegation pattern has proven to be a good alternative to implementation inheritance, and Kotlin supports it natively requiring zero boilerplate code.
 *
 * @param C Commands of type [C] that this aggregate can handle
 * @param S Aggregate state of type [S]
 * @param E Events of type [E] that are used internally to build/fold new state
 * @param decider A decider component of type [IDecider]<[C], [S], [E]>
 * @param eventRepository An aggregate event repository of type [EventRepository]<[C], [E]>
 * @param saga A saga component of type [ISaga]<[E], [C]> - orchestrates the deciders
 * @return An object/instance of type [EventSourcingOrchestratingAggregate]<[C], [S], [E]>
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
@Deprecated(
    message = "Use EventSourcingOrchestratingAggregate constructor-like function instead",
    replaceWith = ReplaceWith("EventSourcingOrchestratingAggregate(decider, eventRepository, saga)")
)
fun <C, S, E> eventSourcingOrchestratingAggregate(
    decider: IDecider<C, S, E>,
    eventRepository: EventRepository<C, E>,
    saga: ISaga<E, C>
): EventSourcingOrchestratingAggregate<C, S, E> =
    object : EventSourcingOrchestratingAggregate<C, S, E>,
        EventRepository<C, E> by eventRepository,
        IDecider<C, S, E> by decider,
        ISaga<E, C> by saga {}

/**
 * Event Sourced Orchestrating aggregate constructor-like function.
 *
 * The Delegation pattern has proven to be a good alternative to implementation inheritance, and Kotlin supports it natively requiring zero boilerplate code.
 *
 * @param C Commands of type [C] that this aggregate can handle
 * @param S Aggregate state of type [S]
 * @param E Events of type [E] that are used internally to build/fold new state
 * @param decider A decider component of type [IDecider]<[C], [S], [E]>
 * @param eventRepository An aggregate event repository of type [EventRepository]<[C], [E]>
 * @param saga A saga component of type [ISaga]<[E], [C]> - orchestrates the deciders
 * @return An object/instance of type [EventSourcingOrchestratingAggregate]<[C], [S], [E]>
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
fun <C, S, E> EventSourcingOrchestratingAggregate(
    decider: IDecider<C, S, E>,
    eventRepository: EventRepository<C, E>,
    saga: ISaga<E, C>
): EventSourcingOrchestratingAggregate<C, S, E> =
    object : EventSourcingOrchestratingAggregate<C, S, E>,
        EventRepository<C, E> by eventRepository,
        IDecider<C, S, E> by decider,
        ISaga<E, C> by saga {}

/**
 * Event Sourced Locking Orchestrating aggregate factory function.
 *
 * The Delegation pattern has proven to be a good alternative to implementation inheritance, and Kotlin supports it natively requiring zero boilerplate code.
 *
 * @param C Commands of type [C] that this aggregate can handle
 * @param S Aggregate state of type [S]
 * @param E Events of type [E] that are used internally to build/fold new state
 * @param V Version
 * @param decider A decider component of type [IDecider]<[C], [S], [E]>
 * @param eventRepository An aggregate event repository of type [EventLockingRepository]<[C], [E], [V]>
 * @param saga A saga component of type [ISaga]<[E], [C]> - orchestrates the deciders
 * @return An object/instance of type [EventSourcingLockingOrchestratingAggregate]<[C], [S], [E] ,[V]>
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
@Deprecated(
    message = "Use EventSourcingLockingOrchestratingAggregate constructor-like function instead",
    replaceWith = ReplaceWith("EventSourcingLockingOrchestratingAggregate(decider, eventRepository, saga)")
)
fun <C, S, E, V> eventSourcingLockingOrchestratingAggregate(
    decider: IDecider<C, S, E>,
    eventRepository: EventLockingRepository<C, E, V>,
    saga: ISaga<E, C>
): EventSourcingLockingOrchestratingAggregate<C, S, E, V> =
    object : EventSourcingLockingOrchestratingAggregate<C, S, E, V>,
        EventLockingRepository<C, E, V> by eventRepository,
        IDecider<C, S, E> by decider,
        ISaga<E, C> by saga {}

/**
 * Event Sourced Locking Orchestrating aggregate constructor-like function.
 *
 * The Delegation pattern has proven to be a good alternative to implementation inheritance, and Kotlin supports it natively requiring zero boilerplate code.
 *
 * @param C Commands of type [C] that this aggregate can handle
 * @param S Aggregate state of type [S]
 * @param E Events of type [E] that are used internally to build/fold new state
 * @param V Version
 * @param decider A decider component of type [IDecider]<[C], [S], [E]>
 * @param eventRepository An aggregate event repository of type [EventLockingRepository]<[C], [E], [V]>
 * @param saga A saga component of type [ISaga]<[E], [C]> - orchestrates the deciders
 * @return An object/instance of type [EventSourcingLockingOrchestratingAggregate]<[C], [S], [E] ,[V]>
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */

fun <C, S, E, V> EventSourcingLockingOrchestratingAggregate(
    decider: IDecider<C, S, E>,
    eventRepository: EventLockingRepository<C, E, V>,
    saga: ISaga<E, C>
): EventSourcingLockingOrchestratingAggregate<C, S, E, V> =
    object : EventSourcingLockingOrchestratingAggregate<C, S, E, V>,
        EventLockingRepository<C, E, V> by eventRepository,
        IDecider<C, S, E> by decider,
        ISaga<E, C> by saga {}