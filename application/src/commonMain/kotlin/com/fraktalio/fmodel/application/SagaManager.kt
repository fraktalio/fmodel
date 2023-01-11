/*
 * Copyright (c) 2023 Fraktalio D.O.O. All rights reserved.
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

import com.fraktalio.fmodel.domain.ISaga
import kotlinx.coroutines.flow.Flow

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
     * Computes new Actions based on the Action Results.
     *
     * @return The newly computed [Flow] of Actions/[A]
     */
    fun AR.computeNewActions(): Flow<A> = react(this)

}

/**
 * Saga Manager factory function.
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
): SagaManager<AR, A> = object : SagaManager<AR, A>,
    ActionPublisher<A> by actionPublisher,
    ISaga<AR, A> by saga {}
