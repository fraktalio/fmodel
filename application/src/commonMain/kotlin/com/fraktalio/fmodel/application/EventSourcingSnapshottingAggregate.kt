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


/**
 * Event sourced, snapshotting aggregate is using/delegating a `decider` of type [IDecider]<[C], [S], [E]>/ [EventComputation]<[C], [S], [E]> to handle commands and produce events.
 * In order to handle the command, aggregate needs to fetch the current state (represented as a list of events) via [EventSnapshottingRepository.fetchEvents] function, and then delegate the command to the `decider` which can produce new event(s) as a result.
 * Produced events are then stored via [EventSnapshottingRepository.save] suspending function.
 *
 * Additionally, Event sourcing aggregate enables `snapshotting` mechanism by using [StateComputation] and [StateRepository] interfaces to store and fetch the current state of the aggregate from time to time, removing the need to always fetch the full list of events.
 *
 * [EventSourcingSnapshottingAggregate] extends [EventComputation], [StateComputation], [StateRepository] and [EventSnapshottingRepository] interfaces,
 * clearly communicating that it is composed out of these behaviours.
 *
 * @param C Commands of type [C] that this aggregate can handle
 * @param S Aggregate state of type [S]
 * @param E Events of type [E] that this aggregate can publish
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
interface EventSourcingSnapshottingAggregate<C, S, E> :
    EventComputation<C, S, E>,
    StateComputation<C, S, E>,
    EventSnapshottingRepository<C, S, E>,
    StateRepository<C, S>

/**
 * Event sourced, snapshotting, locking aggregate is using/delegating a `decider` of type [IDecider]<[C], [S], [E]>/ [EventComputation]<[C], [S], [E]> to handle commands and produce events.
 * In order to handle the command, aggregate needs to fetch the current state (represented as a list of events) via [EventSnapshottingLockingRepository.fetchEvents] function, and then delegate the command to the `decider` which can produce new event(s) as a result.
 * Produced events are then stored via [EventSnapshottingLockingRepository.save] suspending function.
 *
 * Additionally, Event sourcing aggregate enables `snapshotting` mechanism by using [StateComputation] and [StateRepository] interfaces to store and fetch the current state of the aggregate from time to time, removing the need to always fetch the full list of events.
 *
 * Locking Event sourcing aggregate enables `optimistic locking` mechanism more explicitly.
 * If you fetch events from a storage, the application records the `version` number of that event stream.
 * You can append new events, but only if the `version` number in the storage has not changed.
 * If there is a `version` mismatch, it means that someone else has added the event(s) before you did.
 *
 * [EventSourcingSnapshottingLockingAggregate] extends [EventComputation], [StateComputation], [StateLockingRepository] and [EventSnapshottingLockingRepository] interfaces,
 * clearly communicating that it is composed out of these behaviours.
 *
 * @param C Commands of type [C] that this aggregate can handle
 * @param S Aggregate state of type [S]
 * @param E Events of type [E] that this aggregate can publish
 * @param V Version
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
interface EventSourcingSnapshottingLockingAggregate<C, S, E, V> :
    EventComputation<C, S, E>,
    StateComputation<C, S, E>,
    EventSnapshottingLockingRepository<C, S, E, V>,
    StateLockingRepository<C, S, V>

/**
 * Event Sourced Snapshotting aggregate constructor-like function.
 *
 * The Delegation pattern has proven to be a good alternative to implementation inheritance, and Kotlin supports it natively requiring zero boilerplate code.
 *
 * @param C Commands of type [C] that this aggregate can handle
 * @param S Aggregate state of type [S]
 * @param E Events of type [E] that are used internally to build/fold new state
 * @param decider A decider component of type [IDecider]<[C], [S], [E]>
 * @param eventRepository An aggregate event repository of type [EventSnapshottingRepository]<[C], [S], [E]>
 * @return An object/instance of type [EventSourcingSnapshottingAggregate]<[C], [S], [E]>
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
fun <C, S, E> EventSourcingSnapshottingAggregate(
    decider: IDecider<C, S, E>,
    eventRepository: EventSnapshottingRepository<C, S, E>,
    stateRepository: StateRepository<C, S>
): EventSourcingAggregate<C, S, E> =
    object : EventSourcingAggregate<C, S, E>,
        EventSnapshottingRepository<C, S, E> by eventRepository,
        StateRepository<C, S> by stateRepository,
        IDecider<C, S, E> by decider {}


/**
 * Event Sourced Snapshotting and Locking aggregate constructor-like function.
 *
 * The Delegation pattern has proven to be a good alternative to implementation inheritance, and Kotlin supports it natively requiring zero boilerplate code.
 *
 * @param C Commands of type [C] that this aggregate can handle
 * @param S Aggregate state of type [S]
 * @param E Events of type [E] that are used internally to build/fold new state
 * @param V Version
 * @param decider A decider component of type [IDecider]<[C], [S], [E]>
 * @param eventRepository An aggregate event repository of type [EventSnapshottingLockingRepository]<[C], [S], [E], [V]>
 * @return An object/instance of type [EventSourcingSnapshottingLockingAggregate]<[C], [S], [E], [V]>
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
fun <C, S, E, V> EventSourcingSnapshottingLockingAggregate(
    decider: IDecider<C, S, E>,
    eventRepository: EventSnapshottingLockingRepository<C, S, E, V>,
    stateRepository: StateLockingRepository<C, S, V>
): EventSourcingSnapshottingLockingAggregate<C, S, E, V> =
    object : EventSourcingSnapshottingLockingAggregate<C, S, E, V>,
        EventSnapshottingLockingRepository<C, S, E, V> by eventRepository,
        StateLockingRepository<C, S, V> by stateRepository,
        IDecider<C, S, E> by decider {}
