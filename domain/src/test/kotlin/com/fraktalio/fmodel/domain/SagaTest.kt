/*
 * Copyright (c) 2021 Fraktalio D.O.O. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.fraktalio.fmodel.domain

import com.fraktalio.fmodel.domain.examples.numbers.api.Description
import com.fraktalio.fmodel.domain.examples.numbers.api.NumberCommand
import com.fraktalio.fmodel.domain.examples.numbers.api.NumberCommand.OddNumberCommand.AddOddNumber
import com.fraktalio.fmodel.domain.examples.numbers.api.NumberEvent.EvenNumberEvent.EvenNumberAdded
import com.fraktalio.fmodel.domain.examples.numbers.api.NumberValue
import com.fraktalio.fmodel.domain.examples.numbers.evenNumberSaga
import com.fraktalio.fmodel.domain.examples.numbers.numberSaga
import com.fraktalio.fmodel.domain.examples.numbers.oddNumberSaga
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.gherkin.Feature
import kotlin.test.assertTrue


object SagaTest : Spek({

    Feature("Saga") {
        val saga by memoized { numberSaga() }
        val evenSaga by memoized { evenNumberSaga() }
        val oddSaga by memoized { oddNumberSaga() }

        Scenario("React") {
            var result: Sequence<NumberCommand> = emptySequence()

            When("when react on ActionResult/Event of type NumberEvent, publish list of Actions/Commands of type NumberCommand") {
                result = saga.react(EvenNumberAdded(Description("2"), NumberValue(2)))
            }

            Then("non empty list of Actions/Commands of type NumberCommand should be published") {
                assertTrue(result.any())
            }

            Then("AddOddNumber should be published") {
                assertTrue(result.first() is AddOddNumber && result.first().value.get == 1)
                assertTrue(result.count() == 1)
            }

        }

        Scenario("React - Combined Saga") {
            var result: Sequence<NumberCommand> = emptySequence()

            When("when react on ActionResult/Event of type NumberEvent, publish list of Actions/Commands of type NumberCommand") {
                result = evenSaga.combine(oddSaga).react(EvenNumberAdded(Description("2"), NumberValue(2)))
            }

            Then("non empty list of Actions/Commands of type NumberCommand should be published") {
                assertTrue(result.any())
            }

            Then("AddOddNumber should be published") {
                assertTrue(result.first() is AddOddNumber && result.first().value.get == 1)
                assertTrue(result.count() == 1)
            }

        }

        Scenario("React - Combined Saga 2") {
            var result: Sequence<NumberCommand> = emptySequence()

            When("when react on ActionResult/Event of type NumberEvent, publish list of Actions/Commands of type NumberCommand") {
                result = evenSaga.combine(oddSaga).combine(evenSaga)
                    .react(EvenNumberAdded(Description("2"), NumberValue(2)))
            }

            Then("non empty list of Actions/Commands of type NumberCommand should be published") {
                assertTrue(result.any())
            }

            Then("AddOddNumber should be published twice!") {
                assertTrue(result.first() is AddOddNumber && result.first().value.get == 1)
                assertTrue(result.count() == 2)
            }

        }

        Scenario("React - lef map over ActionResult/Event parameter in this case - functor (contravariant)") {
            var result: Sequence<NumberCommand> = emptySequence()

            When("when react on ActionResult/Event of type Int, publish list of Actions/Commands of type NumberCommand") {
                result = saga
                    .mapLeftOnActionResult { aRn: Int -> EvenNumberAdded(Description("$aRn"), NumberValue(aRn)) }
                    .react(2)
            }

            Then("non empty list of Actions/Commands of type NumberCommand should be published") {
                assertTrue(result.any())
            }

            Then("AddOddNumber should be published") {
                assertTrue(result.first() is AddOddNumber && result.first().value.get == 1)
            }

        }

        Scenario("React - Combined Saga - lef map over ActionResult/Event parameter in this case - functor (contravariant)") {
            var result: Sequence<NumberCommand> = emptySequence()

            When("when react on ActionResult/Event of type Int, publish list of Actions/Commands of type NumberCommand") {
                result = evenSaga.combine(oddSaga)
                    .mapLeftOnActionResult { aRn: Int -> EvenNumberAdded(Description("$aRn"), NumberValue(aRn)) }
                    .react(2)
            }

            Then("non empty list of Actions/Commands of type NumberCommand should be published") {
                assertTrue(result.any())
            }

            Then("AddOddNumber should be published") {
                assertTrue(result.first() is AddOddNumber && result.first().value.get == 1)
            }

        }

        Scenario("React - right map over Action/Command parameter in this case - functor (covariant)") {
            var result: Sequence<Int> = emptySequence()

            When("when react on ActionResult/Event of type NumberEvent, publish list of Actions/Commands of type Int") {
                result = saga
                    .mapOnAction { numberCommand -> numberCommand.value.get }
                    .react(EvenNumberAdded(Description("2"), NumberValue(2)))
            }

            Then("non empty list of Actions/Commands of type Int should be published") {
                assertTrue(result.any())
            }

            Then("Int should be published") {
                assertTrue(result.first() == 1)
            }

        }


        Scenario("React - Combined Saga -  right map over Action/Command parameter in this case - functor (covariant)") {
            var result: Sequence<Int> = emptySequence()

            When("when react on ActionResult/Event of type NumberEvent, publish list of Actions/Commands of type Int") {
                result = evenSaga.combine(oddSaga)
                    .mapOnAction { numberCommand -> numberCommand.value.get }
                    .react(EvenNumberAdded(Description("2"), NumberValue(2)))
            }

            Then("non empty list of Actions/Commands of type Int should be published") {
                assertTrue(result.any())
            }

            Then("Int should be published") {
                assertTrue(result.first() == 1)
            }

        }
    }
})

