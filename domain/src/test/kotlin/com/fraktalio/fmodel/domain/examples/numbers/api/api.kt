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

package com.fraktalio.fmodel.domain.examples.numbers.api


inline class Description(val get: String)
inline class NumberValue(val get: Int)


// ############ Commands ###############
sealed class NumberCommand {
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

sealed class NumberEvent {
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
sealed class NumberState
data class EvenNumberState(
    val description: Description,
    val value: NumberValue
) : NumberState()

data class OddNumberState(
    val description: Description,
    val value: NumberValue
) : NumberState()

