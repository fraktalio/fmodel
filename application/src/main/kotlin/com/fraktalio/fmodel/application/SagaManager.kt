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
import arrow.core.nonFatalOrThrow
import com.fraktalio.fmodel.domain.Saga
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flow

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
     * @return [Flow] of [Either] [Error] or [A]/Action
     */
    suspend fun handle(actionResult: AR): Flow<Either<Error, A>> =
        actionResult.calculateNewActions()
            .publishEither()
            .catch<Either<Error, A>> { emit(Either.Left(Error.ActionResultHandlingFailed(it))) }

    /**
     * Handles the the [Flow] of action results of type [AR]
     *
     * @param actionResults Action Results represent the outcome of some action you want to handle in some way
     * @return [Flow] of [Either] [Error] or [A]/Action
     */
    fun handle(actionResults: Flow<AR>): Flow<Either<Error, A>> =
        actionResults.flatMapConcat { handle(it) }

    private fun AR.calculateNewActions(): Flow<A> =
        try {
            saga.react(this)
        } catch (e: Throwable) {
            val nonFatalException = e.nonFatalOrThrow()
            flow { throw nonFatalException }
        }


}

