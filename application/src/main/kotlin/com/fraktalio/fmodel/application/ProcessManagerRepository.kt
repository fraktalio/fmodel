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

/**
 * Process Manager repository interface.
 *
 * Used by Process Manager
 *
 * @param AR Action Result
 * @param S State
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
interface ProcessManagerRepository<AR, S> {
    suspend fun AR.fetchState(): Either<Error.FetchingStateFailed, S?>
    suspend fun S.save(): Either<Error.StoringStateFailed<S>, Success.StateStoredSuccessfully<S>>
    suspend fun List<S>.save(): Either<Error.StoringStateFailed<S>, Iterable<Success.StateStoredSuccessfully<S>>> =
        either { map { it.save().bind() } }
}
