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

package com.fraktalio.fmodel.datatypes

import arrow.core.Either
import arrow.core.computations.either
import arrow.higherkind

/**
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
@higherkind
data class SagaManager<AR, A>(
    val saga: Saga<AR, A>,
    val publishActions: suspend (Iterable<A>) -> Either<Error.PublishingActionsFailed<A>, Success.ActionsPublishedSuccessfully<A>>
) : SagaManagerOf<AR, A> {

    suspend fun handle(actionResult: AR): Either<Error, Success.ActionsPublishedSuccessfully<A>> =
        // Arrow provides a Monad instance for Either. Except for the types signatures, our program remains unchanged when we compute over Either. All values on the left side assume to be Right biased and, whenever a Left value is found, the computation short-circuits, producing a result that is compatible with the function type signature.
        either {
            publishActions(saga.react(actionResult)).bind()
        }

    companion object
}

