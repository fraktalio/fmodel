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

interface Applicative<F> : Apply<F> {

    // ############ (S)tate ############
    fun <C, Si, So, Ei, Eo> rjustOnS(so: So): Kind5<F, C, Si, So, Ei, Eo>

    fun <C, Si, So, Ei, Eo> So.rjustOnS(dummy: Unit = Unit): Kind5<F, C, Si, So, Ei, Eo> =
        rjustOnS(this)

    fun <C, Si, Ei, Eo> unit(): Kind5<F, C, Si, Unit, Ei, Eo> =
        rjustOnS(Unit)

}
