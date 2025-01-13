package com.fraktalio.fmodel.application

import com.fraktalio.fmodel.application.examples.numbers.NumberStateRepository
import com.fraktalio.fmodel.application.examples.numbers.even.command.EvenNumberLockingStateRepository
import com.fraktalio.fmodel.application.examples.numbers.even.command.EvenNumberStateRepository
import com.fraktalio.fmodel.application.examples.numbers.even.command.evenNumberLockingStateRepository
import com.fraktalio.fmodel.application.examples.numbers.even.command.evenNumberStateRepository
import com.fraktalio.fmodel.application.examples.numbers.numberStateRepository
import com.fraktalio.fmodel.domain.IDecider
import com.fraktalio.fmodel.domain.ISaga
import com.fraktalio.fmodel.domain.combine
import com.fraktalio.fmodel.domain.examples.numbers.api.Description
import com.fraktalio.fmodel.domain.examples.numbers.api.EvenNumberState
import com.fraktalio.fmodel.domain.examples.numbers.api.NumberCommand.EvenNumberCommand.AddEvenNumber
import com.fraktalio.fmodel.domain.examples.numbers.api.NumberValue
import com.fraktalio.fmodel.domain.examples.numbers.api.OddNumberState
import com.fraktalio.fmodel.domain.examples.numbers.even.command.evenNumberDecider
import com.fraktalio.fmodel.domain.examples.numbers.numberSaga
import com.fraktalio.fmodel.domain.examples.numbers.odd.command.oddNumberDecider
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.ExperimentalCoroutinesApi

/**
 * DSL - Given
 */
private suspend fun <C, S, E> IDecider<C, S, E>.given(repository: StateRepository<C, S>, command: () -> C): S =
    StateStoredAggregate(
        decider = this,
        stateRepository = repository
    ).handle(command())

private suspend fun <C, S, E, V> IDecider<C, S, E>.given(
    repository: StateLockingRepository<C, S, V>,
    command: () -> C
): Pair<S, V> =
    StateStoredLockingAggregate(
        decider = this,
        stateRepository = repository
    ).handleOptimistically(command())

private suspend fun <C, S, E> IDecider<C, S, E>.given(
    repository: StateRepository<C, S>,
    saga: ISaga<E, C>,
    command: () -> C
) = StateStoredOrchestratingAggregate(
    decider = this,
    saga = saga,
    stateRepository = repository
).handle(command())

/**
 * DSL - When
 */
@Suppress("unused")
private fun <C, S, E> IDecider<C, S, E>.whenCommand(command: C): C = command

/**
 * DSL - Then
 */
private infix fun <S> S.thenState(expected: S) = shouldBe(expected)
private infix fun <S, V> Pair<S, V>.thenStateAndVersion(expected: Pair<S, V>) = shouldBe(expected)

/**
 * State-stored aggregate test
 */
@OptIn(ExperimentalCoroutinesApi::class)
class StateStoredAggregateTest : FunSpec({
    val evenDecider = evenNumberDecider()
    val oddDecider = oddNumberDecider()
    val combinedDecider = evenDecider.combine(oddDecider)
    val evenNumberStateRepository = evenNumberStateRepository() as EvenNumberStateRepository
    val evenNumberLockingStateRepository = evenNumberLockingStateRepository() as EvenNumberLockingStateRepository
    val numberStateRepository = numberStateRepository() as NumberStateRepository
    val numberSaga = numberSaga()

    test("State-stored aggregate - add even number") {
        with(evenDecider) {
            evenNumberStateRepository.deleteAll()

            given(evenNumberStateRepository) {
                whenCommand(AddEvenNumber(Description("2"), NumberValue(2)))
            } thenState EvenNumberState(Description("2"), NumberValue(2))
        }
    }
    test("State-stored locking aggregate - add even number") {
        with(evenDecider) {
            evenNumberLockingStateRepository.deleteAll()

            given(evenNumberLockingStateRepository) {
                whenCommand(AddEvenNumber(Description("2"), NumberValue(2)))
            } thenStateAndVersion Pair(EvenNumberState(Description("2"), NumberValue(2)), 1)
        }
    }

    test("State-stored aggregate - add even number - exception (large number > 1000)") {
        shouldThrow<UnsupportedOperationException> {
            with(evenDecider) {
                evenNumberStateRepository.deleteAll()

                given(evenNumberStateRepository) {
                    whenCommand(AddEvenNumber(Description("2000"), NumberValue(2000)))
                } thenState EvenNumberState(Description("2000"), NumberValue(2000))
            }
        }
    }

    test("Combined State-stored aggregate - add even number") {
        with(combinedDecider) {
            numberStateRepository.deleteAll()

            given(numberStateRepository) {
                whenCommand(AddEvenNumber(Description("2"), NumberValue(2)))
            } thenState Pair(
                EvenNumberState(Description("2"), NumberValue(2)),
                OddNumberState(Description("0"), NumberValue(0))
            )
        }
    }

    test("Orchestrated state-stored aggregate - add even and odd number") {
        with(combinedDecider) {
            numberStateRepository.deleteAll()
            given(numberStateRepository, numberSaga) {
                whenCommand(AddEvenNumber(Description("4"), NumberValue(4)))
            } thenState Pair(
                EvenNumberState(Description("4"), NumberValue(4)),
                OddNumberState(Description("3"), NumberValue(3))
            )
        }
    }
})
