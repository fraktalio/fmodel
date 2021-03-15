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

package com.fraktalio.fmodel.extensions.view.materializedview

import arrow.Kind2
import arrow.extension
import com.fraktalio.fmodel.datatypes.ForMaterializedView
import com.fraktalio.fmodel.datatypes.MaterializedView
import com.fraktalio.fmodel.datatypes.fix
import com.fraktalio.fmodel.extensions.view._view.contravariant.lmapOnE

@extension
interface MaterializedViewExtension : Contravariant<ForMaterializedView> {
    override fun <S, E, En> Kind2<ForMaterializedView, S, E>.lmapOnE(f: (En) -> E): Kind2<ForMaterializedView, S, En> {
        val stateStoredView = this.fix()
        return MaterializedView(
            view = stateStoredView.view.lmapOnE(f),
            fetchState = { en -> stateStoredView.fetchState(f(en)) },
            storeState = { s -> stateStoredView.storeState(s) }
        )
    }

    companion object
}
