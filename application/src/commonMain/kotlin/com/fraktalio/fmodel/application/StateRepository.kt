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

/**
 * State repository interface.
 *
 * @param C Command
 * @param S State
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
interface StateRepository<C, S> {
    /**
     * Fetch state
     *
     * @receiver Command of type [C]
     * @return the State of type [S] or null
     */
    suspend fun C.fetchState(): S?

    /**
     * Save state
     *
     * @receiver State of type [S]
     * @return newly saved State of type [S]
     */
    suspend fun S.save(): S
}
