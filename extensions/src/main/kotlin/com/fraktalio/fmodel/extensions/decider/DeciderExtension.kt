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
import arrow.core.Either
import arrow.core.Left
import arrow.core.Right
import arrow.core.Tuple2
import arrow.extension
import com.fraktalio.fmodel.datatypes.For_Decider
import com.fraktalio.fmodel.datatypes._Decider
import com.fraktalio.fmodel.datatypes.fix

@extension
interface DeciderContravariantExtension : Contravariant<For_Decider> {

    /**
     * Contravariant functor on the C (Command)
     */
    override fun <C, Cn, Si, So, Ei, Eo> Kind5<For_Decider, C, Si, So, Ei, Eo>.lmapOnC(f: (Cn) -> C): Kind5<For_Decider, Cn, Si, So, Ei, Eo> {
        val decider = this.fix()
        return _Decider(
            //Only decide method is affected
            decide = { cn, s -> decider.decide(f(cn), s) },
            evolve = { si, ei -> decider.evolve(si, ei) },
            initialState = decider.initialState,
            isTerminal = { si -> decider.isTerminal(si) }
        )
    }

    companion object

}

@extension
interface DeciderProfunctorExtension : Profunctor<For_Decider> {
    /**
     * Contravariant functor on the Ei (input Event)
     * Covariant functor on the Eo (output Event)
     * Contravariant on input and Covariant on output = Profunctor
     */
    override fun <C, Si, So, Ei, Eo, Ein, Eon> Kind5<For_Decider, C, Si, So, Ei, Eo>.dimapOnE(
        fl: (Ein) -> Ei,
        fr: (Eo) -> Eon
    ): Kind5<For_Decider, C, Si, So, Ein, Eon> {
        val decider = this.fix()
        return _Decider(
            decide = { c, si -> decider.decide(c, si).map(fr) },
            evolve = { si, ein -> decider.evolve(si, fl(ein)) },
            initialState = decider.initialState,
            isTerminal = { si -> decider.isTerminal(si) }
        )

    }

    /**
     * Contravariant functor on the Si (input State)
     * Covariant functor on the So (output State)
     * Contravariant on input and Covariant on output = Profunctor
     */
    override fun <C, Si, So, Ei, Eo, Sin, Son> Kind5<For_Decider, C, Si, So, Ei, Eo>.dimapOnS(
        fl: (Sin) -> Si,
        fr: (So) -> Son
    ): Kind5<For_Decider, C, Sin, Son, Ei, Eo> {
        val decider = this.fix()
        return _Decider(
            decide = { c, sin -> decider.decide(c, fl(sin)) },
            evolve = { sin, ei -> fr(decider.evolve(fl(sin), ei)) },
            initialState = fr(decider.initialState),
            isTerminal = { si -> decider.isTerminal(fl(si)) }
        )
    }

    companion object
}

@extension
interface DeciderApplyExtension : Apply<For_Decider>, DeciderProfunctorExtension {

    override fun <C, Si, So, Son, Ei, Eo> Kind5<For_Decider, C, Si, So, Ei, Eo>.rapplyOnS(ff: Kind5<For_Decider, C, Si, (So) -> Son, Ei, Eo>): Kind5<For_Decider, C, Si, Son, Ei, Eo> {
        val decider = this.fix()
        val ffDecider = ff.fix()
        return _Decider(
            decide = { c, si -> ffDecider.decide(c, si).plus(decider.decide(c, si)) },
            evolve = { si, ei -> ffDecider.evolve(si, ei).invoke(decider.evolve(si, ei)) },
            initialState = ffDecider.initialState.invoke(decider.initialState),
            isTerminal = { si -> decider.isTerminal(si) && ffDecider.isTerminal(si) }
        )

    }

