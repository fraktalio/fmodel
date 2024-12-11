package com.fraktalio.fmodel.application

import com.fraktalio.fmodel.domain.IView

/**
 * EphemeralView is using/delegating a `view` / [ViewStateComputation]<[S], [E]> to handle events of type [E] without maintaining a state of projection(s).
 *
 * [EphemeralView] extends [ViewStateComputation] and [EphemeralViewRepository] interfaces,
 * clearly communicating that it is composed out of these two behaviours.
 *
 * @param S Ephemeral View state of type [S]
 * @param E Events of type [E] that are handled by this Ephemeral View
 * @param Q Query of type [Q]
 *
 * @author Domenic Cassisi
 */
interface EphemeralView<S, E, Q> : ViewStateComputation<S, E>, EphemeralViewRepository<E, Q>

/**
 * Ephemeral View constructor-like function.
 *
 * The Delegation pattern has proven to be a good alternative to implementation inheritance, and Kotlin supports it natively requiring zero boilerplate code.
 *
 * @param S Ephemeral View state of type [S]
 * @param E Events of type [E] that are used internally to build/fold new state
 * @param Q Identifier of type [Q]
 * @property view A view component of type [IView]<[S], [E]>
 * @property ephemeralViewRepository Interface for fetching events for [Q] - dependencies by delegation
 * @return An object/instance of type [EphemeralView]<[S], [E], [Q]>
 *
 * @author Domenic Cassisi
 */
fun <S, E, Q> EphemeralView(
    view: IView<S, E>,
    ephemeralViewRepository: EphemeralViewRepository<E, Q>
): EphemeralView<S, E, Q> =
    object : EphemeralView<S, E, Q>,
        EphemeralViewRepository<E, Q> by ephemeralViewRepository,
        IView<S, E> by view {}
