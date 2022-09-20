package com.fraktalio.fmodel.application

import com.fraktalio.fmodel.application.examples.numbers.NumberViewRepository
import com.fraktalio.fmodel.application.examples.numbers.even.query.*
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

/**
 * DSL - Given
 */
private suspend fun <S, E> IView<S, E>.given(repository: ViewStateRepository<E, S>, event: () -> E): S =
    materializedView(
        view = this,
        viewStateRepository = repository
    ).handle(event())

private suspend fun <S, E, V> IView<S, E>.given(
    repository: ViewStateLockingRepository<E, S, V>,
    event: () -> E
): Pair<S, V> =
    materializedLockingView(
        view = this,
        viewStateRepository = repository
    ).handleOptimistically(event())

private suspend fun <S, E, EV, SV> IView<S, E>.given(
    repository: ViewStateLockingDeduplicationRepository<E, S, EV, SV>,
    event: () -> Pair<E, EV>
): Pair<S, SV> =
    materializedLockingDeduplicationView(
        view = this,
        viewStateRepository = repository
    ).handleOptimisticallyWithDeduplication(event())

/**
 * DSL - When
 */
@Suppress("unused")
private fun <S, E> IView<S, E>.whenEvent(event: E): E = event

@Suppress("unused")
private fun <S, E, EV> IView<S, E>.whenEvent(event: Pair<E, EV>): Pair<E, EV> = event

/**
 * DSL - Then
 */
private infix fun <S> S.thenState(expected: S) = shouldBe(expected)

private infix fun <S, V> Pair<S, V>.thenStateAndVersion(expected: Pair<S, V>) = shouldBe(expected)

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
    val evenNumberLockingDeduplicationViewRepository =
        evenNumberLockingDeduplicationViewRepository() as EvenNumberLockingDeduplicationViewRepository


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

    test("Locking Deduplication Materialized view - even number added") {
        with(evenView) {
            evenNumberLockingDeduplicationViewRepository.deleteAll()

            given(evenNumberLockingDeduplicationViewRepository) {
                whenEvent(Pair(EvenNumberAdded(Description("2"), NumberValue(2)), 1))
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
