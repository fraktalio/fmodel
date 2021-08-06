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
 * [_Decider] is a datatype that represents the main decision making algorithm.
 * It has five generic parameters [C], [Si], [So], [Ei], [Eo] , representing the type of the values that [_Decider] may contain or use.
 * [_Decider] can be specialized for any type [C] or [Si] or [So] or [Ei] or [Eo] because these types does not affect its behavior.
 * [_Decider] behaves the same for [C]=[Int] or [C]=YourCustomType, for example.
 *
 * [_Decider] is a pure domain component.
 *
 * @param C Command type
 * @param Si Input_State type
 * @param So Output_State type
 * @param Ei Input_Event type
 * @param Eo Output_Event type
 * @property decide A function/lambda that takes command of type [C] and input state of type [Si] as parameters, and returns/emits the flow of output events [Flow]<[Eo]>
 * @property evolve A function/lambda that takes input state of type [Si] and input event of type [Ei] as parameters, and returns the output/new state [So]
 * @property initialState A starting point / An initial state of type [So]
 * @constructor Creates [_Decider]
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
data class _Decider<C, Si, So, Ei, Eo>(
    val decide: (C, Si) -> Flow<Eo>,
    val evolve: suspend (Si, Ei) -> So,
    val initialState: So
) {
    /**
     * Left map on C/Command parameter - Contravariant
     *
     * @param Cn Command new
     * @param f
     */
    inline fun <Cn> mapLeftOnCommand(crossinline f: (Cn) -> C): _Decider<Cn, Si, So, Ei, Eo> = _Decider(
        decide = { cn, s -> this.decide(f(cn), s) },
        evolve = { si, ei -> this.evolve(si, ei) },
        initialState = this.initialState
    )

    /**
     *
     *
     * Dimap on E/Event parameter - Contravariant on input event and Covariant on output event = Profunctor
     *
     * @param Ein Event input new
     * @param Eon Event output new
     * @param fl
     * @param fr
     */
    inline fun <Ein, Eon> dimapOnEvent(
        crossinline fl: (Ein) -> Ei,
        crossinline fr: suspend (Eo) -> Eon
    ): _Decider<C, Si, So, Ein, Eon> = _Decider(
        decide = { c, si -> this.decide(c, si).map(fr) },
        evolve = { si, ein -> this.evolve(si, fl(ein)) },
        initialState = this.initialState
    )

    /**
     * Left map on E/Event parameter - Contravariant
     *
     * @param Ein Event input new
     * @param f
     */
    inline fun <Ein> mapLeftOnEvent(crossinline f: (Ein) -> Ei): _Decider<C, Si, So, Ein, Eo> =
        dimapOnEvent(f, ::identity)

    /**
     * Right map on E/Event parameter - Covariant
     *
     * @param Eon Event output new
     * @param f
     */
    inline fun <Eon> mapOnEvent(crossinline f: suspend (Eo) -> Eon): _Decider<C, Si, So, Ei, Eon> =
        dimapOnEvent(::identity, f)

    /**
     * Dimap on S/State parameter - Contravariant on input state (Si) and Covariant on output state (So) = Profunctor
     *
     * @param Sin State input new
     * @param Son State output new
     * @param fl
     * @param fr
     */
    inline fun <Sin, Son> dimapOnState(
        crossinline fl: (Sin) -> Si,
        crossinline fr: (So) -> Son
    ): _Decider<C, Sin, Son, Ei, Eo> = _Decider(
        decide = { c, sin -> this.decide(c, fl(sin)) },
        evolve = { sin, ei -> fr(this.evolve(fl(sin), ei)) },
        initialState = fr(this.initialState)
    )

    /**
     * Left map on S/State parameter - Contravariant
     *
     * @param Sin State input new
     * @param f
     */
    inline fun <Sin> mapLeftOnState(crossinline f: (Sin) -> Si): _Decider<C, Sin, So, Ei, Eo> =
        dimapOnState(f, ::identity)

    /**
     * Right map on S/State parameter - Covariant
     *
     * @param Son State output new
     * @param f
     */
    inline fun <Son> mapOnState(crossinline f: (So) -> Son): _Decider<C, Si, Son, Ei, Eo> =
        dimapOnState(::identity, f)


    /**
     * Right apply on S/State parameter - Applicative
     *
     * @param Son State output new
     * @param ff
     */
    fun <Son> applyOnState(ff: _Decider<C, Si, (So) -> Son, Ei, Eo>): _Decider<C, Si, Son, Ei, Eo> = _Decider(
        decide = { c, si -> flowOf(ff.decide(c, si), this.decide(c, si)).flattenConcat() },
        evolve = { si, ei -> ff.evolve(si, ei).invoke(this.evolve(si, ei)) },
        initialState = ff.initialState.invoke(this.initialState)
    )

    /**
     * Right product on S/State parameter - Applicative
     *
     * @param Son State output new
     * @param fb
     */
    fun <Son> productOnState(fb: _Decider<C, Si, Son, Ei, Eo>): _Decider<C, Si, Pair<So, Son>, Ei, Eo> =
        applyOnState(fb.mapOnState { b: Son -> { a: So -> Pair(a, b) } })
}


