/*
 * Copyright (c) 2022 Fraktalio D.O.O. All rights reserved.
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

import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*

/**
 * An Interface of the [_Decider].
 *
 * @param C Command type - contravariant/in type parameter
 * @param Si Input State type - contravariant/in type parameter
 * @param So Output State type - covariant/out type parameter
 * @param Ei Input Event type - contravariant/in type parameter
 * @param Eo Output Event type - covariant/out type parameter
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
interface I_Decider<in C, in Si, out So, in Ei, out Eo> {
    val decide: (C, Si) -> Flow<Eo>
    val evolve: (Si, Ei) -> So
    val initialState: So
}

/**
 * A convenient typealias for the [I_Decider] interface. It is specializing the five parameters [I_Decider] interface to only three parameters interface [IDecider].
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
typealias IDecider<C, S, E> = I_Decider<C, S, S, E, E>

/**
 * A typealias for [_Decider]<C, Si, So, Ei, Eo>, specializing the [_Decider] to three generic parameters: C, S and E, where C=C, Si=S, So=S, Ei=E, Eo=E
 */
typealias Decider<C, S, E> = _Decider<C, S, S, E, E>

/**
 * Decider DSL - A convenient builder DSL for the [Decider]
 *
 *
 * Example:
 *
 * ```kotlin
 * fun evenNumberDecider(): Decider<EvenNumberCommand?, EvenNumberState, EvenNumberEvent?> =
 *    decider {
 *        initialState { EvenNumberState(Description("Initial state"), NumberValue(0)) }
 *        decide { c, s ->
 *            if (c != null && c.value.get > 1000) flow<EvenNumberEvent> { throw UnsupportedOperationException("Sorry") } else
 *                when (c) {
 *                    is AddEvenNumber -> flowOf(EvenNumberAdded(c.description, s.value + c.value))
 *                    is SubtractEvenNumber -> flowOf(EvenNumberSubtracted(c.description, s.value - c.value))
 *                    null -> emptyFlow()
 *                }
 *        }
 *        evolve { s, e ->
 *            when (e) {
 *                is EvenNumberAdded -> EvenNumberState(s.description + e.description, e.value)
 *                is EvenNumberSubtracted -> EvenNumberState(s.description - e.description, e.value)
 *                null -> s
 *            }
 *        }
 *    }
 * ```
 */
fun <C, S, E> decider(block: DeciderBuilder<C, S, E>.() -> Unit): Decider<C, S, E> =
    DeciderBuilder<C, S, E>().apply(block).build()

/**
 * Decider builder
 */
class DeciderBuilder<C, S, E> internal constructor() {

    private var decide: (C, S) -> Flow<E> = { _, _ -> emptyFlow() }
    private var evolve: (S, E) -> S = { s, _ -> s }
    private var initialState: () -> S = { error("Initial State is not initialized") }

    fun decide(lambda: (C, S) -> Flow<E>) {
        decide = lambda
    }

    fun evolve(lambda: (S, E) -> S) {
        evolve = lambda
    }

    fun initialState(lambda: () -> S) {
        initialState = lambda
    }

    fun build(): Decider<C, S, E> = Decider(decide, evolve, initialState())
}

