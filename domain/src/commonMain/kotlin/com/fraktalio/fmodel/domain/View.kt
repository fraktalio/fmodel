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

package com.fraktalio.fmodel.domain

import com.fraktalio.fmodel.domain.internal.InternalView
import com.fraktalio.fmodel.domain.internal.combine

/**
 * View interface
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
interface IView<S, in E> {
    val evolve: (S, E) -> S
    val initialState: S
}

/**
 * [View] is a datatype that represents the event handling algorithm,
 * responsible for translating the events into denormalized state,
 * which is more adequate for querying.
 *
 * It has two generic parameters `S`, `E`, representing the type of the values that [View] may contain or use.
 * [View] can be specialized for any type of `S`, `E` because these types does not affect its behavior.
 * [View] behaves the same for `E`=[Int] or `E`=`YourCustomType`.
 *
 * Example:
 *
 * ```kotlin
 * fun oddNumberView(): View<OddNumberState, OddNumberEvent?> = View(
 *     initialState = OddNumberState(Description("Initial state"), NumberValue(0)),
 *     evolve = { s, e ->
 *         when (e) {
 *             is OddNumberAdded -> OddNumberState(s.description + e.description, s.value + e.value)
 *             is OddNumberSubtracted -> OddNumberState(s.description - e.description, s.value - e.value)
 *             null -> s
 *         }
 *     }
 * )
 * ```
 *
 * @param S State type
 * @param E Event type
 * @property evolve A pure function/lambda that takes input state of type [S] and input event of type [E] as parameters, and returns the output/new state [S]
 * @property initialState A starting point / An initial state of type [S]
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
data class View<S, in E>(
    override val evolve: (S, E) -> S,
    override val initialState: S
) : IView<S, E> {
    @PublishedApi
    internal constructor(view: InternalView<S, S, E>) : this(view.evolve, view.initialState)

    /**
     * Left map on E/Event
     *
     * @param En Event new
     * @param f Function that maps type `En` to `E`
     *
     * @return New View of type [View]<[S], [En]>
     */
    inline fun <En> mapLeftOnEvent(
        crossinline f: (En) -> E
    ): View<S, En> = View(InternalView(evolve, initialState).mapLeftOnEvent(f))

    /**
     * Di-map on S/State
     *
     * @param Sn State new
     * @param fl Function that maps type `Sn` to `S`
     * @param fr Function that maps type `S` to `Sn`
     *
     * @return New View of type [View]<[Sn], [E]>
     */
    inline fun <Sn> dimapOnState(
        crossinline fl: (Sn) -> S,
        crossinline fr: (S) -> Sn
    ): View<Sn, E> = View(InternalView(evolve, initialState).dimapOnState(fl, fr))

}

/**
 * Combines [View]s into one [View]
 *
 * Possible to use when [E] and [E2] have common superclass [E_SUPER]
 *
 * @param S State  of the first View
 * @param E Event of the first View
 * @param S2 State of the second View
 * @param E2 Event of the second View
 * @param E_SUPER super type for [E] and [E2]
 * @param y second View
 * @return new View of type [View]<[Pair]<[S], [S2]>, [E_SUPER]>
 */
inline infix fun <S, reified E : E_SUPER, S2, reified E2 : E_SUPER, E_SUPER> View<S, E?>.combine(y: View<S2, E2?>): View<Pair<S, S2>, E_SUPER> =
    View(InternalView(evolve, initialState).combine(InternalView(y.evolve, y.initialState)))

/**
 * View DSL - A convenient builder DSL for the [View]
 *
 * Example:
 *
 * ```kotlin
 * fun evenNumberView(): View<EvenNumberState, EvenNumberEvent?> =
 *     view {
 *         initialState {
 *             EvenNumberState(Description("Initial state"), NumberValue(0))
 *         }
 *         evolve { s, e ->
 *             when (e) {
 *                 is EvenNumberAdded -> EvenNumberState(s.description + e.description, s.value + e.value)
 *                 is EvenNumberSubtracted -> EvenNumberState(s.description - e.description, s.value - e.value)
 *                 null -> s
 *             }
 *         }
 *     }
 * ```
 */
fun <S, E> view(block: ViewBuilder<S, E>.() -> Unit): View<S, E> =
    ViewBuilder<S, E>().apply(block).build()

/**
 * View builder
 */
class ViewBuilder<S, E> internal constructor() {
    private var evolve: (S, E) -> S = { s, _ -> s }
    private var initialState: () -> S = { error("Initial State is not initialized") }

    fun evolve(lambda: (S, E) -> S) {
        evolve = lambda
    }

    fun initialState(lambda: () -> S) {
        initialState = lambda
    }

    fun build(): View<S, E> = View(evolve, initialState())
}
