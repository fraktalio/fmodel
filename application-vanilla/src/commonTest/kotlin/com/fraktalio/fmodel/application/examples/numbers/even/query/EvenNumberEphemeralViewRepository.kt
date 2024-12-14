package com.fraktalio.fmodel.application.examples.numbers.even.query

import com.fraktalio.fmodel.application.EphemeralViewRepository
import com.fraktalio.fmodel.domain.examples.numbers.api.Description
import com.fraktalio.fmodel.domain.examples.numbers.api.NumberEvent
import com.fraktalio.fmodel.domain.examples.numbers.api.NumberValue
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf

/**
 * Simple flows of events to represent previously stored events in the event store
 */
private var numberFlow1 = flowOf(
    NumberEvent.EvenNumberEvent.EvenNumberAdded(Description("Number 2"), NumberValue(2)),
    NumberEvent.EvenNumberEvent.EvenNumberAdded(Description("Number 4"), NumberValue(4))
)

private var numberFlow2 = flowOf(
    NumberEvent.EvenNumberEvent.EvenNumberAdded(Description("Number 4"), NumberValue(4)),
    NumberEvent.EvenNumberEvent.EvenNumberSubtracted(Description("Number 2"), NumberValue(2))
)

/**
 * Even number ephemeral view implementation
 */
class EvenNumberEphemeralViewRepository : EphemeralViewRepository<NumberEvent.EvenNumberEvent?, Int> {

    override fun Int.fetchEvents(): Flow<NumberEvent.EvenNumberEvent?> {
        return when (this) {
            1 -> numberFlow1
            2 -> numberFlow2
            else -> emptyFlow()
        }
    }

}

/**
 * Helper function to create an [EvenNumberEphemeralViewRepository]
 */
fun evenNumberEphemeralViewRepository(): EphemeralViewRepository<NumberEvent.EvenNumberEvent?, Int> =
    EvenNumberEphemeralViewRepository()
