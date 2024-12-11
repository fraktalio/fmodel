package com.fraktalio.fmodel.application

import com.fraktalio.fmodel.application.examples.numbers.even.query.EvenNumberEphemeralViewRepository
import com.fraktalio.fmodel.application.examples.numbers.even.query.evenNumberEphemeralViewRepository
import com.fraktalio.fmodel.domain.IView
import com.fraktalio.fmodel.domain.examples.numbers.api.Description
import com.fraktalio.fmodel.domain.examples.numbers.api.EvenNumberState
import com.fraktalio.fmodel.domain.examples.numbers.api.NumberValue
import com.fraktalio.fmodel.domain.examples.numbers.even.query.evenNumberView
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

/**
 * DSL - Given
 */
private suspend fun <S, E, Q> IView<S, E>.given(repository: EphemeralViewRepository<E, Q>, query: () -> Q): S =
    EphemeralView(
        view = this,
        ephemeralViewRepository = repository
    ).handle(query())

/**
 * DSL - When
 */
private fun <Q> whenQuery(query: Q): Q = query

/**
 * DSL - Then
 */
private infix fun <S> S.thenState(expected: S) = shouldBe(expected)

class EphemeralViewTest : FunSpec({
    val evenView = evenNumberView()
    val ephemeralViewRepository = evenNumberEphemeralViewRepository() as EvenNumberEphemeralViewRepository

    test("Ephemeral View - load number flow 1") {
        with(evenView) {
            given(ephemeralViewRepository) {
                whenQuery(1)
            } thenState EvenNumberState(Description("Initial state, Number 2, Number 4"), NumberValue(6))
        }
    }

    test("Ephemeral View - load number flow 2") {
        with(evenView) {
            given(ephemeralViewRepository) {
                whenQuery(2)
            } thenState EvenNumberState(Description("Initial state, Number 4, Number 2"), NumberValue(2))
        }
    }

    test("Ephemeral View - load non-existing number flow") {
        with(evenView) {
            given(ephemeralViewRepository) {
                whenQuery(3)
            } thenState EvenNumberState(Description("Initial state"), NumberValue(0))
        }
    }

})
