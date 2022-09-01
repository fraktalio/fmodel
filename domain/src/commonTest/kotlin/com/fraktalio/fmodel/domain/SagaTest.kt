package com.fraktalio.fmodel.domain

import com.fraktalio.fmodel.domain.examples.numbers.api.Description
import com.fraktalio.fmodel.domain.examples.numbers.api.NumberCommand.EvenNumberCommand.AddEvenNumber
import com.fraktalio.fmodel.domain.examples.numbers.api.NumberCommand.OddNumberCommand
import com.fraktalio.fmodel.domain.examples.numbers.api.NumberCommand.OddNumberCommand.AddOddNumber
import com.fraktalio.fmodel.domain.examples.numbers.api.NumberEvent.EvenNumberEvent.EvenNumberAdded
import com.fraktalio.fmodel.domain.examples.numbers.api.NumberEvent.OddNumberEvent.OddNumberAdded
import com.fraktalio.fmodel.domain.examples.numbers.api.NumberValue
import com.fraktalio.fmodel.domain.examples.numbers.evenNumberSaga
import com.fraktalio.fmodel.domain.examples.numbers.oddNumberSaga
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactly
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.toList

private fun <AR, A> ISaga<AR, A>.whenActionResult(actionResults: AR) = react(actionResults)
private suspend infix fun <A> Flow<A>.expectActions(expected: Iterable<A>) = toList() shouldContainExactly (expected)

@FlowPreview
class SagaTest : FunSpec({
    val evenSaga = evenNumberSaga()
    val oddSaga = oddNumberSaga()

    test("Saga - even number added") {
        with(evenSaga) {
            whenActionResult(
                EvenNumberAdded(Description("2"), NumberValue(2))
            ) expectActions listOf(
                AddOddNumber(
                    Description("1"),
                    NumberValue(1)
                )
            )
        }
    }

    test("Combined Saga - even number added") {
        val combinedSaga = evenSaga combine oddSaga
        with(combinedSaga) {
            whenActionResult(
                EvenNumberAdded(Description("2"), NumberValue(2))
            ) expectActions listOf(
                AddOddNumber(
                    Description("1"),
                    NumberValue(1)
                )
            )
        }
    }

    test("Combined Saga - odd number added") {
        val combinedSaga = evenSaga combine oddSaga
        with(combinedSaga) {
            whenActionResult(
                OddNumberAdded(Description("1"), NumberValue(1))
            ) expectActions listOf(
                AddEvenNumber(
                    Description("2"),
                    NumberValue(2)
                )
            )
        }
    }

    test("Mapped Saga (mapLeftOnActionResult) - even number added") {
        with(evenSaga.mapLeftOnActionResult { aRn: Int -> EvenNumberAdded(Description("$aRn"), NumberValue(aRn)) }) {
            whenActionResult(
                2
            ) expectActions listOf(
                AddOddNumber(
                    Description("1"),
                    NumberValue(1)
                )
            )
        }
    }

    test("Mapped Saga (mapOnAction) - even number added") {
        with(evenSaga.mapOnAction { a: OddNumberCommand -> a.value.get }) {
            whenActionResult(
                EvenNumberAdded(Description("2"), NumberValue(2))
            ) expectActions listOf(1)
        }
    }

})


