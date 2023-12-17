---
sidebar_position: 1
---

import Tabs from '@theme/Tabs';
import TabItem from '@theme/TabItem';

# Architecture

We learned how to model the domain effectively by using types and functions.
We used `decider`, `view`, and `saga` components from FModel library to model the behavior using pure functions.

- These components do not care about storing or fetching the data.
- They do not produce any side effects of this kind.
- They represent pure computation / pure logics of a program.

![domain components](/img/domain-components.png)

Functional Programming emphasizes separating the pure logic of a program (algebras) and the runtime used to run it.

:::info
Flagging a computation as `suspend` enforces a calling context, meaning the compiler can ensure that we can’t call the
effect from anywhere other than an environment prepared to run suspended effects. That will be another suspended
function or a Coroutine. This effectively means we’re decoupling the pure declaration of our program logic (frequently
called algebras in the functional world) from the runtime. And therefore, the runtime has the chance to see the big
picture of our program and decide how to run and optimize it.
:::

## The Application layer

The logic execution will be orchestrated by the outside components (application components) that use the domain
components (`decider`, `view`, and `saga`) to do the computations. These components will be responsible for fetching and
saving the data (suspended effects).

![onion architecture image](/img/onion.png)

The arrows in the image show the direction of the dependency.
**Notice that all dependencies point inwards and that Domain does not depend on anybody or anything.**

Pushing these decisions from the core domain model is very valuable.
Being able to postpone them is a sign of good architecture.

:::info
This architectural style is known as `Functional Core, Imperative Shell`. A bit simplified, it is characterized by two key architectural attributes:

- There is a core with the core business logic, and a shell that handles interactions with the outside world, such as persisting data in databases or providing UIs to interact with end users.
- The shell can call the core, but the core cannot call the shell and the core is even unaware of the existence of the shell. This is also known as the Dependency Rule (see, for example, Clean Architecture to learn more about this rule).

It is similar to Hexagonal Architecture, Ports and Adapters, Clean Architecture, Onion Architecture which have these two attributes in common.
:::

## Event-sourced or State-stored systems

The domain model is explicitly modeling `events` and `state,` and this opens some interesting options:

- fetch `events`, execute domain components/compute, and store new `events` by appending to the previous events in the
  storage / **event-sourced systems** / **event-stored systems**
- fetch `state`, execute domain components/compute, and store new `state` by overwriting the previous state in the
  storage / traditional / **state-stored systems**

![event-stored or state-stored](/img/es-ss.png)

- Event-Sourced systems are storing the `events` in immutable storage by only appending.
- State-Stored systems are traditional systems that are only storing the current `state` by overwriting the previous `state` in the storage.

It is important to realize that your core domain logic does not have to change in order to transit from one flavor to another!

Fmodel exposes couple of types of repository interfaces/ports within the `application` module to support these two flavors.

:::info
- State-stored systems are using single canonical model for writing and reading/presenting, by default.
- Event-stored/Event_sourced systems are split to command and view/query models, by default.

Fmodel is promoting robust event-driven systems only in case of Event-Sourced scenario.

In case of State-Stored scenario, you are limited to Decider and StateStoredAggregate components only. View, MaterializedView, SagaManager components are not available/useful.
:::

<Tabs groupId="system-type" queryString="system-type">
  <TabItem value="event-stored" label="Event-Stored / Event-Sourced">

- `EventRepository` - responsible for storing and fetching the events of deciders
- `ViewStateRepository` - responsible for storing and fetching the state of materialized views
- `ActionPublisher` - responsible for publishing new actions/commands


  </TabItem>
  <TabItem value="state-stored" label="State-Stored">

- `StateRepository` - responsible for storing and fetching the state of deciders


 </TabItem>
</Tabs>


You can now compose these repository interfaces with the domain components (`decider`) in order to implement any of
these options (event-stored or state-stored system):

<Tabs groupId="system-type" queryString="system-type">
  <TabItem value="event-stored" label="Event-Stored / Event-Sourced">

```kotlin
val events = EventRepository.fetchEvents(command)

val state = events.fold(decider.initialState, decider.evolve)
var newEvents = decider.decide(command, state)

EventRepository.save(newEvents)
```

An event-stored system is fetching the events by using the `fetchEvents` method from the `event repository`.
It then evolves the state based on the fetched events and delegates the state and the command to the `decider` component
which will produce new events by executing the `decide` function.
Finally, the events will be stored via the `save` function from the `event repository`.

![event-modeling-event-driven-systems](/img/event-modeling-event-driven-systems.png)

:::note
The implementation of the `event repository` is not part of the `Application` layer. It is delegated to the outside
Infrastructure/Adapter layers.
:::

With event-stored approach we are effectively splitting the domain model into

- command model / for writing
- view/query model / for querying

States of both, command model/yellow and view model/green, are evolved out of the same events.
This is making a huge difference, as now you have multiple view models independently serving every page/step in the flow
with the data it requires, making these steps decoupled.

We are not limited to use a single canonical model for writing and reading/presenting!

#### View model

The ViewStateRepository can be composed with the `view` domain component:

```kotlin
val state = ViewStateRepository.fetchState(event)

val newState = view.evolve(state, event)

ViewStateRepository.save(newState)
```

A event-stored system is fetching the current view state by using the `fetchState` method from
the `view state repository`.
Finally, the event will fold to the new state by using the `view` `evolve` function, and the new state will be stored
via the `save` function from the `view state repository`.

:::note
The implementation of the `view state repository` is not part of the `Application` layer. It is delegated to the outside
Infrastructure/Adapter layers.
:::

  </TabItem>
  <TabItem value="state-stored" label="State-Stored">

```kotlin
val state = StateRepository.fetchState(command)

val events = decider.decide(command, state)
val newState = events.fold(state, decider.evolve)

StateRepository.save(newState)
```

A state-stored system is fetching the current state by using the `fetchState` method from the `state repository`.
It then delegates the command and the current state to the `decider` which will produce a new list of events by
executing the `decide` function.
Finally, the list of events will fold to the new state by using the `decider` `evolve` function, and the new state will
be stored via the `save` function from the `state repository`.

![event-modeling-traditional-systems](/img/event-modeling-traditional-systems.png)

:::note
The implementation of the `state repository` is not part of the `Application` layer. It is delegated to the outside
Infrastructure/Adapter layers.
:::

State-stored systems are using single canonical model for writing and reading/presenting, by default.
The single state is represented with the yellow color on the image.


  </TabItem>
</Tabs>

**Fmodel offers application interfaces/components which are actually composed out of side-effects (repository interfaces) and core domain logic (decider, view, saga),
providing a default implementation and formalizing the concepts we just described:**

<Tabs groupId="system-type" queryString="system-type">
  <TabItem value="event-stored" label="Event-Stored / Event-Sourced">

- `EventSourcingAggregate`
- `MaterializedView`
- `SagaManager`



  </TabItem>
  <TabItem value="state-stored" label="State-Stored">

- `StateStoredAggregate`


 </TabItem>
</Tabs>
