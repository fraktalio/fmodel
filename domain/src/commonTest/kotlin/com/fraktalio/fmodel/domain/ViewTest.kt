package com.fraktalio.fmodel.domain

import com.fraktalio.fmodel.domain.examples.numbers.api.Description
import com.fraktalio.fmodel.domain.examples.numbers.api.EvenNumberState
import com.fraktalio.fmodel.domain.examples.numbers.api.NumberEvent.EvenNumberEvent.EvenNumberAdded
import com.fraktalio.fmodel.domain.examples.numbers.api.NumberEvent.OddNumberEvent.OddNumberAdded
import com.fraktalio.fmodel.domain.examples.numbers.api.NumberValue
import com.fraktalio.fmodel.domain.examples.numbers.api.OddNumberState
import com.fraktalio.fmodel.domain.examples.numbers.even.query.evenNumberView
import com.fraktalio.fmodel.domain.examples.numbers.odd.query.oddNumberView
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.FlowPreview

private fun <S, E> IView<S, E>.givenEvents(events: Iterable<E>) =
    events.fold(initialState) { s, e -> evolve(s, e) }

private infix fun <S, U : S> S.thenState(expected: U?) = shouldBe(expected)

@FlowPreview
class ViewTest : FunSpec({
    val evenView = evenNumberView()
    val oddView = oddNumberView()

    test("View - even number added") {
        with(evenView) {
            givenEvents(
                listOf(EvenNumberAdded(Description("2"), NumberValue(2)))
            ) thenState EvenNumberState(Description("Initial state + 2"), NumberValue(2))
        }
    }

    test("View - even numbers added") {
        with(evenView) {
            givenEvents(
                listOf(
                    EvenNumberAdded(Description("2"), NumberValue(2)),
                    EvenNumberAdded(Description("4"), NumberValue(4))
                )
            ) thenState EvenNumberState(
                Description("Initial state + 2 + 4"),
                NumberValue(6)
            )
        }
    }

    test("Mapped View (left map on Event) - even numbers added") {
        val mappedEvenView = evenView.mapLeftOnEvent { number: Int ->
            EvenNumberAdded(
                Description(number.toString()),
                NumberValue(number)
            )
        }
        with(mappedEvenView) {
            givenEvents(listOf(2, 4)) thenState EvenNumberState(
                Description("Initial state + 2 + 4"),
                NumberValue(6)
            )
        }
    }

    test("Mapped View (dimap on State) - even numbers added") {
        val mappedEvenView = evenView.dimapOnState(
            fr = { evenNumberState: EvenNumberState -> evenNumberState.value.get },
            fl = { number: Int -> EvenNumberState(Description(number.toString()), NumberValue(number)) })

        with(mappedEvenView) {
            givenEvents(
                listOf(
                    EvenNumberAdded(Description("2"), NumberValue(2)),
                    EvenNumberAdded(Description("4"), NumberValue(4))
                )
            ) thenState 6

        }
    }

    test("Mapped View (combine Views) - even and odd numbers added") {
        val combinedView = evenView combine oddView
        with(combinedView) {
            givenEvents(
                listOf(
                    EvenNumberAdded(Description("2"), NumberValue(2)),
                    OddNumberAdded(Description("3"), NumberValue(3)),
                    EvenNumberAdded(Description("4"), NumberValue(4))
                )
            ) thenState Pair(
                EvenNumberState(
                    Description("Initial state + 2 + 4"),
                    NumberValue(6)
                ),
                OddNumberState(
                    Description("Initial state + 3"),
                    NumberValue(3)
                )
            )
        }
    }
})


