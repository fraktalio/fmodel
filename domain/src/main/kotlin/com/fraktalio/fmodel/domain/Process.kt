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
 * [_Process] is a datatype that represents the central point of control deciding what to execute next ([A]).
 * It is responsible for mapping different events from aggregates into action results ([AR]) that the _[Process] then can use to calculate the next actions ([A]) to be mapped to commands of other aggregates.
 *
 * @param AR Action Result type
 * @param Si Input_State type
 * @param So Output_State type
 * @param Ei Input_Event type
 * @param Eo Output_Event type
 * @param A Action type
 * @property ingest A pure function/lambda that takes action results of type [AR] and input state of type [Si] as parameters, and returns the list of internal events [Iterable]<[Eo]>.
 * @property evolve A pure function/lambda that takes input state of type [Si] and input event of type [Ei] as parameters, and returns the output/new state [So].
 * @property react A pure function/lambda that takes input state of type [Si] and input event of type [Ei] as parameters, and returns the list of actions [Iterable]<[A]>.
 * @property pending A pure function/lambda that takes input state of type [Si], and returns the list of actions [Iterable]<[A]>.
 * @property initialState A starting point / An initial state of type [So].
 * @property isTerminal A pure function/lambda that takes input state of type [Si], and returns [Boolean] showing if the current input state is terminal/final.
 * @constructor Creates [_Process]
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
data class _Process<AR, Si, So, Ei, Eo, A>(
    val ingest: (AR, Si) -> Iterable<Eo>,
    val evolve: (Si, Ei) -> So,
    val react: (Si, Ei) -> Iterable<A>,
    val pending: (Si) -> Iterable<A>,
    val initialState: So,
    val isTerminal: (Si) -> Boolean
) {
    /**
     * Left map on AR/ActionResult parameter - Contravariant
     *
     * @param ARn Action Result new
     * @param f
     */
    inline fun <ARn> mapLeftOnActionResult(crossinline f: (ARn) -> AR): _Process<ARn, Si, So, Ei, Eo, A> = _Process(
        ingest = { arn, si -> this.ingest(f(arn), si) },
        react = { si, ei -> this.react(si, ei) },
        evolve = { si, ei -> this.evolve(si, ei) },
        pending = { si -> this.pending(si) },
        isTerminal = { si -> this.isTerminal(si) },
        initialState = this.initialState
    )

    /**
     * Right map on A/Action - Covariant over the A type
     *
     * @param An Action new
     * @param f
     */
    inline fun <An> mapOnAction(crossinline f: (A) -> An): _Process<AR, Si, So, Ei, Eo, An> = _Process(
        ingest = { ar, si -> this.ingest(ar, si) },
        react = { si, ei -> this.react(si, ei).map(f) },
        evolve = { si, ei -> this.evolve(si, ei) },
        pending = { si -> this.pending(si).map(f) },
        isTerminal = { si -> this.isTerminal(si) },
        initialState = this.initialState
    )

    /**
     * Dimap on E/Event parameter - Contravariant on input event (Ei) and Covariant on output event (Eo) = Profunctor
     *
     * @param Ein Event input new
     * @param Eon Event output new
     * @param fl left map function
     * @param fr right map function
     */
    inline fun <Ein, Eon> dimapOnEvent(
        crossinline fl: (Ein) -> Ei,
        crossinline fr: (Eo) -> Eon
    ): _Process<AR, Si, So, Ein, Eon, A> = _Process(
        ingest = { ar, si -> this.ingest(ar, si).map(fr) },
        react = { si, ein -> this.react(si, fl(ein)) },
        evolve = { si, ein -> this.evolve(si, fl(ein)) },
        pending = { si -> this.pending(si) },
        isTerminal = { si -> this.isTerminal(si) },
        initialState = this.initialState
    )

    /**
     * Left map on E/Event parameter - Contravariant
     *
     * @param Ein Event input new
     * @param f
     */
    inline fun <Ein> mapLeftOnEvent(crossinline f: (Ein) -> Ei): _Process<AR, Si, So, Ein, Eo, A> =
        dimapOnEvent(f, ::identity)

    /**
     * Right map on E/Event parameter - Covariant
     *
     * @param Eon Event output new
     * @param f
     */
    inline fun <Eon> mapOnEvent(crossinline f: (Eo) -> Eon): _Process<AR, Si, So, Ei, Eon, A> =
        dimapOnEvent(::identity, f)

    /**
     * Dimap on S/State parameter - Contravariant on input state (Si) and Covariant on output state (So) = Profunctor
     *
     * @param Sin State input new
     * @param Son State output new
     * @param fl left map function
     * @param fr right map function
     */
    inline fun <Sin, Son> dimapOnState(
        crossinline fl: (Sin) -> Si,
        crossinline fr: (So) -> Son
    ): _Process<AR, Sin, Son, Ei, Eo, A> = _Process(

        ingest = { ar, sin -> this.ingest(ar, fl(sin)) },
        react = { sin, ei -> this.react(fl(sin), ei) },
        evolve = { sin, ei -> fr(this.evolve(fl(sin), ei)) },
        pending = { sin -> this.pending(fl(sin)) },
        isTerminal = { sin -> this.isTerminal(fl(sin)) },
        initialState = fr(this.initialState)
    )

    /**
     * Left map on S/State parameter - Contravariant
     *
     * @param Sin State input new
     * @param f
     */
    inline fun <Sin> mapLeftOnState(crossinline f: (Sin) -> Si): _Process<AR, Sin, So, Ei, Eo, A> =
        dimapOnState(f, ::identity)

    /**
     * Right map on S/State parameter - Covariant
     *
     * @param Son State output new
     * @param f
     */
    inline fun <Son> mapOnState(crossinline f: (So) -> Son): _Process<AR, Si, Son, Ei, Eo, A> =
        dimapOnState(::identity, f)

    /**
     * Right apply on S/State parameter - Applicative
     *
     * @param Son State output new
     * @param ff
     */
    fun <Son> applyOnState(ff: _Process<AR, Si, (So) -> Son, Ei, Eo, A>): _Process<AR, Si, Son, Ei, Eo, A> = _Process(

        ingest = { ar, si -> ff.ingest(ar, si).plus(this.ingest(ar, si)) },
        react = { si, ei -> ff.react(si, ei).plus(this.react(si, ei)) },
        evolve = { si, ei -> ff.evolve(si, ei).invoke(this.evolve(si, ei)) },
        pending = { si -> ff.pending(si).plus(this.pending(si)) },
        isTerminal = { si -> this.isTerminal(si) && ff.isTerminal(si) },
        initialState = ff.initialState.invoke(this.initialState)
    )

    /**
     * Right product on S/State parameter - Applicative
     *
     * @param Son State output new
     * @param fb
     */
    fun <Son> productOnState(fb: _Process<AR, Si, Son, Ei, Eo, A>): _Process<AR, Si, Pair<So, Son>, Ei, Eo, A> =
        applyOnState(fb.mapOnState { b: Son -> { a: So -> Pair(a, b) } })

}

