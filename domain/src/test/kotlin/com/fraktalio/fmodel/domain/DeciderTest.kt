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
import com.fraktalio.fmodel.domain.examples.numbers.api.NumberCommand.EvenNumberCommand.AddEvenNumber
import com.fraktalio.fmodel.domain.examples.numbers.api.NumberEvent.EvenNumberEvent
import com.fraktalio.fmodel.domain.examples.numbers.api.NumberEvent.EvenNumberEvent.EvenNumberAdded
import com.fraktalio.fmodel.domain.examples.numbers.even.command.evenNumberDecider
import com.fraktalio.fmodel.domain.examples.numbers.odd.command.oddNumberDecider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.gherkin.Feature
import kotlin.test.assertEquals


object DeciderTest : Spek({

    Feature("Decider") {
        val evenDecider by memoized { evenNumberDecider() }
        val oddDecider by memoized { oddNumberDecider() }
        val combinedDecider by memoized { evenDecider.combine(oddDecider) }

        Scenario("Decide") {
            lateinit var result: Flow<EvenNumberEvent?>

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
                runBlocking {
                    val expected = mutableListOf<EvenNumberEvent?>()
                    result.take(1).collect {
                        expected.add(it)
                        assertEquals(
                            listOf<EvenNumberEvent?>(
                                EvenNumberAdded(
                                    Description("2"),
                                    NumberValue(2)
                                )
                            ), expected
                        )
                    }
                }
            }

        }

        Scenario("Decide - Combine") {
            lateinit var result: Flow<NumberEvent?>

            When("being in current/initial state of type Pair<EvenNumberState, OddNumberState> and handling command of super type NumberCommand") {
                result = combinedDecider
                    .decide(
                        AddEvenNumber(
                            Description("2"),
                            NumberValue(2)
                        ), combinedDecider.initialState
                    )
            }

            Then("event of super type NumberEvent should be published") {
                runBlocking {
                    assertEquals(
                        listOf(
                            EvenNumberAdded(
                                Description("2"),
                                NumberValue(2)
                            )
                        ), result.toList()
                    )
                }
            }

        }


        Scenario("Decide - left map over Command parameter - functor") {
            lateinit var result: Flow<EvenNumberEvent?>

            When("being in current/initial state of type EvenNumberState and handling command of type Int") {
                result = evenDecider
                    .mapLeftOnCommand { cn: Int ->
                        AddEvenNumber(
                            Description(cn.toString()),
                            NumberValue(cn)
                        )
                    }
                    .decide(2, evenDecider.initialState)
            }

            Then("event of type EvenNumberEvent should be published") {
                runBlocking {
                    assertEquals(
                        listOf(
                            EvenNumberAdded(
                                Description("2"),
                                NumberValue(2)
                            )
                        ), result.toList()
                    )
                }
            }
        }

        Scenario("Decide - dimap over Event parameter - profunctor") {
            lateinit var result: Flow<Int?>

            When("being in current/initial state of type EvenNumberState and handling command of type AddEvenNumber") {
                result = evenDecider
                    .dimapOnEvent(
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
                runBlocking {
                    assertEquals(listOf(2), result.toList())
                }
            }
        }

        Scenario("Decide - dimap over State parameter - profunctor") {
            lateinit var result: Flow<EvenNumberEvent?>

            When("being in current/initial state of type Int and handling command of type AddEvenNumber") {
                result = evenDecider
                    .dimapOnState(
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
                runBlocking {
                    assertEquals(
                        listOf(
                            EvenNumberAdded(
                                Description("2"),
                                NumberValue(2)
                            )
                        ), result.toList()
                    )
                }
            }
        }

        Scenario("Decide - product over State parameter - applicative") {
            lateinit var result: Flow<EvenNumberEvent?>

            When("being in current/initial of type EvenNumberState and handling command of type AddEvenNumber by the product of two deciders") {
                val decider2 =
                    evenDecider.mapOnState { evenNumberState: EvenNumberState? -> evenNumberState?.value?.get }
                result = evenDecider
                    .productOnState(decider2)
                    .decide(
                        AddEvenNumber(
                            Description("2"),
                            NumberValue(2)
                        ), evenDecider.initialState
                    )
            }

            Then("two events of type EvenNumberEvent should be published") {
                runBlocking {
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
                        ), result.toList()
                    )
                }
            }
        }

        Scenario("Evolve") {
            lateinit var result: EvenNumberState

            When("being in current/initial state of type EvenNumberState and handling event of type EvenNumberEvent") {
                runBlocking {
                    result =
                        evenDecider.evolve(evenDecider.initialState, EvenNumberAdded(Description("2"), NumberValue(2)))
                }
            }

            Then("new state of type EvenNumberState should be constructed/evolved") {
                assertEquals(EvenNumberState(Description("2"), NumberValue(2)), result)
            }

        }

        Scenario("Evolve - Combine") {
            lateinit var result: Pair<EvenNumberState, OddNumberState>

            When("being in current/initial state of type EvenNumberState and handling event of type EvenNumberEvent") {
                runBlocking {
                    result = combinedDecider.evolve(
                        combinedDecider.initialState,
                        EvenNumberAdded(Description("2"), NumberValue(2))
                    )
                }
            }

            Then("new state of type EvenNumberState should be constructed/evolved") {
                assertEquals(
                    Pair(
                        EvenNumberState(Description("2"), NumberValue(2)),
                        OddNumberState(Description("Initial state"), NumberValue(0))
                    ), result
                )
            }

        }

        Scenario("Evolve - left map over Command parameter - functor") {
            lateinit var result: EvenNumberState

            When("being in current/initial state of type EvenNumberState and handling event of type EvenNumberEvent") {
                runBlocking {
                    result = evenDecider
                        .mapLeftOnCommand { cn: Int ->
                            AddEvenNumber(
                                Description(cn.toString()),
                                NumberValue(cn)
                            )
                        }
                        .evolve(evenDecider.initialState, EvenNumberAdded(Description("2"), NumberValue(2)))
                }
            }

            Then("new state of type EvenNumberState should be constructed/evolved") {
                assertEquals(EvenNumberState(Description("2"), NumberValue(2)), result)
            }

        }

        Scenario("Evolve - dimap over Event parameter - profunctor") {
            lateinit var result: EvenNumberState

            When("being in current/initial state of type EvenNumberState and handling event of type Int") {
                runBlocking {
                    result = evenDecider
                        .dimapOnEvent(
                            fr = { evenNumberEvent: EvenNumberEvent? -> evenNumberEvent?.value?.get },
                            fl = { number: Int -> EvenNumberAdded(Description(number.toString()), NumberValue(number)) }
                        )
                        .evolve(evenDecider.initialState, 2)
                }
            }

            Then("new state of type EvenNumberState should be constructed/evolved") {
                assertEquals(EvenNumberState(Description("2"), NumberValue(2)), result)
            }

        }

        Scenario("Evolve - dimap over State parameter - profunctor") {
            var result: Int = 0

            When("being in current/initial state of type Int and handling event of type EvenNumberEvent") {
                runBlocking {
                    result = evenDecider
                        .dimapOnState(
                            fr = { evenNumberState: EvenNumberState -> evenNumberState.value.get },
                            fl = { number: Int -> EvenNumberState(Description(number.toString()), NumberValue(number)) }
                        )
                        .evolve(evenDecider.initialState.value.get, EvenNumberAdded(Description("2"), NumberValue(2)))
                }
            }

            Then("new state of type Int should be constructed/evolved") {
                assertEquals(2, result)
            }

        }

        Scenario("Evolve - product over State parameter - applicative") {
            lateinit var result: Pair<EvenNumberState, Int>

            When("being in current/initial state of type Int and handling event of type EvenNumberEvent by the product of 2 deciders") {
                runBlocking {
                    val decider2 =
                        evenDecider.mapOnState { evenNumberState: EvenNumberState -> evenNumberState.value.get }
                    result = evenDecider
                        .productOnState(decider2)
                        .evolve(evenDecider.initialState, EvenNumberAdded(Description("2"), NumberValue(2)))
                }

            }

            Then("new state of type Pair<EvenNumberState, Int> should be constructed/evolved") {
                assertEquals(Pair(EvenNumberState(Description("2"), NumberValue(2)), 2), result)
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
                        .mapLeftOnCommand { cn: Int ->
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
                    .dimapOnEvent(
                        fr = { evenNumberEvent: EvenNumberEvent? -> evenNumberEvent?.value?.get },
                        fl = { number: Int -> EvenNumberAdded(Description(number.toString()), NumberValue(number)) }
                    ).initialState
                )
            }
        }

        Scenario("initial state - dimap over State parameter - profunctor") {
            val result = 0

            Then("it should be in the initial state of type EvenNumberState") {
                assertEquals(result, evenDecider.dimapOnState(
                    fr = { evenNumberState: EvenNumberState -> evenNumberState.value.get },
                    fl = { number: Int -> EvenNumberState(Description(number.toString()), NumberValue(number)) }
                ).initialState)
            }
        }

        Scenario("initial state - product over State parameter - applicative") {
            val result: Pair<EvenNumberState, Int> =
                Pair(EvenNumberState(Description("Initial state"), NumberValue(0)), 0)

            Then("it should be in the initial state of type Pair<EvenNumberState, Int>") {
                val decider2 = evenDecider.mapOnState { evenNumberState: EvenNumberState -> evenNumberState.value.get }

                assertEquals(result, evenDecider.productOnState(decider2).initialState)
            }
        }

    }
})

