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

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapConcat

/**
 * Extension function - Handles the action result of type [AR].
 *
 * @param actionResult Action Result represent the outcome of some action you want to handle in some way
 * @return [Flow] of Actions of type [A]
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
fun <AR, A> SagaManager<AR, A>.handle(actionResult: AR): Flow<A> = actionResult.computeNewActions().publish()

/**
 * Extension function - Handles the the [Flow] of action results of type [AR].
 *
 * @param actionResults Action Results represent the outcome of some action you want to handle in some way
 * @return [Flow] of Actions of type [A]
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
fun <AR, A> SagaManager<AR, A>.handle(actionResults: Flow<AR>): Flow<A> = actionResults.flatMapConcat { handle(it) }

/**
 * Extension function - Publishes the action result of type [AR] to the saga manager of type  [SagaManager]<[AR], [A]>
 * @receiver action result of type [AR]
 * @param sagaManager of type [SagaManager]<[AR], [A]>
 * @return the [Flow] of published Actions of type [A]
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
fun <AR, A> AR.publishTo(sagaManager: SagaManager<AR, A>): Flow<A> = sagaManager.handle(this)

/**
 * Extension function - Publishes the action result of type [AR] to the saga manager of type  [SagaManager]<[AR], [A]>
 * @receiver [Flow] of action results of type [AR]
 * @param sagaManager of type [SagaManager]<[AR], [A]>
 * @return the [Flow] of published Actions of type [A]
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
fun <AR, A> Flow<AR>.publishTo(sagaManager: SagaManager<AR, A>): Flow<A> = sagaManager.handle(this)
