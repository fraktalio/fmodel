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
import com.fraktalio.fmodel.domain.examples.numbers.api.NumberCommand.EvenNumberCommand.AddEvenNumber
import com.fraktalio.fmodel.domain.examples.numbers.api.NumberCommand.OddNumberCommand
import com.fraktalio.fmodel.domain.examples.numbers.api.NumberEvent.EvenNumberEvent
import com.fraktalio.fmodel.domain.examples.numbers.api.NumberEvent.OddNumberEvent
import com.fraktalio.fmodel.domain.examples.numbers.api.NumberValue
import com.fraktalio.fmodel.domain.examples.numbers.even.command.evenNumberDecider
import com.fraktalio.fmodel.domain.examples.numbers.odd.command.oddNumberDecider
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.gherkin.Feature
import kotlin.test.assertTrue


@ExperimentalCoroutinesApi
object AggregateTest : Spek({

    Feature("Aggregate") {
        val evenAggregate by memoized { evenNumberAggregate(evenNumberDecider(), evenNumberRepository()) }
        val allNumbersAggregate by memoized {
            numberAggregate(
                evenNumberDecider(),
                oddNumberDecider(),
                numberRepository()
            )
        }
        Scenario("Success") {
            lateinit var result: Either<Error, Iterable<Success.EventStoredSuccessfully<EvenNumberEvent?>>>

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
                assertTrue(result.isRight())
                assert(result is Either.Right && (result as Either.Right<Iterable<Success.EventStoredSuccessfully<EvenNumberEvent?>>>).value.count() == 1)
            }
        }
        Scenario("Success - handling null command") {
            lateinit var result: Either<Error, Iterable<Success.EventStoredSuccessfully<EvenNumberEvent?>>>

            When("handling command of type null") {
                runBlockingTest {
                    result = evenAggregate.handle(null)
                }
            }
            Then("expect success") {
                assert(result is Either.Right && (result as Either.Right<Iterable<Success.EventStoredSuccessfully<EvenNumberEvent?>>>).value.count() == 0)
            }
        }

        Scenario("Error - AggregateIsInTerminalState") {
            lateinit var result: Either<Error, Iterable<Success.EventStoredSuccessfully<EvenNumberEvent?>>>

            Given("events") {
                runBlockingTest {
                    result = evenAggregate.handle(
                        AddEvenNumber(
                            Description("Add 200"),
                            NumberValue(200)
                        )
                    )
                }
            }
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
            Then("expect error") {
                assertTrue(result is Either.Left && (result as Either.Left<Error>).value is Error.AggregateIsInTerminalState<*>)
            }
        }



        Scenario("Success - All Numbers Aggregate -  Even") {
            lateinit var result: Either<Error, Iterable<Success.EventStoredSuccessfully<Either<EvenNumberEvent?, OddNumberEvent?>>>>

            When("handling command of type AddEvenNumber") {
                runBlockingTest {
                    result = allNumbersAggregate.handle(
                        Either.Left(
                            AddEvenNumber(
                                Description("Add 2"),
                                NumberValue(2)
                            )
                        )
                    )
                }
            }
            Then("expect success") {
                assertTrue(result.isRight())
                assert(result is Either.Right && (result as Either.Right<Iterable<Success.EventStoredSuccessfully<Either<EvenNumberEvent?, OddNumberEvent?>>>>).value.count() == 1)

            }
        }

        Scenario("Success - All Numbers Aggregate -  Odd") {
            lateinit var result: Either<Error, Iterable<Success.EventStoredSuccessfully<Either<EvenNumberEvent?, OddNumberEvent?>>>>

            When("handling command of type AddOddNumber") {
                runBlockingTest {
                    result = allNumbersAggregate.handle(
                        Either.Right(
                            OddNumberCommand.AddOddNumber(
                                Description("Add 1"),
                                NumberValue(1)
                            )
                        )
                    )
                }
            }
            Then("expect success") {
                assertTrue(result.isRight())
                assert(result is Either.Right && (result as Either.Right<Iterable<Success.EventStoredSuccessfully<Either<EvenNumberEvent?, OddNumberEvent?>>>>).value.count() == 1)

            }
        }

    }

})

