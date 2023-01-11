/*
 * Copyright (c) 2023 Fraktalio D.O.O. All rights reserved.
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

import com.fraktalio.fmodel.domain.internal.InternalDecider
import com.fraktalio.fmodel.domain.internal.asDecider
import com.fraktalio.fmodel.domain.internal.combine
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow


/**
 * Decider Interface
 *
 * @param C Command
 * @param S State
 * @param E Event
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
interface IDecider<in C, S, E> {
    val decide: (C, S) -> Flow<E>
    val evolve: (S, E) -> S
    val initialState: S
}

/**
 * [Decider] is a datatype that represents the main decision-making algorithm.
 * It has three generic parameters `C`, `S`, `E` , representing the type of the values that [Decider] may contain or use.
 * [Decider] can be specialized for any type `C` or `S` or `E` because these types does not affect its behavior.
 * [Decider] behaves the same for `C`=[Int] or `C`=`OddNumberCommand`.
 *
 * Example:
 *
 * ```kotlin
 * fun oddNumberDecider(): Decider<OddNumberCommand?, OddNumberState, OddNumberEvent?> =
 *     Decider(
 *         initialState = OddNumberState(Description("Initial state"), NumberValue(0)),
 *         decide = { c, s ->
 *             when (c) {
 *                 is AddOddNumber -> flowOf(OddNumberAdded(c.description, s.value + c.value))
 *                 is SubtractOddNumber -> flowOf(OddNumberSubtracted(c.description, s.value - c.value))
 *                 null -> emptyFlow()
 *             }
 *         },
 *         evolve = { s, e ->
 *             when (e) {
 *                 is OddNumberAdded -> OddNumberState(s.description + e.description, e.value)
 *                 is OddNumberSubtracted -> OddNumberState(s.description - e.description, e.value)
 *                 null -> s
 *             }
 *         }
 *     )
 * ```
 *
 * @param C Command
 * @param S State
 * @param E Event
 * @property decide A function/lambda that takes command of type [C] and input state of type [S] as parameters, and returns/emits the flow of output events [Flow]<[E]>
 * @property evolve A function/lambda that takes input state of type [S] and input event of type [E] as parameters, and returns the output/new state [S]
 * @property initialState A starting point / An initial state of type [S]
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
data class Decider<in C, S, E>(
    override val decide: (C, S) -> Flow<E>,
    override val evolve: (S, E) -> S,
    override val initialState: S
) : IDecider<C, S, E> {
    /**
     * Left map on C/Command parameter
     *
     * @param Cn Command new
     * @param f Function that maps type `Cn` to `C`
     *
     * @return New Decider of type [Decider]<[Cn], [S], [E]>
     */
    inline fun <Cn> mapLeftOnCommand(
        crossinline f: (Cn) -> C
    ): Decider<Cn, S, E> = InternalDecider(decide, evolve, initialState).mapLeftOnCommand(f).asDecider()

    /**
     * Di-map on E/Event parameter
     *
     * @param En Event new
     * @param fl Function that maps type `En` to `E`
     * @param fr Function that maps type `E` to `En`
     *
     * @return New Decider of type [Decider]<[C], [S], [En]>
     */
    inline fun <En> dimapOnEvent(
        crossinline fl: (En) -> E, crossinline fr: (E) -> En
    ): Decider<C, S, En> = InternalDecider(decide, evolve, initialState).dimapOnEvent(fl, fr).asDecider()

    /**
     * Di-map on S/State parameter
     *
     * @param Sn State new
     * @param fl Function that maps type `Sn` to `S`
     * @param fr Function that maps type `S` to `Sn`
     */
    inline fun <Sn> dimapOnState(
        crossinline fl: (Sn) -> S, crossinline fr: (S) -> Sn
    ): Decider<C, Sn, E> = InternalDecider(decide, evolve, initialState).dimapOnState(fl, fr).asDecider()

}

/**
 * Combine [Decider]s into one [Decider]
 *
 * Possible to use when:
 *
 * - [E] and [E2] have common superclass [E_SUPER]
 * - [C] and [C2] have common superclass [C_SUPER]
 *
 * @param C Command type of the first Decider
 * @param S State type of the first Decider
 * @param E Event type of the first Decider
 * @param C2 Command type of the second Decider
 * @param S2 Input_State type of the second Decider
 * @param E2 Event type of the second Decider
 * @param C_SUPER super type of the command types C and C2
 * @param E_SUPER super type of the E and E2 types
 * @param y second Decider
 * @return [Decider]<[C_SUPER], [Pair]<[S], [S2]>, [E_SUPER]>
 */
@FlowPreview
inline infix fun <reified C : C_SUPER, S, reified E : E_SUPER, reified C2 : C_SUPER, S2, reified E2 : E_SUPER, C_SUPER, E_SUPER> Decider<C?, S, E?>.combine(
    y: Decider<C2?, S2, E2?>
): Decider<C_SUPER?, Pair<S, S2>, E_SUPER?> =
    InternalDecider(decide, evolve, initialState).combine(InternalDecider(y.decide, y.evolve, y.initialState))
        .asDecider()


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

