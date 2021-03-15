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
import arrow.extension
import com.fraktalio.fmodel.datatypes.For_View
import com.fraktalio.fmodel.datatypes._View
import com.fraktalio.fmodel.datatypes.fix


@extension
interface ViewContravariantExtension : Contravariant<For_View> {
    /**
     * Contravariant on the (E)vent
     */
    override fun <Si, So, E, En> Kind3<For_View, Si, So, E>.lmapOnE(f: (En) -> E): Kind3<For_View, Si, So, En> {
        val view = this.fix()
        return _View(
            evolve = { si, e -> view.evolve(si, f(e)) },
            initialState = view.initialState
        )
    }

    companion object

}

@extension
interface ViewProfunctorExtension : Profunctor<For_View> {
    /**
     * Contravariant on the Si (input State)
     * Covariant on the So (output State)
     */
    override fun <Si, So, E, Sin, Son> Kind3<For_View, Si, So, E>.dimapOnS(
        fl: (Sin) -> Si,
        fr: (So) -> Son
    ): Kind3<For_View, Sin, Son, E> {
        val view = this.fix()
        return _View(
            evolve = { si, e -> fr(view.evolve(fl(si), e)) },
            initialState = fr(view.initialState)
        )
    }

    companion object
}

@extension
interface ViewApplyExtension : Apply<For_View>, ViewProfunctorExtension {

    override fun <Si, So, Son, E> Kind3<For_View, Si, So, E>.rapplyOnS(ff: Kind3<For_View, Si, (So) -> Son, E>): Kind3<For_View, Si, Son, E> {
        val view = this.fix()
        val ffView = ff.fix()
        return _View(
            evolve = { si, e -> ffView.evolve(si, e).invoke(view.evolve(si, e)) },
            initialState = ffView.initialState.invoke(view.initialState)
        )
    }

    fun <Si, So, Son, E> Kind3<For_View, Si, So, E>.rproductOnS(fb: Kind3<For_View, Si, Son, E>): Kind3<For_View, Si, Tuple2<So, Son>, E> =
        rapplyOnS(fb.rmapOnS { b: Son -> { a: So -> Tuple2(a, b) } })

    fun <Si, So, Son, E, Z> mapN(
        a: Kind3<For_View, Si, So, E>,
        b: Kind3<For_View, Si, Son, E>,
        lbd: (Tuple2<So, Son>) -> Z
    ): Kind3<For_View, Si, Z, E> =
        a.rproductOnS(b).rmapOnS(lbd)

    companion object
}

@extension
interface ViewApplicativeExtension : Applicative<For_View>, ViewApplyExtension {
    override fun <Si, So, E> rjustOnS(so: So): Kind3<For_View, Si, So, E> {
        return _View(
            evolve = { _, _ -> so },
            initialState = so
        )
    }

    override fun <Si, So, E, Son> Kind3<For_View, Si, So, E>.rmapOnS(f: (So) -> Son): Kind3<For_View, Si, Son, E> =
        rapplyOnS(rjustOnS(f))

    companion object

}

@extension
interface ViewSemigroupExtension : Semigroup<For_View>, ViewApplicativeExtension, ViewContravariantExtension {

    override fun <Si1, So1, E1, Si2, So2, E2> Kind3<For_View, Si1, So1, E1>.combineViews(y: Kind3<For_View, Si2, So2, E2>): Kind3<For_View, Tuple2<Si1, Si2>, Tuple2<So1, So2>, Either<E1, E2>> {
        val extractE1: (Either<E1, E2>) -> E1? = { either -> either.fold({ it }, { null }) }
        val extractE2: (Either<E1, E2>) -> E2? = { either -> either.fold({ null }, { it }) }
        val extractS1: (Tuple2<Si1, Si2>) -> Si1 = { tuple -> tuple.a }
        val extractS2: (Tuple2<Si1, Si2>) -> Si2 = { tuple -> tuple.b }

        val viewX = this.fix()
        val viewX2 = viewX
            .lmapOnE(extractE1)
            .lmapOnS(extractS1)

        val viewY = y.fix()
        val viewY2 = viewY
            .lmapOnE(extractE2)
            .lmapOnS(extractS2)

        val viewZ = viewX2.rproductOnS(viewY2).fix()

        return _View(
            evolve = { si, e -> viewZ.evolve(si, e) },
            initialState = viewZ.initialState
        )
    }
}
