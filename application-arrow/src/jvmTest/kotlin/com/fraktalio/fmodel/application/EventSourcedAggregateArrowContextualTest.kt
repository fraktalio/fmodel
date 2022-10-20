package com.fraktalio.fmodel.application

import arrow.core.Either
import arrow.core.Either.Left
import arrow.core.Either.Right
import arrow.core.continuations.Effect
import com.fraktalio.fmodel.application.Error.CommandPublishingFailed
import com.fraktalio.fmodel.application.examples.numbers.even.command.EvenNumberRepository
import com.fraktalio.fmodel.application.examples.numbers.even.command.evenNumberRepository
import com.fraktalio.fmodel.domain.examples.numbers.api.Description
import com.fraktalio.fmodel.domain.examples.numbers.api.NumberCommand.EvenNumberCommand
import com.fraktalio.fmodel.domain.examples.numbers.api.NumberCommand.EvenNumberCommand.AddEvenNumber
import com.fraktalio.fmodel.domain.examples.numbers.api.NumberEvent.EvenNumberEvent.EvenNumberAdded
import com.fraktalio.fmodel.domain.examples.numbers.api.NumberValue
import com.fraktalio.fmodel.domain.examples.numbers.even.command.evenNumberDecider
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactly
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlin.contracts.ExperimentalContracts

private suspend infix fun <E> Flow<Effect<Error, E>>.thenEvents(expected: Iterable<Either<Error, E>>) =
    map { it.toEither() }.toList() shouldContainExactly (expected)

/**
 * Event sourced aggregate contextual (context receivers) test
 */
@ExperimentalContracts
@FlowPreview
class EventSourcedAggregateArrowContextualTest : FunSpec({
    val evenDecider = evenNumberDecider()
    val evenNumberRepository = evenNumberRepository() as EvenNumberRepository

    test("Event-sourced aggregate arrow contextual - add even number") {
        evenNumberRepository.deleteAll()
        with(eventSourcingAggregate(evenDecider, evenNumberRepository)) {
            flowOf(
                AddEvenNumber(Description("desc"), NumberValue(4))
            ).handleWithEffect() thenEvents (
                    listOf(
                        Right(EvenNumberAdded(Description("desc"), NumberValue(4)))
                    )
                    )
        }
    }
    test("Event-sourced aggregate arrow contextual - add even number") {
        evenNumberRepository.deleteAll()
        with(eventSourcingAggregate(evenDecider, evenNumberRepository)) {
            val exception = IllegalStateException("Error")
            flow<EvenNumberCommand> {
                emit(AddEvenNumber(Description("desc"), NumberValue(6)))
                throw exception
            }.handleWithEffect() thenEvents listOf(
                Right(EvenNumberAdded(Description("desc"), NumberValue(6))),
                Left(CommandPublishingFailed(exception))
            )
        }
    }

})
