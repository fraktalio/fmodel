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

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flattenConcat
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

/**
 * [_Saga] is a datatype that represents the central point of control deciding what to execute next ([A]).
 * It is responsible for mapping different events into action results ([AR]) that the [_Saga] then can use to calculate the next actions ([A]) to be mapped to command(s).
 *
 * Saga does not maintain the state.
 *
 * @param AR Action Result type
 * @param A Action type
 * @property react A function/lambda that takes input state of type [AR], and returns the flow of actions [Flow]<[A]>.
 * @constructor Creates [_Saga]
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
data class _Saga<AR, A>(
    val react: (AR) -> Flow<A>
) {
    /**
     * Left map on AR/ActionResult parameter - Contravariant
     *
     * @param ARn ActionResult type new
     * @param f
     */
    inline fun <ARn> mapLeftOnActionResult(crossinline f: (ARn) -> AR): _Saga<ARn, A> = _Saga(
        react = { arn -> this.react(f(arn)) }
    )

    /**
     * Right map on A/Action parameter - Covariant
     *
     * @param An
     * @param f
     */
    inline fun <An> mapOnAction(crossinline f: (A) -> An): _Saga<AR, An> = _Saga(
        react = { ar -> this.react(ar).map { f(it) } }
    )

}

/**
 * Combines [_Saga]s into one [_Saga]
 *
 * Specially convenient when:
 * - [AR] and [AR2] have common superclass [AR_SUPER], or
 * - [A] and [A2] have common superclass [A_SUPER]
 *
 * @param AR Action Result (usually event) of the first Saga
 * @param A Action (usually command) of the first Saga
 * @param AR2 Action Result (usually event) of the second Saga
 * @param A2 Action (usually command) of the second Saga
 * @param AR_SUPER common superclass for [AR] and [AR2]
 * @param A_SUPER common superclass for [A] and [A2]
 * @param y second saga
 * @return new Saga of type `[_Saga]<[AR_SUPER], [A_SUPER]>`
 */
inline fun <reified AR : AR_SUPER, A : A_SUPER, reified AR2 : AR_SUPER, A2 : A_SUPER, AR_SUPER, A_SUPER> _Saga<in AR?, A>.combine(
    y: _Saga<in AR2?, A2>
): _Saga<AR_SUPER, A_SUPER> {

    val sagaX = this
        .mapLeftOnActionResult<AR_SUPER> { it as? AR }
        .mapOnAction<A_SUPER> { it } // As OR/SUM relationship is modeled with inheritance/polymorphism, the 'mapOnAction' function is not needed. It is mapping over the identity function. Used here for the sake of future implementations of `decide` functions, and learning purposes.

    val sagaY = y
        .mapLeftOnActionResult<AR_SUPER> { it as? AR2 }
        .mapOnAction<A_SUPER> { it } // As OR/SUM relationship is modeled with inheritance/polymorphism, the 'mapOnAction' function is not needed. It is mapping over the identity function. Used here for the sake of future implementations of `decide` functions, and learning purposes.

    return _Saga(
        react = { eitherAr -> flowOf(sagaX.react(eitherAr), (sagaY.react(eitherAr))).flattenConcat() }
    )
}

/**
 * A typealias for [_Saga]<AR, A>, specializing the [_Saga] to two generic parameters: AR and A
 */
typealias Saga<AR, A> = _Saga<AR, A>
