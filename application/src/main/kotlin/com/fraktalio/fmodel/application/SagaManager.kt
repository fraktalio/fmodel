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
import com.fraktalio.fmodel.domain.ISaga
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.map

/**
 * Saga manager - Stateless process orchestrator.
 * It is reacting on Action Results of type [AR] and produces new actions [A] based on them.
 *
 * [SagaManager] extends [ISaga] and [ActionPublisher] interfaces,
 * clearly communicating that it is composed out of these two behaviours.
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
interface SagaManager<AR, A> : ISaga<AR, A>, ActionPublisher<A> {
    /**
     * Handles the action result of type [AR].
     *
     * @param actionResult Action Result represent the outcome of some action you want to handle in some way
     * @return [Flow] of [Either] [Error] or Actions of type [A]
     */
    fun handleEither(actionResult: AR): Flow<Either<Error, A>> =
        actionResult
            .computeNewActions()
            .publish()
            .map { Either.Right(it) }
            .catch<Either<Error, A>> { emit(Either.Left(Error.ActionResultHandlingFailed(actionResult))) }

    /**
     * Handles the action result of type [AR].
     *
     * @param actionResult Action Result represent the outcome of some action you want to handle in some way
     * @return [Flow] of Actions of type [A]
     */
    fun handle(actionResult: AR): Flow<A> =
        actionResult
            .computeNewActions()
            .publish()

    /**
     * Handles the the [Flow] of action results of type [AR].
     *
     * @param actionResults Action Results represent the outcome of some action you want to handle in some way
     * @return [Flow] of [Either] [Error] or Actions of type [A]
     */
    fun handleEither(actionResults: Flow<AR>): Flow<Either<Error, A>> =
        actionResults
            .flatMapConcat { handleEither(it) }
            .catch { emit(Either.Left(Error.ActionResultPublishingFailed(it))) }

    /**
     * Handles the the [Flow] of action results of type [AR].
     *
     * @param actionResults Action Results represent the outcome of some action you want to handle in some way
     * @return [Flow] of Actions of type [A]
     */
    fun handle(actionResults: Flow<AR>): Flow<A> =
        actionResults.flatMapConcat { handle(it) }


    /**
     * Computes new Actions based on the Action Results.
     *
     * @return The newly computed [Flow] of Actions/[A]
     */
    private fun AR.computeNewActions(): Flow<A> = react(this)

}

/**
 * Extension function - Saga Manager factory function.
 *
 * The Delegation pattern has proven to be a good alternative to implementation inheritance, and Kotlin supports it natively requiring zero boilerplate code.
 *
 * @param AR Action Result of type [AR], Action Result is usually an Event
 * @param A An Action of type [A] to be taken. Action is usually a Command.
 * @property saga A saga component of type [ISaga]<[AR], [A]>
 * @property actionPublisher Interface for publishing the Actions of type [A] - dependencies by delegation
 * @return An object/instance of type [SagaManager]<[AR], [A]>
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
fun <AR, A> sagaManager(
    saga: ISaga<AR, A>,
    actionPublisher: ActionPublisher<A>
): SagaManager<AR, A> =
    object : SagaManager<AR, A>, ActionPublisher<A> by actionPublisher, ISaga<AR, A> by saga {}

/**
 * Extension function - Publishes the action result of type [AR] to the saga manager of type  [SagaManager]<[AR], [A]>
 * @receiver action result of type [AR]
 * @param sagaManager of type [SagaManager]<[AR], [A]>
 * @return the [Flow] of published Actions of type [A]
 */
fun <AR, A> AR.publishTo(sagaManager: SagaManager<AR, A>): Flow<A> = sagaManager.handle(this)

/**
 * Extension function - Publishes the action result of type [AR] to the saga manager of type  [SagaManager]<[AR], [A]>
 * @receiver [Flow] of action results of type [AR]
 * @param sagaManager of type [SagaManager]<[AR], [A]>
 * @return the [Flow] of published Actions of type [A]
 */
fun <AR, A> Flow<AR>.publishTo(sagaManager: SagaManager<AR, A>): Flow<A> = sagaManager.handle(this)

/**
 * Extension function - Publishes the action result of type [AR] to the saga manager of type  [SagaManager]<[AR], [A]>
 * @receiver action result of type [AR]
 * @param sagaManager of type [SagaManager]<[AR], [A]>
 * @return the [Flow] of [Either] [Error] or successfully published Actions of type [A]
 */
fun <AR, A> AR.publishEitherTo(sagaManager: SagaManager<AR, A>): Flow<Either<Error, A>> = sagaManager.handleEither(this)

/**
 * Extension function - Publishes the action result of type [AR] to the saga manager of type  [SagaManager]<[AR], [A]>
 * @receiver [Flow] of action results of type [AR]
 * @param sagaManager of type [SagaManager]<[AR], [A]>
 * @return the [Flow] of [Either] [Error] or successfully published Actions of type [A]
 */
fun <AR, A> Flow<AR>.publishEitherTo(sagaManager: SagaManager<AR, A>): Flow<Either<Error, A>> =
    sagaManager.handleEither(this)

