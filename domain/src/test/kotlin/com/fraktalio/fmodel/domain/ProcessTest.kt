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

import com.fraktalio.fmodel.domain.examples.numbers.api.*
import com.fraktalio.fmodel.domain.examples.numbers.api.NumberCommand.OddNumberCommand.AddOddNumber
import com.fraktalio.fmodel.domain.examples.numbers.api.NumberEvent.EvenNumberEvent.EvenNumberAdded
import com.fraktalio.fmodel.domain.examples.numbers.numberProcess
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.gherkin.Feature
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue


object ProcessTest : Spek({

    Feature("Process") {
        val process by memoized { numberProcess() }

        Scenario("React") {
            var result: Iterable<NumberCommand> = emptyList()

            When("when react on Event of type NumberEvent, publish list of Actions/Commands of type NumberCommand") {
                result = process.react(null, EvenNumberAdded(Description("2"), NumberValue(2)))
            }

            Then("non empty list of Actions/Commands of type NumberCommand should be published") {
                assertTrue(result.any())
            }

            Then("AddOddNumber should be published") {
                assertTrue(result.first() is AddOddNumber && result.first().value.get == 1)
                assertTrue(result.count() == 1)
            }

        }

        Scenario("React - left map over AR parameter - functor") {
            var result: Iterable<NumberCommand> = emptyList()

            When("when react on Event of type Int, publish list of Actions/Commands of type NumberCommand") {
                result = process
                    .dimapOnE(
                        fr = { evenNumberEvent: NumberEvent -> evenNumberEvent.value.get },
                        fl = { number: Int ->
                            EvenNumberAdded(
                                Description(
                                    number.toString()
                                ), NumberValue(number)
                            )
                        }
                    )
                    .react(null, 2)
            }

            Then("non empty list of Actions/Commands of type NumberCommand should be published") {
                assertTrue(result.any())
            }

            Then("AddOddNumber should be published") {
                assertTrue(result.first() is AddOddNumber && result.first().value.get == 1)
                assertTrue(result.count() == 1)
            }

        }

        Scenario("Evolve") {
            var result: NumberState? = null

            When("when receive Event of type NumberEvent, evolve new state of type NumberState") {
                result = process.evolve(null, EvenNumberAdded(Description("2"), NumberValue(2)))
            }

            Then("expect the new state of type NumberState is evolved (in this case process is stateless and does not maintain the state)") {
                assertNull(result)
            }

        }

        Scenario("Evolve - dimap over State parameter - profunctor") {
            var result: Int? = null

            When("when receive Event of type NumberEvent, evolve new state of type Int") {
                result = process
                    .dimapOnS(
                        fr = { evenNumberState: NumberState? -> evenNumberState?.value?.get },
                        fl = { number: Int -> EvenNumberState(Description(number.toString()), NumberValue(number)) }
                    )
                    .evolve(0, EvenNumberAdded(Description("2"), NumberValue(2)))
            }

            Then("expect the new state of type Int is evolved (in this case process is stateless and does not maintain the state)") {
                assertNull(result)
            }

        }

        Scenario("Pending") {
            var result: Iterable<NumberCommand> = emptyList()

            When("when provided with State of type NumberState") {
                result = process.pending(null)
            }

            Then("`empty` list of Actions/Commands of type NumberCommand should be published") {
                assertTrue(result.none())
            }

        }

        Scenario("Ingest") {
            var result: Iterable<NumberEvent> = emptyList()

            When("when provided with State of type NumberState, and ingesting Action Result of type NumberEvent") {
                result = process.ingest(EvenNumberAdded(Description("2"), NumberValue(2)), null)
            }

            Then("list of Events of type NumberEvent should be published") {
                assertEquals(listOf(EvenNumberAdded(Description("2"), NumberValue(2))), result)
            }

        }

        Scenario("Ingest - left map over AR parameter - functor") {
            var result: Iterable<NumberEvent> = emptyList()

            When("when provided with State of type NumberState, and ingesting Action Result of type Int") {
                result = process
                    .lmapOnAR { aRn: Int -> EvenNumberAdded(Description(aRn.toString()), NumberValue(aRn)) }
                    .ingest(2, null)
            }

            Then("list of Events of type NumberEvent should be published") {
                assertEquals(listOf(EvenNumberAdded(Description("2"), NumberValue(2))), result)
            }

        }

    }
})
