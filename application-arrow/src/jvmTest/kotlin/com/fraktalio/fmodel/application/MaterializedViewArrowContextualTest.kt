package com.fraktalio.fmodel.application

import arrow.core.Either
import arrow.core.continuations.Effect
import arrow.core.continuations.toEither
import com.fraktalio.fmodel.application.examples.numbers.even.query.EvenNumberViewRepository
import com.fraktalio.fmodel.application.examples.numbers.even.query.evenNumberViewRepository
import com.fraktalio.fmodel.domain.examples.numbers.api.Description
import com.fraktalio.fmodel.domain.examples.numbers.api.EvenNumberState
import com.fraktalio.fmodel.domain.examples.numbers.api.NumberEvent.EvenNumberEvent.EvenNumberAdded
import com.fraktalio.fmodel.domain.examples.numbers.api.NumberValue
import com.fraktalio.fmodel.domain.examples.numbers.even.query.evenNumberView
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlin.contracts.ExperimentalContracts

private suspend infix fun <S> Effect<Error, S>.thenState(expected: S) {
    val state = when (val result = this.toEither()) {
        is Either.Right -> result.value
        is Either.Left -> throw AssertionError("Expected Either.Right, but found Either.Left with value ${result.value}")
    }
    return state shouldBe expected
}

/**
 * Materialized View Contextual Test
 */
@FlowPreview
@ExperimentalContracts
class MaterializedViewArrowContextualTest : FunSpec({
    val evenView = evenNumberView()
    val evenNumberViewRepository = evenNumberViewRepository() as EvenNumberViewRepository

    test("Materialized view arrow contextual - even number added") {
        evenNumberViewRepository.deleteAll()
        with(viewStateComputation(evenView)) {
            with(evenNumberViewRepository) {
                flowOf(
                    EvenNumberAdded(Description("EvenNumberAdded"), NumberValue(2)),
                ).handleWithEffect().first() thenState EvenNumberState(
                    Description("Initial state, EvenNumberAdded"),
                    NumberValue(2)
                )
            }
        }
    }

    test("Materialized view arrow contextual materialized view interface - even number added") {
        evenNumberViewRepository.deleteAll()
        with(materializedView(evenView, evenNumberViewRepository)) {
            flowOf(
                EvenNumberAdded(Description("EvenNumberAdded"), NumberValue(2)),
            ).handleWithEffect().first() thenState EvenNumberState(
                Description("Initial state, EvenNumberAdded"),
                NumberValue(2)
            )
        }
    }
})