/**
 * Combine [_Process]es into one big [_Process]
 *
 * @param AR Action Result type
 * @param Si Input_State type
 * @param So Output_State type
 * @param Ei Input_Event type
 * @param Eo Output_Event type
 * @param A Action type
 * @param AR2 Action Result type of the second process
 * @param Si2 Input_State type of the second process
 * @param So2 Output_State type of the second process
 * @param Ei2 Input_Event type of the second process
 * @param Eo2 Output_Event type of the second process
 * @param A2 Action type of the second process
 * @param AR_SUPER super type of the action result types [AR] and [AR2]
 * @param Ei_SUPER super type of the event types [Ei] and [Ei2]
 * @param Eo_SUPER super type of the event types [Eo] and [Eo2]
 * @param A_SUPER super type of the action types [A] and [A2]
 * @param y second Process
 * @return  _Process<AR_SUPER, Pair<Si, Si2>, Pair<So, So2>, Ei_SUPER, Eo_SUPER, A_SUPER>
 */
inline fun <reified AR : AR_SUPER, Si, So, reified Ei : Ei_SUPER, reified Eo : Eo_SUPER, reified A : A_SUPER, reified AR2 : AR_SUPER, Si2, So2, reified Ei2 : Ei_SUPER, reified Eo2 : Eo_SUPER, reified A2 : A_SUPER, AR_SUPER, Ei_SUPER, Eo_SUPER, A_SUPER> _Process<AR?, Si, So, Ei?, Eo, A>.combine(
    y: _Process<AR2?, Si2, So2, Ei2?, Eo2, A2>
): _Process<in AR_SUPER, in Pair<Si, Si2>, out Pair<So, So2>, in Ei_SUPER, out Eo_SUPER, out A_SUPER> {

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
    val extractAR1: (AR_SUPER) -> AR? = {
        when (it) {
            is AR -> it
            else -> null
        }
    }
    val extractAR2: (AR_SUPER) -> AR2? = {
        when (it) {
            is AR2 -> it
            else -> null
        }
    }

    val extractA1SUPER: (A) -> A_SUPER = { it }
    val extractA2SUPER: (A2) -> A_SUPER = { it }

    val extractEoSUPER: (Eo) -> Eo_SUPER = { it }
    val extractEo2SUPER: (Eo2) -> Eo_SUPER = { it }

    val processX = this
        .mapLeftOnActionResult(extractAR1)
        .mapLeftOnState(extractS1)
        .mapOnAction(extractA1SUPER)
        .dimapOnEvent(extractE1, extractEoSUPER)

    val processY = y
        .mapLeftOnActionResult(extractAR2)
        .mapLeftOnState(extractS2)
        .mapOnAction(extractA2SUPER)
        .dimapOnEvent(extractE2, extractEo2SUPER)

    val processZ = processX.productOnState(processY)

    return _Process(
        ingest = { ar, si -> processZ.ingest(ar, si) },
        react = { si, ei -> processZ.react(si, ei) },
        evolve = { si, ei -> processZ.evolve(si, ei) },
        pending = { si -> processZ.pending(si) },
        isTerminal = { pair -> processZ.isTerminal(pair) },
        initialState = processZ.initialState
    )
}

