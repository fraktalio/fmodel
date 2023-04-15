package com.fraktalio.fmodel.application

import arrow.core.Either
import arrow.core.raise.Raise
import arrow.core.raise.catch
import arrow.core.raise.either
import com.fraktalio.fmodel.application.Error.EventHandlingFailed
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map


context (ViewStateComputation<S, E>, ViewStateRepository<E, S>, Raise<Error>)
suspend fun <S, E> E.handleWithEffect(): S = catch({
    fetchState().computeNewState(this@handleWithEffect).save()
}) {
    raise(EventHandlingFailed(this@handleWithEffect, it))
}

context (ViewStateComputation<S, E>, ViewStateLockingRepository<E, S, V>, Raise<Error>)
suspend fun <S, E, V> E.handleOptimisticallyWithEffect(): Pair<S, V> = catch({
    val (state, version) = fetchState()
    state.computeNewState(this@handleOptimisticallyWithEffect).save(version)
}) {
    raise(EventHandlingFailed(this@handleOptimisticallyWithEffect, it))
}

context (ViewStateComputation<S, E>, ViewStateLockingDeduplicationRepository<E, S, EV, SV>, Raise<Error>)
suspend fun <S, E, EV, SV> E.handleOptimisticallyWithDeduplicationAndEffect(eventAndVersion: Pair<E, EV>): Pair<S, SV> {
    val (event, eventVersion) = eventAndVersion
    return catch({
        val (state, stateVersion) = event.fetchState()
        state.computeNewState(event).save(eventVersion, stateVersion)
    }) {
        raise(EventHandlingFailed(this@handleOptimisticallyWithDeduplicationAndEffect, it))
    }
}

context (ViewStateComputation<S, E>, ViewStateRepository<E, S>)
fun <S, E> Flow<E>.handleWithEffect(): Flow<Either<Error, S>> =
    map { either { it.handleWithEffect() } }.catch { emit(either { raise(Error.EventPublishingFailed(it)) }) }

context (ViewStateComputation<S, E>, ViewStateLockingRepository<E, S, V>)
fun <S, E, V> Flow<E>.handleOptimisticallyWithEffect(): Flow<Either<Error, Pair<S, V>>> =
    map { either { it.handleOptimisticallyWithEffect() } }.catch { emit(either { raise(Error.EventPublishingFailed(it)) }) }

context (ViewStateComputation<S, E>, ViewStateLockingDeduplicationRepository<E, S, EV, SV>)
fun <S, E, EV, SV> Flow<E>.handleOptimisticallyWithDeduplicationAndEffect(eventAndVersion: Pair<E, EV>): Flow<Either<Error, Pair<S, SV>>> =
    map { either { it.handleOptimisticallyWithDeduplicationAndEffect(eventAndVersion) } }
        .catch { emit(either { raise(Error.EventPublishingFailed(it)) }) }
