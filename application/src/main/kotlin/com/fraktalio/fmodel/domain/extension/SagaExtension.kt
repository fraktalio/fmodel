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
import com.fraktalio.fmodel.domain._Saga

/**
 * Combine [_Saga]s into one [_Saga] - Semigroup and Monoid with identity element `_Saga<Nothing, Nothing>`
 * This is an associative binary operation which makes it a Semigroup. Additionally, the identity element makes it a Monoid
 *
 * @param AR Action Result (usually event) of the first Saga
 * @param A Action (usually command) of the first Saga
 * @param ARn Action Result (usually event) of the second Saga
 * @param An Action (usually command) of the second Saga
 * @param y second saga
 * @return new Saga of type `[_Saga]<[Either]<[AR], [ARn]>, [Either]<[A], [An]>>`
 */
fun <AR, A, ARn, An> _Saga<AR?, A>.combineSagas(y: _Saga<ARn?, An>): _Saga<Either<AR, ARn>, Either<A, An>> {
    val getAR: (Either<AR, ARn>) -> AR? = { either -> either.fold({ it }, { null }) }
    val getARn: (Either<AR, ARn>) -> ARn? = { either -> either.fold({ null }, { it }) }
    val getAEither: (A) -> Either<A, An> = { a -> Either.Left(a) }
    val getAnEither: (An) -> Either<A, An> = { an -> Either.Right(an) }

    val sagaX = this
        .mapLeftOnActionResult(getAR)
        .mapOnAction(getAEither)

    val sagaY = y
        .mapLeftOnActionResult(getARn)
        .mapOnAction(getAnEither)

    return _Saga(
        react = { eitherAr -> sagaX.react(eitherAr).plus(sagaY.react(eitherAr)) }
    )
}
