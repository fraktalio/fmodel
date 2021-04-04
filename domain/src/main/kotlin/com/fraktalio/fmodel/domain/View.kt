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
 * Combine [_View]s into one [_View] - Semigroup and Monoid with identity `_View<Unit, Unit, Nothing>`
 *
 * This is an associative binary operation which makes it a Semigroup. Additionally, the identity element makes it a Monoid.
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
typealias View<S, E> = _View<S, S, E>
