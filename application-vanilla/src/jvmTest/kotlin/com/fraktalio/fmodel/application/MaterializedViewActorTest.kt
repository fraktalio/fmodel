package com.fraktalio.fmodel.application

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
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.collections.shouldContainExactly
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlin.contracts.ExperimentalContracts

/**
 * DSL - Given
 */
@FlowPreview
@ExperimentalContracts
private fun <S, E> IView<S, E>.given(
    repository: ViewStateRepository<E, S>,
    partitionKey: (E) -> Int,
    event: () -> Flow<E>
): Flow<S> =
    materializedView(
        view = this,
        viewStateRepository = repository
    ).handleConcurrently(event()) { partitionKey(it) }

/**
 * DSL - When
 */
@Suppress("unused")
private fun <S, E> IView<S, E>.whenEvent(events: Flow<E>): Flow<E> = events

/**
 * DSL - Then
 */
private suspend infix fun <S> Flow<S>.thenStateExactly(expected: Iterable<S>) = toList() shouldContainExactly (expected)
private suspend infix fun <S> Flow<S>.thenState(expected: Collection<S>) = toList() shouldContainAll (expected)

/**
 * Materialized View Actor Test
 */
@FlowPreview
@ExperimentalContracts
class MaterializedViewActorTest : FunSpec({
    val evenView = evenNumberView()
    val oddView = oddNumberView()
    val combinedView = evenView.combine(oddView)
    val evenNumberViewRepository = evenNumberViewRepository() as EvenNumberViewRepository
    val numberViewRepository = numberViewRepository() as NumberViewRepository

    test("Materialized view actor - even number added") {
        with(evenView) {
            evenNumberViewRepository.deleteAll()

            given(evenNumberViewRepository, { it?.description.hashCode() }) {
                whenEvent(
                    flowOf(
                        EvenNumberAdded(Description("EvenNumberAdded"), NumberValue(2)),
                        EvenNumberAdded(Description("EvenNumberAdded"), NumberValue(4))
                    )
                )
            } thenStateExactly listOf(
                EvenNumberState(Description("Initial state, EvenNumberAdded"), NumberValue(2)),
                EvenNumberState(Description("Initial state, EvenNumberAdded, EvenNumberAdded"), NumberValue(6)),
            )
        }
    }

    test("Combined Materialized view actor - odd number added") {
        with(combinedView) {
            numberViewRepository.deleteAll()

            given(numberViewRepository, { it?.description.hashCode() }) {
                whenEvent(
                    flowOf(
                        OddNumberAdded(Description("3"), NumberValue(3)),
                        OddNumberAdded(Description("1"), NumberValue(1))
                    )
                )
            } thenState listOf(
                Pair(null, OddNumberState(Description("0, 3"), NumberValue(3))),
                Pair(null, OddNumberState(Description("0, 3, 1"), NumberValue(4)))
            )
        }
    }

    test("Combined Materialized view actor- even number added") {
        with(combinedView) {
            numberViewRepository.deleteAll()

            given(numberViewRepository, { it?.description.hashCode() }) {
                whenEvent(flowOf(EvenNumberAdded(Description("4"), NumberValue(4))))
            } thenStateExactly listOf(Pair(EvenNumberState(Description("0, 4"), NumberValue(4)), null))
        }
    }

})
