package com.fraktalio.fmodel.application

import arrow.core.continuations.Effect
import arrow.core.continuations.Raise
import arrow.core.continuations.catch
import arrow.core.continuations.effect
import com.fraktalio.fmodel.application.Error.CommandHandlingFailed
import com.fraktalio.fmodel.application.Error.CommandPublishingFailed
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

context (StateComputation<C, S, E>, StateRepository<C, S>, Raise<Error>)
suspend fun <C, S, E> C.handleWithEffect(): S =
    catch({
        fetchState().computeNewState(this@handleWithEffect).save()
    }) {
        raise(CommandHandlingFailed(this@handleWithEffect, it))
    }

context (StateComputation<C, S, E>, StateRepository<C, S>)
fun <C, S, E> Flow<C>.handleWithEffect(): Flow<Effect<Error, S>> =
    map { effect { it.handleWithEffect() } }.catch { emit(effect { raise(CommandPublishingFailed(it)) }) }


context (StateComputation<C, S, E>, StateLockingRepository<C, S, V>, Raise<Error>)
suspend fun <C, S, E, V> C.handleOptimisticallyWithEffect(): Pair<S, V> =
    catch({
        val (state, version) = this@handleOptimisticallyWithEffect.fetchState()
        state
            .computeNewState(this@handleOptimisticallyWithEffect)
            .save(version)
    }) {
        raise(CommandHandlingFailed(this@handleOptimisticallyWithEffect, it))
    }

context (StateComputation<C, S, E>, StateLockingRepository<C, S, V>)
fun <C, S, E, V> Flow<C>.handleOptimisticallyWithEffect(): Flow<Effect<Error, Pair<S, V>>> =
    map { effect { it.handleOptimisticallyWithEffect() } }.catch { emit(effect { raise(CommandPublishingFailed(it)) }) }

context (StateOrchestratingComputation<C, S, E>, StateRepository<C, S>, Raise<Error>)
@FlowPreview
suspend fun <C, S, E> C.handleWithEffect(): S =
    catch({
        fetchState().computeNewState(this@handleWithEffect).save()
    }) {
        raise(CommandHandlingFailed(this@handleWithEffect, it))
    }

context (StateOrchestratingComputation<C, S, E>, StateRepository<C, S>)
@FlowPreview
suspend fun <C, S, E> Flow<C>.handleWithEffect(): Flow<Effect<Error, S>> =
    map { effect { it.handleWithEffect() } }.catch { emit(effect { raise(CommandPublishingFailed(it)) }) }


context (StateOrchestratingComputation<C, S, E>, StateLockingRepository<C, S, V>, Raise<Error>)
@FlowPreview
suspend fun <C, S, E, V> C.handleOptimisticallyWithEffect(): Pair<S, V> =
    catch({
        val (state, version) = this@handleOptimisticallyWithEffect.fetchState()
        state
            .computeNewState(this@handleOptimisticallyWithEffect)
            .save(version)
    }) {
        raise(CommandHandlingFailed(this@handleOptimisticallyWithEffect, it))
    }

context (StateOrchestratingComputation<C, S, E>, StateLockingRepository<C, S, V>)
@FlowPreview
suspend fun <C, S, E, V> Flow<C>.handleOptimisticallyWithEffect(): Flow<Effect<Error, Pair<S, V>>> =
    map { effect { it.handleOptimisticallyWithEffect() } }.catch { emit(effect { raise(CommandPublishingFailed(it)) }) }
