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

/**
 * [_Saga] is a datatype that represents the central point of control deciding what to execute next ([A]).
 * It is responsible for mapping different events from aggregates into action results ([AR]) that the [_Saga] then can use to calculate the next actions ([A]) to be mapped to commands of other aggregates.
 *
 * The biggest difference between `Saga` and `Process` is that `Saga` is stateless, it does not maintain the state.
 *
 * @param AR Action Result type
 * @param A Action type
 * @property react A pure function/lambda that takes input state of type [AR], and returns the list of actions [Iterable]<[A]>.
 * @constructor Creates [_Saga]
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
data class _Saga<AR, A>(
    val react: (AR) -> Iterable<A>
) {
    /**
     * Left map over [AR] parameter - Contravariant
     *
     * @param ARn ActionResult type new
     * @param f
     */
    inline fun <ARn> lmapOnAR(crossinline f: (ARn) -> AR): _Saga<ARn, A> = _Saga(
        react = { ar -> this.react(f(ar)) }
    )

    /**
     * Right map over A/Action parameter - Covariant
     *
     * @param An
     * @param f
     */
    inline fun <An> rmapOnA(crossinline f: (A) -> An): _Saga<AR, An> = _Saga(
        react = { ar -> this.react(ar).map(f) }
    )

}

/**
 * Combine [_Saga]s into one [_Saga] - Semigroup and Monoid with identity element `_Saga<Nothing, Nothing>`
 * This is an associative binary operation which makes it a Semigroup. Additionally, the identity element makes it a Monoid
 *
 * @param AR Action Result (usually event) of the first Saga
 * @param A Action (usually command) of the first Saga
 * @param ARn Action Result (usually event) of the second Saga
 * @param An Action (usually command) of the second Saga
 * @param y second saga
 * @return new Saga of type `[_Saga]<[Either]<[AR], [ARn]>, [Either]<[A], [An]>>`
 */
fun <AR, A, ARn, An> _Saga<AR?, A>.combineSagas(y: _Saga<ARn?, An>): _Saga<Either<AR, ARn>, Either<A, An>> {
    val getAR: (Either<AR, ARn>) -> AR? = { either -> either.fold({ it }, { null }) }
    val getARn: (Either<AR, ARn>) -> ARn? = { either -> either.fold({ null }, { it }) }
    val getAEither: (A) -> Either<A, An> = { a -> Either.Left(a) }
    val getAnEither: (An) -> Either<A, An> = { an -> Either.Right(an) }

    val sagaX = this
        .lmapOnAR(getAR)
        .rmapOnA(getAEither)

    val sagaY = y
        .lmapOnAR(getARn)
        .rmapOnA(getAnEither)

    return _Saga(
        react = { eitherAr -> sagaX.react(eitherAr).plus(sagaY.react(eitherAr)) }
    )
}

/**
 * Combines [_Saga]s into one [_Saga]
 *
 * Specially convenient when:
 * - [AR] and [ARn] have common superclass [AR_SUPER], or
 * - [A] and [An] have common superclass [A_SUPER]
 *
 * @param AR Action Result (usually event) of the first Saga
 * @param A Action (usually command) of the first Saga
 * @param ARn Action Result (usually event) of the second Saga
 * @param An Action (usually command) of the second Saga
 * @param AR_SUPER common superclass for [AR] and [ARn]
 * @param A_SUPER common superclass for [A] and [An]
 * @param y second saga
 * @return new Saga of type `[_Saga]<[AR_SUPER], [A_SUPER]>`
 */
inline fun <reified AR : AR_SUPER, A : A_SUPER, reified ARn : AR_SUPER, An : A_SUPER, AR_SUPER, A_SUPER> _Saga<in AR?, out A>.combine(
    y: _Saga<in ARn?, out An>
): _Saga<AR_SUPER, A_SUPER> {
    val getAR: (AR_SUPER) -> AR? = {
        when (it) {
            is AR -> it
            else -> null
        }
    }
    val getARn: (AR_SUPER) -> ARn? = {
        when (it) {
            is ARn -> it
            else -> null
        }
    }
    val getABase: (A) -> A_SUPER = { it }
    val getAnBase: (An) -> A_SUPER = { it }

    val sagaX = this
        .lmapOnAR(getAR)
        .rmapOnA(getABase)

    val sagaY = y
        .lmapOnAR(getARn)
        .rmapOnA(getAnBase)

    return _Saga(
        react = { eitherAr -> sagaX.react(eitherAr).plus(sagaY.react(eitherAr)) }
    )
}

/**
 * A typealias for [_Saga]<AR, A>, specializing the [_Saga] to two generic parameters: AR and A
 */
typealias Saga<AR, A> = _Saga<AR, A>
