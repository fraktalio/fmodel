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

package com.fraktalio.fmodel.application

import arrow.core.Either
import com.fraktalio.fmodel.application.examples.numbers.even.command.evenNumberAggregate
import com.fraktalio.fmodel.application.examples.numbers.even.command.evenNumberRepository
import com.fraktalio.fmodel.application.examples.numbers.numberAggregate
import com.fraktalio.fmodel.application.examples.numbers.numberRepository
import com.fraktalio.fmodel.domain.examples.numbers.api.Description
import com.fraktalio.fmodel.domain.examples.numbers.api.NumberCommand
import com.fraktalio.fmodel.domain.examples.numbers.api.NumberCommand.EvenNumberCommand.AddEvenNumber
import com.fraktalio.fmodel.domain.examples.numbers.api.NumberEvent
import com.fraktalio.fmodel.domain.examples.numbers.api.NumberEvent.EvenNumberEvent
import com.fraktalio.fmodel.domain.examples.numbers.api.NumberValue
import com.fraktalio.fmodel.domain.examples.numbers.even.command.evenNumberDecider
import com.fraktalio.fmodel.domain.examples.numbers.evenNumberSaga
import com.fraktalio.fmodel.domain.examples.numbers.odd.command.oddNumberDecider
import com.fraktalio.fmodel.domain.examples.numbers.oddNumberSaga
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.runBlockingTest
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.gherkin.Feature
import java.lang.RuntimeException
import kotlin.test.assertFails
import kotlin.test.assertTrue


@ExperimentalCoroutinesApi
object AggregateTest : Spek({

    Feature("Aggregate") {
        val evenAggregate by memoized { evenNumberAggregate(evenNumberDecider(), evenNumberRepository()) }
        val allNumbersAggregate by memoized {
            numberAggregate(
                evenNumberDecider(),
                oddNumberDecider(),
                evenNumberSaga(),
                oddNumberSaga(),
                numberRepository()
            )
        }

        Scenario("Success") {
            lateinit var result: Flow<EvenNumberEvent?>

            When("handling command of type AddEvenNumber") {
                runBlockingTest {
                    result = evenAggregate.handle(
                        AddEvenNumber(
                            Description("Add 2"),
                            NumberValue(2)
                        )
                    )
                }
            }
            Then("expect success") {
                runBlockingTest {
                    result.take(1).collect {
                        assert(it is EvenNumberEvent.EvenNumberAdded && it.value.get == 2)
                    }
                }
            }
        }

        Scenario("Success - combined aggregate - send even number") {
            lateinit var result: Flow<NumberEvent?>

            When("handling command of type AddEvenNumber") {
                runBlockingTest {
                    result = allNumbersAggregate.handle(
                        AddEvenNumber(
                            Description("Add 2"),
                            NumberValue(2)
                        )
                    )
                }
            }
            Then("expect success") {
                runBlockingTest {
                    result.take(1).collect {
                        assert(it is EvenNumberEvent.EvenNumberAdded && it.value.get == 2)
                    }
                }
            }
        }
        Scenario("Success - combined aggregate - send odd number") {
            lateinit var result: Flow<NumberEvent?>

            When("handling command of type AddOddNumber") {
                runBlockingTest {
                    result = allNumbersAggregate.handle(
                        NumberCommand.OddNumberCommand.AddOddNumber(
                            Description("Add 1"),
                            NumberValue(1)
                        )
                    )
                }
            }
            Then("expect success - saga kicks in, and 2 events are published") {
                runBlockingTest {
                    assertTrue { result.toList().size == 2 }
                }
            }

        }

        Scenario("Success - combined aggregate - send flow of even number(s)") {
            lateinit var result: Flow<NumberEvent?>

            When("handling command of type AddEvenNumber") {
                runBlockingTest {
                    result = allNumbersAggregate.handle(
                        flowOf(
                            AddEvenNumber(
                                Description("Add 2"),
                                NumberValue(2)
                            )
                        )
                    )
                }
            }
            Then("expect success") {
                runBlockingTest {
                    result.take(1).collect {
                        assert(it is EvenNumberEvent.EvenNumberAdded && it.value.get == 2)
                    }
                }
            }
        }


        Scenario("Exception - handled with exception") {
            lateinit var result: Flow<EvenNumberEvent?>

            When("handling command of type AddEvenNumber") {
                runBlockingTest {
                    result = evenAggregate.handle(
                        AddEvenNumber(
                            Description("Add 2000"),
                            NumberValue(2000)
                        )
                    )
                }
            }
            Then("expect exception") {
                runBlockingTest {
                    assertFails { result.collect() }
                }
            }
        }
        Scenario("Exception - sending the flow of command(s) -  handled with exception - number greater than expected") {
            lateinit var result: Flow<EvenNumberEvent?>

            When("handling command of type AddEvenNumber") {
                runBlockingTest {
                    result = evenAggregate.handle(
                        flowOf(
                            AddEvenNumber(
                                Description("Add 2000"),
                                NumberValue(2000)
                            )
                        )
                    )
                }
            }
            Then("expect exception") {
                runBlockingTest {
                    assertFails { result.collect() }
                }
            }
        }
        Scenario("Exception - sending the flow of command(s) -  handled with exception - throw exception explicitly") {
            lateinit var result: Flow<EvenNumberEvent?>

            When("handling command of type AddEvenNumber") {
                runBlockingTest {
                    result = evenAggregate.handle(
                        flow {
                            throw RuntimeException("Some exception")
                        }
                    )
                }
            }
            Then("expect exception") {
                runBlockingTest {
                    assertFails { result.collect() }
                }
            }
        }
    }

})

