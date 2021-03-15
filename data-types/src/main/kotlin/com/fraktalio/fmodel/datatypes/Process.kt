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

package com.fraktalio.fmodel.datatypes

import arrow.higherkind

/**
 * [_Process] is a datatype that represents the central point of control deciding what to execute next ([A]).
 * It is responsible for mapping different events from aggregates into action results ([AR]) that the _[Process] then can use to calculate the next actions ([A]) to be mapped to commands of other aggregates.
 *
 * @param AR Action Result type
 * @param Si Input_State type
 * @param So Output_State type
 * @param Ei Input_Event type
 * @param Eo Output_Event type
 * @param A Action type
 * @property ingest A pure function/lambda that takes action results of type [AR] and input state of type [Si] as parameters, and returns the list of internal events [Iterable]<[Eo]>.
 * @property evolve A pure function/lambda that takes input state of type [Si] and input event of type [Ei] as parameters, and returns the output/new state [So].
 * @property react A pure function/lambda that takes input state of type [Si] and input event of type [Ei] as parameters, and returns the list of actions [Iterable]<[A]>.
 * @property pending A pure function/lambda that takes input state of type [Si], and returns the list of actions [Iterable]<[A]>.
 * @property initialState A starting point / An initial state of type [So].
 * @property isTerminal A pure function/lambda that takes input state of type [Si], and returns [Boolean] showing if the current input state is terminal/final.
 * @constructor Creates [_Process]
 */
@higherkind
data class _Process<AR, Si, So, Ei, Eo, A>(
    val ingest: (AR, Si) -> Iterable<Eo>,
    val evolve: (Si, Ei) -> So,
    val react: (Si, Ei) -> Iterable<A>,
    val pending: (Si) -> Iterable<A>,
    val initialState: So,
    val isTerminal: (Si) -> Boolean
) : _ProcessOf<AR, Si, So, Ei, Eo, A> {

    companion object
}

typealias Process<AR, S, E, A> = _Process<AR, S, S, E, E, A>
