package com.fraktalio.fmodel.application

import kotlinx.coroutines.flow.Flow

/**
 * Ephemeral view repository interface
 *
 * @param E Event
 * @param Q Query
 *
 * @author Domenic Cassisi
 */
fun interface EphemeralViewRepository<E, Q> {

    /**
     * Fetch events
     *
     * @receiver Query of type [Q]
     * @return the Flow of events of type [E]
     */
    fun Q.fetchEvents(): Flow<E>

}
