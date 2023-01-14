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
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.fold
import kotlinx.coroutines.flow.onEach

/**
 * `StateComputation` interface formalizes the `State Computation` algorithm by using a `decider` of type [IDecider]<[C], [S], [E]> to handle commands based on the current state, and produce new state.
 */
interface StateComputation<C, S, E> : IDecider<C, S, E> {
    /**
     * Computes new State based on the previous State and the [command].
     *
     * @param command of type [C]
     * @return The newly computed state of type [S]
     */
    suspend fun S?.computeNewState(command: C): S {
        val currentState = this ?: initialState
        val events = decide(command, currentState)
        return events.fold(currentState) { s, e -> evolve(s, e) }
    }
}

/**
 * State stored aggregate is using/delegating a `decider` of type [IDecider]<[C], [S], [E]> ([StateComputation]<[C], [S], [E]>) to handle commands and produce new state.
 * In order to handle the command, aggregate needs to fetch the current state via [StateRepository.fetchState] function first, and then delegate the command to the `decider` which can compute new state as a result.
 * New state is then stored via [StateRepository.save] suspending function.
 *
 * [StateStoredAggregate] extends [StateComputation] and [StateRepository] interfaces,
 * clearly communicating that it is composed out of these two behaviours.
 *
 * The Delegation pattern has proven to be a good alternative to `implementation inheritance`,
 * and Kotlin supports it natively requiring zero boilerplate code. [stateStoredAggregate] function is a good example.
 *
 *
 * @param C Commands of type [C] that this aggregate can handle
 * @param S Aggregate state of type [S]
 * @param E Events of type [E] that are used internally to build/fold new state
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
interface StateStoredAggregate<C, S, E> : StateComputation<C, S, E>, StateRepository<C, S>

/**
 * State stored locking aggregate is using/delegating a `decider` of type [IDecider]<[C], [S], [E]> ([StateComputation]<[C], [S], [E]>) to handle commands and produce new state.
 * In order to handle the command, aggregate needs to fetch the current state and current version via [StateLockingRepository.fetchState] function first, and then delegate the command to the `decider` which can compute new state as a result.
 * New state and version is then stored via [StateLockingRepository.save] suspending function.
 *
 * State stored locking aggregate enables `optimistic locking` mechanism more explicitly.
 * If you fetch an item/state from a storage, the application records the `version` number of that item.
 * You can update/save the item/state, but only if the `version` number in the storage has not changed.
 * If there is a `version` mismatch, it means that someone else has modified the item/state before you did.
 *
 * [StateStoredAggregate] extends [StateComputation] and [StateLockingRepository] interfaces,
 * clearly communicating that it is composed out of these two behaviours.
 *
 * The Delegation pattern has proven to be a good alternative to `implementation inheritance`,
 * and Kotlin supports it natively requiring zero boilerplate code. [stateStoredAggregate] function is a good example.
 *
 *
 * @param C Commands of type [C] that this aggregate can handle
 * @param S Aggregate state of type [S]
 * @param E Events of type [E] that are used internally to build/fold new state
 * @param V The Version of the aggregate/state
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
interface StateStoredLockingAggregate<C, S, E, V> : StateComputation<C, S, E>, StateLockingRepository<C, S, V>


/**
 * `StateOrchestratingComputation` interface formalizes the `Orchestrating State Computation` algorithm by using a `decider` of type [IDecider]<[C], [S], [E]> and `saga` of type [ISaga]<[E], [C]> to handle commands based on the current state, and produce new state.
 * If the `decider` is combined out of many deciders via `combine` function, a `saga` could be used to react on new events and send new commands to the `decider` recursively, in single transaction.
 */
interface StateOrchestratingComputation<C, S, E> : ISaga<E, C>, StateComputation<C, S, E> {
    /**
     * Computes new State based on the previous State and the [command].
     *
     * `saga` reacts on new events and sends new commands to the `decider` recursively, until it is done.
     *
     * @param command of type [C]
     * @return The newly computed state of type [S]
     */
    @FlowPreview
    override suspend fun S?.computeNewState(command: C): S {
        val currentState = this ?: initialState
        val events = decide(command, currentState)
        val newState = events.fold(currentState) { s, e -> evolve(s, e) }
        events.flatMapConcat { react(it) }.onEach { newState.computeNewState(it) }.collect()
        return newState
    }
}


