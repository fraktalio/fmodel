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

package com.fraktalio.fmodel.domain

/**
 * [_View] is a datatype that represents the event handling algorithm,
 * responsible for translating the events into denormalized state,
 * which is more adequate for querying.
 *
 * It has three generic parameters [Si], [So], [E], representing the type of the values that [_View] may contain or use.
 * [_View] can be specialized for any type of [Si], [So], [E] because these types does not affect its behavior.
 * [_View] behaves the same for [E]=[Int] or [E]=YourCustomType, for example.
 *
 * [_View] is a pure domain component
 *
 * @param Si Input_State type
 * @param So Output_State type
 * @param E Event type
 * @property evolve A pure function/lambda that takes input state of type [Si] and input event of type [E] as parameters, and returns the output/new state [So]
 * @property initialState A starting point / An initial state of type [So]
 * @constructor Creates [_View]
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
data class _View<Si, So, E>(
    val evolve: suspend (Si, E) -> So,
    val initialState: So,
) {
    /**
     * Left map on E/Event parameter - Contravariant
     *
     * @param En Event new
     * @param f
     */
    inline fun <En> mapLeftOnEvent(crossinline f: (En) -> E): _View<Si, So, En> = _View(
        evolve = { si, e -> this.evolve(si, f(e)) },
        initialState = this.initialState
    )

    /**
     * Dimap on S/State parameter - Contravariant on the Si (input State) - Covariant on the So (output State) = Profunctor
     *
     * @param Sin State input new
     * @param Son State output new
     * @param fl
     * @param fr
     */
    inline fun <Sin, Son> dimapOnState(
        crossinline fl: (Sin) -> Si,
        crossinline fr: (So) -> Son
    ): _View<Sin, Son, E> = _View(
        evolve = { si, e -> fr(this.evolve(fl(si), e)) },
        initialState = fr(this.initialState)
    )

    /**
     * Left map on S/State parameter - Contravariant
     *
     * @param Sin State input new
     * @param f
     */
    inline fun <Sin> mapLeftOnState(crossinline f: (Sin) -> Si): _View<Sin, So, E> =
        dimapOnState(f) { it }

    /**
     * Right map on S/State parameter - Covariant
     *
     * @param Son State output new
     * @param f
     */
    inline fun <Son> mapOnState(crossinline f: (So) -> Son): _View<Si, Son, E> =
        dimapOnState({ it }, f)

    /**
     * Right apply on S/State parameter - Applicative
     *
     * @param Son State output new
     * @param ff
     */
    fun <Son> applyOnState(ff: _View<Si, (So) -> Son, E>): _View<Si, Son, E> = _View(
        evolve = { si, e -> ff.evolve(si, e).invoke(this.evolve(si, e)) },
        initialState = ff.initialState.invoke(this.initialState)
    )

    /**
     * Right product on S/State parameter - Applicative
     *
     * @param Son State output new
     * @param fb
     */
    fun <Son> productOnState(fb: _View<Si, Son, E>): _View<Si, Pair<So, Son>, E> =
        applyOnState(fb.mapOnState { b: Son -> { a: So -> Pair(a, b) } })

    /**
     * Right just on S/State parameter - Applicative
     *
     * @param so State output
     */
    fun justOnState(so: So): _View<Si, So, E> = _View(
        evolve = { _, _ -> so },
        initialState = so
    )

}

/**
 * Combines [_View]s into one bigger [_View]
 *
 * Possible to use when [E] and [E2] have common superclass [E_SUPER]
 *
 * @param Si State input of the first View
 * @param So State output of the first View
 * @param E Event of the first View
 * @param Si2 State input of the second View
 * @param So2 State output of the second View
 * @param E2 Event of the second View
 * @param E_SUPER super type for [E] and [E2]
 * @param y second View
 * @return new View of type [_View]<[Pair]<[Si], [Si2]>, [Pair]<[So], [So2]>, [E_SUPER]>
 */
inline fun <Si, So, reified E : E_SUPER, Si2, So2, reified E2 : E_SUPER, E_SUPER> _View<in Si, out So, in E?>.combine(
    y: _View<in Si2, out So2, in E2?>
): _View<Pair<Si, Si2>, Pair<So, So2>, E_SUPER> {

    val viewX = this
        .mapLeftOnEvent<E_SUPER> { it as? E }
        .mapLeftOnState<Pair<Si, Si2>> { pair -> pair.first }

    val viewY = y
        .mapLeftOnEvent<E_SUPER> { it as? E2 }
        .mapLeftOnState<Pair<Si, Si2>> { pair -> pair.second }

    val viewZ = viewX.productOnState(viewY)

    return _View(
        evolve = { si, e -> viewZ.evolve(si, e) },
        initialState = viewZ.initialState
    )
}

/**
 * A typealias for [_View]<Si, So, E>, specializing the [_View] to two generic parameters: S and E, where Si=S, So=S, E=E
 */
typealias View<S, E> = _View<S, S, E>
