package com.fraktalio.fmodel.application

import arrow.core.Either
import com.fraktalio.fmodel.application.examples.numbers.NumberViewRepository
import com.fraktalio.fmodel.application.examples.numbers.even.query.EvenNumberViewRepository
import com.fraktalio.fmodel.application.examples.numbers.even.query.evenNumberViewRepository
import com.fraktalio.fmodel.application.examples.numbers.numberViewRepository
import com.fraktalio.fmodel.domain.IView
import com.fraktalio.fmodel.domain.combine
import com.fraktalio.fmodel.domain.examples.numbers.api.Description
import com.fraktalio.fmodel.domain.examples.numbers.api.EvenNumberState
import com.fraktalio.fmodel.domain.examples.numbers.api.NumberEvent.EvenNumberEvent.EvenNumberAdded
import com.fraktalio.fmodel.domain.examples.numbers.api.NumberEvent.OddNumberEvent.OddNumberAdded
import com.fraktalio.fmodel.domain.examples.numbers.api.NumberValue
import com.fraktalio.fmodel.domain.examples.numbers.api.OddNumberState
import com.fraktalio.fmodel.domain.examples.numbers.even.query.evenNumberView
import com.fraktalio.fmodel.domain.examples.numbers.odd.query.oddNumberView
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.equality.shouldBeEqualToComparingFields
import io.kotest.matchers.shouldBe

/**
 * DSL - Given
 */
private suspend fun <S, E> IView<S, E>.given(repository: ViewStateRepository<E, S>, event: () -> E): Either<Error, S> =
    materializedView(
        view = this,
        viewStateRepository = repository
    ).handleEither(event())

/**
 * DSL - When
 */
@Suppress("unused")
private fun <S, E> IView<S, E>.whenEvent(event: E): E = event

/**
 * DSL - Then
 */
private infix fun <S> Either<Error, S>.thenState(expected: S) {
    val state = when (this) {
        is Either.Right -> value
        is Either.Left -> throw AssertionError("Expected Either.Right, but found Either.Left with value ${this.value}")
    }
    return state shouldBe expected
}

private infix fun <S> Either<Error, S>.thenError(expected: Error) {
    val error = when (this) {
        is Either.Right -> throw AssertionError("Expected Either.Left, but found Either.Right with value ${this.value}")
        is Either.Left -> value
    }
    return error shouldBeEqualToComparingFields expected
}

/**
 * Materialized View Test
 */
class MaterializedViewTest : FunSpec({
    val evenView = evenNumberView()
    val oddView = oddNumberView()
    val combinedView = evenView.combine(oddView)
    val evenNumberViewRepository = evenNumberViewRepository() as EvenNumberViewRepository
    val numberViewRepository = numberViewRepository() as NumberViewRepository

    test("Materialized view - even number added") {
        with(evenView) {
            evenNumberViewRepository.deleteAll()

            given(evenNumberViewRepository) {
                whenEvent(EvenNumberAdded(Description("2"), NumberValue(2)))
            } thenState EvenNumberState(Description("Initial state, 2"), NumberValue(2))
        }
    }

    test("Combined Materialized view - odd number added") {
        with(combinedView) {
            numberViewRepository.deleteAll()

            given(numberViewRepository) {
                whenEvent(OddNumberAdded(Description("3"), NumberValue(3)))
            } thenState Pair(null, OddNumberState(Description("0, 3"), NumberValue(3)))
        }
    }

    test("Combined Materialized view - even number added") {
        with(combinedView) {
            numberViewRepository.deleteAll()

            given(numberViewRepository) {
                whenEvent(EvenNumberAdded(Description("4"), NumberValue(4)))
            } thenState Pair(EvenNumberState(Description("0, 4"), NumberValue(4)), null)
        }
    }

})
