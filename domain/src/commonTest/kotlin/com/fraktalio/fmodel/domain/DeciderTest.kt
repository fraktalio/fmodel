package com.fraktalio.fmodel.domain

import com.fraktalio.fmodel.domain.examples.numbers.api.Description
import com.fraktalio.fmodel.domain.examples.numbers.api.EvenNumberState
import com.fraktalio.fmodel.domain.examples.numbers.api.NumberCommand.EvenNumberCommand.AddEvenNumber
import com.fraktalio.fmodel.domain.examples.numbers.api.NumberCommand.EvenNumberCommand.SubtractEvenNumber
import com.fraktalio.fmodel.domain.examples.numbers.api.NumberCommand.OddNumberCommand.AddOddNumber
import com.fraktalio.fmodel.domain.examples.numbers.api.NumberCommand.OddNumberCommand.SubtractOddNumber
import com.fraktalio.fmodel.domain.examples.numbers.api.NumberEvent.EvenNumberEvent
import com.fraktalio.fmodel.domain.examples.numbers.api.NumberEvent.EvenNumberEvent.EvenNumberAdded
import com.fraktalio.fmodel.domain.examples.numbers.api.NumberEvent.EvenNumberEvent.EvenNumberSubtracted
import com.fraktalio.fmodel.domain.examples.numbers.api.NumberEvent.OddNumberEvent.OddNumberAdded
import com.fraktalio.fmodel.domain.examples.numbers.api.NumberEvent.OddNumberEvent.OddNumberSubtracted
import com.fraktalio.fmodel.domain.examples.numbers.api.NumberValue
import com.fraktalio.fmodel.domain.examples.numbers.api.OddNumberState
import com.fraktalio.fmodel.domain.examples.numbers.even.command.evenNumberDecider
import com.fraktalio.fmodel.domain.examples.numbers.odd.command.oddNumberDecider
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.fold
import kotlinx.coroutines.flow.toList

private fun <C, S, E> IDecider<C, S, E>.givenEvents(events: Iterable<E>, command: () -> C): Flow<E> {
    val currentState = events.fold(initialState) { s, e -> evolve(s, e) }
    return decide(command(), currentState)
}

private suspend fun <C, S, E> IDecider<C, S, E>.givenState(state: S?, command: () -> C): S {
    val currentState = state ?: initialState
    val events = decide(command(), currentState)
    return events.fold(currentState) { s, e -> evolve(s, e) }
}

@Suppress("unused")
private fun <C, S, E> IDecider<C, S, E>.whenCommand(command: C): C = command

private suspend infix fun <E> Flow<E>.thenEvents(expected: Iterable<E>) = toList() shouldContainExactly (expected)
private infix fun <S, U : S> S.thenState(expected: U?) = shouldBe(expected)

