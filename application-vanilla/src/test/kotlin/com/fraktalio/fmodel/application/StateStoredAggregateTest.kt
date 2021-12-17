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

import com.fraktalio.fmodel.application.examples.numbers.NumberStateRepository
import com.fraktalio.fmodel.application.examples.numbers.even.command.EvenNumberStateRepository
import com.fraktalio.fmodel.application.examples.numbers.even.command.evenNumberStateRepository
import com.fraktalio.fmodel.application.examples.numbers.even.command.evenNumberStateStoredAggregate
import com.fraktalio.fmodel.application.examples.numbers.numberStateRepository
import com.fraktalio.fmodel.application.examples.numbers.numberStateStoredAggregate
import com.fraktalio.fmodel.domain.examples.numbers.api.Description
import com.fraktalio.fmodel.domain.examples.numbers.api.EvenNumberState
import com.fraktalio.fmodel.domain.examples.numbers.api.NumberCommand.EvenNumberCommand.AddEvenNumber
import com.fraktalio.fmodel.domain.examples.numbers.api.NumberValue
import com.fraktalio.fmodel.domain.examples.numbers.api.OddNumberState
import com.fraktalio.fmodel.domain.examples.numbers.even.command.evenNumberDecider
import com.fraktalio.fmodel.domain.examples.numbers.evenNumberSaga
import com.fraktalio.fmodel.domain.examples.numbers.odd.command.oddNumberDecider
import com.fraktalio.fmodel.domain.examples.numbers.oddNumberSaga
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.gherkin.Feature
import kotlin.test.assertEquals


@ExperimentalCoroutinesApi
object StateStoredAggregateTest : Spek({

    Feature("Aggregate") {
        val evenNumberStateRepository by memoized {
            evenNumberStateRepository()
        }
        val evenAggregate by memoized {
            evenNumberStateStoredAggregate(
                evenNumberDecider(),
                evenNumberStateRepository()
            )
        }
        val allNumbersAggregate by memoized {
            numberStateStoredAggregate(
                evenNumberDecider(),
                oddNumberDecider(),
                evenNumberSaga(),
                oddNumberSaga(),
                numberStateRepository()
            )
        }

        Scenario("Success") {
            lateinit var result: EvenNumberState

            When("handling command of type AddEvenNumber") {
                runBlockingTest {
                    (evenNumberStateRepository as EvenNumberStateRepository).deleteAll()
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
                    assertEquals(
                        EvenNumberState(Description("Add 2"), NumberValue(2)),
                        result
                    )
                }
            }
        }

        Scenario("Success - combined aggregate") {
            lateinit var result: Pair<EvenNumberState, OddNumberState>

            When("handling command of type AddEvenNumber") {
                runBlockingTest {
                    (numberStateRepository() as NumberStateRepository).deleteAll()
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
                    assertEquals(
                        EvenNumberState(Description("Add 2"), NumberValue(2)),
                        result.first
                    )
                }
            }
        }

    }

})

