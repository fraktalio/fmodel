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

import com.fraktalio.fmodel.application.examples.numbers.NumberViewRepository
import com.fraktalio.fmodel.application.examples.numbers.even.query.EvenNumberViewRepository
import com.fraktalio.fmodel.application.examples.numbers.even.query.evenNumberMaterializedView
import com.fraktalio.fmodel.application.examples.numbers.even.query.evenNumberViewRepository
import com.fraktalio.fmodel.application.examples.numbers.numberMaterializedView
import com.fraktalio.fmodel.application.examples.numbers.numberViewRepository
import com.fraktalio.fmodel.application.examples.numbers.odd.query.OddNumberViewRepository
import com.fraktalio.fmodel.application.examples.numbers.odd.query.oddNumberViewRepository
import com.fraktalio.fmodel.domain.examples.numbers.api.Description
import com.fraktalio.fmodel.domain.examples.numbers.api.EvenNumberState
import com.fraktalio.fmodel.domain.examples.numbers.api.NumberEvent.EvenNumberEvent.EvenNumberAdded
import com.fraktalio.fmodel.domain.examples.numbers.api.NumberEvent.OddNumberEvent.OddNumberAdded
import com.fraktalio.fmodel.domain.examples.numbers.api.NumberValue
import com.fraktalio.fmodel.domain.examples.numbers.api.OddNumberState
import com.fraktalio.fmodel.domain.examples.numbers.even.query.evenNumberView
import com.fraktalio.fmodel.domain.examples.numbers.odd.query.oddNumberView
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runBlockingTest
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.gherkin.Feature
import kotlin.test.assertTrue


@ExperimentalCoroutinesApi
object MaterializedViewTest : Spek({

    Feature("MaterializedView") {
        val evenView by memoized { evenNumberMaterializedView(evenNumberView(), evenNumberViewRepository()) }
        val allNumbersView by memoized {
            numberMaterializedView(
                oddNumberView(),
                evenNumberView(),
                numberViewRepository()
            )
        }
        Scenario("Success") {
            var result: EvenNumberState? = null

            When("handling event of type EvenNumberAdded") {
                runBlockingTest {
                    (evenNumberViewRepository() as EvenNumberViewRepository).deleteAll()
                    result = evenView.handle(
                        EvenNumberAdded(
                            Description("Add 2"),
                            NumberValue(2)
                        )
                    )
                }
            }
            Then("expect success") {
                assertTrue(result?.value?.get == 2)
            }
        }

        Scenario("Success - combined view - even") {
            lateinit var result: Pair<EvenNumberState?, OddNumberState?>

            When("handling event of type EvenNumberAdded") {
                runBlockingTest {
                    (numberViewRepository() as NumberViewRepository).deleteAll()
                    result = allNumbersView.handle(
                        EvenNumberAdded(
                            Description("Add 2"),
                            NumberValue(2)
                        )
                    )
                }
            }
            Then("expect success") {
                assertTrue(result.first?.value?.get == 2)
            }
        }

        Scenario("Success - combined view - odd") {
            lateinit var result: Pair<EvenNumberState?, OddNumberState?>

            When("handling event of type EvenNumberAdded") {
                runBlockingTest {
                    (numberViewRepository() as NumberViewRepository).deleteAll()
                    result = allNumbersView.handle(
                        OddNumberAdded(
                            Description("Add 3"),
                            NumberValue(3)
                        )
                    )
                }
            }
            Then("expect success") {
                assertTrue(result.second?.value?.get == 3)
            }
        }

        Scenario("Success - combined view - flow - even") {
            lateinit var result: Flow<Pair<EvenNumberState?, OddNumberState?>>

            When("handling flow of event(s) of type EvenNumberAdded") {
                runBlockingTest {
                    (numberViewRepository() as NumberViewRepository).deleteAll()
                    result = allNumbersView.handle(
                        flowOf(
                            EvenNumberAdded(
                                Description("Add 2"),
                                NumberValue(2)
                            )
                        )
                    )
                }
            }
            Then("expect success") {
                runBlockingTest {
                    result.collect {
                        assertTrue(it.first?.value?.get == 2)
                    }
                }
            }
        }

    }

})