/**
 * Combine [_Decider]s into one big [_Decider]
 *
 * Possible to use when:
 *
 * - [Ei] and [Ei2] have common superclass [Ei_SUPER]
 * - [Eo] and [Eo2] have common superclass [Eo_SUPER]
 * - [C] and [C2] have common superclass [C_SUPER]
 *
 * @param C Command type of the first Decider
 * @param Si Input_State type of the first Decider
 * @param So Output_State type of the first Decider
 * @param Ei Input_Event type of the first Decider
 * @param Eo Output_Event type of the first Decider
 * @param C2 Command type of the second Decider
 * @param Si2 Input_State type of the second Decider
 * @param So2 Output_State type of the second Decider
 * @param Ei2 Input_Event type of the second Decider
 * @param Eo2 Output_Event type of the second Decider
 * @param C_SUPER super type of the command types C and C2
 * @param Ei_SUPER super type of the Ei and Ei2 types
 * @param Eo_SUPER super type of the Eo and Eo2 types
 * @param y second Decider
 * @return [_Decider]<[C_SUPER], [Pair]<[Si], [Si2]>, [Pair]<[So], [So2]>, [Ei_SUPER], [Eo_SUPER]>
 */
inline fun <reified C : C_SUPER, Si, So, reified Ei : Ei_SUPER, reified Eo : Eo_SUPER, reified C2 : C_SUPER, Si2, So2, reified Ei2 : Ei_SUPER, reified Eo2 : Eo_SUPER, C_SUPER, Ei_SUPER, Eo_SUPER> _Decider<in C?, in Si, out So, in Ei?, out Eo>.combine(
    y: _Decider<in C2?, in Si2, out So2, in Ei2?, out Eo2>
): _Decider<C_SUPER, Pair<Si, Si2>, Pair<So, So2>, Ei_SUPER, Eo_SUPER> {

    val extractS1: (Pair<Si, Si2>) -> Si = { pair -> pair.first }
    val extractS2: (Pair<Si, Si2>) -> Si2 = { pair -> pair.second }
    val extractE1: (Ei_SUPER) -> Ei? = {
        when (it) {
            is Ei -> it
            else -> null
        }
    }
    val extractE2: (Ei_SUPER) -> Ei2? = {
        when (it) {
            is Ei2 -> it
            else -> null
        }
    }
    val extractC1: (C_SUPER) -> C? = {
        when (it) {
            is C -> it
            else -> null
        }
    }
    val extractC2: (C_SUPER) -> C2? = {
        when (it) {
            is C2 -> it
            else -> null
        }
    }
    val extractEoSUPER: suspend (Eo) -> Eo_SUPER = { it }
    val extractEo2SUPER: suspend (Eo2) -> Eo_SUPER = { it }

    val deciderX = this
        .mapLeftOnCommand(extractC1)
        .mapLeftOnState(extractS1)
        .dimapOnEvent(extractE1, extractEoSUPER)

    val deciderY = y
        .mapLeftOnCommand(extractC2)
        .mapLeftOnState(extractS2)
        .dimapOnEvent(extractE2, extractEo2SUPER)

    val deciderZ = deciderX.productOnState(deciderY)

    return _Decider(
        decide = { c, si -> deciderZ.decide(c, si) },
        evolve = { pair, ei -> deciderZ.evolve(pair, ei) },
        initialState = deciderZ.initialState
    )
}


/**
 * A typealias for [_Decider]<C, Si, So, Ei, Eo>, specializing the [_Decider] to three generic parameters: C, S and E, where C=C, Si=S, So=S, Ei=E, Eo=E
 */
typealias Decider<C, S, E> = _Decider<C, S, S, E, E>
