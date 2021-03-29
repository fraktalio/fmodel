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
 * [_View] is a datatype that represents the event handling algorithm,
 * responsible for translating the events into denormalized state,
 * which is more adequate for querying.
 *
 * It has three generic parameters [Si], [So], [E], representing the type of the values that [_View] may contain or use.
 * [_View] can be specialized for any type of [Si], [So], [E] because these types does not affect its behavior.
 * [_View] behaves the same for [E]=[Int] or [E]=YourCustomType, for example.
 *
 * [_View] is a pure domain component
 *
 * @param Si Input_State type
 * @param So Output_State type
 * @param E Event type
 * @property evolve A pure function/lambda that takes input state of type [Si] and input event of type [E] as parameters, and returns the output/new state [So]
 * @property initialState A starting point / An initial state of type [So]
 * @constructor Creates [_View]
 *
 * @author Иван Дугалић / Ivan Dugalic / @idugalic
 */
@higherkind
data class _View<Si, So, E>(
    val evolve: (Si, E) -> So,
    val initialState: So,
) : _ViewOf<Si, So, E> {
    companion object
}

typealias View<S, E> = _View<S, S, E>
