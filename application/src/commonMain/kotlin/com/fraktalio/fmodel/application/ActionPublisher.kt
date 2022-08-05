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

package com.fraktalio.fmodel.application

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
    fun Flow<A>.publish(): Flow<A> = map { it.publish() }
}
