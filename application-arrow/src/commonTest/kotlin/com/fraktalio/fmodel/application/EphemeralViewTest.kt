package com.fraktalio.fmodel.application

import arrow.core.Either
import com.fraktalio.fmodel.application.examples.numbers.even.query.EvenNumberEphemeralViewRepository
import com.fraktalio.fmodel.application.examples.numbers.even.query.evenNumberEphemeralViewRepository
import com.fraktalio.fmodel.domain.IView
import com.fraktalio.fmodel.domain.examples.numbers.api.Description
import com.fraktalio.fmodel.domain.examples.numbers.api.EvenNumberState
import com.fraktalio.fmodel.domain.examples.numbers.api.NumberValue
import com.fraktalio.fmodel.domain.examples.numbers.even.query.evenNumberView
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf

/**
 * DSL - Given
 */
private suspend fun <S, E, Q> IView<S, E>.given(repository: EphemeralViewRepository<E, Q>, query: () -> Q): Either<Error, S> =
    EphemeralView(
        view = this,
        ephemeralViewRepository = repository
    ).handleWithEffect(query())

/**
 * DSL - When
 */
private fun <Q> whenQuery(query: Q): Q = query

/**
 * DSL - Then
 */
private infix fun <S> Either<Error, S>.thenState(expected: S) {
    val state = when (this) {
        is Either.Right -> value
        is Either.Left -> throw AssertionError("Expected Either.Right, but found Either.Left with value $value")
    }
    state shouldBe expected
}

private fun <S> Either<Error, S>.thenError() {
    val error = when (this) {
        is Either.Right -> throw AssertionError("Expected Either.Left, but found Either.Right with value $value")
        is Either.Left -> value
    }
    error.shouldBeInstanceOf<Error>()
}

/**
 * Ephemeral View Test
 */
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

    test("Ephemeral View - load number flow 3 - with error") {
        with(evenView) {
            given(ephemeralViewRepository) {
                whenQuery(3)
            }.thenError()
        }
    }

    test("Ephemeral View - load non-existing number flow") {
        with(evenView) {
            given(ephemeralViewRepository) {
                whenQuery(4)
            } thenState EvenNumberState(Description("Initial state"), NumberValue(0))
        }
    }
})
