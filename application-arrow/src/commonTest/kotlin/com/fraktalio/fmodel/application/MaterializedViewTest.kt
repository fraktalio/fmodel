package com.fraktalio.fmodel.application

import arrow.core.Either
import arrow.core.continuations.Effect
import arrow.core.continuations.toEither
import com.fraktalio.fmodel.application.examples.numbers.NumberViewRepository
import com.fraktalio.fmodel.application.examples.numbers.even.query.EvenNumberLockingViewRepository
import com.fraktalio.fmodel.application.examples.numbers.even.query.EvenNumberViewRepository
import com.fraktalio.fmodel.application.examples.numbers.even.query.evenNumberLockingViewRepository
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
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf

/**
 * DSL - Given
 */
private suspend fun <S, E> IView<S, E>.given(repository: ViewStateRepository<E, S>, event: () -> E): Effect<Error, S> =
    materializedView(
        view = this,
        viewStateRepository = repository
    ).handleWithEffect(event())

private suspend fun <S, E, V> IView<S, E>.given(
    repository: ViewStateLockingRepository<E, S, V>,
    event: () -> E
): Effect<Error, Pair<S, V>> =
    materializedLockingView(
        view = this,
        viewStateRepository = repository
    ).handleOptimisticallyWithEffect(event())

/**
 * DSL - When
 */
@Suppress("unused")
private fun <S, E> IView<S, E>.whenEvent(event: E): E = event

/**
 * DSL - Then
 */
private suspend infix fun <S> Effect<Error, S>.thenState(expected: S) {
    val state = when (val result = this.toEither()) {
        is Either.Right -> result.value
        is Either.Left -> throw AssertionError("Expected Either.Right, but found Either.Left with value ${result.value}")
    }
    return state shouldBe expected
}

private suspend infix fun <S, V> Effect<Error, Pair<S, V>>.thenStateAndVersion(expected: Pair<S, V>) {
    val state = when (val result = this.toEither()) {
        is Either.Right -> result.value
        is Either.Left -> throw AssertionError("Expected Either.Right, but found Either.Left with value ${result.value}")
    }
    return state shouldBe expected
}


private suspend fun <S> Effect<Error, S>.thenError() {
    val error = when (val result = this.toEither()) {
        is Either.Right -> throw AssertionError("Expected Either.Left, but found Either.Right with value ${result.value}")
        is Either.Left -> result.value
    }
    error.shouldBeInstanceOf<Error>()
}

/**
 * Materialized View Test
 */
class MaterializedViewTest : FunSpec({
    val evenView = evenNumberView()
    val oddView = oddNumberView()
    val combinedView = evenView.combine(oddView)
    val evenNumberViewRepository = evenNumberViewRepository() as EvenNumberViewRepository
    val evenNumberLockingViewRepository = evenNumberLockingViewRepository() as EvenNumberLockingViewRepository
    val numberViewRepository = numberViewRepository() as NumberViewRepository

    test("Materialized view - even number added") {
        with(evenView) {
            evenNumberViewRepository.deleteAll()

            given(evenNumberViewRepository) {
                whenEvent(EvenNumberAdded(Description("2"), NumberValue(2)))
            } thenState EvenNumberState(Description("Initial state, 2"), NumberValue(2))
        }
    }

    test("Locking Materialized view - even number added") {
        with(evenView) {
            evenNumberLockingViewRepository.deleteAll()

            given(evenNumberLockingViewRepository) {
                whenEvent(EvenNumberAdded(Description("2"), NumberValue(2)))
            } thenStateAndVersion Pair(EvenNumberState(Description("0, 2"), NumberValue(2)), 1)
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
