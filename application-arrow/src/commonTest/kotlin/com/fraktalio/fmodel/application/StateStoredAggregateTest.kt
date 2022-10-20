package com.fraktalio.fmodel.application

import arrow.core.Either
import arrow.core.continuations.Effect
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
import kotlinx.coroutines.FlowPreview

/**
 * DSL - Given
 */
@FlowPreview
private suspend fun <C, S, E> IDecider<C, S, E>.given(
    repository: StateRepository<C, S>,
    command: () -> C
): Effect<Error, S> =
    stateStoredAggregate(
        decider = this,
        stateRepository = repository
    ).handleWithEffect(command())

@FlowPreview
private suspend fun <C, S, E, V> IDecider<C, S, E>.given(
    repository: StateLockingRepository<C, S, V>,
    command: () -> C
): Effect<Error, Pair<S, V>> =
    stateStoredLockingAggregate(
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
 * State-stored aggregate test
 */
@FlowPreview
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
