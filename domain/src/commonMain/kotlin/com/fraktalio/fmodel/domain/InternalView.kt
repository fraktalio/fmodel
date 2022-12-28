package com.fraktalio.fmodel.domain

/**
 * [InternalView] is a datatype that represents the event handling algorithm,
 * responsible for translating the events into denormalized state,
 * which is more adequate for querying.
 *
 * It has three generic parameters [Si], [So], [E], representing the type of the values that [InternalView] may contain or use.
 * [InternalView] can be specialized for any type of [Si], [So], [E] because these types does not affect its behavior.
 * [InternalView] behaves the same for [E]=[Int] or [E]=YourCustomType, for example.
 *
 * [InternalView] is a pure domain component
 *
 * @param Si Input State type
 * @param So Output State type
 * @param E Event type
 * @property evolve A pure function/lambda that takes input state of type [Si] and input event of type [E] as parameters, and returns the output/new state [So]
 * @property initialState A starting point / An initial state of type [So]
 * @constructor Creates [InternalView]
 *
 * @see [View]
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */

@PublishedApi
internal data class InternalView<in Si, out So, in E>(
    val evolve: (Si, E) -> So,
    val initialState: So,
) {
    /**
     * Left map on E/Event parameter - Contravariant
     *
     * @param En Event new
     * @param f
     */
    inline fun <En> mapLeftOnEvent(
        crossinline f: (En) -> E
    ): InternalView<Si, So, En> = InternalView(
        evolve = { si, en -> evolve(si, f(en)) },
        initialState = initialState
    )

    /**
     * Dimap on S/State parameter - Contravariant on the Si (input State) - Covariant on the So (output State) = Profunctor
     *
     * @param Sin State input new
     * @param Son State output new
     * @param fl
     * @param fr
     */
    inline fun <Sin, Son> dimapOnState(
        crossinline fl: (Sin) -> Si, crossinline fr: (So) -> Son
    ): InternalView<Sin, Son, E> = InternalView(
        evolve = { sin, e -> fr(evolve(fl(sin), e)) },
        initialState = fr(initialState)
    )

    /**
     * Left map on S/State parameter - Contravariant
     *
     * @param Sin State input new
     * @param f
     */
    inline fun <Sin> mapLeftOnState(crossinline f: (Sin) -> Si): InternalView<Sin, So, E> = dimapOnState(f) { it }

    /**
     * Right map on S/State parameter - Covariant
     *
     * @param Son State output new
     * @param f
     */
    inline fun <Son> mapOnState(crossinline f: (So) -> Son): InternalView<Si, Son, E> = dimapOnState({ it }, f)
}

/**
 * Apply on S/State parameter - Applicative
 *
 * @param Si State input type
 * @param So State output type
 * @param E Event type
 * @param Son State output new type
 * @param ff
 */
internal fun <Si, So, E, Son> InternalView<Si, So, E>.applyOnState(
    ff: InternalView<Si, (So) -> Son, E>
): InternalView<Si, Son, E> = InternalView(
    evolve = { si, e -> ff.evolve(si, e)(evolve(si, e)) },
    initialState = ff.initialState(initialState)
)

/**
 * Product on S/State parameter - Applicative
 *
 * @param Si State input type
 * @param So State output type
 * @param E Event type
 * @param Son State output new type
 * @param fb
 */
@PublishedApi
internal fun <Si, So, E, Son> InternalView<Si, So, E>.productOnState(fb: InternalView<Si, Son, E>): InternalView<Si, Pair<So, Son>, E> =
    applyOnState(fb.mapOnState { b: Son -> { a: So -> Pair(a, b) } })

/**
 * Combines [InternalView]s into one bigger [InternalView]
 *
 * Possible to use when [E] and [E2] have common superclass [E_SUPER]
 *
 * @param Si State input of the first View
 * @param So State output of the first View
 * @param E Event of the first View
 * @param Si2 State input of the second View
 * @param So2 State output of the second View
 * @param E2 Event of the second View
 * @param E_SUPER super type for [E] and [E2]
 * @param y second View
 * @return new View of type [InternalView]<[Pair]<[Si], [Si2]>, [Pair]<[So], [So2]>, [E_SUPER]>
 */
@PublishedApi
internal inline infix fun <Si, So, reified E : E_SUPER, Si2, So2, reified E2 : E_SUPER, E_SUPER> InternalView<Si, So, E?>.combine(
    y: InternalView<Si2, So2, E2?>
): InternalView<Pair<Si, Si2>, Pair<So, So2>, E_SUPER> {

    val viewX = this.mapLeftOnEvent<E_SUPER> { it as? E }.mapLeftOnState<Pair<Si, Si2>> { pair -> pair.first }

    val viewY = y.mapLeftOnEvent<E_SUPER> { it as? E2 }.mapLeftOnState<Pair<Si, Si2>> { pair -> pair.second }

    return viewX.productOnState(viewY)
}
