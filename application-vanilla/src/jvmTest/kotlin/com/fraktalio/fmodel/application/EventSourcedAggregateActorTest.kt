package com.fraktalio.fmodel.application

import com.fraktalio.fmodel.application.examples.numbers.even.command.EvenNumberRepository
import com.fraktalio.fmodel.application.examples.numbers.even.command.evenNumberRepository
import com.fraktalio.fmodel.domain.IDecider
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
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlin.contracts.ExperimentalContracts

/**
 * DSL - Given
 */
@ObsoleteCoroutinesApi
@ExperimentalContracts
@FlowPreview
private fun <C, S, E> IDecider<C, S, E>.given(
    repository: EventRepository<C, E>,
    partitionKey: (C) -> Int,
    command: () -> Flow<C>
): Flow<E> =
    eventSourcingAggregate(
        decider = this,
        eventRepository = repository
    ).handleConcurrently(command()) { partitionKey(it) }

/**
 * DSL - When
 */
@Suppress("unused")
private fun <C, S, E> IDecider<C, S, E>.whenCommand(command: Flow<C>): Flow<C> = command

/**
 * DSL - Then
 */
private suspend infix fun <E> Flow<E>.thenEventsExactly(expected: Iterable<E>) =
    toList() shouldContainExactly (expected)

private suspend infix fun <E> Flow<E>.thenEvents(expected: Collection<E>) = toList() shouldContainAll (expected)

/**
 * Event sourced aggregate actor test
 */
@OptIn(ObsoleteCoroutinesApi::class)
@ExperimentalContracts
@FlowPreview
class EventSourcedAggregateActorTest : FunSpec({
    val evenDecider = evenNumberDecider()
    val evenNumberRepository = evenNumberRepository() as EvenNumberRepository

    test("Event-sourced aggregate actor - add even number") {
        with(evenDecider) {
            evenNumberRepository.deleteAll()

            // choosing command `description` hash as a partition key. It is the same for these two commands.
            given(evenNumberRepository, { it?.description.hashCode() }) {
                whenCommand(
                    flowOf(
                        AddEvenNumber(Description("desc"), NumberValue(6)),
                        AddEvenNumber(Description("desc"), NumberValue(4))
                    )
                )
            } thenEventsExactly listOf(
                EvenNumberAdded(Description("desc"), NumberValue(6)),
                EvenNumberAdded(Description("desc"), NumberValue(4))
            )
        }
    }

    test("Event-sourced aggregate actor - add even number - different partition keys") {
        with(evenDecider) {
            evenNumberRepository.deleteAll()

            // choosing command `value` hash as a partition key. It is not the same for these two commands.
            given(evenNumberRepository, { it?.value.hashCode() }) {
                whenCommand(
                    flowOf(
                        AddEvenNumber(Description("desc"), NumberValue(6)),
                        AddEvenNumber(Description("desc"), NumberValue(8))
                    )
                )
            } thenEvents listOf(
                EvenNumberAdded(Description("desc"), NumberValue(8)),
                EvenNumberAdded(Description("desc"), NumberValue(6))
            )
        }
    }

    test("Event-sourced aggregate actor - add even number - exception (large number > 1000)") {
        shouldThrow<UnsupportedOperationException> {
            with(evenDecider) {
                evenNumberRepository.deleteAll()

                given(evenNumberRepository, { it?.description.hashCode() }) {
                    whenCommand(flowOf(AddEvenNumber(Description("2000"), NumberValue(2000))))
                } thenEventsExactly listOf(EvenNumberAdded(Description("2000"), NumberValue(2000)))
            }
        }
    }

})
