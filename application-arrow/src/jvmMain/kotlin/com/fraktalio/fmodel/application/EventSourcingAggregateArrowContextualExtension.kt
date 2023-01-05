package com.fraktalio.fmodel.application

import arrow.core.continuations.Effect
import arrow.core.continuations.effect
import com.fraktalio.fmodel.application.Error.CommandHandlingFailed
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*


context (EventComputation<C, S, E>, EventRepository<C, E>)
@FlowPreview
fun <C, S, E> C.handleWithEffect(): Flow<Effect<Error, E>> =
    fetchEvents()
        .computeNewEvents(this)
        .save()
        .map { effect<Error, E> { it } }
        .catch { emit(effect { raise(CommandHandlingFailed(this@handleWithEffect)) }) }

context (EventComputation<C, S, E>, EventLockingRepository<C, E, V>)
@FlowPreview
fun <C, S, E, V> C.handleOptimisticallyWithEffect(): Flow<Effect<Error, Pair<E, V>>> = flow {
    val events = this@handleOptimisticallyWithEffect.fetchEvents()
    emitAll(
        events.map { it.first }
            .computeNewEvents(this@handleOptimisticallyWithEffect)
            .save(events.lastOrNull())
            .map { effect<Error, Pair<E, V>> { it } }
            .catch { emit(effect { raise(CommandHandlingFailed(this@handleOptimisticallyWithEffect)) }) }
    )
}

context (EventOrchestratingComputation<C, S, E>, EventRepository<C, E>)
@FlowPreview
fun <C, S, E> C.handleWithEffect(): Flow<Effect<Error, E>> =
    fetchEvents()
        .computeNewEventsByOrchestrating(this) { it.fetchEvents() }
        .save()
        .map { effect<Error, E> { it } }
        .catch { emit(effect { raise(CommandHandlingFailed(this@handleWithEffect)) }) }

context (EventOrchestratingComputation<C, S, E>, EventLockingRepository<C, E, V>)
@FlowPreview
fun <C, S, E, V> C.handleOptimisticallyWithEffect(): Flow<Effect<Error, Pair<E, V>>> =
    fetchEvents().map { it.first }
        .computeNewEventsByOrchestrating(this) { it.fetchEvents().map { pair -> pair.first } }
        .save(latestVersionProvider)
        .map { effect<Error, Pair<E, V>> { it } }
        .catch { emit(effect { raise(CommandHandlingFailed(this@handleOptimisticallyWithEffect)) }) }

context (EventComputation<C, S, E>, EventRepository<C, E>)
@FlowPreview
fun <C, S, E> Flow<C>.handleWithEffect(): Flow<Effect<Error, E>> =
    flatMapConcat { it.handleWithEffect() }.catch { emit(effect { raise(Error.CommandPublishingFailed(it)) }) }

context (EventComputation<C, S, E>, EventLockingRepository<C, E, V>)
@FlowPreview
fun <C, S, E, V> Flow<C>.handleOptimisticallyWithEffect(): Flow<Effect<Error, Pair<E, V>>> =
    flatMapConcat { it.handleOptimisticallyWithEffect() }.catch { emit(effect { raise(Error.CommandPublishingFailed(it)) }) }

context (EventOrchestratingComputation<C, S, E>, EventRepository<C, E>)
@FlowPreview
fun <C, S, E> Flow<C>.handleWithEffect(): Flow<Effect<Error, E>> =
    flatMapConcat { it.handleWithEffect() }.catch { emit(effect { raise(Error.CommandPublishingFailed(it)) }) }

context (EventOrchestratingComputation<C, S, E>, EventLockingRepository<C, E, V>)
@FlowPreview
fun <C, S, E, V> Flow<C>.handleOptimisticallyWithEffect(): Flow<Effect<Error, Pair<E, V>>> =
    flatMapConcat { it.handleOptimisticallyWithEffect() }.catch { emit(effect { raise(Error.CommandPublishingFailed(it)) }) }
