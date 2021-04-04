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
     * Left map over the parameter of type [AR] - Contravariant over AR type
     *
     * @param ARn New ActionResult type that you are mapping to
     * @param f
     */
    inline fun <ARn> lmapOnAR(crossinline f: (ARn) -> AR): _Saga<ARn, A> = _Saga(
        react = { ar -> this.react(f(ar)) }
    )

    /**
     * Right map on A - Covariant over the A type
     *
     * @param An
     * @param f
     */
    inline fun <An> rmapOnA(crossinline f: (A) -> An): _Saga<AR, An> = _Saga(
        react = { ar -> this.react(ar).map(f) }
    )

}

typealias Saga<AR, A> = _Saga<AR, A>
