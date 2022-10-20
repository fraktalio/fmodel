package com.fraktalio.fmodel.application

import arrow.core.continuations.Effect
import arrow.core.continuations.EffectScope
import arrow.core.continuations.effect
import arrow.core.nonFatalOrThrow
import com.fraktalio.fmodel.application.Error.EventHandlingFailed
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map


context (ViewStateComputation<S, E>, ViewStateRepository<E, S>, EffectScope<Error>)
suspend fun <S, E> E.handleWithEffect(): S = try {
    fetchState().computeNewState(this).save()
} catch (t: Throwable) {
    shift(EventHandlingFailed(this, t.nonFatalOrThrow()))
}

context (ViewStateComputation<S, E>, ViewStateLockingRepository<E, S, V>, EffectScope<Error>)
suspend fun <S, E, V> E.handleOptimisticallyWithEffect(): Pair<S, V> = try {
    val (state, version) = fetchState()
    state.computeNewState(this).save(version)
} catch (t: Throwable) {
    shift(EventHandlingFailed(this, t.nonFatalOrThrow()))
}

context (ViewStateComputation<S, E>, ViewStateLockingDeduplicationRepository<E, S, EV, SV>, EffectScope<Error>)
suspend fun <S, E, EV, SV> E.handleOptimisticallyWithDeduplicationAndEffect(eventAndVersion: Pair<E, EV>): Pair<S, SV> {
    val (event, eventVersion) = eventAndVersion
    return try {
        val (state, stateVersion) = event.fetchState()
        state.computeNewState(event).save(eventVersion, stateVersion)
    } catch (t: Throwable) {
        shift(EventHandlingFailed(this, t.nonFatalOrThrow()))
    }
}

context (ViewStateComputation<S, E>, ViewStateRepository<E, S>)
fun <S, E> Flow<E>.handleWithEffect(): Flow<Effect<Error, S>> =
    map { effect { it.handleWithEffect() } }.catch { emit(effect { shift(Error.EventPublishingFailed(it)) }) }

context (ViewStateComputation<S, E>, ViewStateLockingRepository<E, S, V>)
fun <S, E, V> Flow<E>.handleOptimisticallyWithEffect(): Flow<Effect<Error, Pair<S, V>>> =
    map { effect { it.handleOptimisticallyWithEffect() } }.catch { emit(effect { shift(Error.EventPublishingFailed(it)) }) }

context (ViewStateComputation<S, E>, ViewStateLockingDeduplicationRepository<E, S, EV, SV>)
fun <S, E, EV, SV> Flow<E>.handleOptimisticallyWithDeduplicationAndEffect(eventAndVersion: Pair<E, EV>): Flow<Effect<Error, Pair<S, SV>>> =
    map { effect { it.handleOptimisticallyWithDeduplicationAndEffect(eventAndVersion) } }
        .catch { emit(effect { shift(Error.EventPublishingFailed(it)) }) }
