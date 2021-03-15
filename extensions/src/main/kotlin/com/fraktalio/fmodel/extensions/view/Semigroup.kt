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

package com.fraktalio.fmodel.extensions.view

import arrow.Kind3
import arrow.core.Either
import arrow.core.Tuple2

interface Semigroup<F> {
    fun <Si1, So1, E1, Si2, So2, E2> Kind3<F, Si1, So1, E1>.combineViews(y: Kind3<F, Si2, So2, E2>): Kind3<F, Tuple2<Si1, Si2>, Tuple2<So1, So2>, Either<E1, E2>>
}
