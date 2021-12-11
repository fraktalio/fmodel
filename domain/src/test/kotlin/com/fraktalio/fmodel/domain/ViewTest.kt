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
import com.fraktalio.fmodel.domain.examples.numbers.api.EvenNumberState
import com.fraktalio.fmodel.domain.examples.numbers.api.NumberEvent.EvenNumberEvent.EvenNumberAdded
import com.fraktalio.fmodel.domain.examples.numbers.api.NumberValue
import com.fraktalio.fmodel.domain.examples.numbers.api.OddNumberState
import com.fraktalio.fmodel.domain.examples.numbers.even.query.evenNumberView
import com.fraktalio.fmodel.domain.examples.numbers.odd.query.oddNumberView
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.gherkin.Feature
import kotlin.test.assertEquals


object ViewTest : Spek({

    Feature("View") {

        val evenView by memoized { evenNumberView() }
        val oddView by memoized { oddNumberView() }
        val combinedView by memoized { evenView.combine(oddView) }


        Scenario("Evolve") {
            var result: EvenNumberState? = null

            When("being in current/initial state of type EvenNumberState and handling event of type EvenNumberEvent") {
                result = evenView.evolve(evenView.initialState, EvenNumberAdded(Description("2"), NumberValue(2)))
            }

            Then("new state of type EvenNumberState should be constructed/evolved") {
                assertEquals(EvenNumberState(Description("Initial state, 2"), NumberValue(2)), result)
            }

        }

        Scenario("Evolve - lef map over Event parameter - functor (contravariant)") {
            var result: EvenNumberState? = null

            When("being in current/initial state of type EvenNumberState and handling event of type Int") {
                result = evenView
                    .mapLeftOnEvent { number: Int ->
                        EvenNumberAdded(
                            Description(number.toString()),
                            NumberValue(number)
                        )
                    }
                    .evolve(evenView.initialState, 2)
            }

            Then("new state of type EvenNumberState should be constructed/evolved") {
                assertEquals(EvenNumberState(Description("Initial state, 2"), NumberValue(2)), result)
            }

        }

        Scenario("Evolve - dimap over State parameter - profunctor") {
            var result: Int? = 0

            When("being in current/initial state of type Int and handling event of type EvenNumberEvent") {
                result = evenView.initialState?.value?.let {
                    evenView
                        .dimapOnState(
                            fr = { evenNumberState: EvenNumberState? -> evenNumberState?.value?.get },
                            fl = { number: Int -> EvenNumberState(Description(number.toString()), NumberValue(number)) }
                        )
                        .evolve(it.get, EvenNumberAdded(Description("2"), NumberValue(2)))
                }
            }

            Then("new state of type Int should be constructed/evolved") {
                assertEquals(2, result)
            }

        }

        Scenario("Evolve - product over State parameter - applicative") {
            lateinit var result: Pair<EvenNumberState?, Int?>

            When("being in current/initial state, and handling event of type EvenNumberEvent by the product of 2 views") {
                val view2 = evenView.mapOnState { evenNumberState: EvenNumberState? -> evenNumberState?.value?.get }
                result = evenView
                    .productOnState(view2)
                    .evolve(evenView.initialState, EvenNumberAdded(Description("2"), NumberValue(2)))

            }

            Then("new state of type Pair<EvenNumberState?, Int?> should be constructed/evolved") {
                assertEquals(Pair(EvenNumberState(Description("Initial state, 2"), NumberValue(2)), 2), result)
            }
        }

        Scenario("initial state") {
            val result = EvenNumberState(Description("Initial state"), NumberValue(0))

            When("view is created") {}

            Then("it should be in the initial state of type EvenNumberState") {
                assertEquals(result, evenView.initialState)
            }
        }

        Scenario("initial state - left map over Event parameter - functor (contravariant)") {
            val result = EvenNumberState(Description("Initial state"), NumberValue(0))

            Then("it should be in the initial state of type EvenNumberState") {
                assertEquals(
                    result, evenView
                        .mapLeftOnEvent { e: Int ->
                            EvenNumberAdded(
                                Description(e.toString()),
                                NumberValue(e)
                            )
                        }.initialState
                )
            }
        }

        Scenario("initial state - dimap over State parameter - profunctor") {
            val result = 0

            Then("it should be in the initial state of type Int") {
                assertEquals(result, evenView
                    .dimapOnState(
                        fr = { evenNumberState: EvenNumberState? -> evenNumberState?.value?.get },
                        fl = { number: Int -> EvenNumberState(Description(number.toString()), NumberValue(number)) }
                    ).initialState
                )
            }
        }

        Scenario("initial state - product over State parameter - applicative") {
            val result: Pair<EvenNumberState, Int> =
                Pair(EvenNumberState(Description("Initial state"), NumberValue(0)), 0)

            Then("it should be in the initial state of type Pair<EvenNumberState, Int>") {
                val view2 = evenView.mapOnState { evenNumberState: EvenNumberState? -> evenNumberState?.value?.get }

                assertEquals(result, evenView.productOnState(view2).initialState)
            }
        }


        Scenario("Evolve - combine - pair") {
            var result: Pair<EvenNumberState?, OddNumberState?> = Pair(null, null)

            When("being in current/initial state of type Pair(EvenNumberState, OddNumberState) and handling event of super type NumberEvent") {
                result =
                    combinedView.evolve(combinedView.initialState, EvenNumberAdded(Description("2"), NumberValue(2)))
            }

            Then("new state of type Pair(EvenNumberState, OddNumberState) should be constructed/evolved") {
                assertEquals(
                    Pair(
                        EvenNumberState(Description("Initial state, 2"), NumberValue(2)),
                        OddNumberState(Description("Initial state"), NumberValue(0))
                    ), result
                )
            }
        }
    }
})

