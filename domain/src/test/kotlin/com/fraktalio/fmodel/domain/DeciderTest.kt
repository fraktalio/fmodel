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

import arrow.core.Either.Left
import arrow.core.Either.Right
import com.fraktalio.fmodel.domain.examples.numbers.api.Description
import com.fraktalio.fmodel.domain.examples.numbers.api.EvenNumberState
import com.fraktalio.fmodel.domain.examples.numbers.api.NumberCommand.EvenNumberCommand.AddEvenNumber
import com.fraktalio.fmodel.domain.examples.numbers.api.NumberCommand.OddNumberCommand.AddOddNumber
import com.fraktalio.fmodel.domain.examples.numbers.api.NumberEvent.EvenNumberEvent
import com.fraktalio.fmodel.domain.examples.numbers.api.NumberEvent.EvenNumberEvent.EvenNumberAdded
import com.fraktalio.fmodel.domain.examples.numbers.api.NumberEvent.OddNumberEvent.OddNumberAdded
import com.fraktalio.fmodel.domain.examples.numbers.api.NumberValue
import com.fraktalio.fmodel.domain.examples.numbers.api.OddNumberState
import com.fraktalio.fmodel.domain.examples.numbers.even.command.evenNumberDecider
import com.fraktalio.fmodel.domain.examples.numbers.odd.command.oddNumberDecider
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.gherkin.Feature
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue


