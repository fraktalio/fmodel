package com.fraktalio.fmodel.application

import arrow.core.Either
import arrow.core.raise.either
import com.fraktalio.fmodel.application.Error.CommandHandlingFailed
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*


context (EventComputation<C, S, E>, EventRepository<C, E>)
@FlowPreview
fun <C, S, E> C.handleWithEffect(): Flow<Either<Error, E>> =
    fetchEvents()
        .computeNewEvents(this)
        .save()
        .map { either<Error, E> { it } }
        .catch { emit(either { raise(CommandHandlingFailed(this@handleWithEffect)) }) }

context (EventComputation<C, S, E>, EventLockingRepository<C, E, V>)
@FlowPreview
fun <C, S, E, V> C.handleOptimisticallyWithEffect(): Flow<Either<Error, Pair<E, V>>> = flow {
    val events = this@handleOptimisticallyWithEffect.fetchEvents()
    emitAll(
        events.map { it.first }
            .computeNewEvents(this@handleOptimisticallyWithEffect)
            .save(events.lastOrNull())
            .map { either<Error, Pair<E, V>> { it } }
            .catch { emit(either { raise(CommandHandlingFailed(this@handleOptimisticallyWithEffect)) }) }
    )
}

context (EventOrchestratingComputation<C, S, E>, EventRepository<C, E>)
@FlowPreview
fun <C, S, E> C.handleWithEffect(): Flow<Either<Error, E>> =
    fetchEvents()
        .computeNewEventsByOrchestrating(this) { it.fetchEvents() }
        .save()
        .map { either<Error, E> { it } }
        .catch { emit(either { raise(CommandHandlingFailed(this@handleWithEffect)) }) }

context (EventOrchestratingComputation<C, S, E>, EventLockingRepository<C, E, V>)
@FlowPreview
fun <C, S, E, V> C.handleOptimisticallyWithEffect(): Flow<Either<Error, Pair<E, V>>> =
    fetchEvents().map { it.first }
        .computeNewEventsByOrchestrating(this) { it.fetchEvents().map { pair -> pair.first } }
        .save(latestVersionProvider)
        .map { either<Error, Pair<E, V>> { it } }
        .catch { emit(either { raise(CommandHandlingFailed(this@handleOptimisticallyWithEffect)) }) }

context (EventComputation<C, S, E>, EventRepository<C, E>)
@FlowPreview
fun <C, S, E> Flow<C>.handleWithEffect(): Flow<Either<Error, E>> =
    flatMapConcat { it.handleWithEffect() }.catch { emit(either { raise(Error.CommandPublishingFailed(it)) }) }

context (EventComputation<C, S, E>, EventLockingRepository<C, E, V>)
@FlowPreview
fun <C, S, E, V> Flow<C>.handleOptimisticallyWithEffect(): Flow<Either<Error, Pair<E, V>>> =
    flatMapConcat { it.handleOptimisticallyWithEffect() }.catch { emit(either { raise(Error.CommandPublishingFailed(it)) }) }

context (EventOrchestratingComputation<C, S, E>, EventRepository<C, E>)
@FlowPreview
fun <C, S, E> Flow<C>.handleWithEffect(): Flow<Either<Error, E>> =
    flatMapConcat { it.handleWithEffect() }.catch { emit(either { raise(Error.CommandPublishingFailed(it)) }) }

context (EventOrchestratingComputation<C, S, E>, EventLockingRepository<C, E, V>)
@FlowPreview
fun <C, S, E, V> Flow<C>.handleOptimisticallyWithEffect(): Flow<Either<Error, Pair<E, V>>> =
    flatMapConcat { it.handleOptimisticallyWithEffect() }.catch { emit(either { raise(Error.CommandPublishingFailed(it)) }) }
