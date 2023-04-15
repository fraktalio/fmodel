package com.fraktalio.fmodel.application

import arrow.core.Either
import arrow.core.raise.either
import com.fraktalio.fmodel.application.Error.ActionResultHandlingFailed
import com.fraktalio.fmodel.application.Error.ActionResultPublishingFailed
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.map

context (SagaManager<AR, A>)
fun <AR, A> AR.handleWithEffect(): Flow<Either<Error, A>> =
    computeNewActions()
        .publish()
        .map { either<Error, A> { it } }
        .catch { emit(either { raise(ActionResultHandlingFailed(this@handleWithEffect, it)) }) }

context (SagaManager<AR, A>)
@FlowPreview
fun <AR, A> Flow<AR>.handleWithEffect(): Flow<Either<Error, A>> =
    flatMapConcat { it.handleWithEffect() }.catch { emit(either { raise(ActionResultPublishingFailed(it)) }) }
