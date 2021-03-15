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
import arrow.core.Either.Companion.catch
import arrow.core.Tuple2
import com.fraktalio.fmodel.datatypes.Error
import com.fraktalio.fmodel.datatypes.MaterializedView
import com.fraktalio.fmodel.datatypes.Success
import com.fraktalio.fmodel.examples.numbers.api.*
import com.fraktalio.fmodel.examples.numbers.even.query.EVEN_NUMBER_VIEW
import com.fraktalio.fmodel.examples.numbers.odd.query.ODD_NUMBER_VIEW
import com.fraktalio.fmodel.extensions.view._view.semigroup.combineViews
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock


/**
 * Represents the state store (it can be SQL table(s), NoSQL storage, ...)
 */
var numberStateStorage1: EvenNumberState? = EvenNumberState(Description("0"), NumberValue(0))
var numberStateStorage2: OddNumberState? = OddNumberState(Description("0"), NumberValue(0))
val numberStateStorageMutex = Mutex()

/**
 * State stored view of all the Numbers
 */
val NUMBER_MATERIALIZED_VIEW: MaterializedView<Tuple2<EvenNumberState?, OddNumberState?>, Either<NumberEvent.EvenNumberEvent?, NumberEvent.OddNumberEvent?>> =
    MaterializedView(
        view = EVEN_NUMBER_VIEW.combineViews(ODD_NUMBER_VIEW),
        fetchState = {
            catch {
                when (it) {
                    is Either.Left -> Tuple2(numberStateStorage1, null)
                    is Either.Right -> Tuple2(null, numberStateStorage2)
                }
            }.mapLeft { throwable ->
                Error.FetchingStateFailed(throwable)
            }
        },
        storeState = {
            catch {
                numberStateStorageMutex.withLock {
                    when {
                        it.a != null -> numberStateStorage1 = it.a
                        it.b != null -> numberStateStorage2 = it.b
                    }

                }
                println("""= $numberStateStorage1""")
                println("""= $numberStateStorage2""")
                Success.StateStoredSuccessfully(it)
            }.mapLeft { throwable ->
                Error.StoringStateFailed(it, throwable)
            }
        }
    )