/**
 * Orchestrating State stored aggregate is using/delegating a `decider` of type [IDecider]<[C], [S], [E]> and a `saga` of type [ISaga]<[E], [C]> ([StateOrchestratingComputation]<[C], [S], [E]>) to handle commands and produce new state.
 * In order to handle the command, aggregate needs to fetch the current state via [StateRepository.fetchState] function first, and then delegate the command to the `decider` which can compute new state as a result.
 * A `saga` is used to react on new events and send new commands to the `decider` recursively, in single transaction.
 * New state is then stored via [StateRepository.save] suspending function.
 *
 * [StateStoredOrchestratingAggregate] extends [StateOrchestratingComputation] and [StateStoredAggregate] interfaces,
 * clearly communicating that it is composed out of these two behaviours.
 *
 * The Delegation pattern has proven to be a good alternative to `implementation inheritance`,
 * and Kotlin supports it natively requiring zero boilerplate code.
 * [stateStoredOrchestratingAggregate] function is a good example.
 *
 *
 * @param C Commands of type [C] that this aggregate can handle
 * @param S Aggregate state of type [S]
 * @param E Events of type [E] that are used internally to build/fold new state
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
interface StateStoredOrchestratingAggregate<C, S, E> : StateOrchestratingComputation<C, S, E>, StateRepository<C, S>

/**
 * Orchestrating and Locking State stored aggregate is using/delegating a `decider` of type [IDecider]<[C], [S], [E]> and a `saga` of type [ISaga]<[E], [C]> ([StateOrchestratingComputation]<[C], [S], [E]>) to handle commands and produce new state.
 * In order to handle the command, aggregate needs to fetch the current state via [StateRepository.fetchState] function first, and then delegate the command to the `decider` which can compute new state as a result.
 * A `saga` is used to react on new events and send new commands to the `decider` recursively, in single transaction.
 * New state is then stored via [StateRepository.save] suspending function.
 *
 * `State stored orchestrating locking` aggregate enables `optimistic locking` mechanism more explicitly.
 * If you fetch an item/state from a storage, the application records the `version` number of that item.
 * You can update/save the item/state, but only if the `version` number in the storage has not changed.
 * If there is a `version` mismatch, it means that someone else has modified the item/state before you did.
 *
 * [StateStoredOrchestratingAggregate] extends [StateOrchestratingComputation] and [StateStoredAggregate] interfaces,
 * clearly communicating that it is composed out of these two behaviours.
 *
 * The Delegation pattern has proven to be a good alternative to `implementation inheritance`,
 * and Kotlin supports it natively requiring zero boilerplate code.
 * [stateStoredOrchestratingAggregate] function is a good example.
 *
 *
 * @param C Commands of type [C] that this aggregate can handle
 * @param S Aggregate state of type [S]
 * @param E Events of type [E] that are used internally to build/fold new state
 * @param V Version of the aggregate/state
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
interface StateStoredLockingOrchestratingAggregate<C, S, E, V> : StateOrchestratingComputation<C, S, E>,
    StateLockingRepository<C, S, V>

/**
 * State stored aggregate factory function.
 *
 * The Delegation pattern has proven to be a good alternative to implementation inheritance, and Kotlin supports it natively requiring zero boilerplate code.
 *
 * @param C Commands of type [C] that this aggregate can handle
 * @param S Aggregate state of type [S]
 * @param E Events of type [E] that are used internally to build/fold new state
 * @param decider A decider component of type [IDecider]<[C], [S], [E]>
 * @param stateRepository An aggregate state repository of type [StateRepository]<[C], [S]>
 * @return An object/instance of type [StateStoredAggregate]<[C], [S], [E]>
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
fun <C, S, E> stateStoredAggregate(
    decider: IDecider<C, S, E>,
    stateRepository: StateRepository<C, S>
): StateStoredAggregate<C, S, E> =
    object : StateStoredAggregate<C, S, E>,
        StateRepository<C, S> by stateRepository,
        IDecider<C, S, E> by decider {}

/**
 * State stored locking aggregate factory function.
 *
 * The Delegation pattern has proven to be a good alternative to implementation inheritance, and Kotlin supports it natively requiring zero boilerplate code.
 *
 * @param C Commands of type [C] that this aggregate can handle
 * @param S Aggregate state of type [S]
 * @param E Events of type [E] that are used internally to build/fold new state
 * @param V Version of the aggregate/state
 * @param decider A decider component of type [IDecider]<[C], [S], [E]>
 * @param stateRepository An aggregate state repository of type [StateLockingRepository]<[C], [S], [V]>
 * @return An object/instance of type [StateStoredLockingAggregate]<[C], [S], [E], [V]>
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
fun <C, S, E, V> stateStoredLockingAggregate(
    decider: IDecider<C, S, E>,
    stateRepository: StateLockingRepository<C, S, V>
): StateStoredLockingAggregate<C, S, E, V> =
    object : StateStoredLockingAggregate<C, S, E, V>,
        StateLockingRepository<C, S, V> by stateRepository,
        IDecider<C, S, E> by decider {}

/**
 * State stored orchestrating aggregate factory function.
 *
 * The Delegation pattern has proven to be a good alternative to implementation inheritance, and Kotlin supports it natively requiring zero boilerplate code.
 *
 * @param C Commands of type [C] that this aggregate can handle
 * @param S Aggregate state of type [S]
 * @param E Events of type [E] that are used internally to build/fold new state
 * @param decider A decider component of type [IDecider]<[C], [S], [E]>
 * @param stateRepository An aggregate state repository of type [StateRepository]<[C], [S]>
 * @param saga A saga component of type [ISaga]<[E], [C]>
 * @return An object/instance of type [StateStoredOrchestratingAggregate]<[C], [S], [E]>
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
fun <C, S, E> stateStoredOrchestratingAggregate(
    decider: IDecider<C, S, E>,
    stateRepository: StateRepository<C, S>,
    saga: ISaga<E, C>
): StateStoredOrchestratingAggregate<C, S, E> =
    object : StateStoredOrchestratingAggregate<C, S, E>,
        StateRepository<C, S> by stateRepository,
        IDecider<C, S, E> by decider,
        ISaga<E, C> by saga {}

/**
 * State stored orchestrating and locking aggregate factory function.
 *
 * The Delegation pattern has proven to be a good alternative to implementation inheritance, and Kotlin supports it natively requiring zero boilerplate code.
 *
 * @param C Commands of type [C] that this aggregate can handle
 * @param S Aggregate state of type [S]
 * @param E Events of type [E] that are used internally to build/fold new state
 * @param V Version of the aggregate/state
 * @param decider A decider component of type [IDecider]<[C], [S], [E]>
 * @param stateRepository An aggregate state repository of type [StateLockingRepository]<[C], [S], [V]>
 * @param saga A saga component of type [ISaga]<[E], [C]>
 * @return An object/instance of type [StateStoredLockingOrchestratingAggregate]<[C], [S], [E], [V]>
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
fun <C, S, E, V> stateStoredLockingOrchestratingAggregate(
    decider: IDecider<C, S, E>,
    stateRepository: StateLockingRepository<C, S, V>,
    saga: ISaga<E, C>
): StateStoredLockingOrchestratingAggregate<C, S, E, V> =
    object : StateStoredLockingOrchestratingAggregate<C, S, E, V>,
        StateLockingRepository<C, S, V> by stateRepository,
        IDecider<C, S, E> by decider,
        ISaga<E, C> by saga {}

fun <C, S, E> stateComputation(
    decider: IDecider<C, S, E>
): StateComputation<C, S, E> =
    object : StateComputation<C, S, E>,
        IDecider<C, S, E> by decider {}

fun <C, S, E> stateOrchestratingComputation(
    decider: IDecider<C, S, E>,
    saga: ISaga<E, C>
): StateOrchestratingComputation<C, S, E> =
    object : StateOrchestratingComputation<C, S, E>,
        IDecider<C, S, E> by decider,
        ISaga<E, C> by saga {}