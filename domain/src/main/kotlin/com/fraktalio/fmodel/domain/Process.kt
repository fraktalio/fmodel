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

import arrow.core.identity

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
     * Left map over [AR] parameter - Contravariant
     *
     * @param ARn Action Result new
     * @param f
     */
    inline fun <ARn> lmapOnAR(crossinline f: (ARn) -> AR): _Process<ARn, Si, So, Ei, Eo, A> = _Process(
        ingest = { arn, si -> this.ingest(f(arn), si) },
        react = { si, ei -> this.react(si, ei) },
        evolve = { si, ei -> this.evolve(si, ei) },
        pending = { si -> this.pending(si) },
        isTerminal = { si -> this.isTerminal(si) },
        initialState = this.initialState
    )

    /**
     * Right map over A - Covariant over the A type
     *
     * @param An Action new
     * @param f
     */
    inline fun <An> rmapOnA(crossinline f: (A) -> An): _Process<AR, Si, So, Ei, Eo, An> = _Process(
        ingest = { ar, si -> this.ingest(ar, si) },
        react = { si, ei -> this.react(si, ei).map(f) },
        evolve = { si, ei -> this.evolve(si, ei) },
        pending = { si -> this.pending(si).map(f) },
        isTerminal = { si -> this.isTerminal(si) },
        initialState = this.initialState
    )

    /**
     * Dimap over E/Event parameter - Contravariant over input event (Ei) and Covariant over output event (Eo) = Profunctor
     *
     * @param Ein Event input new
     * @param Eon Event output new
     * @param fl left map function
     * @param fr right map function
     */
    inline fun <Ein, Eon> dimapOnE(
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
     * Left map over E/Event parameter - Contravariant
     *
     * @param Ein Event input new
     * @param f
     */
    inline fun <Ein> lmapOnE(crossinline f: (Ein) -> Ei): _Process<AR, Si, So, Ein, Eo, A> =
        dimapOnE(f, ::identity)

    /**
     * Right map over E/Event parameter - Covariant
     *
     * @param Eon Event output new
     * @param f
     */
    inline fun <Eon> rmapOnE(crossinline f: (Eo) -> Eon): _Process<AR, Si, So, Ei, Eon, A> =
        dimapOnE(::identity, f)

    /**
     * Dimap over S/State parameter - Contravariant over input state (Si) and Covariant over output state (So) = Profunctor
     *
     * @param Sin State input new
     * @param Son State output new
     * @param fl left map function
     * @param fr right map function
     */
    inline fun <Sin, Son> dimapOnS(
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
     * Left map over S/State parameter - Contravariant
     *
     * @param Sin State input new
     * @param f
     */
    inline fun <Sin> lmapOnS(crossinline f: (Sin) -> Si): _Process<AR, Sin, So, Ei, Eo, A> =
        dimapOnS(f, ::identity)

    /**
     * Right map over S/State parameter - Covariant
     *
     * @param Son State output new
     * @param f
     */
    inline fun <Son> rmapOnS(crossinline f: (So) -> Son): _Process<AR, Si, Son, Ei, Eo, A> =
        dimapOnS(::identity, f)

    /**
     * Right apply over S/State parameter - Applicative
     *
     * @param Son State output new
     * @param ff
     */
    fun <Son> rapplyOnS(ff: _Process<AR, Si, (So) -> Son, Ei, Eo, A>): _Process<AR, Si, Son, Ei, Eo, A> = _Process(

        ingest = { ar, si -> ff.ingest(ar, si).plus(this.ingest(ar, si)) },
        react = { si, ei -> ff.react(si, ei).plus(this.react(si, ei)) },
        evolve = { si, ei -> ff.evolve(si, ei).invoke(this.evolve(si, ei)) },
        pending = { si -> ff.pending(si).plus(this.pending(si)) },
        isTerminal = { si -> this.isTerminal(si) && ff.isTerminal(si) },
        initialState = ff.initialState.invoke(this.initialState)
    )

    /**
     * Right product over S/State parameter - Applicative
     *
     * @param Son State output new
     * @param fb
     */
    fun <Son> rproductOnS(fb: _Process<AR, Si, Son, Ei, Eo, A>): _Process<AR, Si, Pair<So, Son>, Ei, Eo, A> =
        rapplyOnS(fb.rmapOnS { b: Son -> { a: So -> Pair(a, b) } })

}


/**
 * A typealias for [_Process]<AR, Si, So, Ei, Eo, A>, specializing the [_Process] to four generic parameters: AR, S, E, A, where AR=AR, Si=S, So=S, Ei=E, Eo=E, A=A
 */
typealias Process<AR, S, E, A> = _Process<AR, S, S, E, E, A>
