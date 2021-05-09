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
import com.fraktalio.fmodel.domain._Decider


/**
 * Combine [_Decider]s into one big [_Decider]
 *
 * @param C Command type of the first Decider
 * @param Si Input_State type of the first Decider
 * @param So Output_State type of the first Decider
 * @param Ei Input_Event type of the first Decider
 * @param Eo Output_Event type of the first Decider
 * @param Cn Command type of the second Decider
 * @param Sin Input_State type of the second Decider
 * @param Son Output_State type of the second Decider
 * @param Ein Input_Event type of the second Decider
 * @param Eon Output_Event type of the second Decider
 * @param y second Decider
 * @return [_Decider]< [Either]<[C], [Cn]>, [Pair]<[Si], [Sin]>, [Pair]<[So], [Son]>, [Either]<[Ei], [Ein]>, [Either]<[Eo], [Eon]> >
 */
fun <C, Si, So, Ei, Eo, Cn, Sin, Son, Ein, Eon> _Decider<C?, Si, So, Ei?, Eo>.combineDeciders(
    y: _Decider<Cn?, Sin, Son, Ein?, Eon>
): _Decider<Either<C, Cn>, Pair<Si, Sin>, Pair<So, Son>, Either<Ei, Ein>, Either<Eo, Eon>> {
    val getC1: (Either<C, Cn>) -> C? = { either -> either.fold({ it }, { null }) }
    val getC2: (Either<C, Cn>) -> Cn? = { either -> either.fold({ null }, { it }) }
    val getE1: (Either<Ei, Ein>) -> Ei? = { either -> either.fold({ it }, { null }) }
    val getE1Either: (Eo) -> Either<Eo, Eon> = { eo1 -> Either.Left(eo1) }
    val getE2: (Either<Ei, Ein>) -> Ein? = { either -> either.fold({ null }, { it }) }
    val getE2Either: (Eon) -> Either<Eo, Eon> = { eo2 -> Either.Right(eo2) }
    val getS1: (Pair<Si, Sin>) -> Si = { pair -> pair.first }
    val getS2: (Pair<Si, Sin>) -> Sin = { pair -> pair.second }

    val deciderX = this
        .mapLeftOnCommand(getC1)
        .mapLeftOnState(getS1)
        .dimapOnEvent(getE1, getE1Either)

    val deciderY = y
        .mapLeftOnCommand(getC2)
        .mapLeftOnState(getS2)
        .dimapOnEvent(getE2, getE2Either)

    val deciderZ = deciderX.productOnState(deciderY)

    return _Decider(
        decide = { c, si -> deciderZ.decide(c, si) },
        evolve = { pair, ei -> deciderZ.evolve(pair, ei) },
        initialState = deciderZ.initialState,
        isTerminal = { pair -> deciderZ.isTerminal(pair) }
    )
}


