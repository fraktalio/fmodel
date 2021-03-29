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

package com.fraktalio.fmodel.extensions.saga

import arrow.Kind2
import arrow.extension
import com.fraktalio.fmodel.datatypes.For_Saga
import com.fraktalio.fmodel.datatypes._Saga
import com.fraktalio.fmodel.datatypes.fix


@extension
interface SagaContravariantExtension : Contravariant<For_Saga> {

    override fun <AR, ARn, A> Kind2<For_Saga, AR, A>.lmapOnAR(f: (ARn) -> AR): Kind2<For_Saga, ARn, A> {
        val saga = this.fix()
        return _Saga(
            react = { ar -> saga.react(f(ar)) }
        )
    }

    companion object

}

@extension
interface SagaCovariantExtension : Covariant<For_Saga>{

    override fun <AR, A, An> Kind2<For_Saga, AR, A>.rmapOnA(f: (A) -> An): Kind2<For_Saga, AR, An> {
        val saga = this.fix()
        return _Saga(
            react = { ar -> saga.react(ar).map(f) }
        )
    }


    companion object

}


