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

package com.fraktalio.fmodel.extensions.decider

import arrow.Kind5
import arrow.core.Either
import arrow.core.Tuple2

interface Semigroup<F> {
    fun <C1, Si1, So1, Ei1, Eo1, C2, Si2, So2, Ei2, Eo2> Kind5<F, C1, Si1, So1, Ei1, Eo1>.combineDeciders(y: Kind5<F, C2, Si2, So2, Ei2, Eo2>): Kind5<F, Either<C1, C2>, Tuple2<Si1, Si2>, Tuple2<So1, So2>, Either<Ei1, Ei2>, Either<Eo1, Eo2>>
}