/**
 * [_Decider] is a datatype that represents the main decision-making algorithm.
 * It has five generic parameters [C], [Si], [So], [Ei], [Eo] , representing the type of the values that [_Decider] may contain or use.
 * [_Decider] can be specialized for any type [C] or [Si] or [So] or [Ei] or [Eo] because these types does not affect its behavior.
 * [_Decider] behaves the same for [C]=[Int] or [C]=YourCustomType, for example.
 *
 * [_Decider] is a pure domain component.
 *
 * @param C Command type - contravariant/in type parameter
 * @param Si Input State type - contravariant/in type parameter
 * @param So Output State type - covariant/out type parameter
 * @param Ei Input Event type - contravariant/in type parameter
 * @param Eo Output Event type - covariant/out type parameter
 * @property decide A function/lambda that takes command of type [C] and input state of type [Si] as parameters, and returns/emits the flow of output events [Flow]<[Eo]>
 * @property evolve A function/lambda that takes input state of type [Si] and input event of type [Ei] as parameters, and returns the output/new state [So]
 * @property initialState A starting point / An initial state of type [So]
 * @constructor Creates [_Decider]
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
data class _Decider<in C, in Si, out So, in Ei, out Eo>(
    override val decide: (C, Si) -> Flow<Eo>,
    override val evolve: (Si, Ei) -> So,
    override val initialState: So
) : I_Decider<C, Si, So, Ei, Eo> {
    /**
     * Left map on C/Command parameter - Contravariant
     *
     * @param Cn Command new
     * @param f
     */
    inline fun <Cn> mapLeftOnCommand(
        crossinline f: (Cn) -> C
    ): _Decider<Cn, Si, So, Ei, Eo> = _Decider(
        decide = { cn, si -> decide(f(cn), si) },
        evolve = { si, ei -> evolve(si, ei) },
        initialState = initialState
    )

    /**
     * Dimap on E/Event parameter - Contravariant on input event and Covariant on output event = Profunctor
     *
     * @param Ein Event input new
     * @param Eon Event output new
     * @param fl
     * @param fr
     */
    inline fun <Ein, Eon> dimapOnEvent(
        crossinline fl: (Ein) -> Ei, crossinline fr: (Eo) -> Eon
    ): _Decider<C, Si, So, Ein, Eon> = _Decider(
        decide = { c, si -> decide(c, si).map { fr(it) } },
        evolve = { si, ein -> evolve(si, fl(ein)) },
        initialState = initialState
    )

    /**
     * Left map on E/Event parameter - Contravariant
     *
     * @param Ein Event input new
     * @param f
     */
    inline fun <Ein> mapLeftOnEvent(crossinline f: (Ein) -> Ei): _Decider<C, Si, So, Ein, Eo> = dimapOnEvent(f) { it }

    /**
     * Right map on E/Event parameter - Covariant
     *
     * @param Eon Event output new
     * @param f
     */
    inline fun <Eon> mapOnEvent(crossinline f: (Eo) -> Eon): _Decider<C, Si, So, Ei, Eon> = dimapOnEvent({ it }, f)

    /**
     * Dimap on S/State parameter - Contravariant on input state (Si) and Covariant on output state (So) = Profunctor
     *
     * @param Sin State input new
     * @param Son State output new
     * @param fl
     * @param fr
     */
    inline fun <Sin, Son> dimapOnState(
        crossinline fl: (Sin) -> Si, crossinline fr: (So) -> Son
    ): _Decider<C, Sin, Son, Ei, Eo> = _Decider(
        decide = { c, sin -> decide(c, fl(sin)) },
        evolve = { sin, ei -> fr(evolve(fl(sin), ei)) },
        initialState = fr(initialState)
    )

    /**
     * Left map on S/State parameter - Contravariant
     *
     * @param Sin State input new
     * @param f
     */
    inline fun <Sin> mapLeftOnState(crossinline f: (Sin) -> Si): _Decider<C, Sin, So, Ei, Eo> = dimapOnState(f) { it }

    /**
     * Right map on S/State parameter - Covariant
     *
     * @param Son State output new
     * @param f
     */
    inline fun <Son> mapOnState(crossinline f: (So) -> Son): _Decider<C, Si, Son, Ei, Eo> = dimapOnState({ it }, f)
}

/**
 * Apply on S/State - Applicative
 *
 * @param C Command type
 * @param Si Input_State type
 * @param So Output_State type
 * @param Ei Input_Event type
 * @param Eo Output_Event type
 * @param Son Output_State type new
 *
 * @param ff of type [_Decider]<[C], [Si], ([So]) -> [Son], [Ei], [Eo]>
 *
 * @return new decider of type [_Decider]<[C], [Si], [Son], [Ei], [Eo]>
 */
@FlowPreview
fun <C, Si, So, Ei, Eo, Son> _Decider<C, Si, So, Ei, Eo>.applyOnState(
    ff: _Decider<C, Si, (So) -> Son, Ei, Eo>
): _Decider<C, Si, Son, Ei, Eo> = _Decider(
    decide = { c, si -> flowOf(ff.decide(c, si), decide(c, si)).flattenConcat() },
    evolve = { si, ei -> ff.evolve(si, ei)(evolve(si, ei)) },
    initialState = ff.initialState(initialState)
)

/**
 * Product on S/State parameter - Applicative
 *
 * @param C Command type
 * @param Si Input_State type
 * @param So Output_State type
 * @param Ei Input_Event type
 * @param Eo Output_Event type
 * @param Son Output_State type new
 * @param fb
 *
 * @return new decider of type [_Decider]<[C], [Si], [Pair]<[So], [Son]>, [Ei], [Eo]>
 */
@FlowPreview
fun <C, Si, So, Ei, Eo, Son> _Decider<C, Si, So, Ei, Eo>.productOnState(
    fb: _Decider<C, Si, Son, Ei, Eo>
): _Decider<C, Si, Pair<So, Son>, Ei, Eo> = applyOnState(fb.mapOnState { b: Son -> { a: So -> Pair(a, b) } })


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
@FlowPreview
inline infix fun <reified C : C_SUPER, Si, So, reified Ei : Ei_SUPER, Eo : Eo_SUPER, reified C2 : C_SUPER, Si2, So2, reified Ei2 : Ei_SUPER, Eo2 : Eo_SUPER, C_SUPER, Ei_SUPER, Eo_SUPER> _Decider<C?, Si, So, Ei?, Eo>.combine(
    y: _Decider<C2?, Si2, So2, Ei2?, Eo2>
): _Decider<C_SUPER, Pair<Si, Si2>, Pair<So, So2>, Ei_SUPER, Eo_SUPER> {

    val deciderX = this.mapLeftOnCommand<C_SUPER> { it as? C }.mapLeftOnState<Pair<Si, Si2>> { pair -> pair.first }
        .dimapOnEvent<Ei_SUPER, Eo_SUPER>({ it as? Ei }, { it })

    val deciderY = y.mapLeftOnCommand<C_SUPER> { it as? C2 }.mapLeftOnState<Pair<Si, Si2>> { pair -> pair.second }
        .dimapOnEvent<Ei_SUPER, Eo_SUPER>({ it as? Ei2 }, { it })

    return deciderX.productOnState(deciderY)
}
