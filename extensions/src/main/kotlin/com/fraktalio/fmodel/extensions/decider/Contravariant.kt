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

interface Contravariant<F> : Invariant<F> {

    // ############ (C)ommands ############
    fun <C, Cn, Si, So, Ei, Eo> Kind5<F, C, Si, So, Ei, Eo>.lmapOnC(f: (Cn) -> C): Kind5<F, Cn, Si, So, Ei, Eo>

    override fun <C, Cn, Si, So, Ei, Eo> Kind5<F, C, Si, So, Ei, Eo>.imapOnC(
        f: (C) -> Cn,
        g: (Cn) -> C
    ): Kind5<F, Cn, Si, So, Ei, Eo> =
        lmapOnC(g)

}
