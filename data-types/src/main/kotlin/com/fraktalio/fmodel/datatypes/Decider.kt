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

package com.fraktalio.fmodel.datatypes

import arrow.core.Either
import arrow.core.identity

/**
 * [_Decider] is a datatype that represents the main decision making algorithm.
 * It has five generic parameters [C], [Si], [So], [Ei], [Eo] , representing the type of the values that [_Decider] may contain or use.
 * [_Decider] can be specialized for any type [C] or [Si] or [So] or [Ei] or [Eo] because these types does not affect its behavior. [_Decider] behaves the same for [C]=[Int] or [C]=YourCustomType, for example.
 * To indicate that [_Decider] is a type constructor for all values of [C], [Si], [So], [Ei], [Eo], it implements [_DeciderOf]<[C], [Si], [So], [Ei], [Eo]>, which is a typealias of [Kind5]<[For_Decider], [C], [Si], [So], [Ei], [Eo]>
 *
 * [_Decider] is a pure domain component.
 *
 * @param C Command type
 * @param Si Input_State type
 * @param So Output_State type
 * @param Ei Input_Event type
 * @param Eo Output_Event type
 * @property decide A pure function/lambda that takes command of type [C] and input state of type [Si] as parameters, and returns the iterable of output events [Iterable]<[Eo]> as a result
 * @property evolve A pure function/lambda that takes input state of type [Si] and input event of type [Ei] as parameters, and returns the output/new state [So]
 * @property initialState A starting point / An initial state of type [So]
 * @property isTerminal A pure function/lambda that takes input state of type [Si], and returns [Boolean] showing if the current input state is terminal/final
 * @constructor Creates [_Decider]
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
data class _Decider<C, Si, So, Ei, Eo>(
    val decide: (C, Si) -> Iterable<Eo>,
    val evolve: (Si, Ei) -> So,
    val initialState: So,
    val isTerminal: (Si) -> Boolean
) {
    /**
     * Left map over C/Command parameter - Contravariant
     *
     * @param Cn Command new
     * @param f
     */
    inline fun <Cn> lmapOnC(crossinline f: (Cn) -> C): _Decider<Cn, Si, So, Ei, Eo> = _Decider(
        decide = { cn, s -> this.decide(f(cn), s) },
        evolve = { si, ei -> this.evolve(si, ei) },
        initialState = this.initialState,
        isTerminal = { si -> this.isTerminal(si) }
    )

    /**
     *
     *
     * Dimap over E/Event parameter - Contravariant on input event and Covariant on output event = Profunctor
     *
     * @param Ein Event input new
     * @param Eon Event output new
     * @param fl
     * @param fr
     */
    inline fun <Ein, Eon> dimapOnE(
        crossinline fl: (Ein) -> Ei,
        crossinline fr: (Eo) -> Eon
    ): _Decider<C, Si, So, Ein, Eon> = _Decider(
        decide = { c, si -> this.decide(c, si).map(fr) },
        evolve = { si, ein -> this.evolve(si, fl(ein)) },
        initialState = this.initialState,
        isTerminal = { si -> this.isTerminal(si) }
    )

    /**
     * Left map over E/Event parameter - Contravariant
     *
     * @param Ein Event input new
     * @param f
     */
    inline fun <Ein> lmapOnE(crossinline f: (Ein) -> Ei): _Decider<C, Si, So, Ein, Eo> =
        dimapOnE(f, ::identity)

    /**
     * Right map over E/Event parameter - Covariant
     *
     * @param Eon Event output new
     * @param f
     */
    inline fun <Eon> rmapOnE(crossinline f: (Eo) -> Eon): _Decider<C, Si, So, Ei, Eon> =
        dimapOnE(::identity, f)

    /**
     * Dimap over S/State parameter - Contravariant on input state and Covariant on output state = Profunctor
     *
     * @param Sin
     * @param Son
     * @param fl
     * @param fr
     */
    inline fun <Sin, Son> dimapOnS(
        crossinline fl: (Sin) -> Si,
        crossinline fr: (So) -> Son
    ): _Decider<C, Sin, Son, Ei, Eo> = _Decider(
        decide = { c, sin -> this.decide(c, fl(sin)) },
        evolve = { sin, ei -> fr(this.evolve(fl(sin), ei)) },
        initialState = fr(this.initialState),
        isTerminal = { si -> this.isTerminal(fl(si)) }
    )

    /**
     * Left map over S/State parameter - Contravariant
     *
     * @param Sin State input new
     * @param f
     */
    inline fun <Sin> lmapOnS(crossinline f: (Sin) -> Si): _Decider<C, Sin, So, Ei, Eo> =
        dimapOnS(f, ::identity)

    /**
     * Right map over S/State parameter - Covariant
     *
     * @param Son State output new
     * @param f
     */
    inline fun <Son> rmapOnS(crossinline f: (So) -> Son): _Decider<C, Si, Son, Ei, Eo> =
        dimapOnS(::identity, f)


    /**
     * Right apply over S/State parameter - Applicative
     *
     * @param Son State output new
     * @param ff
     */
    fun <Son> rapplyOnS(ff: _Decider<C, Si, (So) -> Son, Ei, Eo>): _Decider<C, Si, Son, Ei, Eo> = _Decider(
        decide = { c, si -> ff.decide(c, si).plus(this.decide(c, si)) },
        evolve = { si, ei -> ff.evolve(si, ei).invoke(this.evolve(si, ei)) },
        initialState = ff.initialState.invoke(this.initialState),
        isTerminal = { si -> this.isTerminal(si) && ff.isTerminal(si) }
    )

    /**
     * Right product over S/State parameter - Applicative
     *
     * @param Son State output new
     * @param fb
     */
    fun <Son> rproductOnS(fb: _Decider<C, Si, Son, Ei, Eo>): _Decider<C, Si, Pair<So, Son>, Ei, Eo> =
        rapplyOnS(fb.rmapOnS { b: Son -> { a: So -> Pair(a, b) } })

    /**
     * Right just over S/State parameter - Applicative
     *
     * @param so State output
     */
    fun rjustOnS(so: So): _Decider<C, Si, So, Ei, Eo> = _Decider(
        decide = { c, si -> emptyList() },
        evolve = { si, ei -> so },
        initialState = so,
        isTerminal = { si -> true }
    )


}


