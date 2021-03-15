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

package com.fraktalio.fmodel.extensions.decider.aggregate

import arrow.Kind3

interface Invariant<F> {
    fun <C, Cn, S, E> Kind3<F, C, S, E>.imapOnC(
        f: (C) -> Cn,
        g: (Cn) -> C
    ): Kind3<F, Cn, S, E>

}
