/*
 * Copyright (c) 2022 Fraktalio D.O.O. All rights reserved.
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


package com.fraktalio.fmodel.domain.examples.numbers.api

import com.fraktalio.fmodel.domain.examples.numbers.api.NumberCommand.EvenNumberCommand.AddEvenNumber
import com.fraktalio.fmodel.domain.examples.numbers.api.NumberCommand.EvenNumberCommand.SubtractEvenNumber
import com.fraktalio.fmodel.domain.examples.numbers.api.NumberEvent.EvenNumberEvent.EvenNumberAdded
import com.fraktalio.fmodel.domain.examples.numbers.api.NumberEvent.EvenNumberEvent.EvenNumberSubtracted
import kotlin.jvm.JvmInline

@JvmInline
value class Description(val get: String) {
    operator fun plus(param: Description) = Description("${this.get} + ${param.get}")
    operator fun minus(param: Description) = Description("${this.get} - ${param.get}")
}

@JvmInline
value class NumberValue(val get: Int) {
    operator fun plus(param: NumberValue) = NumberValue(this.get + param.get)
    operator fun minus(param: NumberValue) = NumberValue(this.get - param.get)
}


// ############ Commands ###############
abstract class NumberCommand {
    abstract val description: Description
    abstract val value: NumberValue

    sealed class EvenNumberCommand : NumberCommand() {
        data class AddEvenNumber(
            override val description: Description,
            override val value: NumberValue
        ) : EvenNumberCommand()

        data class SubtractEvenNumber(
            override val description: Description,
            override val value: NumberValue
        ) : EvenNumberCommand()
    }


    sealed class OddNumberCommand : NumberCommand() {
        data class AddOddNumber(
            override val description: Description,
            override val value: NumberValue
        ) : OddNumberCommand()

        data class SubtractOddNumber(
            override val description: Description,
            override val value: NumberValue
        ) : OddNumberCommand()
    }
}


// ############ Events ###############

abstract class NumberEvent {
    abstract val description: Description
    abstract val value: NumberValue

    sealed class EvenNumberEvent : NumberEvent() {
        data class EvenNumberAdded(
            override val description: Description,
            override val value: NumberValue
        ) : EvenNumberEvent()

        data class EvenNumberSubtracted(
            override val description: Description,
            override val value: NumberValue
        ) : EvenNumberEvent()
    }

    sealed class OddNumberEvent : NumberEvent() {
        data class OddNumberAdded(
            override val description: Description,
            override val value: NumberValue
        ) : OddNumberEvent()

        data class OddNumberSubtracted(
            override val description: Description,
            override val value: NumberValue
        ) : OddNumberEvent()
    }
}

// ############ State ###############
sealed class NumberState {
    abstract val description: Description
    abstract val value: NumberValue
}

data class EvenNumberState(
    override val description: Description,
    override val value: NumberValue
) : NumberState()

data class OddNumberState(
    override val description: Description,
    override val value: NumberValue
) : NumberState()


// DSL - if you think DSL provides value in this case...nothing wrong in using constructors

fun desc(block: () -> String): Description = Description(block())
fun num(block: () -> Int): NumberValue = NumberValue(block())

// Commands
class AddEvenNumberBuilder internal constructor() {

    private var description: Description = Description("")
    private var value: NumberValue = NumberValue(0)

    fun description(lambda: () -> Description) {
        description = lambda()
    }

    fun value(lambda: () -> NumberValue) {
        value = lambda()
    }

    fun build(): AddEvenNumber = AddEvenNumber(description, value)
}

fun addEvenNumber(block: AddEvenNumberBuilder.() -> Unit): AddEvenNumber =
    AddEvenNumberBuilder().apply(block).build()

class SubtractEvenNumberBuilder internal constructor() {

    private var description: Description = Description("")
    private var value: NumberValue = NumberValue(0)

    fun description(lambda: () -> Description) {
        description = lambda()
    }

    fun value(lambda: () -> NumberValue) {
        value = lambda()
    }

    fun build(): SubtractEvenNumber = SubtractEvenNumber(description, value)
}

fun subtractEvenNumber(block: SubtractEvenNumberBuilder.() -> Unit): SubtractEvenNumber =
    SubtractEvenNumberBuilder().apply(block).build()

// Events
class EvenNumberAddedBuilder internal constructor() {

    private var description: Description = Description("")
    private var value: NumberValue = NumberValue(0)

    fun description(lambda: () -> Description) {
        description = lambda()
    }

    fun value(lambda: () -> NumberValue) {
        value = lambda()
    }

    fun build(): EvenNumberAdded = EvenNumberAdded(description, value)
}

fun evenNumberAdded(block: EvenNumberAddedBuilder.() -> Unit): EvenNumberAdded =
    EvenNumberAddedBuilder().apply(block).build()

class EvenNumberSubtractedBuilder internal constructor() {

    private var description: Description = Description("")
    private var value: NumberValue = NumberValue(0)

    fun description(lambda: () -> Description) {
        description = lambda()
    }

    fun value(lambda: () -> NumberValue) {
        value = lambda()
    }

    fun build(): EvenNumberSubtracted = EvenNumberSubtracted(description, value)
}

fun evenNumberSubtracted(block: EvenNumberSubtractedBuilder.() -> Unit): EvenNumberSubtracted =
    EvenNumberSubtractedBuilder().apply(block).build()

// State
class EvenNumberStateBuilder internal constructor() {

    private var description: Description = Description("")
    private var value: NumberValue = NumberValue(0)

    fun description(lambda: () -> Description) {
        description = lambda()
    }

    fun descriptionString(lambda: () -> String) {
        description = Description(lambda())
    }

    fun value(lambda: () -> NumberValue) {
        value = lambda()
    }

    fun valueInt(lambda: () -> Int) {
        value = NumberValue(lambda())
    }

    fun build(): EvenNumberState = EvenNumberState(description, value)
}

fun evenNumberState(block: EvenNumberStateBuilder.() -> Unit): EvenNumberState =
    EvenNumberStateBuilder().apply(block).build()

class OddNumberStateBuilder internal constructor() {

    private var description: Description = Description("")
    private var value: NumberValue = NumberValue(0)

    fun description(lambda: () -> Description) {
        description = lambda()
    }

    fun value(lambda: () -> NumberValue) {
        value = lambda()
    }

    fun build(): OddNumberState = OddNumberState(description, value)
}

fun oddNumberState(block: OddNumberStateBuilder.() -> Unit): OddNumberState =
    OddNumberStateBuilder().apply(block).build()