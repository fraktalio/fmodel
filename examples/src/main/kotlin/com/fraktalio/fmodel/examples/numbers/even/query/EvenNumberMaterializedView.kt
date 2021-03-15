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

package com.fraktalio.fmodel.examples.numbers.even.query

import arrow.core.Either.Companion.catch
import com.fraktalio.fmodel.datatypes.Error
import com.fraktalio.fmodel.datatypes.MaterializedView
import com.fraktalio.fmodel.datatypes.Success
import com.fraktalio.fmodel.examples.numbers.api.EvenNumberState
import com.fraktalio.fmodel.examples.numbers.api.NumberEvent
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock


/**
 * Represents the state store (it can be SQL table, NoSQL storage, ...)
 */
var evenNumberStateStorage: EvenNumberState? = null
val evenNumberStateStorageMutex = Mutex()

/**
 * State stored view of the Even Numbers
 */
val EVEN_NUMBER_MATERIALIZED_VIEW: MaterializedView<EvenNumberState?, NumberEvent.EvenNumberEvent?> = MaterializedView(
    view = EVEN_NUMBER_VIEW,
    fetchState = {
        catch {
            evenNumberStateStorage
        }.mapLeft { throwable ->
            Error.FetchingStateFailed(throwable)
        }
    },
    storeState = {
        catch {
            evenNumberStateStorageMutex.withLock {
                evenNumberStateStorage = it
            }
            println("""= $evenNumberStateStorage""")
            Success.StateStoredSuccessfully(it)
        }.mapLeft { throwable ->
            Error.StoringStateFailed(it, throwable)
        }
    }
)
