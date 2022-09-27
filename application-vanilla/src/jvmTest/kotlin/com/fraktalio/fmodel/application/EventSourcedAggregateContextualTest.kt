package com.fraktalio.fmodel.application

import com.fraktalio.fmodel.application.examples.numbers.even.command.EvenNumberRepository
import com.fraktalio.fmodel.application.examples.numbers.even.command.evenNumberRepository
import com.fraktalio.fmodel.domain.examples.numbers.api.Description
import com.fraktalio.fmodel.domain.examples.numbers.api.NumberCommand.EvenNumberCommand.AddEvenNumber
import com.fraktalio.fmodel.domain.examples.numbers.api.NumberEvent.EvenNumberEvent.EvenNumberAdded
import com.fraktalio.fmodel.domain.examples.numbers.api.NumberValue
import com.fraktalio.fmodel.domain.examples.numbers.even.command.evenNumberDecider
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactly
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlin.contracts.ExperimentalContracts

/**
 * Event sourced aggregate contextual (context receivers) test
 */
@ExperimentalContracts
@FlowPreview
class EventSourcedAggregateContextualTest : FunSpec({
    val evenDecider = evenNumberDecider()
    val evenNumberRepository = evenNumberRepository() as EvenNumberRepository

    // version 1
    test("Event-sourced aggregate contextual - add even number") {
        evenNumberRepository.deleteAll()
        with(eventSourcingAggregate(evenDecider, evenNumberRepository)) {
            flowOf(
                AddEvenNumber(Description("desc"), NumberValue(6)),
                AddEvenNumber(Description("desc"), NumberValue(4))
            ).handle().toList() shouldContainExactly listOf(
                EvenNumberAdded(Description("desc"), NumberValue(6)),
                EvenNumberAdded(Description("desc"), NumberValue(4))
            )
        }

    }
    // version 2
    test("Event-sourced decider and repository - contextual - add even number") {
        evenNumberRepository.deleteAll()
        with(evenDecider) {
            with(evenNumberRepository) {
                flowOf(
                    AddEvenNumber(Description("desc"), NumberValue(6)),
                    AddEvenNumber(Description("desc"), NumberValue(4))
                ).handle().toList() shouldContainExactly listOf(
                    EvenNumberAdded(Description("desc"), NumberValue(6)),
                    EvenNumberAdded(Description("desc"), NumberValue(4))
                )
            }
        }

    }
})
