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

import arrow.core.Either
import arrow.core.identity

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
    val evolve: (Si, E) -> So,
    val initialState: So,
) {
    /**
     * Left map over E/Event - Contravariant
     *
     * @param En Event new
     * @param f
     */
    inline fun <En> lmapOnE(crossinline f: (En) -> E): _View<Si, So, En> = _View(
        evolve = { si, e -> this.evolve(si, f(e)) },
        initialState = this.initialState
    )

    /**
     * Dimap over S/State parameter - Contravariant on the Si (input State) - Covariant on the So (output State) = Profunctor
     *
     * @param Sin State input new
     * @param Son State output new
     * @param fl
     * @param fr
     */
    inline fun <Sin, Son> dimapOnS(
        crossinline fl: (Sin) -> Si,
        crossinline fr: (So) -> Son
    ): _View<Sin, Son, E> = _View(
        evolve = { si, e -> fr(this.evolve(fl(si), e)) },
        initialState = fr(this.initialState)
    )

    /**
     * Left map over S/State parameter - Contravariant
     *
     * @param Sin State input new
     * @param f
     */
    inline fun <Sin> lmapOnS(crossinline f: (Sin) -> Si): _View<Sin, So, E> =
        dimapOnS(f, ::identity)

    /**
     * Right map over S/State parameter - Covariant
     *
     * @param Son State output new
     * @param f
     */
    inline fun <Son> rmapOnS(crossinline f: (So) -> Son): _View<Si, Son, E> =
        dimapOnS(::identity, f)

    /**
     * Right apply over S/State parameter - Applicative
     *
     * @param Son State output new
     * @param ff
     */
    fun <Son> rapplyOnS(ff: _View<Si, (So) -> Son, E>): _View<Si, Son, E> = _View(
        evolve = { si, e -> ff.evolve(si, e).invoke(this.evolve(si, e)) },
        initialState = ff.initialState.invoke(this.initialState)
    )

    /**
     * Right product over S/State - Applicative
     *
     * @param Son State output new
     * @param fb
     */
    fun <Son> rproductOnS(fb: _View<Si, Son, E>): _View<Si, Pair<So, Son>, E> =
        rapplyOnS(fb.rmapOnS { b: Son -> { a: So -> Pair(a, b) } })

    /**
     * Right just over S/State - Applicative
     *
     * @param so State output
     */
    fun rjustOnS(so: So): _View<Si, So, E> = _View(
        evolve = { _, _ -> so },
        initialState = so
    )

}


/**
 * ############### Extension ###############
 *
 * Combines [_View]s into one bigger [_View]
 *
 * @param Si State input of the first View
 * @param So State output of the first View
 * @param E Event of the first View
 * @param Si2 State input of the second View
 * @param So2 State output of the second View
 * @param E2 Event of the second View
 * @param y second View
 * @return new View of type [_View]< [Pair]<[Si], [Si2]>, [Pair]<[So], [So2]>, [Either]<[E], [E2]> >
 */
fun <Si, So, E, Si2, So2, E2> _View<Si, So, E?>.combineViews(y: _View<Si2, So2, E2?>): _View<Pair<Si, Si2>, Pair<So, So2>, Either<E, E2>> {
    val extractE1: (Either<E, E2>) -> E? = { either -> either.fold({ it }, { null }) }
    val extractE2: (Either<E, E2>) -> E2? = { either -> either.fold({ null }, { it }) }
    val extractS1: (Pair<Si, Si2>) -> Si = { pair -> pair.first }
    val extractS2: (Pair<Si, Si2>) -> Si2 = { pair -> pair.second }

    val viewX = this
        .lmapOnE(extractE1)
        .lmapOnS(extractS1)

    val viewY = y
        .lmapOnE(extractE2)
        .lmapOnS(extractS2)

    val viewZ = viewX.rproductOnS(viewY)

    return _View(
        evolve = { si, e -> viewZ.evolve(si, e) },
        initialState = viewZ.initialState
    )
}


/**
 * ############### Extension ###############
 *
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
inline fun <Si, So, reified E : E_SUPER, Si2, So2, reified E2 : E_SUPER, E_SUPER> _View<Si, So, in E?>.combine(
    y: _View<Si2, So2, in E2?>
): _View<Pair<Si, Si2>, Pair<So, So2>, E_SUPER> {
    val extractE1: (E_SUPER) -> E? = {
        when (it) {
            is E -> it
            else -> null
        }
    }
    val extractE2: (E_SUPER) -> E2? = {
        when (it) {
            is E2 -> it
            else -> null
        }
    }
    val extractS1: (Pair<Si, Si2>) -> Si = { pair -> pair.first }
    val extractS2: (Pair<Si, Si2>) -> Si2 = { pair -> pair.second }

    val viewX = this
        .lmapOnE(extractE1)
        .lmapOnS(extractS1)

    val viewY = y
        .lmapOnE(extractE2)
        .lmapOnS(extractS2)

    val viewZ = viewX.rproductOnS(viewY)

    return _View(
        evolve = { si, e -> viewZ.evolve(si, e) },
        initialState = viewZ.initialState
    )
}

/**
 * ############### Extension ###############
 *
 * Combines [_View]s into one bigger [_View]
 *
 * Possible to use when:
 * - [E] and [E2] have common superclass [E_SUPER]
 * - [Si] and [Si2] have common superclass [Si_SUPER]
 * - [So] and [So2] have common superclass [So_SUPER]
 *
 * @param Si State input of the first View
 * @param So State output of the first View
 * @param E Event of the first View
 * @param Si2 State input of the second View
 * @param So2 State output of the second View
 * @param E2 Event of the second View
 * @param Si_SUPER super type for [Si] and [Si2]
 * @param So_SUPER super type for [So] and [So2]
 * @param E_SUPER super type for [E] and [E2]
 * @param y second View
 * @return new View of type [_View]< [List]<[Si_SUPER]>, [List]<[So_SUPER]>, [E_SUPER] >
 */
inline fun <reified Si : Si_SUPER, So : So_SUPER, reified E : E_SUPER, reified Si2 : Si_SUPER, So2 : So_SUPER, reified E2 : E_SUPER, Si_SUPER, So_SUPER, E_SUPER> _View<List<Si>, List<So>, in E?>.combineL(
    y: _View<in List<Si2>, out List<So2>, in E2?>
): _View<List<Si_SUPER>, List<So_SUPER>, E_SUPER> {

    val extractE1: (E_SUPER) -> E? = {
        when (it) {
            is E -> it
            else -> null
        }
    }
    val extractE2: (E_SUPER) -> E2? = {
        when (it) {
            is E2 -> it
            else -> null
        }
    }
    val extractS1: (List<Si_SUPER>) -> List<Si> = { list -> list.filterIsInstance(Si::class.java) }
    val extractS2: (List<Si_SUPER>) -> List<Si2> = { list -> list.filterIsInstance(Si2::class.java) }

    val viewX = this
        .lmapOnE(extractE1)
        .lmapOnS(extractS1)

    val viewY = y
        .lmapOnE(extractE2)
        .lmapOnS(extractS2)

    val viewZ = viewX.rproductOnS(viewY).rmapOnS { pair: Pair<List<So>, List<So2>> -> pair.toList().flatten() }

    return _View(
        evolve = { si, e -> viewZ.evolve(si, e) },
        initialState = viewZ.initialState
    )
}

typealias View<S, E> = _View<S, S, E>
