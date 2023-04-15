package com.fraktalio.fmodel.application

import arrow.core.Either
import com.fraktalio.fmodel.application.examples.numbers.even.command.EvenNumberStateRepository
import com.fraktalio.fmodel.application.examples.numbers.even.command.evenNumberStateRepository
import com.fraktalio.fmodel.domain.examples.numbers.api.Description
import com.fraktalio.fmodel.domain.examples.numbers.api.EvenNumberState
import com.fraktalio.fmodel.domain.examples.numbers.api.NumberCommand.EvenNumberCommand.AddEvenNumber
import com.fraktalio.fmodel.domain.examples.numbers.api.NumberValue
import com.fraktalio.fmodel.domain.examples.numbers.even.command.evenNumberDecider
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.last
import kotlin.contracts.ExperimentalContracts


private fun <S> Either<Error, S>.thenError() {
    val error = when (val result = this) {
        is Either.Right -> throw AssertionError("Expected Either.Left, but found Either.Right with value ${result.value}")
        is Either.Left -> result.value
    }
    error.shouldBeInstanceOf<Error>()
}

private infix fun <S> Either<Error, S>.thenState(expected: S) {
    val state = when (val result = this) {
        is Either.Right -> result.value
        is Either.Left -> throw AssertionError("Expected Either.Right, but found Either.Left with value ${result.value}")
    }
    return state shouldBe expected
}

/**
 * State-stored aggregate arrow, contextual test
 */
@ExperimentalContracts
@FlowPreview
class StateStoredAggregateArrowContextualTest : FunSpec({
    val evenDecider = evenNumberDecider()
    val evenNumberStateRepository = evenNumberStateRepository() as EvenNumberStateRepository

    test("State-stored aggregatearrow contextual - add even number") {
        evenNumberStateRepository.deleteAll()
        with(stateComputation(evenDecider)) {
            with(evenNumberStateRepository) {
                flowOf(
                    AddEvenNumber(Description("desc"), NumberValue(6))
                ).handleWithEffect().first() thenState EvenNumberState(Description("desc"), NumberValue(6))
            }
        }
    }

    test("State-stored aggregate arrow contextual with aggregate interface - add even number") {
        evenNumberStateRepository.deleteAll()
        with(stateStoredAggregate(evenDecider, evenNumberStateRepository)) {
            flowOf(
                AddEvenNumber(Description("desc"), NumberValue(6))
            ).handleWithEffect().first() thenState EvenNumberState(Description("desc"), NumberValue(6))
        }
    }

    test("State-stored aggregate arrow contextual - add even number - exception (large number > 1000)") {
        evenNumberStateRepository.deleteAll()
        with(stateComputation(evenDecider)) {
            with(evenNumberStateRepository) {
                flowOf(
                    AddEvenNumber(Description("desc"), NumberValue(6000)),
                    AddEvenNumber(Description("desc"), NumberValue(6))
                ).handleWithEffect().first().thenError()
            }
        }
    }

    test("State-stored aggregate arrow contextual - add even number - exception (large number > 1000) - 2") {
        evenNumberStateRepository.deleteAll()
        with(stateComputation(evenDecider)) {
            with(evenNumberStateRepository) {
                flowOf(
                    AddEvenNumber(Description("desc"), NumberValue(6000)),
                    AddEvenNumber(Description("desc"), NumberValue(6))
                ).handleWithEffect().last() thenState EvenNumberState(Description("desc"), NumberValue(6))
            }
        }
    }
})
