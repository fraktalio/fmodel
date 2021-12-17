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

import com.fraktalio.fmodel.domain.IDecider
import com.fraktalio.fmodel.domain.ISaga


/**
 * State stored aggregate is using/delegating a `decider` of type [IDecider]<[C], [S], [E]> to handle commands and produce new state.
 * In order to handle the command, aggregate needs to fetch the current state via [StateRepository.fetchState] function first, and then delegate the command to the `decider` which can compute new state as a result.
 * New state is then stored via [StateRepository.save] suspending function.
 *
 * [StateStoredAggregate] extends [IDecider] and [StateRepository] interfaces,
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
interface StateStoredAggregate<C, S, E> : IDecider<C, S, E>, StateRepository<C, S> {
    /**
     * Computes new State based on the previous State and the [command].
     *
     * @param command of type [C]
     * @return The newly computed state of type [S]
     */
    suspend fun S?.computeNewState(command: C): S = decide(command, this ?: initialState)
        .fold(this ?: initialState) { s, e -> evolve(s, e) }
}

/**
 * Orchestrating State stored aggregate is using/delegating a `decider` of type [IDecider]<[C], [S], [E]> to handle commands and produce new state.
 * In order to handle the command, aggregate needs to fetch the current state via [StateRepository.fetchState] function first, and then delegate the command to the `decider` which can compute new state as a result.
 * If the `decider` is combined out of many deciders via `combine` function, a `saga` could be used to react on new events and send new commands to the `decider` recursively, in single transaction.
 * New state is then stored via [StateRepository.save] suspending function.
 *
 * [StateStoredOrchestratingAggregate] extends [ISaga] and [StateStoredAggregate] interfaces,
 * clearly communicating that it is composed out of these two behaviours.
 *
 * The Delegation pattern has proven to be a good alternative to `implementation inheritance`,
 * and Kotlin supports it natively requiring zero boilerplate code. [stateStoredOrchestratingAggregate] function is a good example.
 *
 *
 * @param C Commands of type [C] that this aggregate can handle
 * @param S Aggregate state of type [S]
 * @param E Events of type [E] that are used internally to build/fold new state
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
interface StateStoredOrchestratingAggregate<C, S, E> : ISaga<E, C>, StateStoredAggregate<C, S, E> {
    /**
     * Computes new State based on the previous State and the [command].
     *
     * @param command of type [C]
     * @return The newly computed state of type [S]
     */
    override suspend fun S?.computeNewState(command: C): S {
        val currentState = this ?: initialState
        val events = decide(command, currentState)
        val newState = events.fold(currentState) { s, e -> evolve(s, e) }
        events.flatMap { react(it) }.forEach { newState.computeNewState(it) }
        return newState
    }
}

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
    object :
        StateStoredAggregate<C, S, E>,
        StateRepository<C, S> by stateRepository,
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
