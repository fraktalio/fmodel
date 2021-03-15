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
import arrow.core.identity

interface Profunctor<F> {

    // ############ (S)tate ############
    fun <Si, So, E, Sin, Son> Kind3<F, Si, So, E>.dimapOnS(
        fl: (Sin) -> Si,
        fr: (So) -> Son
    ): Kind3<F, Sin, Son, E>

    fun <Si, So, E, Sin> Kind3<F, Si, So, E>.lmapOnS(f: (Sin) -> Si): Kind3<F, Sin, So, E> =
        dimapOnS(f, ::identity)

    fun <Si, So, E, Son> Kind3<F, Si, So, E>.rmapOnS(f: (So) -> Son): Kind3<F, Si, Son, E> =
        dimapOnS(::identity, f)
}
