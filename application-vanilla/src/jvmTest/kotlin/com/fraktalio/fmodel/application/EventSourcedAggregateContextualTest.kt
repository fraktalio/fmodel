package com.fraktalio.fmodel.application

import com.fraktalio.fmodel.application.examples.numbers.even.command.EvenNumberRepository
import com.fraktalio.fmodel.application.examples.numbers.even.command.evenNumberRepository
import com.fraktalio.fmodel.domain.examples.numbers.api.Description
import com.fraktalio.fmodel.domain.examples.numbers.api.NumberCommand.EvenNumberCommand.AddEvenNumber
import com.fraktalio.fmodel.domain.examples.numbers.api.NumberEvent.EvenNumberEvent.EvenNumberAdded
import com.fraktalio.fmodel.domain.examples.numbers.api.NumberValue
import com.fraktalio.fmodel.domain.examples.numbers.even.command.evenNumberDecider
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainAll
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
    // version 1
    test("Event-sourced aggregate - concurrent and contextual - add even number") {
        evenNumberRepository.deleteAll()
        with(eventSourcingAggregate(evenDecider, evenNumberRepository)) {
            flowOf(
                AddEvenNumber(Description("desc"), NumberValue(6)),
                AddEvenNumber(Description("desc"), NumberValue(4))
            )
                .handleConcurrently { it?.description.hashCode() }
                .toList() shouldContainExactly listOf(
                EvenNumberAdded(Description("desc"), NumberValue(6)),
                EvenNumberAdded(Description("desc"), NumberValue(4))
            )
        }
    }
    // version 2
    test("Event-sourced decider and repository - concurrent and contextual - add even number") {
        evenNumberRepository.deleteAll()
        with(evenDecider) {
            with(evenNumberRepository) {
                flowOf(
                    AddEvenNumber(Description("desc"), NumberValue(6)),
                    AddEvenNumber(Description("desc"), NumberValue(4))
                )
                    .handleConcurrently { it?.description.hashCode() }
                    .toList() shouldContainExactly listOf(
                    EvenNumberAdded(Description("desc"), NumberValue(6)),
                    EvenNumberAdded(Description("desc"), NumberValue(4))
                )
            }
        }
    }
    // version 1
    test("Event-sourced aggregate - concurrent and contextual - add even number - different partition keys") {
        evenNumberRepository.deleteAll()
        // choosing command `value` hash as a partition key. It is not the same for these two commands.
        with(eventSourcingAggregate(evenDecider, evenNumberRepository)) {
            flowOf(
                AddEvenNumber(Description("desc"), NumberValue(6)),
                AddEvenNumber(Description("desc"), NumberValue(4))
            )
                .handleConcurrently { it?.description.hashCode() }
                .toList() shouldContainAll listOf(
                EvenNumberAdded(Description("desc"), NumberValue(4)),
                EvenNumberAdded(Description("desc"), NumberValue(6))
            )
        }
    }
    // version 2
    test("Event-sourced decider and repository - concurrent and contextual - add even number - different partition keys") {
        evenNumberRepository.deleteAll()
        // choosing command `value` hash as a partition key. It is not the same for these two commands.
        with(evenDecider) {
            with(evenNumberRepository) {
                flowOf(
                    AddEvenNumber(Description("desc"), NumberValue(6)),
                    AddEvenNumber(Description("desc"), NumberValue(4))
                )
                    .handleConcurrently { it?.description.hashCode() }
                    .toList() shouldContainAll listOf(
                    EvenNumberAdded(Description("desc"), NumberValue(4)),
                    EvenNumberAdded(Description("desc"), NumberValue(6))
                )
            }
        }

    }

    test("Event-sourced aggregate concurrent and contextual - add even number - exception (large number > 1000)") {
        shouldThrow<UnsupportedOperationException> {
            evenNumberRepository.deleteAll()
            with(evenDecider) {
                with(evenNumberRepository) {
                    flowOf(
                        AddEvenNumber(Description("2000"), NumberValue(2000))
                    )
                        .handleConcurrently { it?.description.hashCode() }.toList() shouldContainAll listOf(
                        EvenNumberAdded(Description("2000"), NumberValue(2000))
                    )
                }
            }
        }
    }


})
