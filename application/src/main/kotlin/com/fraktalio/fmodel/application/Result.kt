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
 * A general result of any operation
 *
 * @constructor Creates [Result]
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
sealed class Result

/**
 * The result of type [Error]
 *
 * @constructor Creates [Error]
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
sealed class Error : Result() {
    abstract val throwable: Throwable?

    data class CommandHandlingFailed<C>(val command: C, override val throwable: Throwable? = null) : Error()
    data class ActionResultHandlingFailed<AR>(val actionResult: AR, override val throwable: Throwable? = null) : Error()
    data class FetchingStateFailed<C>(val command: C, override val throwable: Throwable? = null) : Error()
    data class FetchingViewStateFailed<E>(val event: E, override val throwable: Throwable? = null) : Error()
    data class CalculatingNewStateFailed<S, C>(
        val state: S,
        val command: C,
        override val throwable: Throwable? = null
    ) : Error()

    data class CalculatingNewViewStateFailed<S, E>(
        val state: S,
        val event: E,
        override val throwable: Throwable? = null
    ) : Error()

    data class StoringStateFailed<S>(val state: S, override val throwable: Throwable? = null) : Error()
}


