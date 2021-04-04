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

package com.fraktalio.fmodel.examples.numbers

import arrow.core.Either
import com.fraktalio.fmodel.application.Error
import com.fraktalio.fmodel.application.Success
import com.fraktalio.fmodel.application.ViewStateRepository
import com.fraktalio.fmodel.examples.numbers.api.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * A very simple state store ;)  It is initially empty.
 */
var numberStateStorage1: EvenNumberState? = EvenNumberState(Description("0"), NumberValue(0))
var numberStateStorage2: OddNumberState? = OddNumberState(Description("0"), NumberValue(0))
private val numberStateStorageMutex = Mutex()

/**
 * Number (state) repository implementation
 *
 * @constructor Creates number repository
 */
class NumberViewRepository :
    ViewStateRepository<Either<NumberEvent.EvenNumberEvent?, NumberEvent.OddNumberEvent?>, Pair<EvenNumberState?, OddNumberState?>> {

    override suspend fun Either<NumberEvent.EvenNumberEvent?, NumberEvent.OddNumberEvent?>.fetchState(): Either<Error.FetchingStateFailed, Pair<EvenNumberState?, OddNumberState?>?> =

        Either.catch {
            when (this) {
                is Either.Left -> Pair(numberStateStorage1, null)
                is Either.Right -> Pair(null, numberStateStorage2)
            }
        }.mapLeft { throwable ->
            Error.FetchingStateFailed(throwable)
        }


    override suspend fun Pair<EvenNumberState?, OddNumberState?>.save(): Either<Error.StoringStateFailed<Pair<EvenNumberState?, OddNumberState?>>, Success.StateStoredSuccessfully<Pair<EvenNumberState?, OddNumberState?>>> =

        Either.catch {
            numberStateStorageMutex.withLock {
                when {
                    this.first != null -> numberStateStorage1 = this.first
                    this.second != null -> numberStateStorage2 = this.second
                }

            }
            Success.StateStoredSuccessfully(this)
        }.mapLeft { throwable ->
            Error.StoringStateFailed(this, throwable)
        }
}

/**
 * Number state repository
 *
 * @return state repository instance for all numbers
 */
fun numberViewRepository(): ViewStateRepository<Either<NumberEvent.EvenNumberEvent?, NumberEvent.OddNumberEvent?>, Pair<EvenNumberState?, OddNumberState?>> =
    NumberViewRepository()