/**
 * Combine [_Process]es into one big [_Process]
 *
 * @param AR Action Result type
 * @param Si Input_State type
 * @param So Output_State type
 * @param Ei Input_Event type
 * @param Eo Output_Event type
 * @param A Action type
 * @param AR2 Action Result type of the second process
 * @param Si2 Input_State type of the second process
 * @param So2 Output_State type of the second process
 * @param Ei2 Input_Event type of the second process
 * @param Eo2 Output_Event type of the second process
 * @param A2 Action type of the second process
 * @param AR_SUPER super type of the action result types [AR] and [AR2]
 * @param Ei_SUPER super type of the event types [Ei] and [Ei2]
 * @param Eo_SUPER super type of the event types [Eo] and [Eo2]
 * @param Si_SUPER super type of the event types [Si] and [Si2]
 * @param So_SUPER super type of the event types [So] and [So2]
 * @param A_SUPER super type of the action types [A] and [A2]
 * @param y second Process
 * @return  _Process<AR_SUPER, List<Si_SUPER>, List<So_SUPER>, Ei_SUPER, Eo_SUPER, A_SUPER>
 */
inline fun <reified AR : AR_SUPER, reified Si : Si_SUPER, So : So_SUPER, reified Ei : Ei_SUPER, reified Eo : Eo_SUPER, reified A : A_SUPER, reified AR2 : AR_SUPER, reified Si2 : Si_SUPER, So2 : So_SUPER, reified Ei2 : Ei_SUPER, reified Eo2 : Eo_SUPER, reified A2 : A_SUPER, AR_SUPER, Si_SUPER, So_SUPER, Ei_SUPER, Eo_SUPER, A_SUPER> _Process<in AR?, in List<Si>, out List<So>, in Ei?, out Eo, out A>.combineL(
    y: _Process<in AR2?, in List<Si2>, out List<So2>, in Ei2?, out Eo2, out A2>
): _Process<in AR_SUPER, in List<Si_SUPER>, out List<So_SUPER>, in Ei_SUPER, out Eo_SUPER, out A_SUPER> {

    val extractS1: (List<Si_SUPER>) -> List<Si> = { list -> list.filterIsInstance(Si::class.java) }
    val extractS2: (List<Si_SUPER>) -> List<Si2> = { list -> list.filterIsInstance(Si2::class.java) }

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
    val extractAR1: (AR_SUPER) -> AR? = {
        when (it) {
            is AR -> it
            else -> null
        }
    }
    val extractAR2: (AR_SUPER) -> AR2? = {
        when (it) {
            is AR2 -> it
            else -> null
        }
    }

    val extractA1SUPER: (A) -> A_SUPER = { it }
    val extractA2SUPER: (A2) -> A_SUPER = { it }

    val extractEoSUPER: (Eo) -> Eo_SUPER = { it }
    val extractEo2SUPER: (Eo2) -> Eo_SUPER = { it }

    val processX = this
        .mapLeftOnActionResult(extractAR1)
        .mapLeftOnState(extractS1)
        .mapOnAction(extractA1SUPER)
        .dimapOnEvent(extractE1, extractEoSUPER)

    val processY = y
        .mapLeftOnActionResult(extractAR2)
        .mapLeftOnState(extractS2)
        .mapOnAction(extractA2SUPER)
        .dimapOnEvent(extractE2, extractEo2SUPER)

    val processZ =
        processX.productOnState(processY).mapOnState { pair: Pair<List<So>, List<So2>> -> pair.toList().flatten() }

    return _Process(
        ingest = { ar, si -> processZ.ingest(ar, si) },
        react = { si, ei -> processZ.react(si, ei) },
        evolve = { si, ei -> processZ.evolve(si, ei) },
        pending = { si -> processZ.pending(si) },
        isTerminal = { pair -> processZ.isTerminal(pair) },
        initialState = processZ.initialState
    )
}

/**
 * A typealias for [_Process]<AR, Si, So, Ei, Eo, A>, specializing the [_Process] to four generic parameters: AR, S, E, A, where AR=AR, Si=S, So=S, Ei=E, Eo=E, A=A
 */
typealias Process<AR, S, E, A> = _Process<AR, S, S, E, E, A>
