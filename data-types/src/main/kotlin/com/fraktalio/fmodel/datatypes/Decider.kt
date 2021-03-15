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
 * [_Decider] is a datatype that represents the main decision making algorithm.
 * It has five generic parameters [C], [Si], [So], [Ei], [Eo] , representing the type of the values that [_Decider] may contain or use.
 * [_Decider] can be specialized for any type [C] or [Si] or [So] or [Ei] or [Eo] because these types does not affect its behavior. [_Decider] behaves the same for [C]=[Int] or [C]=YourCustomType, for example.
 * To indicate that [_Decider] is a type constructor for all values of [C], [Si], [So], [Ei], [Eo], it implements [_DeciderOf]<[C], [Si], [So], [Ei], [Eo]>, which is a typealias of [Kind5]<[For_Decider], [C], [Si], [So], [Ei], [Eo]>
 *
 * [_Decider] is a pure domain component.
 *
 * @param C Command type
 * @param Si Input_State type
 * @param So Output_State type
 * @param Ei Input_Event type
 * @param Eo Output_Event type
 * @property decide A pure function/lambda that takes command of type [C] and input state of type [Si] as parameters, and returns the iterable of output events [Iterable]<[Eo]> as a result
 * @property evolve A pure function/lambda that takes input state of type [Si] and input event of type [Ei] as parameters, and returns the output/new state [So]
 * @property initialState A starting point / An initial state of type [So]
 * @property isTerminal A pure function/lambda that takes input state of type [Si], and returns [Boolean] showing if the current input state is terminal/final
 * @constructor Creates [_Decider]
 */
@higherkind
data class _Decider<C, Si, So, Ei, Eo>(
    val decide: (C, Si) -> Iterable<Eo>,
    val evolve: (Si, Ei) -> So,
    val initialState: So,
    val isTerminal: (Si) -> Boolean
) : _DeciderOf<C, Si, So, Ei, Eo> {
    companion object
}

/**
 * A typealias for [_Decider]<C, Si, So, Ei, Eo>, specializing the [_Decider] to three generic parameters: C, S and E, where C=C, Si=S, So=S, Ei=E, Eo=E
 */
typealias Decider<C, S, E> = _Decider<C, S, S, E, E>
