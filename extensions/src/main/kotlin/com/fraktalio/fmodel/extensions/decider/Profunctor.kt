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
import arrow.core.identity

interface Profunctor<F> {

    // ############ (E)vents ############
    fun <C, Si, So, Ei, Eo, Ein, Eon> Kind5<F, C, Si, So, Ei, Eo>.dimapOnE(
        fl: (Ein) -> Ei,
        fr: (Eo) -> Eon
    ): Kind5<F, C, Si, So, Ein, Eon>

    fun <C, Si, So, Ei, Eo, Ein> Kind5<F, C, Si, So, Ei, Eo>.lmapOnE(f: (Ein) -> Ei): Kind5<F, C, Si, So, Ein, Eo> =
        dimapOnE(f, ::identity)

    fun <C, Si, So, Ei, Eo, Eon> Kind5<F, C, Si, So, Ei, Eo>.rmapOnE(f: (Eo) -> Eon): Kind5<F, C, Si, So, Ei, Eon> =
        dimapOnE(::identity, f)

    // ############ (S)tate ############
    fun <C, Si, So, Ei, Eo, Sin, Son> Kind5<F, C, Si, So, Ei, Eo>.dimapOnS(
        fl: (Sin) -> Si,
        fr: (So) -> Son
    ): Kind5<F, C, Sin, Son, Ei, Eo>

    fun <C, Si, So, Ei, Eo, Sin> Kind5<F, C, Si, So, Ei, Eo>.lmapOnS(f: (Sin) -> Si): Kind5<F, C, Sin, So, Ei, Eo> =
        dimapOnS(f, ::identity)

    fun <C, Si, So, Ei, Eo, Son> Kind5<F, C, Si, So, Ei, Eo>.rmapOnS(f: (So) -> Son): Kind5<F, C, Si, Son, Ei, Eo> =
        dimapOnS(::identity, f)

}
