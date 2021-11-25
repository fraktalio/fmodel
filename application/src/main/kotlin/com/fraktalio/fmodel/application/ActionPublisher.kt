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
import com.fraktalio.fmodel.domain.Saga
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Action publisher interface
 *
 * Used by [SagaManager]
 *
 * @param A Action
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
interface ActionPublisher<A> {
    /**
     * Publish action
     *
     * @receiver Action of type [A]
     * @return newly published Action of type [A]
     */
    suspend fun A.publish(): A

    /**
     * Publish actions
     *
     * @receiver [Flow] of Actions of type [A]
     * @return [Flow] of newly published Actions of type [A]
     */
    fun Flow<A>.publish(): Flow<A> =
        map { it.publish() }
}

/**
 * Handle the Action Result of type [AR] and publish [Flow] of Actions of type [A] based on it.
 * Usually, Action Results are Events, and Actions are Commands.
 * But, it does not have to be like that. For example, an Action could be an HTTP Request to a third-party system.
 *
 * Dependency injection
 * ____________________
 * Internally, the [sagaManager] is used to create the object/instance of [SagaManager]<[AR], [A]>.
 * The Delegation pattern has proven to be a good alternative to implementation inheritance, and Kotlin supports it natively requiring zero boilerplate code.
 * Kotlin natively supports dependency injection with receivers.
 * -------------------
 *
 * @param P The type of the publisher - [P] : [ActionPublisher]<[A]>
 * @param AR Action Result
 * @param A Action
 * @param actionResult Action Result represent the outcome of some action you want to handle in some way
 * @param saga Saga component
 * @return The flow of published actions - [Flow]<[A]>
 */
fun <P, AR, A> P.handle(
    actionResult: AR,
    saga: Saga<AR, A>
): Flow<A> where P : ActionPublisher<A> =
    actionResult.publishTo(sagaManager(saga, this))

/**
 * Handle the [Flow] of Action Results of type [AR] and publish [Flow] of Actions of type [A] based on it.
 * Usually, Action Results are Events, and Actions are Commands.
 * But, it does not have to be like that. For example, an Action could be an HTTP Request to a third-party system.
 *
 * Dependency injection
 * ____________________
 * Internally, the [sagaManager] is used to create the object/instance of [SagaManager]<[AR], [A]>.
 * The Delegation pattern has proven to be a good alternative to implementation inheritance, and Kotlin supports it natively requiring zero boilerplate code.
 * Kotlin natively supports dependency injection with receivers.
 * -------------------
 *
 * @param P The type of the publisher - [P] : [ActionPublisher]<[A]>
 * @param AR Action Result
 * @param A Action
 * @param actionResults The flow of Action Results represent the outcome of some action you want to handle in some way
 * @param saga Saga component
 * @return The flow of published actions - [Flow]<[A]>
 */
fun <P, AR, A> P.handle(
    actionResults: Flow<AR>,
    saga: Saga<AR, A>
): Flow<A> where P : ActionPublisher<A> =
    actionResults.publishTo(sagaManager(saga, this))

/**
 * Handle the Action Result of type [AR] and publish [Flow] of [Either] Actions of type [A] or [Error].
 * Usually, Action Results are Events, and Actions are Commands.
 * But, it does not have to be like that. For example, an Action could be an HTTP Request to a third-party system.
 *
 * Dependency injection
 * ____________________
 * Internally, the [sagaManager] is used to create the object/instance of [SagaManager]<[AR], [A]>.
 * The Delegation pattern has proven to be a good alternative to implementation inheritance, and Kotlin supports it natively requiring zero boilerplate code.
 * Kotlin natively supports dependency injection with receivers.
 * -------------------
 *
 * @param P The type of the publisher - [P] : [ActionPublisher]<[A]>
 * @param AR Action Result
 * @param A Action
 * @param actionResult Action Result represent the outcome of some action you want to handle in some way
 * @param saga Saga component
 * @return The flow of published actions - [Flow]<[Either]<[Error], [A]>>
 */
fun <P, AR, A> P.eitherHandleOrFail(
    actionResult: AR,
    saga: Saga<AR, A>
): Flow<Either<Error, A>> where P : ActionPublisher<A> =
    actionResult.publishEitherTo(sagaManager(saga, this))

/**
 * Handle the Action Result of type [AR] and publish [Flow] of [Either] Actions of type [A] or [Error].
 * Usually, Action Results are Events, and Actions are Commands.
 * But, it does not have to be like that. For example, an Action could be an HTTP Request to a third-party system.
 *
 * Dependency injection
 * ____________________
 * Internally, the [sagaManager] is used to create the object/instance of [SagaManager]<[AR], [A]>.
 * The Delegation pattern has proven to be a good alternative to implementation inheritance, and Kotlin supports it natively requiring zero boilerplate code.
 * Kotlin natively supports dependency injection with receivers.
 * -------------------
 *
 * @param P The type of the publisher - [P] : [ActionPublisher]<[A]>
 * @param AR Action Result
 * @param A Action
 * @param actionResult Action Result represent the outcome of some action you want to handle in some way
 * @param saga Saga component
 * @return The flow of published actions - [Flow]<[Either]<[Error], [A]>>
 */
fun <P, AR, A> P.eitherHandleOrFail(
    actionResult: Flow<AR>,
    saga: Saga<AR, A>
): Flow<Either<Error, A>> where P : ActionPublisher<A> =
    actionResult.publishEitherTo(sagaManager(saga, this))