object DeciderTest : Spek({

    Feature("Decider") {
        val evenDecider by memoized { evenNumberDecider() }
        val oddDecider by memoized { oddNumberDecider() }
        val combinedDecider by memoized { evenDecider.combineDeciders(oddDecider) }

        Scenario("Decide") {
            lateinit var result: Iterable<EvenNumberEvent?>

            When("being in current/initial state of type EvenNumberState and handling command of type AddEvenNumber") {
                result = evenDecider
                    .decide(
                        AddEvenNumber(
                            Description("2"),
                            NumberValue(2)
                        ), evenDecider.initialState
                    )
            }

            Then("event of type EvenNumberEvent should be published") {
                assertEquals(
                    listOf(
                        EvenNumberAdded(
                            Description("2"),
                            NumberValue(2)
                        )
                    ), result
                )
            }

        }

        Scenario("Decide - left map over Command parameter - functor") {
            lateinit var result: Iterable<EvenNumberEvent?>

            When("being in current/initial state of type EvenNumberState and handling command of type Int") {
                result = evenDecider
                    .lmapOnC { cn: Int ->
                        AddEvenNumber(
                            Description(cn.toString()),
                            NumberValue(cn)
                        )
                    }
                    .decide(2, evenDecider.initialState)
            }

            Then("event of type EvenNumberEvent should be published") {
                assertEquals(
                    listOf(
                        EvenNumberAdded(
                            Description("2"),
                            NumberValue(2)
                        )
                    ), result
                )
            }
        }

        Scenario("Decide - dimap over Event parameter - profunctor") {
            lateinit var result: Iterable<Int?>

            When("being in current/initial state of type EvenNumberState and handling command of type AddEvenNumber") {
                result = evenDecider
                    .dimapOnE(
                        fr = { evenNumberEvent: EvenNumberEvent? -> evenNumberEvent?.value?.get },
                        fl = { number: Int ->
                            EvenNumberAdded(
                                Description(
                                    number.toString()
                                ), NumberValue(number)
                            )
                        }
                    )
                    .decide(
                        AddEvenNumber(
                            Description("2"),
                            NumberValue(2)
                        ), evenDecider.initialState
                    )
            }

            Then("event of type Int should be published") {
                assertEquals(listOf(2), result)
            }
        }

        Scenario("Decide - dimap over State parameter - profunctor") {
            lateinit var result: Iterable<EvenNumberEvent?>

            When("being in current/initial state of type Int and handling command of type AddEvenNumber") {
                result = evenDecider
                    .dimapOnS(
                        fr = { evenNumberState: EvenNumberState? -> evenNumberState?.value?.get },
                        fl = { number: Int -> EvenNumberState(Description(number.toString()), NumberValue(number)) }
                    )
                    .decide(
                        AddEvenNumber(
                            Description("2"),
                            NumberValue(2)
                        ), evenDecider.initialState.value.get
                    )
            }

            Then("event of type EvenNumberEvent should be published") {
                assertEquals(
                    listOf(
                        EvenNumberAdded(
                            Description("2"),
                            NumberValue(2)
                        )
                    ), result
                )
            }
        }

        Scenario("Decide - product over State parameter - applicative") {
            lateinit var result: Iterable<EvenNumberEvent?>

            When("being in current/initial of type EvenNumberState and handling command of type AddEvenNumber by the product of two deciders") {
                val decider2 = evenDecider.rmapOnS { evenNumberState: EvenNumberState? -> evenNumberState?.value?.get }
                result = evenDecider
                    .rproductOnS(decider2)
                    .decide(
                        AddEvenNumber(
                            Description("2"),
                            NumberValue(2)
                        ), evenDecider.initialState
                    )
            }

            Then("two events of type EvenNumberEvent should be published") {
                assertEquals(
                    listOf(
                        EvenNumberAdded(
                            Description("2"),
                            NumberValue(2)
                        ),
                        EvenNumberAdded(
                            Description("2"),
                            NumberValue(2)
                        )
                    ), result
                )
            }
        }

        Scenario("Evolve") {
            lateinit var result: EvenNumberState

            When("being in current/initial state of type EvenNumberState and handling event of type EvenNumberEvent") {
                result = evenDecider.evolve(evenDecider.initialState, EvenNumberAdded(Description("2"), NumberValue(2)))
            }

            Then("new state of type EvenNumberState should be constructed/evolved") {
                assertEquals(EvenNumberState(Description("2"), NumberValue(2)), result)
            }

        }

        Scenario("Evolve - left map over Command parameter - functor") {
            lateinit var result: EvenNumberState

            When("being in current/initial state of type EvenNumberState and handling event of type EvenNumberEvent") {
                result = evenDecider
                    .lmapOnC { cn: Int ->
                        AddEvenNumber(
                            Description(cn.toString()),
                            NumberValue(cn)
                        )
                    }
                    .evolve(evenDecider.initialState, EvenNumberAdded(Description("2"), NumberValue(2)))
            }

            Then("new state of type EvenNumberState should be constructed/evolved") {
                assertEquals(EvenNumberState(Description("2"), NumberValue(2)), result)
            }

        }

        Scenario("Evolve - dimap over Event parameter - profunctor") {
            lateinit var result: EvenNumberState

            When("being in current/initial state of type EvenNumberState and handling event of type Int") {
                result = evenDecider
                    .dimapOnE(
                        fr = { evenNumberEvent: EvenNumberEvent? -> evenNumberEvent?.value?.get },
                        fl = { number: Int -> EvenNumberAdded(Description(number.toString()), NumberValue(number)) }
                    )
                    .evolve(evenDecider.initialState, 2)
            }

            Then("new state of type EvenNumberState should be constructed/evolved") {
                assertEquals(EvenNumberState(Description("2"), NumberValue(2)), result)
            }

        }

        Scenario("Evolve - dimap over State parameter - profunctor") {
            var result: Int = 0

            When("being in current/initial state of type Int and handling event of type EvenNumberEvent") {
                result = evenDecider
                    .dimapOnS(
                        fr = { evenNumberState: EvenNumberState -> evenNumberState.value.get },
                        fl = { number: Int -> EvenNumberState(Description(number.toString()), NumberValue(number)) }
                    )
                    .evolve(evenDecider.initialState.value.get, EvenNumberAdded(Description("2"), NumberValue(2)))
            }

            Then("new state of type Int should be constructed/evolved") {
                assertEquals(2, result)
            }

        }

        Scenario("Evolve - product over State parameter - applicative") {
            lateinit var result: Pair<EvenNumberState, Int>

            When("being in current/initial state of type Int and handling event of type EvenNumberEvent by the product of 2 deciders") {
                val decider2 = evenDecider.rmapOnS { evenNumberState: EvenNumberState -> evenNumberState.value.get }
                result = evenDecider
                    .rproductOnS(decider2)
                    .evolve(evenDecider.initialState, EvenNumberAdded(Description("2"), NumberValue(2)))

            }

            Then("new state of type Pair<EvenNumberState, Int> should be constructed/evolved") {
                assertEquals(Pair(EvenNumberState(Description("2"), NumberValue(2)), 2), result)
            }
        }

        Scenario("is terminal state") {
            var isTerminalResult: Boolean = false

            When("being in current/initial state of type EvenNumberState and handling new state of type EvenNumberState > 100") {
                isTerminalResult = evenDecider
                    .isTerminal(EvenNumberState(Description("101"), NumberValue(101)))
            }

            Then("it should enter in terminal/final state") {
                assertTrue(isTerminalResult)
            }
        }

        Scenario("is terminal state - left map over Command parameter - functor") {
            var isTerminalResult: Boolean = false

            When("being in current/initial state of type EvenNumberState and handling new state of type EvenNumberState > 100") {
                isTerminalResult = evenDecider
                    .lmapOnC { cn: Int ->
                        AddEvenNumber(
                            Description(cn.toString()),
                            NumberValue(cn)
                        )
                    }
                    .isTerminal(EvenNumberState(Description("101"), NumberValue(101)))
            }

            Then("it should enter in terminal/final state") {
                assertTrue(isTerminalResult)
            }
        }

        Scenario("is terminal state - dimap over Event parameter - profunctor") {
            var isTerminalResult: Boolean = false

            When("being in current/initial state of type EvenNumberState and handling new state of type EvenNumberState > 100") {
                isTerminalResult = evenDecider
                    .dimapOnE(
                        fr = { evenNumberEvent: EvenNumberEvent? -> evenNumberEvent?.value?.get },
                        fl = { number: Int -> EvenNumberAdded(Description(number.toString()), NumberValue(number)) }
                    )
                    .isTerminal(EvenNumberState(Description("101"), NumberValue(101)))
            }

            Then("it should enter in terminal/final state") {
                assertTrue(isTerminalResult)
            }
        }

        Scenario("is terminal state - dimap over State parameter - profunctor") {
            var isTerminalResult: Boolean = false

            When("being in current/initial state of type Int and handling new state of type Int > 100") {
                isTerminalResult = evenDecider
                    .dimapOnS(
                        fr = { evenNumberState: EvenNumberState -> evenNumberState.value.get },
                        fl = { number: Int -> EvenNumberState(Description(number.toString()), NumberValue(number)) }
                    )
                    .isTerminal(101)
            }

            Then("it should enter in terminal/final state") {
                assertTrue(isTerminalResult)
            }
        }

        Scenario("is terminal state - product over State parameter - applicative") {
            var isTerminalResult: Boolean = false

            When("being in current/initial state of type Int and handling event of type EvenNumberEvent by the product of 2 deciders") {
                val decider2 = evenDecider.rmapOnS { evenNumberState: EvenNumberState -> evenNumberState.value.get }
                isTerminalResult = evenDecider
                    .rproductOnS(decider2)
                    .isTerminal(EvenNumberState(Description("101"), NumberValue(101)))

            }

            Then("new state of type Pair<EvenNumberState, Int> should be constructed/evolved") {
                assertTrue(isTerminalResult)
            }
        }

        Scenario("initial state") {
            val result = EvenNumberState(Description("Initial state"), NumberValue(0))

            When("decider is created") {}

            Then("it should be in the initial state of type EvenNumberState") {
                assertEquals(result, evenDecider.initialState)
            }
        }

        Scenario("initial state - left map over Command parameter - functor") {
            val result = EvenNumberState(Description("Initial state"), NumberValue(0))

            Then("it should be in the initial state of type EvenNumberState") {
                assertEquals(
                    result, evenDecider
                        .lmapOnC { cn: Int ->
                            AddEvenNumber(
                                Description(cn.toString()),
                                NumberValue(cn)
                            )
                        }.initialState
                )
            }
        }

        Scenario("initial state - dimap over Even parameter - profunctor") {
            val result = EvenNumberState(Description("Initial state"), NumberValue(0))

            Then("it should be in the initial state of type EvenNumberState") {
                assertEquals(result, evenDecider
                    .dimapOnE(
                        fr = { evenNumberEvent: EvenNumberEvent? -> evenNumberEvent?.value?.get },
                        fl = { number: Int -> EvenNumberAdded(Description(number.toString()), NumberValue(number)) }
                    ).initialState
                )
            }
        }

        Scenario("initial state - dimap over State parameter - profunctor") {
            val result = 0

            Then("it should be in the initial state of type EvenNumberState") {
                assertEquals(result, evenDecider.dimapOnS(
                    fr = { evenNumberState: EvenNumberState -> evenNumberState.value.get },
                    fl = { number: Int -> EvenNumberState(Description(number.toString()), NumberValue(number)) }
                ).initialState)
            }
        }

        Scenario("initial state - product over State parameter - applicative") {
            val result: Pair<EvenNumberState, Int> =
                Pair(EvenNumberState(Description("Initial state"), NumberValue(0)), 0)

            Then("it should be in the initial state of type Pair<EvenNumberState, Int>") {
                val decider2 = evenDecider.rmapOnS { evenNumberState: EvenNumberState -> evenNumberState.value.get }

                assertEquals(result, evenDecider.rproductOnS(decider2).initialState)
            }
        }

        Scenario("Deciders are combinable - monoid") {

            Then("this one big decider is acting as a command bus, being able to handle both type of commands (Left command in this case) and publish appropriate event as a result") {
                val resultOfDecide = listOf(Left(EvenNumberAdded(Description("2"), NumberValue(2))))

                assertEquals(
                    resultOfDecide,
                    combinedDecider
                        .decide(
                            Left(AddEvenNumber(Description("2"), NumberValue(2))),
                            Pair(
                                EvenNumberState(Description("0"), NumberValue(0)),
                                OddNumberState(Description("1"), NumberValue(1))
                            )
                        )
                )
            }

            Then("this one big decider is acting as a command bus, being able to handle both type of commands (Right command in this case) and publish appropriate event as a result") {
                val resultOfDecide = listOf(Right(OddNumberAdded(Description("1"), NumberValue(1))))

                assertEquals(
                    resultOfDecide,
                    combinedDecider
                        .decide(
                            Right(AddOddNumber(Description("1"), NumberValue(1))),
                            Pair(
                                EvenNumberState(Description("0"), NumberValue(0)),
                                OddNumberState(Description("1"), NumberValue(1))
                            )
                        )
                )
            }

            Then("this one big decider is acting as a event bus, being able to handle both type of events (Left event in this case) and construct the new Decider state as a result") {
                val resultOfDecide = Pair(
                    EvenNumberState(Description("2+0"), NumberValue(2)),
                    OddNumberState(Description("1"), NumberValue(1))
                )

                assertEquals(
                    resultOfDecide,
                    combinedDecider
                        .evolve(
                            Pair(
                                EvenNumberState(Description("0"), NumberValue(0)),
                                OddNumberState(Description("1"), NumberValue(1))
                            ),
                            Left(EvenNumberAdded(Description("2+0"), NumberValue(2)))
                        )
                )
            }

            Then("this one big decider is acting as a event bus, being able to handle both type of events (Right event in this case) and construct the new Decider state as a result") {
                val resultOfDecide = Pair(
                    EvenNumberState(Description("0"), NumberValue(0)),
                    OddNumberState(Description("3+1"), NumberValue(4))
                )

                assertEquals(
                    resultOfDecide,
                    combinedDecider
                        .evolve(
                            Pair(
                                EvenNumberState(Description("0"), NumberValue(0)),
                                OddNumberState(Description("1"), NumberValue(1))
                            ),
                            Right(OddNumberAdded(Description("3+1"), NumberValue(3)))
                        )
                )
            }

            Then("this one big decider is combining terminal state of both deciders with AND operator") {
                assertFalse(
                    combinedDecider
                        .isTerminal(
                            Pair(
                                EvenNumberState(Description("101"), NumberValue(101)),
                                OddNumberState(Description("1"), NumberValue(1))
                            )
                        )
                )
                assertFalse(
                    combinedDecider
                        .isTerminal(
                            Pair(
                                EvenNumberState(Description("0"), NumberValue(0)),
                                OddNumberState(Description("101"), NumberValue(101))
                            )
                        )
                )
                assertTrue(
                    combinedDecider
                        .isTerminal(
                            Pair(
                                EvenNumberState(Description("101"), NumberValue(101)),
                                OddNumberState(Description("102"), NumberValue(102))
                            )
                        )
                )
            }


        }
    }
})

