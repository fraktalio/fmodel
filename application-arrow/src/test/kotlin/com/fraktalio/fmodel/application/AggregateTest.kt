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
            lateinit var result: Flow<Either<Error, EvenNumberEvent?>>

            When("handling command of type AddEvenNumber") {
                runBlockingTest {
                    result = evenAggregate.handleEither(
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
                        assert(it is Either.Right && it.value is EvenNumberEvent.EvenNumberAdded)
                    }
                }
            }
        }

        Scenario("Success - All Numbers Aggregate -  Even") {
            lateinit var result: Flow<Either<Error, NumberEvent?>>

            When("handling command of type AddEvenNumber") {
                runBlockingTest {
                    result = allNumbersAggregate.handleEither(
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
                        assert(it is Either.Right && it.value is EvenNumberEvent.EvenNumberAdded)
                    }
                }
            }
        }

        Scenario("Success - All Numbers Aggregate -  Odd") {
            lateinit var result: Flow<Either<Error, NumberEvent?>>

            When("handling command of type AddOddNumber") {
                runBlockingTest {
                    result = allNumbersAggregate.handleEither(
                        NumberCommand.OddNumberCommand.AddOddNumber(
                            Description("Add 1"),
                            NumberValue(1)
                        )
                    )
                }
            }
            Then("expect success") {
                runBlockingTest {
                    assertTrue { result.toList().size == 2 }
                }
            }

        }

        Scenario("handled with Either Left / Error") {
            lateinit var result: Flow<Either<Error, EvenNumberEvent?>>

            When("handling command of type AddEvenNumber") {
                runBlockingTest {
                    result = evenAggregate.handleEither(
                        AddEvenNumber(
                            Description("Add 2000"),
                            NumberValue(2000)
                        )
                    )
                }
            }
            Then("expect left Error") {
                runBlockingTest {
                    result.take(1).collect {
                        assert(it is Either.Left && it.value is Error.CommandHandlingFailed<*>)
                    }
                }
            }
        }

        Scenario("Error - All Numbers Aggregate -  Even") {
            lateinit var result: Flow<Either<Error, NumberEvent?>>

            When("handling command of type AddEvenNumber") {
                runBlockingTest {
                    result = allNumbersAggregate.handleEither(
                        AddEvenNumber(
                            Description("Add 2000"),
                            NumberValue(2000)
                        )
                    )
                }
            }
            Then("expect error") {

                runBlockingTest {
                    result.take(1).collect {
                        assert(it is Either.Left && it.value is Error.CommandHandlingFailed<*>)
                    }
                }
            }
        }

        Scenario("Error Publishing - All Numbers Aggregate -  Even") {
            lateinit var result: Flow<Either<Error, EvenNumberEvent?>>

            When("handling/publishing command of type AddEvenNumber") {
                runBlockingTest {
                    result = flow<NumberCommand.EvenNumberCommand?> {
                        emit(
                            AddEvenNumber(
                                Description("Add 20"),
                                NumberValue(20)
                            )
                        )
                        throw RuntimeException("just any exception")
                    }
                        .publishEitherTo(evenAggregate)

                }
            }
            Then("expect error") {

                runBlockingTest {

                    val listRes = result.toList()
                    val zero = listRes[0]
                    val first = listRes[1]
                    assert(zero is Either.Right && zero.value is EvenNumberEvent)
                    assert(first is Either.Left && first.value is Error.CommandPublishingFailed)

                }
            }
        }
    }

})

