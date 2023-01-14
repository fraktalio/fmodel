package com.fraktalio.fmodel.application

import arrow.core.Either
import arrow.core.Either.Left
import arrow.core.Either.Right
import com.fraktalio.fmodel.application.examples.numbers.NumberRepository
import com.fraktalio.fmodel.application.examples.numbers.even.command.EvenNumberLockingRepository
import com.fraktalio.fmodel.application.examples.numbers.even.command.EvenNumberRepository
import com.fraktalio.fmodel.application.examples.numbers.even.command.evenNumberLockingRepository
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
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactly
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.toList

/**
 * DSL - Given
 */
@FlowPreview
private fun <C, S, E> IDecider<C, S, E>.given(
    repository: EventRepository<C, E>,
    command: () -> C
): Flow<Either<Error, E>> =
    eventSourcingAggregate(
        decider = this,
        eventRepository = repository
    ).handleWithEffect(command())

@FlowPreview
private fun <C, S, E, V> IDecider<C, S, E>.given(
    repository: EventLockingRepository<C, E, V>,
    command: () -> C
): Flow<Either<Error, Pair<E, V>>> =
    eventSourcingLockingAggregate(
        decider = this,
        eventRepository = repository
    ).handleOptimisticallyWithEffect(command())

/**
 * DSL - When
 */
@Suppress("unused")
private fun <C, S, E> IDecider<C, S, E>.whenCommand(command: C): C = command

/**
 * DSL - Then
 */
private suspend infix fun <E> Flow<Either<Error, E>>.thenEvents(expected: Iterable<Either<Error, E>>) =
    toList() shouldContainExactly (expected)

private suspend infix fun <E, V> Flow<Either<Error, Pair<E, V>>>.thenEventPairs(expected: Iterable<Either<Error, Pair<E, V>>>) =
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
            } thenEvents listOf(Right(EvenNumberAdded(Description("2"), NumberValue(2))))
        }
    }

    test("Event-sourced locking aggregate - add even number") {
        with(evenDecider) {
            evenNumberLockingRepository.deleteAll()

            given(evenNumberLockingRepository) {
                whenCommand(AddEvenNumber(Description("2"), NumberValue(2)))
            } thenEventPairs listOf(
                Right(
                    Pair(EvenNumberAdded(Description("2"), NumberValue(2)), 1)
                )
            )
        }
    }

    test("Event-sourced aggregate - add even number - error (large number > 1000)") {
        with(evenDecider) {
            evenNumberRepository.deleteAll()
            val command = AddEvenNumber(Description("2000"), NumberValue(2000))

            given(evenNumberRepository) {
                whenCommand(command)
            } thenEvents listOf(Left(Error.CommandHandlingFailed(command)))
        }

    }

    test("Combined Event-sourced aggregate - add even number") {
        with(combinedDecider) {
            numberRepository.deleteAll()

            given(numberRepository) {
                whenCommand(AddEvenNumber(Description("2"), NumberValue(2)))
            } thenEvents listOf(Right(EvenNumberAdded(Description("2"), NumberValue(2))))
        }
    }

})
