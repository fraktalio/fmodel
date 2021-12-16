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

import com.fraktalio.fmodel.domain.IView

/**
 * Materialized view is using/delegating a `view` to handle events of type [E] and to maintain a state of denormalized projection(s) as a result.
 * Essentially, it represents the query/view side of the CQRS pattern.
 *
 * [MaterializedView] extends [IView] and [ViewStateRepository] interfaces,
 * clearly communicating that it is composed out of these two behaviours.
 *
 * @param S Materialized View state of type [S]
 * @param E Events of type [E] that are handled by this Materialized View
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
interface MaterializedView<S, E> : IView<S, E>, ViewStateRepository<E, S> {
    /**
     * Computes new State based on the Event.
     *
     * @param event of type [E]
     * @return The newly computed state of type [S]
     */
    fun S?.computeNewState(event: E): S = evolve(this ?: initialState, event)
}

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
    object : MaterializedView<S, E>, ViewStateRepository<E, S> by viewStateRepository, IView<S, E> by view {}
