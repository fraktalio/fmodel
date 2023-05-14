package com.fraktalio.fmodel.domain.internal

import com.fraktalio.fmodel.domain.Decider
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flattenConcat
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

/**
 * [InternalDecider] is a datatype that represents the main decision-making algorithm.
 * It has five generic parameters [C], [Si], [So], [Ei], [Eo] , representing the type of the values that [InternalDecider] may contain or use.
 * [InternalDecider] can be specialized for any type [C] or [Si] or [So] or [Ei] or [Eo] because these types does not affect its behavior.
 * [InternalDecider] behaves the same for [C]=[Int] or [C]=YourCustomType, for example.
 *
 * [InternalDecider] is a pure domain component.
 *
 * @param C Command type - contravariant/in type parameter
 * @param Si Input State type - contravariant/in type parameter
 * @param So Output State type - covariant/out type parameter
 * @param Ei Input Event type - contravariant/in type parameter
 * @param Eo Output Event type - covariant/out type parameter
 * @property decide A function/lambda that takes command of type [C] and input state of type [Si] as parameters, and returns/emits the flow of output events [Flow]<[Eo]>
 * @property evolve A function/lambda that takes input state of type [Si] and input event of type [Ei] as parameters, and returns the output/new state [So]
 * @property initialState A starting point / An initial state of type [So]
 * @constructor Creates [InternalDecider]
 *
 * @see [Decider]
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */

@PublishedApi
internal data class InternalDecider<in C, in Si, out So, in Ei, out Eo>(
    val decide: (C, Si) -> Flow<Eo>,
    val evolve: (Si, Ei) -> So,
    val initialState: So
) {
    /**
     * Left map on C/Command parameter - Contravariant
     *
     * @param Cn Command new
     * @param f
     */
    inline fun <Cn> mapLeftOnCommand(
        crossinline f: (Cn) -> C
    ): InternalDecider<Cn, Si, So, Ei, Eo> = InternalDecider(
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
    ): InternalDecider<C, Si, So, Ein, Eon> = InternalDecider(
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
    inline fun <Ein> mapLeftOnEvent(crossinline f: (Ein) -> Ei): InternalDecider<C, Si, So, Ein, Eo> =
        dimapOnEvent(f) { it }

    /**
     * Right map on E/Event parameter - Covariant
     *
     * @param Eon Event output new
     * @param f
     */
    inline fun <Eon> mapOnEvent(crossinline f: (Eo) -> Eon): InternalDecider<C, Si, So, Ei, Eon> =
        dimapOnEvent({ it }, f)

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
    ): InternalDecider<C, Sin, Son, Ei, Eo> = InternalDecider(
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
    inline fun <Sin> mapLeftOnState(crossinline f: (Sin) -> Si): InternalDecider<C, Sin, So, Ei, Eo> =
        dimapOnState(f) { it }

    /**
     * Right map on S/State parameter - Covariant
     *
     * @param Son State output new
     * @param f
     */
    inline fun <Son> mapOnState(crossinline f: (So) -> Son): InternalDecider<C, Si, Son, Ei, Eo> =
        dimapOnState({ it }, f)
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
 * @param ff of type [InternalDecider]<[C], [Si], ([So]) -> [Son], [Ei], [Eo]>
 *
 * @return new decider of type [InternalDecider]<[C], [Si], [Son], [Ei], [Eo]>
 */
@ExperimentalCoroutinesApi
internal fun <C, Si, So, Ei, Eo, Son> InternalDecider<C, Si, So, Ei, Eo>.applyOnState(
    ff: InternalDecider<C, Si, (So) -> Son, Ei, Eo>
): InternalDecider<C, Si, Son, Ei, Eo> = InternalDecider(
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
 * @return new decider of type [InternalDecider]<[C], [Si], [Pair]<[So], [Son]>, [Ei], [Eo]>
 */
@ExperimentalCoroutinesApi
@PublishedApi
internal fun <C, Si, So, Ei, Eo, Son> InternalDecider<C, Si, So, Ei, Eo>.productOnState(
    fb: InternalDecider<C, Si, Son, Ei, Eo>
): InternalDecider<C, Si, Pair<So, Son>, Ei, Eo> = applyOnState(fb.mapOnState { b: Son -> { a: So -> Pair(a, b) } })


/**
 * Combine [InternalDecider]s into one big [InternalDecider]
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
 * @return [InternalDecider]<[C_SUPER], [Pair]<[Si], [Si2]>, [Pair]<[So], [So2]>, [Ei_SUPER], [Eo_SUPER]>
 */

@ExperimentalCoroutinesApi
@PublishedApi
internal inline infix fun <reified C : C_SUPER, Si, So, reified Ei : Ei_SUPER, Eo : Eo_SUPER, reified C2 : C_SUPER, Si2, So2, reified Ei2 : Ei_SUPER, Eo2 : Eo_SUPER, C_SUPER, Ei_SUPER, Eo_SUPER> InternalDecider<C?, Si, So, Ei?, Eo?>.combine(
    y: InternalDecider<C2?, Si2, So2, Ei2?, Eo2?>
): InternalDecider<C_SUPER?, Pair<Si, Si2>, Pair<So, So2>, Ei_SUPER, Eo_SUPER?> {

    val deciderX = this.mapLeftOnCommand<C_SUPER?> { it as? C }.mapLeftOnState<Pair<Si, Si2>> { pair -> pair.first }
        .dimapOnEvent<Ei_SUPER, Eo_SUPER?>({ it as? Ei }, { it })

    val deciderY = y.mapLeftOnCommand<C_SUPER?> { it as? C2 }.mapLeftOnState<Pair<Si, Si2>> { pair -> pair.second }
        .dimapOnEvent<Ei_SUPER, Eo_SUPER?>({ it as? Ei2 }, { it })

    return deciderX.productOnState(deciderY)
}

@PublishedApi
internal fun <C, S, E> InternalDecider<C, S, S, E, E>.asDecider() = Decider(this.decide, this.evolve, this.initialState)
