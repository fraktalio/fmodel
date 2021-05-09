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

package com.fraktalio.fmodel.domain.extension

import arrow.core.Either
import com.fraktalio.fmodel.domain._View


/**
 * Combines [_View]s into one bigger [_View]
 *
 * @param Si State input of the first View
 * @param So State output of the first View
 * @param E Event of the first View
 * @param Si2 State input of the second View
 * @param So2 State output of the second View
 * @param E2 Event of the second View
 * @param y second View
 * @return new View of type [_View]< [Pair]<[Si], [Si2]>, [Pair]<[So], [So2]>, [Either]<[E], [E2]> >
 */
fun <Si, So, E, Si2, So2, E2> _View<Si, So, E?>.combineViews(y: _View<Si2, So2, E2?>): _View<Pair<Si, Si2>, Pair<So, So2>, Either<E, E2>> {
    val extractE1: (Either<E, E2>) -> E? = { either -> either.fold({ it }, { null }) }
    val extractE2: (Either<E, E2>) -> E2? = { either -> either.fold({ null }, { it }) }
    val extractS1: (Pair<Si, Si2>) -> Si = { pair -> pair.first }
    val extractS2: (Pair<Si, Si2>) -> Si2 = { pair -> pair.second }

    val viewX = this
        .mapLeftOnEvent(extractE1)
        .mapLeftOnState(extractS1)

    val viewY = y
        .mapLeftOnEvent(extractE2)
        .mapLeftOnState(extractS2)

    val viewZ = viewX.productOnState(viewY)

    return _View(
        evolve = { si, e -> viewZ.evolve(si, e) },
        initialState = viewZ.initialState
    )
}

