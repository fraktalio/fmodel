package com.fraktalio.fmodel.application

import com.fraktalio.fmodel.application.examples.numbers.NumberStateRepository
import com.fraktalio.fmodel.application.examples.numbers.even.command.EvenNumberStateRepository
import com.fraktalio.fmodel.application.examples.numbers.even.command.evenNumberStateRepository
import com.fraktalio.fmodel.application.examples.numbers.numberStateRepository
import com.fraktalio.fmodel.domain.IDecider
import com.fraktalio.fmodel.domain.combine
import com.fraktalio.fmodel.domain.examples.numbers.api.Description
import com.fraktalio.fmodel.domain.examples.numbers.api.EvenNumberState
import com.fraktalio.fmodel.domain.examples.numbers.api.NumberCommand.EvenNumberCommand.AddEvenNumber
import com.fraktalio.fmodel.domain.examples.numbers.api.NumberValue
import com.fraktalio.fmodel.domain.examples.numbers.api.OddNumberState
import com.fraktalio.fmodel.domain.examples.numbers.even.command.evenNumberDecider
import com.fraktalio.fmodel.domain.examples.numbers.odd.command.oddNumberDecider
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
    repository: StateRepository<C, S>,
    partitionKey: (C) -> Int,
    command: () -> Flow<C>
): Flow<S> =
    stateStoredAggregate(
        decider = this,
        stateRepository = repository,
    ).handleConcurrently(command()) { partitionKey(it) }

/**
 * DSL - When
 */
@Suppress("unused")
private fun <C, S, E> IDecider<C, S, E>.whenCommand(command: Flow<C>): Flow<C> = command

/**
 * DSL - Then
 */
private suspend infix fun <S> Flow<S>.thenStateExactly(expected: Iterable<S>) = toList() shouldContainExactly (expected)
private suspend infix fun <S> Flow<S>.thenState(expected: Collection<S>) = toList() shouldContainAll (expected)

/**
 * State-stored aggregate actor test
 */
@OptIn(ObsoleteCoroutinesApi::class)
@ExperimentalContracts
@FlowPreview
class StateStoredAggregateActorTest : FunSpec({
    val evenDecider = evenNumberDecider()
    val oddDecider = oddNumberDecider()
    val combinedDecider = evenDecider.combine(oddDecider)
    val evenNumberStateRepository = evenNumberStateRepository() as EvenNumberStateRepository
    val numberStateRepository = numberStateRepository() as NumberStateRepository

    test("State-stored aggregate actor - add even number") {
        with(evenDecider) {
            evenNumberStateRepository.deleteAll()

            given(evenNumberStateRepository, { it?.description.hashCode() }) {
                whenCommand(
                    flowOf(
                        AddEvenNumber(Description("desc"), NumberValue(2)),
                        AddEvenNumber(Description("desc2"), NumberValue(2))
                    )
                )
            } thenState listOf(
                EvenNumberState(Description("desc"), NumberValue(2)),
                EvenNumberState(Description("desc2"), NumberValue(4))
            )
        }
    }

    test("State-stored aggregate actor - add even number - exception (large number > 1000)") {
        shouldThrow<UnsupportedOperationException> {
            with(evenDecider) {
                evenNumberStateRepository.deleteAll()

                given(evenNumberStateRepository, { it?.description.hashCode() }) {
                    whenCommand(flowOf(AddEvenNumber(Description("2000"), NumberValue(2000))))
                } thenStateExactly listOf(EvenNumberState(Description("2000"), NumberValue(2000)))
            }
        }
    }

    test("Combined State-stored aggregate actor - add even number") {
        with(combinedDecider) {
            numberStateRepository.deleteAll()

            given(numberStateRepository, { it?.description.hashCode() }) {
                whenCommand(flowOf(AddEvenNumber(Description("2"), NumberValue(2))))
            } thenStateExactly listOf(
                Pair(
                    EvenNumberState(Description("2"), NumberValue(2)),
                    OddNumberState(Description("0"), NumberValue(0))
                )
            )
        }
    }
})
