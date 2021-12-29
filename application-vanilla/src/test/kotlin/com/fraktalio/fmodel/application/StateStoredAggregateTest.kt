package com.fraktalio.fmodel.application

import com.fraktalio.fmodel.application.examples.numbers.NumberStateRepository
import com.fraktalio.fmodel.application.examples.numbers.even.command.EvenNumberStateRepository
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
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.FlowPreview

/**
 * DSL - Given
 */
@FlowPreview
private suspend fun <C, S, E> IDecider<C, S, E>.given(repository: StateRepository<C, S>, command: () -> C): S =
    stateStoredAggregate(
        decider = this,
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

/**
 * State-stored aggregate test
 */
@FlowPreview
class StateStoredAggregateTest : FunSpec({
    val evenDecider = evenNumberDecider()
    val oddDecider = oddNumberDecider()
    val combinedDecider = evenDecider.combine(oddDecider)
    val evenNumberStateRepository = evenNumberStateRepository() as EvenNumberStateRepository
    val numberStateRepository = numberStateRepository() as NumberStateRepository

    test("State-stored aggregate - add even number") {
        with(evenDecider) {
            evenNumberStateRepository.deleteAll()

            given(evenNumberStateRepository) {
                whenCommand(AddEvenNumber(Description("2"), NumberValue(2)))
            } thenState EvenNumberState(Description("2"), NumberValue(2))
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
            evenNumberStateRepository.deleteAll()

            given(numberStateRepository) {
                whenCommand(AddEvenNumber(Description("2"), NumberValue(2)))
            } thenState Pair(
                EvenNumberState(Description("2"), NumberValue(2)),
                OddNumberState(Description("0"), NumberValue(0))
            )
        }
    }
})
