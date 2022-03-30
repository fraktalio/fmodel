package com.fraktalio.fmodel.application

import com.fraktalio.fmodel.application.examples.numbers.even.query.EvenNumberViewRepository
import com.fraktalio.fmodel.application.examples.numbers.even.query.evenNumberViewRepository
import com.fraktalio.fmodel.domain.examples.numbers.api.Description
import com.fraktalio.fmodel.domain.examples.numbers.api.EvenNumberState
import com.fraktalio.fmodel.domain.examples.numbers.api.NumberEvent.EvenNumberEvent.EvenNumberAdded
import com.fraktalio.fmodel.domain.examples.numbers.api.NumberValue
import com.fraktalio.fmodel.domain.examples.numbers.even.query.evenNumberView
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactly
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlin.contracts.ExperimentalContracts

/**
 * Materialized View Contextual Test
 */
@FlowPreview
@ExperimentalContracts
class MaterializedViewContextualTest : FunSpec({
    val evenView = evenNumberView()
    val evenNumberViewRepository = evenNumberViewRepository() as EvenNumberViewRepository

    test("Materialized view contextual - even number added") {
        evenNumberViewRepository.deleteAll()
        with(evenView) {
            with(evenNumberViewRepository) {
                flowOf(
                    EvenNumberAdded(Description("EvenNumberAdded"), NumberValue(2)),
                    EvenNumberAdded(Description("EvenNumberAdded"), NumberValue(4))
                ).handle().toList() shouldContainExactly listOf(
                    EvenNumberState(Description("Initial state, EvenNumberAdded"), NumberValue(2)),
                    EvenNumberState(Description("Initial state, EvenNumberAdded, EvenNumberAdded"), NumberValue(6))
                )
            }
        }
    }

    test("Materialized view contextual materialized view interface - even number added") {
        evenNumberViewRepository.deleteAll()
        with(materializedView(evenView, evenNumberViewRepository)) {
            flowOf(
                EvenNumberAdded(Description("EvenNumberAdded"), NumberValue(2)),
                EvenNumberAdded(Description("EvenNumberAdded"), NumberValue(4))
            ).handle().toList() shouldContainExactly listOf(
                EvenNumberState(Description("Initial state, EvenNumberAdded"), NumberValue(2)),
                EvenNumberState(Description("Initial state, EvenNumberAdded, EvenNumberAdded"), NumberValue(6))
            )
        }
    }

    test("Materialized view concurrent and contextual - even number added") {
        evenNumberViewRepository.deleteAll()
        with(evenView) {
            with(evenNumberViewRepository) {
                flowOf(
                    EvenNumberAdded(Description("EvenNumberAdded"), NumberValue(2)),
                    EvenNumberAdded(Description("EvenNumberAdded"), NumberValue(4))
                ).handleConcurrently { it?.description.hashCode() }.toList() shouldContainExactly listOf(
                    EvenNumberState(Description("Initial state, EvenNumberAdded"), NumberValue(2)),
                    EvenNumberState(Description("Initial state, EvenNumberAdded, EvenNumberAdded"), NumberValue(6))
                )
            }
        }
    }

    test("Materialized view concurrent and contextual materialized view interface - even number added") {
        evenNumberViewRepository.deleteAll()
        with(materializedView(evenView, evenNumberViewRepository)) {
            flowOf(
                EvenNumberAdded(Description("EvenNumberAdded"), NumberValue(2)),
                EvenNumberAdded(Description("EvenNumberAdded"), NumberValue(4))
            ).handleConcurrently { it?.description.hashCode() }.toList() shouldContainExactly listOf(
                EvenNumberState(Description("Initial state, EvenNumberAdded"), NumberValue(2)),
                EvenNumberState(Description("Initial state, EvenNumberAdded, EvenNumberAdded"), NumberValue(6))
            )
        }
    }

})