@FlowPreview
class DeciderTest : FunSpec({
    val evenDecider = evenNumberDecider()
    val oddDecider = oddNumberDecider()

    test("Event-sourced Decider - add even number") {
        with(evenDecider) {
            givenEvents(emptyList()) {
                whenCommand(AddEvenNumber(Description("2"), NumberValue(2)))
            } thenEvents listOf(EvenNumberAdded(Description("2"), NumberValue(2)))
        }
    }

    test("Event-sourced Decider - given previous state, add even number") {
        with(evenDecider) {
            givenEvents(listOf(EvenNumberAdded(Description("2"), NumberValue(2)))) {
                whenCommand(AddEvenNumber(Description("4"), NumberValue(4)))
            } thenEvents listOf(EvenNumberAdded(Description("4"), NumberValue(6)))
        }
    }

    test("Event-sourced Decider - given previous state, subtract even number") {
        with(evenDecider) {
            givenEvents(listOf(EvenNumberAdded(Description("8"), NumberValue(8)))) {
                whenCommand(SubtractEvenNumber(Description("2"), NumberValue(2)))
            } thenEvents listOf(EvenNumberSubtracted(Description("2"), NumberValue(6)))
        }
    }

    test("Event-sourced Decider - given previous state, add odd number") {
        with(oddDecider) {
            givenEvents(listOf(OddNumberAdded(Description("3"), NumberValue(3)))) {
                whenCommand(AddOddNumber(Description("1"), NumberValue(1)))
            } thenEvents listOf(OddNumberAdded(Description("1"), NumberValue(4)))
        }
    }

    test("Event-sourced Decider - given previous state, subtract odd number") {
        with(oddDecider) {
            givenEvents(listOf(OddNumberAdded(Description("3"), NumberValue(3)))) {
                whenCommand(SubtractOddNumber(Description("1"), NumberValue(1)))
            } thenEvents listOf(OddNumberSubtracted(Description("1"), NumberValue(2)))
        }
    }


    test("State-stored Decider - add even number") {
        with(evenDecider) {
            givenState(null) {
                whenCommand(AddEvenNumber(Description("2"), NumberValue(2)))
            } thenState EvenNumberState(Description("Initial state + 2"), NumberValue(2))
        }
    }

    test("State-stored Decider - given previous state, add even number") {
        with(evenDecider) {
            givenState(EvenNumberState(Description("2"), NumberValue(2))) {
                whenCommand(AddEvenNumber(Description("4"), NumberValue(4)))
            } thenState EvenNumberState(Description("2 + 4"), NumberValue(6))
        }
    }

    test("State-stored Decider - given previous state, add odd number") {
        with(oddDecider) {
            givenState(OddNumberState(Description("3"), NumberValue(3))) {
                whenCommand(AddOddNumber(Description("1"), NumberValue(1)))
            } thenState OddNumberState(Description("3 + 1"), NumberValue(4))
        }
    }

    test("Event-sourced Combined Decider - add even number") {
        val combinedDecider = evenDecider combine oddDecider
        with(combinedDecider) {
            givenEvents(emptyList()) {
                whenCommand(AddEvenNumber(Description("2"), NumberValue(2)))
            } thenEvents listOf(EvenNumberAdded(Description("2"), NumberValue(2)))
        }
    }

    test("State-stored Combined Decider - add even number") {
        val combinedDecider = evenDecider combine oddDecider
        with(combinedDecider) {
            givenState(null) {
                whenCommand(AddEvenNumber(Description("2"), NumberValue(2)))
            } thenState Pair(
                EvenNumberState(Description("Initial state + 2"), NumberValue(2)),
                oddDecider.initialState
            )
        }
    }

    test("Event-sourced Combined Decider - given previous state, add even number") {
        val combinedDecider = evenDecider combine oddDecider
        with(combinedDecider) {
            givenEvents(listOf(EvenNumberAdded(Description("2"), NumberValue(2)))) {
                whenCommand(AddEvenNumber(Description("4"), NumberValue(4)))
            } thenEvents listOf(EvenNumberAdded(Description("4"), NumberValue(6)))
        }
    }

    test("State-stored Combined Decider - given previous state, add even number") {
        val combinedDecider = evenDecider combine oddDecider
        with(combinedDecider) {
            givenState(
                Pair(
                    EvenNumberState(Description("2"), NumberValue(2)),
                    oddDecider.initialState
                )
            ) {
                whenCommand(AddEvenNumber(Description("4"), NumberValue(4)))
            } thenState Pair(
                EvenNumberState(Description("2 + 4"), NumberValue(6)),
                oddDecider.initialState
            )
        }
    }

    test("Event-sourced Mapped Decider (left map over Command parameter) - add even number") {
        with(evenDecider.mapLeftOnCommand { cn: Int -> AddEvenNumber(Description(cn.toString()), NumberValue(cn)) }) {
            givenEvents(emptyList()) {
                whenCommand(2)
            } thenEvents listOf(EvenNumberAdded(Description("2"), NumberValue(2)))
        }
    }

    test("State-stored Mapped Decider (left map over Command parameter) - add even number") {
        with(evenDecider.mapLeftOnCommand { cn: Int -> AddEvenNumber(Description(cn.toString()), NumberValue(cn)) }) {
            givenState(null) {
                whenCommand(2)
            } thenState EvenNumberState(Description("Initial state + 2"), NumberValue(2))
        }
    }

    test("Event-sourced Mapped Decider (dimap over Event parameter) - add even number") {
        with(
            evenDecider
                .dimapOnEvent(
                    fr = { evenNumberEvent: EvenNumberEvent? -> evenNumberEvent?.value?.get },
                    fl = { number: Int? ->
                        if (number != null) EvenNumberAdded(
                            Description(number.toString()),
                            NumberValue(number)
                        ) else null
                    })
        ) {
            givenEvents(emptyList()) {
                whenCommand(AddEvenNumber(Description("2"), NumberValue(2)))
            } thenEvents listOf(2)
        }
    }

    test("State-stored Mapped Decider (dimap over Event parameter) - add even number") {
        with(
            evenDecider
                .dimapOnEvent(
                    fr = { evenNumberEvent: EvenNumberEvent? -> evenNumberEvent?.value?.get },
                    fl = { number: Int? ->
                        if (number != null) EvenNumberAdded(
                            Description(number.toString()),
                            NumberValue(number)
                        ) else null
                    })
        ) {
            givenState(null) {
                whenCommand(AddEvenNumber(Description("2"), NumberValue(2)))
            } thenState EvenNumberState(Description("Initial state + 2"), NumberValue(2))

        }
    }

    test("Event-sourced Mapped Decider (dimap over State parameter) - add even number") {
        with(
            evenDecider
                .dimapOnState(
                    fr = { evenNumberState: EvenNumberState -> evenNumberState.value.get },
                    fl = { number: Int -> EvenNumberState(Description(number.toString()), NumberValue(number)) })
        ) {
            givenEvents(emptyList()) {
                whenCommand(AddEvenNumber(Description("2"), NumberValue(2)))
            } thenEvents listOf(EvenNumberAdded(Description("2"), NumberValue(2)))
        }
    }

    test("State-stored Mapped Decider (dimap over State parameter) - add even number") {
        with(
            evenDecider
                .dimapOnState(
                    fr = { evenNumberState: EvenNumberState -> evenNumberState.value.get },
                    fl = { number: Int -> EvenNumberState(Description(number.toString()), NumberValue(number)) })
        ) {
            givenState(null) {
                whenCommand(AddEvenNumber(Description("2"), NumberValue(2)))
            } thenState 2
        }
    }

})