    fun <C, Si, So, Son, Ei, Eo> Kind5<For_Decider, C, Si, So, Ei, Eo>.rproductOnS(fb: Kind5<For_Decider, C, Si, Son, Ei, Eo>): Kind5<For_Decider, C, Si, Tuple2<So, Son>, Ei, Eo> =
        rapplyOnS(fb.rmapOnS { b: Son -> { a: So -> Tuple2(a, b) } })

    fun <C, Si, So, Son, Ei, Eo, Z> mapN(
        a: Kind5<For_Decider, C, Si, So, Ei, Eo>,
        b: Kind5<For_Decider, C, Si, Son, Ei, Eo>,
        lbd: (Tuple2<So, Son>) -> Z
    ): Kind5<For_Decider, C, Si, Z, Ei, Eo> =
        a.rproductOnS(b).rmapOnS(lbd)

    companion object
}

@extension
interface DeciderApplicativeExtension : Applicative<For_Decider>, DeciderApplyExtension {

    /**
     * Applicative on s
     */
    override fun <C, Si, So, Ei, Eo> rjustOnS(so: So): Kind5<For_Decider, C, Si, So, Ei, Eo> {
        return _Decider(
            decide = { c, si -> emptyList() },
            evolve = { si, ei -> so },
            initialState = so,
            isTerminal = { si -> true }
        )
    }

    override fun <C, Si, So, Ei, Eo, Son> Kind5<For_Decider, C, Si, So, Ei, Eo>.rmapOnS(f: (So) -> Son): Kind5<For_Decider, C, Si, Son, Ei, Eo> =
        rapplyOnS(rjustOnS(f))

    companion object

}

@extension
interface DeciderSemigroupExtension : Semigroup<For_Decider>, DeciderApplicativeExtension,
    DeciderContravariantExtension {
    /**
     * Semigroup
     */
    override fun <C1, Si1, So1, Ei1, Eo1, C2, Si2, So2, Ei2, Eo2> Kind5<For_Decider, C1, Si1, So1, Ei1, Eo1>.combineDeciders(
        y: Kind5<For_Decider, C2, Si2, So2, Ei2, Eo2>
    ): Kind5<For_Decider, Either<C1, C2>, Tuple2<Si1, Si2>, Tuple2<So1, So2>, Either<Ei1, Ei2>, Either<Eo1, Eo2>> {
        val getC1: (Either<C1, C2>) -> C1? = { either -> either.fold({ it }, { null }) }
        val getC2: (Either<C1, C2>) -> C2? = { either -> either.fold({ null }, { it }) }
        val getE1: (Either<Ei1, Ei2>) -> Ei1? = { either -> either.fold({ it }, { null }) }
        val getE1Either: (Eo1) -> Either<Eo1, Nothing> = { eo1 -> Left(eo1) }
        val getE2: (Either<Ei1, Ei2>) -> Ei2? = { either -> either.fold({ null }, { it }) }
        val getE2Either: (Eo2) -> Either<Nothing, Eo2> = { eo2 -> Right(eo2) }
        val getS1: (Tuple2<Si1, Si2>) -> Si1 = { tuple -> tuple.a }
        val getS2: (Tuple2<Si1, Si2>) -> Si2 = { tuple -> tuple.b }

        val deciderX = this.fix()
        val deciderX1 = deciderX
            .lmapOnC(getC1)
            .dimapOnE(getE1, getE1Either)
            .lmapOnS(getS1)

        val deciderY = y.fix()
        val deciderY2 = deciderY
            .lmapOnC(getC2)
            .dimapOnE(getE2, getE2Either)
            .lmapOnS(getS2)

        val deciderZ = deciderX1.rproductOnS(deciderY2).fix()

        return _Decider(
            decide = { c, si -> deciderZ.decide(c, si) },
            evolve = { tuple2, ei -> deciderZ.evolve(tuple2, ei) },
            initialState = deciderZ.initialState,
            isTerminal = { tuple2 -> deciderZ.isTerminal(tuple2) }
        )
    }

    companion object
}
