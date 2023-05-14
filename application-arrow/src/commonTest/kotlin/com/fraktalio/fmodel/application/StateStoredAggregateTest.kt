package com.fraktalio.fmodel.application

import arrow.core.Either
import com.fraktalio.fmodel.application.examples.numbers.NumberStateRepository
import com.fraktalio.fmodel.application.examples.numbers.even.command.EvenNumberLockingStateRepository
import com.fraktalio.fmodel.application.examples.numbers.even.command.EvenNumberStateRepository
import com.fraktalio.fmodel.application.examples.numbers.even.command.evenNumberLockingStateRepository
import com.fraktalio.fmodel.application.examples.numbers.even.command.evenNumberStateRepository
import com.fraktalio.fmodel.application.examples.numbers.numberStateRepository
import com.fraktalio.fmodel.domain.IDecider
import com.fraktalio.fmodel.domain.combine
import com.fraktalio.fmodel.domain.examples.numbers.api.Description
import com.fraktalio.fmodel.domain.examples.numbers.api.EvenNumberState
import com.fraktalio.fmodel.domain.examples.numbers.api.NumberCommand.EvenNumberCommand.AddEvenNumber
import com.fraktalio.fmodel.domain.examples.numbers.api.NumberValue
import com.fraktalio.fmodel.domain.examples.numbers.api.OddNumberState
import com.fraktalio.fmodel.domain.examples.numbers.even.command.evenNumberDecider
import com.fraktalio.fmodel.domain.examples.numbers.odd.command.oddNumberDecider
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.coroutines.ExperimentalCoroutinesApi

/**
 * DSL - Given
 */
private suspend fun <C, S, E> IDecider<C, S, E>.given(
    repository: StateRepository<C, S>,
    command: () -> C
): Either<Error, S> =
    StateStoredAggregate(
        decider = this,
        stateRepository = repository
    ).handleWithEffect(command())

private suspend fun <C, S, E, V> IDecider<C, S, E>.given(
    repository: StateLockingRepository<C, S, V>,
    command: () -> C
): Either<Error, Pair<S, V>> =
    StateStoredLockingAggregate(
        decider = this,
        stateRepository = repository
    ).handleOptimisticallyWithEffect(command())

/**
 * DSL - When
 */
@Suppress("unused")
private fun <C, S, E> IDecider<C, S, E>.whenCommand(command: C): C = command

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

private infix fun <S, V> Either<Error, Pair<S, V>>.thenStateAndVersion(expected: Pair<S, V>) {
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

    test("State-stored aggregate - add even number - error (large number > 1000)") {
        with(evenDecider) {
            evenNumberStateRepository.deleteAll()
            val command = AddEvenNumber(Description("2000"), NumberValue(2000))

            given(evenNumberStateRepository) {
                whenCommand(command)
            }.thenError()

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
})
