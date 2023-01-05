package com.fraktalio.fmodel.application

import arrow.core.continuations.Effect
import arrow.core.continuations.effect
import com.fraktalio.fmodel.application.Error.ActionResultHandlingFailed
import com.fraktalio.fmodel.application.Error.ActionResultPublishingFailed
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.map

context (SagaManager<AR, A>)
fun <AR, A> AR.handleWithEffect(): Flow<Effect<Error, A>> =
    computeNewActions()
        .publish()
        .map { effect<Error, A> { it } }
        .catch { emit(effect { raise(ActionResultHandlingFailed(this@handleWithEffect, it)) }) }

context (SagaManager<AR, A>)
@FlowPreview
fun <AR, A> Flow<AR>.handleWithEffect(): Flow<Effect<Error, A>> =
    flatMapConcat { it.handleWithEffect() }.catch { emit(effect { raise(ActionResultPublishingFailed(it)) }) }
