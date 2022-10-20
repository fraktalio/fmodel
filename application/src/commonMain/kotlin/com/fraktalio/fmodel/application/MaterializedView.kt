/*
 * Copyright (c) 2022 Fraktalio D.O.O. All rights reserved.
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

import com.fraktalio.fmodel.domain.IView

/**
 * `ViewStateComputation` interface formalizes the `View State Computation` algorithm by using a `view` of type [IView]<[S], [E]> to handle events based on the current state, and produce/update new state.
 */
interface ViewStateComputation<S, E> : IView<S, E> {
    /**
     * Computes new State based on the Event.
     *
     * @param event of type [E]
     * @return The newly computed state of type [S]
     */
    fun S?.computeNewState(event: E): S = evolve(this ?: initialState, event)
}

/**
 * Materialized view is using/delegating a `view` / [ViewStateComputation]<[S], [E]> to handle events of type [E] and to maintain a state of projection(s) as a result.
 *
 * [MaterializedView] extends [ViewStateComputation] and [ViewStateRepository] interfaces,
 * clearly communicating that it is composed out of these two behaviours.
 *
 * @param S Materialized View state of type [S]
 * @param E Events of type [E] that are handled by this Materialized View
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
interface MaterializedView<S, E> : ViewStateComputation<S, E>, ViewStateRepository<E, S>

/**
 * Materialized view is using/delegating a `view` / [ViewStateComputation]<[S], [E]> to handle events of type [E] and to maintain a state of projection(s) as a result.
 *
 * [MaterializedLockingView] extends [ViewStateComputation] and [ViewStateLockingRepository] interfaces,
 * clearly communicating that it is composed out of these two behaviours.
 *
 * @param S Materialized View state of type [S]
 * @param E Events of type [E] that are handled by this Materialized View
 * @param V Version
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
interface MaterializedLockingView<S, E, V> : ViewStateComputation<S, E>, ViewStateLockingRepository<E, S, V>

/**
 * Materialized view is using/delegating a `view` / [ViewStateComputation]<[S], [E]> to handle events of type [E] and to maintain a state of projection(s) as a result.
 *
 * [MaterializedLockingDeduplicationView] extends [ViewStateComputation] and [ViewStateLockingDeduplicationRepository] interfaces,
 * clearly communicating that it is composed out of these two behaviours.
 *
 * @param S Materialized View state of type [S]
 * @param E Events of type [E] that are handled by this Materialized View
 * @param EV Event Version
 * @param SV State Version
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
interface MaterializedLockingDeduplicationView<S, E, EV, SV> : ViewStateComputation<S, E>,
    ViewStateLockingDeduplicationRepository<E, S, EV, SV>

/**
 * Materialized View factory function.
 *
 * The Delegation pattern has proven to be a good alternative to implementation inheritance, and Kotlin supports it natively requiring zero boilerplate code.
 *
 * @param S Aggregate state of type [S]
 * @param E Events of type [E] that are used internally to build/fold new state
 * @property view A view component of type [IView]<[S], [E]>
 * @property viewStateRepository Interface for [S]tate management/persistence - dependencies by delegation
 * @return An object/instance of type [MaterializedView]<[S], [E]>
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
fun <S, E> materializedView(
    view: IView<S, E>,
    viewStateRepository: ViewStateRepository<E, S>,
): MaterializedView<S, E> =
    object : MaterializedView<S, E>,
        ViewStateRepository<E, S> by viewStateRepository,
        IView<S, E> by view {}

/**
 * Materialized Locking View factory function.
 *
 * The Delegation pattern has proven to be a good alternative to implementation inheritance, and Kotlin supports it natively requiring zero boilerplate code.
 *
 * @param S Aggregate state of type [S]
 * @param E Events of type [E] that are used internally to build/fold new state
 * @param V Version
 * @property view A view component of type [IView]<[S], [E]>
 * @property viewStateRepository Interface for [S]tate management/persistence - dependencies by delegation
 * @return An object/instance of type [MaterializedLockingView]<[S], [E], [V]>
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
fun <S, E, V> materializedLockingView(
    view: IView<S, E>,
    viewStateRepository: ViewStateLockingRepository<E, S, V>,
): MaterializedLockingView<S, E, V> =
    object : MaterializedLockingView<S, E, V>,
        ViewStateLockingRepository<E, S, V> by viewStateRepository,
        IView<S, E> by view {}

/**
 * Materialized Locking Deduplication View factory function.
 *
 * The Delegation pattern has proven to be a good alternative to implementation inheritance, and Kotlin supports it natively requiring zero boilerplate code.
 *
 * @param S Aggregate state of type [S]
 * @param E Events of type [E] that are used internally to build/fold new state
 * @param EV Event Version
 * @param SV State Version
 * @property view A view component of type [IView]<[S], [E]>
 * @property viewStateRepository Interface for [S]tate management/persistence - dependencies by delegation
 * @return An object/instance of type [MaterializedLockingDeduplicationView]<[S], [E], [EV], [SV]>
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
fun <S, E, EV, SV> materializedLockingDeduplicationView(
    view: IView<S, E>,
    viewStateRepository: ViewStateLockingDeduplicationRepository<E, S, EV, SV>,
): MaterializedLockingDeduplicationView<S, E, EV, SV> =
    object : MaterializedLockingDeduplicationView<S, E, EV, SV>,
        ViewStateLockingDeduplicationRepository<E, S, EV, SV> by viewStateRepository,
        IView<S, E> by view {}

fun <S, E> viewStateComputation(
    view: IView<S, E>,
): ViewStateComputation<S, E> =
    object : ViewStateComputation<S, E>,
        IView<S, E> by view {}