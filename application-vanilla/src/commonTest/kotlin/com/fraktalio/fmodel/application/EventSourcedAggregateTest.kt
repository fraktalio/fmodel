package com.fraktalio.fmodel.application

import com.fraktalio.fmodel.application.examples.numbers.NumberRepository
import com.fraktalio.fmodel.application.examples.numbers.even.command.*
import com.fraktalio.fmodel.application.examples.numbers.numberRepository
import com.fraktalio.fmodel.domain.IDecider
import com.fraktalio.fmodel.domain.ISaga
import com.fraktalio.fmodel.domain.combine
import com.fraktalio.fmodel.domain.examples.numbers.api.Description
import com.fraktalio.fmodel.domain.examples.numbers.api.NumberCommand.EvenNumberCommand.AddEvenNumber
import com.fraktalio.fmodel.domain.examples.numbers.api.NumberEvent.EvenNumberEvent.EvenNumberAdded
import com.fraktalio.fmodel.domain.examples.numbers.api.NumberEvent.OddNumberEvent.OddNumberAdded
import com.fraktalio.fmodel.domain.examples.numbers.api.NumberValue
import com.fraktalio.fmodel.domain.examples.numbers.even.command.evenNumberDecider
import com.fraktalio.fmodel.domain.examples.numbers.numberSaga
import com.fraktalio.fmodel.domain.examples.numbers.odd.command.oddNumberDecider
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactly
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.toList

/**
 * DSL - Given
 */
@FlowPreview
private fun <C, S, E> IDecider<C, S, E>.given(repository: EventRepository<C, E>, command: () -> C): Flow<E> =
    eventSourcingAggregate(
        decider = this,
        eventRepository = repository
    ).handle(command())

@FlowPreview
private fun <C, S, E, V> IDecider<C, S, E>.given(
    repository: EventLockingRepository<C, E, V>,
    command: () -> C
): Flow<Pair<E, V>> =
    eventSourcingLockingAggregate(
        decider = this,
        eventRepository = repository
    ).handleOptimistically(command())

@FlowPreview
private fun <C, S, E> IDecider<C, S, E>.given(
    saga: ISaga<E, C>,
    repository: EventRepository<C, E>,
    command: () -> C
): Flow<E> =
    eventSourcingOrchestratingAggregate(
        decider = this,
        saga = saga,
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
private suspend infix fun <E, V> Flow<Pair<E, V>>.thenEventPairs(expected: Iterable<Pair<E, V>>) =
    toList() shouldContainExactly (expected)


/**
 * Event sourced aggregate test
 */
@FlowPreview
class EventSourcedAggregateTest : FunSpec({
    val evenDecider = evenNumberDecider()
    val oddDecider = oddNumberDecider()
    val combinedDecider = evenDecider.combine(oddDecider)
    val evenNumberRepository = evenNumberRepository() as EvenNumberRepository
    val evenNumberLockingRepository = evenNumberLockingRepository() as EvenNumberLockingRepository
    val numberRepository = numberRepository() as NumberRepository

    test("Event-sourced aggregate - add even number") {
        with(evenDecider) {
            evenNumberRepository.deleteAll()

            given(evenNumberRepository) {
                whenCommand(AddEvenNumber(Description("2"), NumberValue(2)))
            } thenEvents listOf(EvenNumberAdded(Description("2"), NumberValue(2)))
        }
    }

    test("Event-sourced locking aggregate - add even number") {
        with(evenDecider) {
            evenNumberLockingRepository.deleteAll()

            given(evenNumberLockingRepository) {
                whenCommand(AddEvenNumber(Description("2"), NumberValue(2)))
            } thenEventPairs listOf(Pair(EvenNumberAdded(Description("2"), NumberValue(2)), 1))
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

    test("Combined Event-sourced orchestrating aggregate - add even number") {
        with(combinedDecider) {
            numberRepository.deleteAll()

            given(numberSaga(), numberRepository) {
                whenCommand(AddEvenNumber(Description("2"), NumberValue(2)))
            } thenEvents listOf(
                EvenNumberAdded(Description("2"), NumberValue(2)),
                OddNumberAdded(Description("1"), NumberValue(1))
            )
        }
    }


})
