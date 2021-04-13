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
    data class FetchingStateFailed(val throwable: Throwable?) : Error()
    data class FetchingEventsFailed(val throwable: Throwable?) : Error()
    data class StoringEventFailed<E>(val event: E, val throwable: Throwable?) : Error()
    data class StoringStateFailed<S>(val state: S, val throwable: Throwable?) : Error()
    data class AggregateIsInTerminalState<S>(val state: S) : Error()
    data class ProcessManagerIsInTerminalState<S>(val state: S) : Error()
    data class PublishingActionFailed<A>(val action: A, val throwable: Throwable?) : Error()


}

/**
 * The result of type [Success]
 *
 * @constructor Creates [Success]
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
sealed class Success : Result() {
    data class EventStoredSuccessfully<E>(val event: E) : Success()
    data class StateStoredSuccessfully<S>(val state: S) : Success()
    data class StateStoredAndEventsPublishedSuccessfully<S, E>(val state: S, val event: Iterable<E> = emptyList()) :
        Success()

    data class ActionPublishedSuccessfully<A>(val action: A) : Success()

}