/**
 * Combine [_Decider]s into one [_Decider] - Semigroup and Monoid with identity element `_Decider<Nothing, Unit, Unit, Nothing, Nothing>`
 * This is an associative binary operation which makes it a Semigroup. Additionally, the identity element makes it a Monoid

 */
fun <C, Si, So, Ei, Eo, Cn, Sin, Son, Ein, Eon> _Decider<C?, Si, So, Ei?, Eo>.combineDeciders(
    y: _Decider<Cn?, Sin, Son, Ein?, Eon>
): _Decider<Either<C, Cn>, Pair<Si, Sin>, Pair<So, Son>, Either<Ei, Ein>, Either<Eo, Eon>> {
    val getC1: (Either<C, Cn>) -> C? = { either -> either.fold({ it }, { null }) }
    val getC2: (Either<C, Cn>) -> Cn? = { either -> either.fold({ null }, { it }) }
    val getE1: (Either<Ei, Ein>) -> Ei? = { either -> either.fold({ it }, { null }) }
    val getE1Either: (Eo) -> Either<Eo, Eon> = { eo1 -> Either.Left(eo1) }
    val getE2: (Either<Ei, Ein>) -> Ein? = { either -> either.fold({ null }, { it }) }
    val getE2Either: (Eon) -> Either<Eo, Eon> = { eo2 -> Either.Right(eo2) }
    val getS1: (Pair<Si, Sin>) -> Si = { pair -> pair.first }
    val getS2: (Pair<Si, Sin>) -> Sin = { pair -> pair.second }

    val deciderX = this
        .lmapOnC(getC1)
        .lmapOnS(getS1)
        .dimapOnE(getE1, getE1Either)

    val deciderY = y
        .lmapOnC(getC2)
        .lmapOnS(getS2)
        .dimapOnE(getE2, getE2Either)

    val deciderZ = deciderX.rproductOnS(deciderY)

    return _Decider(
        decide = { c, si -> deciderZ.decide(c, si) },
        evolve = { pair, ei -> deciderZ.evolve(pair, ei) },
        initialState = deciderZ.initialState,
        isTerminal = { pair -> deciderZ.isTerminal(pair) }
    )
}

/**
 * A typealias for [_Decider]<C, Si, So, Ei, Eo>, specializing the [_Decider] to three generic parameters: C, S and E, where C=C, Si=S, So=S, Ei=E, Eo=E
 */
typealias Decider<C, S, E> = _Decider<C, S, S, E, E>
