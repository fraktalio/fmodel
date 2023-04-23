---
sidebar_position: 2
---

import Tabs from '@theme/Tabs';
import TabItem from '@theme/TabItem';

# Application

The only responsibility of the application layer is to orchestrate the execution of the logic by

- loading the state from a repository
- execute logic by executing domain layer components
- store the new state

Fmodel offers application interfaces/components which are actually composed out of repository interfaces/components (side effects) and core domain components (decision-making):

## Event-Sourced vs State-Stored

<Tabs groupId="system-type" queryString="system-type">
  <TabItem value="event-stored" label="Event-Stored / Event-Sourced">

Event-stored systems are split to command and view/query models, by default.
This is making a huge difference as we are not limited to use a single canonical model for writing and
reading/presenting!

![event-modeling-event-driven-systems](/img/event-modeling-event-driven-systems.png)

**Event-sourcing Aggregate** is a formalization of the event-stored/event-sourced system (Command
Model) [mentioned previously](architecture.md#event-stored-or-state-stored-systems).

```kotlin
interface EventSourcingAggregate<C, S, E> : IDecider<C, S, E>, EventRepository<C, E>
interface EventSourcingOrchestratingAggregate<C, S, E> : IDecider<C, S, E>, EventRepository<C, E>, ISaga<E, C>
interface MaterializedView<S, E> : IView<S, E>, ViewStateRepository<E, S>
interface SagaManager<AR, A> : ISaga<AR, A>, ActionPublisher<A>
```

Event-sourcing Aggregate is using/delegating a `Decider` to handle commands and produce events. It belongs to the
Application layer. In order to
handle the command, aggregate needs to fetch the current state (represented as a list of events)
via `EventRepository.fetchEvents` function, and then delegate the command to the decider which can produce new events as
a result. Produced events are then stored via `EventRepository.save` suspending function.

The Delegation pattern has proven to be a good alternative to `implementation inheritance`, and Kotlin supports it
natively requiring zero boilerplate code.
`eventSourcingAggregate` and `eventSourcingOrchestratingAggregate` functions, provided by the Fmodel, are good example:

```kotlin
fun <C, S, E> eventSourcingAggregate(
    decider: IDecider<C, S, E>,
    eventRepository: EventRepository<C, E>
): EventSourcingAggregate<C, S, E> =
    object :
        EventSourcingAggregate<C, S, E>,
        EventRepository<C, E> by eventRepository,
        IDecider<C, S, E> by decider {}

fun <C, S, E> eventSourcingOrchestratingAggregate(
   decider: IDecider<C, S, E>,
   eventRepository: EventRepository<C, E>,
   saga: ISaga<E, C>
): EventSourcingOrchestratingAggregate<C, S, E> =
   object : EventSourcingOrchestratingAggregate<C, S, E>,
      EventRepository<C, E> by eventRepository,
      IDecider<C, S, E> by decider,
      ISaga<E, C> by saga {}
```

<Tabs groupId="style" queryString="style">

  <TabItem value="monolith" label="monolith">

*Example of a monolith scenario, in which Order and Restaurant deciders
are [combined/aggregated](/domain/aggregating-the-behaviour.md?component-type=decider) in one big decider and then
wrapped by one aggregate component:*

```kotlin
/**
 * A convenient type alias for Decider<OrderCommand?, Order?, OrderEvent?>
 */
typealias OrderDecider = Decider<OrderCommand?, Order?, OrderEvent?>

/**
 * A convenient type alias for Decider<RestaurantCommand?, Restaurant?, RestaurantEvent?>
 */
typealias RestaurantDecider = Decider<RestaurantCommand?, Restaurant?, RestaurantEvent?>

/**
 * A convenient type alias for EventRepository<Command?, Event?>
 *     
 * notice that OrderCommand and RestaurantCommand are extending `sealed` Command,
 * and that OrderEvent and RestaurantEvent are extending `sealed` Event
 */
typealias AggregateEventRepository = EventRepository<Command?, Event?>


val aggregate = eventSourcingAggregate(orderDecider combine restaurantDecider, aggregateEventRepository)


/**
 * Start handling all your commands!
 */
aggregate.handle(command)
```

 </TabItem>

   <TabItem value="monolith-orchestrated" label="monolith orchestrated">

*Example of a monolith scenario, in which Order and Restaurant deciders
are [combined/aggregated](/domain/aggregating-the-behaviour.md?component-type=decider) in one big decider, additionally Order and Restaurant sagas are [combined](/domain/aggregating-the-behaviour.md?component-type=saga) into one orchestrating saga.
Decider and Saga are then wrapped by one aggregate component. Saga is responsible to integrate deciders internally, and enable event of one decider to trigger command of another, automatically*

:::info
Events produced by both deciders belong to the same transaction, and they are immediately consistent.
:::


```kotlin
/**
 * A convenient type alias for Decider<OrderCommand?, Order?, OrderEvent?>
 */
typealias OrderDecider = Decider<OrderCommand?, Order?, OrderEvent?>

/**
 * A convenient type alias for Decider<RestaurantCommand?, Restaurant?, RestaurantEvent?>
 */
typealias RestaurantDecider = Decider<RestaurantCommand?, Restaurant?, RestaurantEvent?>

/**
 * A convenient type alias for Saga<RestaurantEvent?, OrderCommand?>
 */
typealias OrderSaga = Saga<RestaurantEvent?, OrderCommand>

/**
 * A convenient type alias for Saga<OrderEvent?, RestaurantCommand>
 */
typealias RestaurantSaga = Saga<OrderEvent?, RestaurantCommand>

/**
 * A convenient type alias for EventRepository<Command?, Event?>
 *     
 * notice that OrderCommand and RestaurantCommand are extending `sealed` Command,
 * and that OrderEvent and RestaurantEvent are extending `sealed` Event
 */
typealias AggregateEventRepository = EventRepository<Command?, Event?>


val aggregate = eventSourcingOrchestratingAggregate(orderDecider combine restaurantDecider, aggregateEventRepository, orderSaga combine restaurantSaga)


/**
 * Start handling all your commands!
 */
aggregate.handle(command)
```

   </TabItem>

 
  <TabItem value="distributed" label="distributed">

*Example of a distributed scenario, in which Order and Restaurant deciders are wrapped by independent aggregate
components:*

:::info
In distributed scenario, all aggregate components could be deployed as independent applications
:::

```kotlin
/**
 * A convenient type alias for Decider<OrderCommand?, Order?, OrderEvent?>
 */
typealias OrderDecider = Decider<OrderCommand?, Order?, OrderEvent?>

/**
 * A convenient type alias for Decider<RestaurantCommand?, Restaurant?, RestaurantEvent?>
 */
typealias RestaurantDecider = Decider<RestaurantCommand?, Restaurant?, RestaurantEvent?>

/**
 * A convenient type alias for EventRepository<OrderCommand?, OrderEvent?>
 */
typealias OrderAggregateEventRepository = EventRepository<OrderCommand?, OrderEvent?>

/**
 * A convenient type alias for EventRepository<RestaurantCommand?, RestaurantEvent?>
 */
typealias RestaurantAggregateEventRepository = EventRepository<RestaurantCommand?, RestaurantEvent?>

val orderAggregate = eventSourcingAggregate(orderDecider, orderAggregateEventRepository)
val restaurantAggregate = eventSourcingAggregate(restaurantDecider, restaurantAggregateEventRepository)


/**
 * Start handling your Order commands!
 */
orderAggregate.handle(orderCommand)

/**
 * Start handling your Restaurant commands!
 */
restaurantAggregate.handle(restaurantCommand)
```

  </TabItem>

  <TabItem value="distributed-orchestrated" label="distributed orchestrated">

*Example of a distributed scenario, in which Order and Restaurant deciders are wrapped by independent aggregate
components, and Order and Restaurant sagas are combined into one Saga and wrapped by unique Saga Manager:*

:::info
In distributed scenario, all aggregate and saga manager component(s) could be deployed as independent applications, communicating over the wire.

In this scenario we have three components on the application layer that should communicate to each other over the wire:

 - orderAggregate (app1)
 - restaurantAggregate (app2)
 - orchestratedSagaManager (app3)

The combined (orchestrating) `orchestratedSagaManager` will react on events (**over the wire**) produced by both aggregates and send commands (**over the wire**) to these aggregates. 
:::

```kotlin
/**
 * A convenient type alias for Decider<OrderCommand?, Order?, OrderEvent?>
 */
typealias OrderDecider = Decider<OrderCommand?, Order?, OrderEvent?>

/**
 * A convenient type alias for Decider<RestaurantCommand?, Restaurant?, RestaurantEvent?>
 */
typealias RestaurantDecider = Decider<RestaurantCommand?, Restaurant?, RestaurantEvent?>

/**
 * A convenient type alias for Saga<RestaurantEvent?, OrderCommand?>
 */
typealias OrderSaga = Saga<RestaurantEvent?, OrderCommand>

/**
 * A convenient type alias for Saga<OrderEvent?, RestaurantCommand>
 */
typealias RestaurantSaga = Saga<OrderEvent?, RestaurantCommand>

/**
 * A convenient type alias for EventRepository<OrderCommand?, OrderEvent?>
 */
typealias OrderAggregateEventRepository = EventRepository<OrderCommand?, OrderEvent?>

/**
 * A convenient type alias for EventRepository<RestaurantCommand?, RestaurantEvent?>
 */
typealias RestaurantAggregateEventRepository = EventRepository<RestaurantCommand?, RestaurantEvent?>

/**
 * A convenient type alias for ActionPublisher<Command?>
 */
typealias OrchestratingSagaManagerPublisher = ActionPublisher<Command?>

val orderAggregate = eventSourcingAggregate(orderDecider, orderAggregateEventRepository)
val restaurantAggregate = eventSourcingAggregate(restaurantDecider, restaurantAggregateEventRepository)
val orchestratedSagaManager = sagaManager(orderSaga combine restaurantSaga, actionPublisher)


/**
 * Start handling your Order commands!
 */
orderAggregate.handle(orderCommand)

/**
 * Start handling your Restaurant commands!
 */
restaurantAggregate.handle(restaurantCommand)

/**
 * Additionally, orchestratedSagaManager is reacting on events from one aggregate and publishes command to the other.
 */
val someFlowOfEventsComingViaKafkaOrDB: Flow<Event>
someFlowOfEventsComingViaKafkaOrDB.publishTo(orchestratedSagaManager).collect {...}
```

  </TabItem>

  <TabItem value="distributed-choreography" label="distributed choreography">

*Example of a distributed scenario, in which Order and Restaurant deciders are wrapped by independent aggregate
components, and Order and Restaurant sagas are wrapped by independent Saga managers:*

:::info
In distributed scenario, all aggregate and corresponding saga manager component(s) could be deployed as independent applications, communicating over the wire.

In this scenario we have four components on the application layer that should communicate to each other over the wire:

- orderAggregate (app1)
- orderSagaManager (app1)
- restaurantAggregate (app2)
- restaurantSagaManager (app2)

The `orderSagaManager / restaurantSagaManager` will react on events (**over the wire**) produced by aggregates and send commands (**locally**) to the appropriate aggregate(s).
:::

```kotlin
/**
 * A convenient type alias for Decider<OrderCommand?, Order?, OrderEvent?>
 */
typealias OrderDecider = Decider<OrderCommand?, Order?, OrderEvent?>

/**
 * A convenient type alias for Decider<RestaurantCommand?, Restaurant?, RestaurantEvent?>
 */
typealias RestaurantDecider = Decider<RestaurantCommand?, Restaurant?, RestaurantEvent?>

/**
 * A convenient type alias for Saga<RestaurantEvent?, OrderCommand?>
 */
typealias OrderSaga = Saga<RestaurantEvent?, OrderCommand>

/**
 * A convenient type alias for Saga<OrderEvent?, RestaurantCommand>
 */
typealias RestaurantSaga = Saga<OrderEvent?, RestaurantCommand>

/**
 * A convenient type alias for EventRepository<OrderCommand?, OrderEvent?>
 */
typealias OrderAggregateEventRepository = EventRepository<OrderCommand?, OrderEvent?>

/**
 * A convenient type alias for EventRepository<RestaurantCommand?, RestaurantEvent?>
 */
typealias RestaurantAggregateEventRepository = EventRepository<RestaurantCommand?, RestaurantEvent?>

/**
 * A convenient type alias for ActionPublisher<OrderCommand?>
 */
typealias OrderSagaManagerPublisher = ActionPublisher<OrderCommand?>

/**
 * A convenient type alias for ActionPublisher<RestaurantCommand?>
 */
typealias RestaurantSagaManagerPublisher = ActionPublisher<RestaurantCommand?>

val orderAggregate = eventSourcingAggregate(orderDecider, orderAggregateEventRepository)
val restaurantAggregate = eventSourcingAggregate(restaurantDecider, restaurantAggregateEventRepository)
val orderSagaManager = sagaManager(orderSaga, orderSagaManagerPublisher)
val restaurantSagaManager = sagaManager(restaurantSaga, restaurantSagaManagerPublisher)


/**
 * Start handling your Order commands!
 */
orderAggregate.handle(orderCommand)

/**
 * Start handling your Restaurant commands!
 */
restaurantAggregate.handle(restaurantCommand)

/**
 * Additionally, orderSagaManager is reacting on events from one `restaurantAggregate` and publishes command to the `orderAggregate`.
 */
val someFlowOfRestaurantEventsComingViaKafkaOrDB: Flow<RestaurantEvent>
someFlowOfRestaurantEventsComingViaKafkaOrDB.publishTo(orderSagaManager).collect {...}

/**
 * Additionally, restaurantSagaManager is reacting on events from one `orderAggregate` and publishes command to the `restaurantAggregate`.
 */
val someFlowOfOrderEventsComingViaKafkaOrDB: Flow<OrderEvent>
someFlowOfOrderEventsComingViaKafkaOrDB.publishTo(restaurantSagaManager).collect {...}
```

  </TabItem>
</Tabs>


**Materialized View** is a formalization of the event-stored/event-sourced system (View
Model) [mentioned previously](architecture.md#event-stored-or-state-stored-systems).

```kotlin
interface MaterializedView<S, E> : IView<S, E>, ViewStateRepository<E, S>
```

Materialized view is using/delegating a `View` (domain component) to handle events of type `E` and to maintain a state
of denormalized projection(s) as a result.

```kotlin
interface MaterializedView<S, E> : IView<S, E>, ViewStateRepository<E, S>

// Notice the `delegation pattern`
fun <S, E> materializedView(
    view: IView<S, E>,
    viewStateRepository: ViewStateRepository<E, S>,
): MaterializedView<S, E> =
    object : MaterializedView<S, E>, ViewStateRepository<E, S> by viewStateRepository, IView<S, E> by view {}

```

<Tabs groupId="style" queryString="style">

  <TabItem value="monolith" label="monolith">

*Example of a monolith scenario, in which Order and Restaurant views
are [combined](/domain/aggregating-the-behaviour.md?component-type=view) in one big view and then wrapped by one
materialized-view component:*

```kotlin
/**
 * A convenient type alias for View<OrderViewState?, OrderEvent?>
 */
typealias OrderView = View<OrderViewState?, OrderEvent?>

/**
 * A convenient type alias for View<RestaurantViewState?, RestaurantEvent?>
 */
typealias RestaurantView = View<RestaurantViewState?, RestaurantEvent?>

/**
 * A convenient type alias for ViewStateRepository<OrderEvent?, Pair<OrderViewState?, RestaurantViewState?>>
 */
typealias MaterializedViewStateRepository = ViewStateRepository<Event?, Pair<OrderViewState?, RestaurantViewState?>>


val materializedView = materializedView(orderView combine restaurantView, materializedViewStateRepository)


/**
 * Start handling all your events, and projecting them into denormalized state which is adequate for querying.
 */
materializedView.handle(event)
```

  </TabItem>
  <TabItem value="distributed" label="distributed">

*Example of a distributed scenario, in which Order and Restaurant views are wrapped by independent materialized-view
components:*

```kotlin
/**
 * A convenient type alias for View<OrderViewState?, OrderEvent?>
 */
typealias OrderView = View<OrderViewState?, OrderEvent?>

/**
 * A convenient type alias for View<RestaurantViewState?, RestaurantEvent?>
 */
typealias RestaurantView = View<RestaurantViewState?, RestaurantEvent?>

/**
 * A convenient type alias for ViewStateRepository<OrderEvent?, OrderViewState?>
 */
typealias OrderMaterializedViewStateRepository = ViewStateRepository<OrderEvent?, OrderViewState?>

/**
 * A convenient type alias for ViewStateRepository<RestaurantEvent?, RestaurantViewState?>
 */
typealias RestaurantMaterializedViewStateRepository = ViewStateRepository<RestaurantEvent?, RestaurantViewState?>

val orderMaterializedView = materializedView(orderView, orderMaterializedViewStateRepository)
val restaurantMaterializedView = materializedView(restaurantView, restaurantMaterializedViewStateRepository)


/**
 * Start handling your Order events, and projecting them into denormalized state which is adequate for querying.
 */
orderMaterializedView.handle(orderEvent)

/**
 * Start handling your Restaurant events, and projecting them into denormalized state which is adequate for querying.
 */
restaurantMaterializedView.handle(restauranEvent)
```

  </TabItem>
</Tabs>




  </TabItem>

  <TabItem value="state-stored" label="State-Stored">

State-stored systems are using single canonical model for writing and reading/presenting, by default.

![event-modeling-traditional-systems](/img/event-modeling-traditional-systems.png)

**State-stored Aggregate** is a formalization of the state-stored
system [mentioned previously](architecture.md#event-stored-or-state-stored-systems).

```kotlin
interface StateStoredAggregate<C, S, E> : IDecider<C, S, E>, StateRepository<C, S>
interface StateStoredOrchestratingAggregate<C, S, E> : IDecider<C, S, E>, StateRepository<C, S>, ISaga<E, C>

```

State-stored Aggregate is using/delegating a `Decider` to handle commands and produce new state. It belongs to the
Application layer. In order to
handle the command, aggregate needs to fetch the current state via `StateRepository.fetchState` function first, and then
delegate the command to the decider which can produce new state as a result. New state is then stored
via `StateRepository.save` suspending function.

The Delegation pattern has proven to be a good alternative to `implementation inheritance`, and Kotlin supports it
natively requiring zero boilerplate code. `stateStoredAggregate` and `stateStoredOrchestratingAggregate` functions are good example:

```kotlin
fun <C, S, E> stateStoredAggregate(
    decider: IDecider<C, S, E>,
    stateRepository: StateRepository<C, S>
): StateStoredAggregate<C, S, E> =
    object :
        StateStoredAggregate<C, S, E>,
        StateRepository<C, S> by stateRepository,
        IDecider<C, S, E> by decider {}

fun <C, S, E> stateStoredOrchestratingAggregate(
   decider: IDecider<C, S, E>,
   stateRepository: StateRepository<C, S>,
   saga: ISaga<E, C>
): StateStoredOrchestratingAggregate<C, S, E> =
   object : StateStoredOrchestratingAggregate<C, S, E>,
      StateRepository<C, S> by stateRepository,
      IDecider<C, S, E> by decider,
      ISaga<E, C> by saga {}
```

<Tabs groupId="style" queryString="style">

  <TabItem value="monolith" label="monolith">

*Example of a monolith scenario, in which Order and Restaurant deciders
are [combined/aggregated](/domain/aggregating-the-behaviour.md?component-type=decider) in one big decider and then
wrapped by one aggregate component:*

```kotlin
/**
 * A convenient type alias for Decider<OrderCommand?, Order?, OrderEvent?>
 */
typealias OrderDecider = Decider<OrderCommand?, Order?, OrderEvent?>

/**
 * A convenient type alias for Decider<RestaurantCommand?, Restaurant?, RestaurantEvent?>
 */
typealias RestaurantDecider = Decider<RestaurantCommand?, Restaurant?, RestaurantEvent?>

/**
 * A convenient type alias for StateRepository<Command?, Pair<Restaurant?,Order?>>
 */
typealias AggregateStateRepository = StateRepository<Command?, Pair<Restaurant?,Order?>>


val aggregate = stateStoredAggregate(orderDecider combine restaurantDecider, aggregateStateRepository)

/**
 * Start handling all your commands!
 */
aggregate.handle(orderCommand)
```

  </TabItem>

  <TabItem value="monolith-distributed" label="monolith distributed">

*Example of a monolith scenario, in which Order and Restaurant deciders
are [combined/aggregated](/domain/aggregating-the-behaviour.md?component-type=decider) in one big decider, additionally Order and Restaurant sagas are [combined](/domain/aggregating-the-behaviour.md?component-type=saga) into one orchestrating saga.
Decider and Saga are then wrapped by one aggregate component.
Saga is responsible to integrate deciders internally, and enable event of one decider to trigger command of another, automatically*

```kotlin
/**
 * A convenient type alias for Decider<OrderCommand?, Order?, OrderEvent?>
 */
typealias OrderDecider = Decider<OrderCommand?, Order?, OrderEvent?>

/**
 * A convenient type alias for Decider<RestaurantCommand?, Restaurant?, RestaurantEvent?>
 */
typealias RestaurantDecider = Decider<RestaurantCommand?, Restaurant?, RestaurantEvent?>

/**
 * A convenient type alias for Saga<RestaurantEvent?, OrderCommand?>
 */
typealias OrderSaga = Saga<RestaurantEvent?, OrderCommand>

/**
 * A convenient type alias for Saga<OrderEvent?, RestaurantCommand>
 */
typealias RestaurantSaga = Saga<OrderEvent?, RestaurantCommand>

/**
 * A convenient type alias for StateRepository<Command?, Pair<Restaurant?,Order?>>
 */
typealias AggregateStateRepository = StateRepository<Command?, Pair<Restaurant?,Order?>>


val aggregate = stateStoredOrchestratingAggregate(orderDecider combine restaurantDecider, aggregateStateRepository, orderSaga combine restaurantSaga)

/**
 * Start handling all your commands!
 */
aggregate.handle(orderCommand)
```

  </TabItem>

  <TabItem value="distributed" label="distributed">

*Example of a distributed scenario, in which Order and Restaurant deciders are wrapped by independent aggregate
components:*

```kotlin
/**
 * A convenient type alias for Decider<OrderCommand?, Order?, OrderEvent?>
 */
typealias OrderDecider = Decider<OrderCommand?, Order?, OrderEvent?>

/**
 * A convenient type alias for Decider<RestaurantCommand?, Restaurant?, RestaurantEvent?>
 */
typealias RestaurantDecider = Decider<RestaurantCommand?, Restaurant?, RestaurantEvent?>

/**
 * A convenient type alias for StateRepository<OrderCommand?, Order?>
 */
typealias OrderAggregateStateRepository = StateRepository<OrderCommand?, Order?>

/**
 * A convenient type alias for StateRepository<RestaurantCommand?, Restaurant?>
 */
typealias RestaurantAggregateStateRepository = StateRepository<RestaurantCommand?, Restaurant?>

val orderAggregate = stateStoredAggregate(orderDecider, orderAggregateStateRepository)
val restaurantAggregate = stateStoredAggregate(restaurantDecider, restaurantAggregateStateRepository)

/**
 * Start handling your commands of type OrderCommand!
 */
orderAggregate.handle(orderCommand)

/**
 * Start handling your commands of type RestaurantCommand!
 */
restaurantAggregate.handle(restaurantCommand)
```

  </TabItem>
</Tabs>


 </TabItem>
</Tabs>

## Application modules

The `application` modules are organized in hierarchy:

 - `application` - **base module** is declaring all application interfaces: aggregate, materialized-view, saga-manager.
 - `extensions` - **extension modules** are extending the base module by providing concrete implementation of the `handle` method as an extension function(s).
    - `application-vanilla` - is using plain/vanilla Kotlin to implement the application layer in order to load the state, orchestrate the execution of the logic and save new state.
    - `application-arrow` - is using [Arrow](https://arrow-kt.io/) and Kotlin to implement the application layer in order to load the state, orchestrate the execution of the logic and save new state - providing structured, predictable and efficient handling of errors (using `Either`).

:::info
The libraries are non-intrusive, and you can select any flavor, choose both (vanilla and/or arrow) or make your own extension.

You can use only domain library and model the orchestration (application library) on your own.
:::

An example (taken from FModel `application-vanilla` library):

<Tabs groupId="system-type" queryString="system-type">
  <TabItem value="event-stored" label="Event-Stored / Event-Sourced">


```kotlin
fun <C, S, E> EventSourcingAggregate<C, S, E>.handle(command: C): Flow<E> =
   command
      .fetchEvents()
      .computeNewEvents(command)
      .save()
```

</TabItem>
  <TabItem value="state-stored" label="State-Stored">

```kotlin
suspend fun <C, S, E> StateStoredAggregate.handle(command: C): S =
    command
        .fetchState()
        .computeNewState(command)
        .save()
```

</TabItem>
</Tabs>

Feel free to use these two extension modules, or create your own by using these two as a fine example.