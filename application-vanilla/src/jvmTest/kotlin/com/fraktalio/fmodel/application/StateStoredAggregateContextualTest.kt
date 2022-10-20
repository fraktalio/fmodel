package com.fraktalio.fmodel.application

import com.fraktalio.fmodel.application.examples.numbers.even.command.EvenNumberStateRepository
import com.fraktalio.fmodel.application.examples.numbers.even.command.evenNumberStateRepository
import com.fraktalio.fmodel.domain.examples.numbers.api.Description
import com.fraktalio.fmodel.domain.examples.numbers.api.EvenNumberState
import com.fraktalio.fmodel.domain.examples.numbers.api.NumberCommand.EvenNumberCommand.AddEvenNumber
import com.fraktalio.fmodel.domain.examples.numbers.api.NumberValue
import com.fraktalio.fmodel.domain.examples.numbers.even.command.evenNumberDecider
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactly
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlin.contracts.ExperimentalContracts

/**
 * State-stored aggregate contextual test
 */
@ExperimentalContracts
@FlowPreview
class StateStoredAggregateContextualTest : FunSpec({
    val evenDecider = evenNumberDecider()
    val evenNumberStateRepository = evenNumberStateRepository() as EvenNumberStateRepository

    test("State-stored aggregate contextual - add even number") {
        evenNumberStateRepository.deleteAll()
        with(stateComputation(evenDecider)) {
            with(evenNumberStateRepository) {
                flowOf(
                    AddEvenNumber(Description("desc"), NumberValue(6)),
                    AddEvenNumber(Description("desc"), NumberValue(4))
                ).handle().toList() shouldContainExactly listOf(
                    EvenNumberState(Description("desc"), NumberValue(6)),
                    EvenNumberState(Description("desc"), NumberValue(10))
                )
            }
        }
    }

    test("State-stored aggregate contextual with aggregate interface - add even number") {
        evenNumberStateRepository.deleteAll()
        with(stateStoredAggregate(evenDecider, evenNumberStateRepository)) {
            flowOf(
                AddEvenNumber(Description("desc"), NumberValue(6)),
                AddEvenNumber(Description("desc"), NumberValue(4))
            ).handle().toList() shouldContainExactly listOf(
                EvenNumberState(Description("desc"), NumberValue(6)),
                EvenNumberState(Description("desc"), NumberValue(10))
            )
        }
    }

    test("State-stored aggregate contextual - add even number - exception (large number > 1000)") {
        shouldThrow<UnsupportedOperationException> {
            with(stateComputation(evenDecider)) {
                with(evenNumberStateRepository) {
                    flowOf(
                        AddEvenNumber(Description("desc"), NumberValue(6000))
                    ).handle().toList() shouldContainExactly listOf(
                        EvenNumberState(Description("desc"), NumberValue(6000))
                    )
                }
            }
        }
    }
})
