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
import com.fraktalio.fmodel.application.examples.numbers.NumberViewRepository
import com.fraktalio.fmodel.application.examples.numbers.even.query.EvenNumberViewRepository
import com.fraktalio.fmodel.application.examples.numbers.even.query.evenNumberMaterializedView
import com.fraktalio.fmodel.application.examples.numbers.even.query.evenNumberViewRepository
import com.fraktalio.fmodel.application.examples.numbers.numberMaterializedView
import com.fraktalio.fmodel.application.examples.numbers.numberViewRepository
import com.fraktalio.fmodel.domain.examples.numbers.api.Description
import com.fraktalio.fmodel.domain.examples.numbers.api.EvenNumberState
import com.fraktalio.fmodel.domain.examples.numbers.api.NumberEvent.EvenNumberEvent.EvenNumberAdded
import com.fraktalio.fmodel.domain.examples.numbers.api.NumberValue
import com.fraktalio.fmodel.domain.examples.numbers.api.OddNumberState
import com.fraktalio.fmodel.domain.examples.numbers.even.query.evenNumberView
import com.fraktalio.fmodel.domain.examples.numbers.odd.query.oddNumberView
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
            lateinit var result: Either<Error, EvenNumberState?>

            When("handling event of type EvenNumberAdded") {
                runBlockingTest {
                    (evenNumberViewRepository() as EvenNumberViewRepository).deleteAll()
                    result = evenView.handleEither(
                        EvenNumberAdded(
                            Description("Add 2"),
                            NumberValue(2)
                        )
                    )
                }
            }
            Then("expect success") {
                assertTrue(result.isRight())
                assert(result is Either.Right && (result as Either.Right<EvenNumberState?>).value?.value?.get == 2)
            }
        }

        Scenario("Success - handling null event") {
            lateinit var result: Either<Error, EvenNumberState?>

            When("handling null") {
                runBlockingTest {
                    (evenNumberViewRepository() as EvenNumberViewRepository).deleteAll()
                    result = evenView.handleEither(
                        null
                    )
                }
            }
            Then("expect success") {
                assertTrue(result.isRight())
            }
        }

        Scenario("Success - All numbers View") {
            lateinit var result: Either<Error, Pair<EvenNumberState?, OddNumberState?>>

            When("handling event of type EvenNumberAdded") {
                runBlockingTest {
                    (numberViewRepository() as NumberViewRepository).deleteAll()
                    result = allNumbersView.handleEither(
                        EvenNumberAdded(
                            Description("Add 2"),
                            NumberValue(2)
                        )
                    )
                }
            }
            Then("expect success") {
                assertTrue(result.isRight())
            }
        }

    }

})

