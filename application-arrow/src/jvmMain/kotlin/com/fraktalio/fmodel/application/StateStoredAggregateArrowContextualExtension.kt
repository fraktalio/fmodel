package com.fraktalio.fmodel.application

import arrow.core.continuations.Effect
import arrow.core.continuations.EffectScope
import arrow.core.continuations.effect
import arrow.core.nonFatalOrThrow
import com.fraktalio.fmodel.application.Error.CommandHandlingFailed
import com.fraktalio.fmodel.application.Error.CommandPublishingFailed

import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

context (StateComputation<C, S, E>, StateRepository<C, S>, EffectScope<Error>)
suspend fun <C, S, E> C.handleWithEffect(): S =
    try {
        fetchState().computeNewState(this).save()
    } catch (t: Throwable) {
        shift(CommandHandlingFailed(this, t.nonFatalOrThrow()))
    }

context (StateComputation<C, S, E>, StateRepository<C, S>)
fun <C, S, E> Flow<C>.handleWithEffect(): Flow<Effect<Error, S>> =
    map { effect { it.handleWithEffect() } }.catch { emit(effect { shift(CommandPublishingFailed(it)) }) }

context (StateComputation<C, S, E>, StateLockingRepository<C, S, V>, EffectScope<Error>)
suspend fun <C, S, E, V> C.handleOptimisticallyWithEffect(): Pair<S, V> =
    try {
        val (state, version) = this@handleOptimisticallyWithEffect.fetchState()
        state
            .computeNewState(this@handleOptimisticallyWithEffect)
            .save(version)
    } catch (t: Throwable) {
        shift(CommandHandlingFailed(this, t.nonFatalOrThrow()))
    }

context (StateComputation<C, S, E>, StateLockingRepository<C, S, V>)
fun <C, S, E, V> Flow<C>.handleOptimisticallyWithEffect(): Flow<Effect<Error, Pair<S, V>>> =
    map { effect { it.handleOptimisticallyWithEffect() } }.catch { emit(effect { shift(CommandPublishingFailed(it)) }) }

context (StateOrchestratingComputation<C, S, E>, StateRepository<C, S>, EffectScope<Error>)
@FlowPreview
suspend fun <C, S, E> C.handleWithEffect(): S =
    try {
        fetchState().computeNewState(this).save()
    } catch (t: Throwable) {
        shift(CommandHandlingFailed(this, t.nonFatalOrThrow()))
    }

context (StateOrchestratingComputation<C, S, E>, StateRepository<C, S>)
@FlowPreview
suspend fun <C, S, E> Flow<C>.handleWithEffect(): Flow<Effect<Error, S>> =
    map { effect { it.handleWithEffect() } }.catch { emit(effect { shift(CommandPublishingFailed(it)) }) }


context (StateOrchestratingComputation<C, S, E>, StateLockingRepository<C, S, V>, EffectScope<Error>)
@FlowPreview
suspend fun <C, S, E, V> C.handleOptimisticallyWithEffect(): Pair<S, V> =
    try {
        val (state, version) = this@handleOptimisticallyWithEffect.fetchState()
        state
            .computeNewState(this@handleOptimisticallyWithEffect)
            .save(version)
    } catch (t: Throwable) {
        shift(CommandHandlingFailed(this, t.nonFatalOrThrow()))
    }

context (StateOrchestratingComputation<C, S, E>, StateLockingRepository<C, S, V>)
@FlowPreview
suspend fun <C, S, E, V> Flow<C>.handleOptimisticallyWithEffect(): Flow<Effect<Error, Pair<S, V>>> =
    map { effect { it.handleOptimisticallyWithEffect() } }.catch { emit(effect { shift(CommandPublishingFailed(it)) }) }
