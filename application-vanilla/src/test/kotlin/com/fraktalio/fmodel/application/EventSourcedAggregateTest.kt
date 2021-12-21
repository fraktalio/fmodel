package com.fraktalio.fmodel.application

import com.fraktalio.fmodel.application.examples.numbers.NumberRepository
import com.fraktalio.fmodel.application.examples.numbers.even.command.EvenNumberRepository
import com.fraktalio.fmodel.application.examples.numbers.even.command.evenNumberRepository
import com.fraktalio.fmodel.application.examples.numbers.numberRepository
import com.fraktalio.fmodel.domain.IDecider
import com.fraktalio.fmodel.domain.combine
import com.fraktalio.fmodel.domain.examples.numbers.api.Description
import com.fraktalio.fmodel.domain.examples.numbers.api.NumberCommand.EvenNumberCommand.AddEvenNumber
import com.fraktalio.fmodel.domain.examples.numbers.api.NumberEvent.EvenNumberEvent.EvenNumberAdded
import com.fraktalio.fmodel.domain.examples.numbers.api.NumberValue
import com.fraktalio.fmodel.domain.examples.numbers.even.command.evenNumberDecider
import com.fraktalio.fmodel.domain.examples.numbers.odd.command.oddNumberDecider
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactly
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.toList

/**
 * DSL - Given
 */
private fun <C, S, E> IDecider<C, S, E>.given(repository: EventRepository<C, E>, command: () -> C): Flow<E> =
    eventSourcingAggregate(
        decider = this,
        eventRepository = repository
    ).handle(command())

/**
 * DSL - When
 */
@Suppress("unused")
private fun <C, S, E> IDecider<C, S, E>.whenCommand(command: C): C = command

/**
 * DSL - Then
 */
private suspend infix fun <E> Flow<E>.thenEvents(expected: Iterable<E>) = toList() shouldContainExactly (expected)

/**
 * Event sourced aggregate test
 */
class EventSourcedAggregateTest : FunSpec({
    val evenDecider = evenNumberDecider()
    val oddDecider = oddNumberDecider()
    val combinedDecider = evenDecider.combine(oddDecider)
    val evenNumberRepository = evenNumberRepository() as EvenNumberRepository
    val numberRepository = numberRepository() as NumberRepository

    test("Event-sourced aggregate - add even number") {
        with(evenDecider) {
            evenNumberRepository.deleteAll()

            given(evenNumberRepository) {
                whenCommand(AddEvenNumber(Description("2"), NumberValue(2)))
            } thenEvents listOf(EvenNumberAdded(Description("2"), NumberValue(2)))
        }
    }

    test("Event-sourced aggregate - add even number - exception (large number > 1000)") {
        shouldThrow<UnsupportedOperationException> {
            with(evenDecider) {
                evenNumberRepository.deleteAll()

                given(evenNumberRepository) {
                    whenCommand(AddEvenNumber(Description("2000"), NumberValue(2000)))
                } thenEvents listOf(EvenNumberAdded(Description("2000"), NumberValue(2000)))
            }
        }
    }

    test("Combined Event-sourced aggregate - add even number") {
        with(combinedDecider) {
            numberRepository.deleteAll()

            given(numberRepository) {
                whenCommand(AddEvenNumber(Description("2"), NumberValue(2)))
            } thenEvents listOf(EvenNumberAdded(Description("2"), NumberValue(2)))
        }
    }


})
