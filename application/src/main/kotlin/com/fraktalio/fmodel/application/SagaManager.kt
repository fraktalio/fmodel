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
import arrow.core.computations.either
import com.fraktalio.fmodel.domain.Saga

/**
 * Saga manager - Stateless process orchestrator
 * It is reacting on Action Results of type [AR] and produces new actions [A] based on them
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
data class SagaManager<AR, A>(
    private val saga: Saga<AR, A>,
    private val actionPublisher: ActionPublisher<A>,
) : ActionPublisher<A> by actionPublisher {

    /**
     * Handles the action result of type [AR]
     *
     * @param actionResult Action Result represent the outcome of some action you want to handle in some way
     * @return Either [Error] or [Success]
     */
    suspend fun handle(actionResult: AR): Either<Error, Iterable<Success.ActionPublishedSuccessfully<A>>> =
        // Arrow provides a Monad instance for Either. Except for the types signatures, our program remains unchanged when we compute over Either. All values on the left side assume to be Right biased and, whenever a Left value is found, the computation short-circuits, producing a result that is compatible with the function type signature.
        either {
            saga.react(actionResult).publish().bind()
        }
}

