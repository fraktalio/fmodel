# [fraktalio](https://fraktalio.com/) / **f`(`model`)`** - Functional and Reactive domain modeling - Multiplatform


![workflow](https://github.com/fraktalio/fmodel/actions/workflows/gradle-test-build-publish.yml/badge.svg) | [playground](https://fraktalio.com/blog/playground) | [blog](https://fraktalio.com/blog/)

When you’re developing an information system to automate the activities of the business, you are modeling the business.
The abstractions that you design, the behaviors that you implement, and the UI interactions that you build all reflect
the business — together, they constitute the model of the domain.

<iframe width="560" height="315" src="https://www.youtube.com/embed/U8NzcWV8b4Y" title="YouTube video player" frameborder="0" allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture" allowfullscreen></iframe>


![event-modeling](https://github.com/fraktalio/fmodel/raw/main/.assets/event-modeling.png)


## `IOR<Library, Inspiration>`

This project can be used as a library, or as an inspiration, or both. It provides just enough tactical Domain-Driven
Design patterns, optimised for Event Sourcing and CQRS.

- The `domain` model library is fully isolated from the application layer and API-related concerns. It represents a pure
  declaration of the program logic. It is written in [Kotlin](https://kotlinlang.org/) programming language, without
  additional
  dependencies. [![Maven Central - domain](https://img.shields.io/maven-central/v/com.fraktalio.fmodel/domain.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22com.fraktalio.fmodel%22%20AND%20a:%22domain%22)
- The `application` libraries orchestrates the execution of the logic by loading state, executing `domain` components
  and storing new state. It is written in [Kotlin](https://kotlinlang.org/) programming language. Two flavors (
  extensions of `Application` module) are available:
  [![Maven Central - application](https://img.shields.io/maven-central/v/com.fraktalio.fmodel/application.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22com.fraktalio.fmodel%22%20AND%20a:%22application%22)
    - `application-vanilla` is using plain/vanilla Kotlin to implement the application layer in order to load the state,
      orchestrate the execution of the logic and save new state.
    - `application-arrow` is using [Arrow](https://arrow-kt.io/) and Kotlin to implement the application layer in order
      to load the state, orchestrate the execution of the logic and save new state - managing errors much better (using
      Either).
      
![onion architecture image](https://github.com/fraktalio/fmodel/raw/main/.assets/onion.png)
      
The libraries are non-intrusive, and you can select any flavor, or choose both (`vanila` and `arrow`). You can use
only `domain` library and model the orchestration (`application` library) on your own. Or, you can simply be inspired by
this project :)

## Table of Contents

* [<strong>f(model)</strong> - Functional domain modeling](#fmodel---functional-domain-modeling)
    * [decide: (C, S) -&gt; Flow&lt;E&gt;](#decide-c-s---flowe)
    * [evolve: (S, E) -&gt; S](#evolve-s-e---s)
    * [Event-sourced or State-stored systems](#event-sourced-or-state-stored-systems)
    * [Decider](#decider)
        * [Decider extensions and functions](#decider-extensions-and-functions)
        * [Event-sourcing aggregate](#event-sourcing-aggregate)
        * [State-stored aggregate](#state-stored-aggregate)
    * [View](#view)
        * [View extensions and functions](#view-extensions-and-functions)
        * [Materialized View](#materialized-view)
    * [Saga](#saga)
        * [Saga extensions and functions](#saga-extensions-and-functions)
        * [Saga Manager](#saga-manager)
    * [Kotlin](#kotlin)
    * [Examples](#start-using-the-libraries)
    * [References and further reading](#references-and-further-reading)


## `decide: (C, S) -> Flow<E>`

On a higher level of abstraction, any information system is responsible for handling the intent (`Command`) and based on
the current `State`, produce new facts (`Events`):

- given the current `State/S` *on the input*,
- when `Command/C` is handled *on the input*,
- expect `flow` of new `Events/E` to be published/emitted *on the output*

## `evolve: (S, E) -> S`

The new state is always evolved out of the current state `S` and the current event `E`:

- given the current `State/S` *on the input*,
- when `Event/E` is handled *on the input*,
- expect new `State/S` to be published *on the output*

## Event-sourced or State-stored systems

- State-stored systems are traditional systems that are only storing the current State by overwriting the previous State
  in the storage.
- Event-sourced systems are storing the events in immutable storage by only appending.


Both types of systems can be designed by using only these two functions and three generic parameters:

- `decide: (C, S) -> Flow<E>`
- `evolve: (S, E) -> S`

![event sourced vs state stored](https://github.com/fraktalio/fmodel/raw/main/.assets/es-ss-system.png)

There is more to it! You can switch from one system type to another or have both flavors included within your systems
landscape.

  
Two functions are wrapped in a datatype class (algebraic data structure), which is generalized with three generic
parameters:

```kotlin
data class Decider<C, S, E>(
    val decide: (C, S) -> Flow<E>,
    val evolve: (S, E) -> S,
)
```

`Decider` is the most important datatype, but it is not the only one:
 
 - Domain layer
   - Decider
   - View
   - Saga
- Application layer (orchstrates the execution of the logic + effects)
   - Event-sourcing aggregate and State-stored aggregate (*uses the Decider*)
   - Materialized View (*uses the View*)
   - SagaManager (*uses the Saga*)


## Decider

`_Decider` is a datatype that represents the main decision-making algorithm. It belongs to the Domain layer. It has five
generic parameters `C`, `Si`, `So`, `Ei`, `Eo` , representing the type of the values that `_Decider` may contain or use.
`_Decider` can be specialized for any type `C` or `Si` or `So` or `Ei` or `Eo` because these types do not affect its
behavior. `_Decider` behaves the same for `C`=`Int` or `C`=`YourCustomType`, for example.

`_Decider` is a pure domain component.

- `C` - Command
- `Si` - input State
- `So` - output State
- `Ei` - input Event
- `Eo` - output Event

We make a difference between input and output types, and we are more general in this case. We can always specialize down
to the 3 generic parameters: `typealias Decider<C, S, E> = _Decider<C, S, S, E, E>`

```kotlin
data class _Decider<C, Si, So, Ei, Eo>(
    val decide: (C, Si) -> Flow<Eo>,
    val evolve: (Si, Ei) -> So,
    val initialState: So
) : I_Decider<C, Si, So, Ei, Eo>

typealias Decider<C, S, E> = _Decider<C, S, S, E, E>
typealias IDecider<C, S, E> = I_Decider<C, S, S, E, E>
```

Additionally, `initialState` of the Decider is introduced to gain more control over the initial state of the Decider.
Notice that `Decider` implements an interface `IDecider` to communicate the contract.

**Example:**

```kotlin
fun restaurantOrderDecider() = Decider<RestaurantOrderCommand?, RestaurantOrder?, RestaurantOrderEvent?>(
// Initial state of the Restaurant Order is `null`. It does not exist.
initialState = null,
// Exhaustive command handler(s): for each type of [RestaurantCommand] you are going to publish specific events/facts, as required by the current state/s of the [RestaurantOrder].
decide = { c, s ->
    when (c) {
        is CreateRestaurantOrderCommand ->
            // ** positive flow **
            if (s == null) flowOf(RestaurantOrderCreatedEvent(c.identifier, c.lineItems, c.restaurantIdentifier))
            // ** negative flow **
            else flowOf(RestaurantOrderRejectedEvent(c.identifier, "Restaurant order already exists"))
        is MarkRestaurantOrderAsPreparedCommand ->
            // ** positive flow **
            if ((s != null && CREATED == s.status)) flowOf(RestaurantOrderPreparedEvent(c.identifier))
            // ** negative flow **
            else flowOf(
                RestaurantOrderNotPreparedEvent(
                    c.identifier,
                    "Restaurant order does not exist or not in CREATED state"
                )
            )
        null -> emptyFlow() // We ignore the `null` command by emitting the empty flow. Only the Decider that can handle `null` command can be combined (Monoid) with other Deciders.
    }
},
// Exhaustive event-sourcing handler(s): for each event of type [RestaurantEvent] you are going to evolve from the current state/s of the [RestaurantOrder] to a new state of the [RestaurantOrder]
evolve = { s, e ->
    when (e) {
        is RestaurantOrderCreatedEvent -> RestaurantOrder(e.identifier, e.restaurantId, CREATED, e.lineItems)
        is RestaurantOrderPreparedEvent -> s?.copy(status = PREPARED)
        is RestaurantOrderErrorEvent -> s // Error events are not changing the state / We return current state instead.
        null -> s // Null events are not changing the state / We return current state instead. Only the Decider that can handle `null` event can be combined (Monoid) with other Deciders.
    }
}
)
```


![decider image](https://github.com/fraktalio/fmodel/raw/main/.assets/decider.png)

### Decider extensions and functions

#### Contravariant

- `Decider<C, Si, So, Ei, Eo>.mapLeftOnCommand(f: (Cn) -> C): Decider<Cn, Si, So, Ei, Eo>`

#### Profunctor (Contravariant and Covariant)

- `Decider<C, Si, So, Ei, Eo>.dimapOnEvent(
  fl: (Ein) -> Ei, fr: (Eo) -> Eon
  ): Decider<C, Si, So, Ein, Eon>`
- `Decider<C, Si, So, Ei, Eo>.mapLeftOnEvent(f: (Ein) -> Ei): Decider<C, Si, So, Ein, Eo>`
- `Decider<C, Si, So, Ei, Eo>.mapOnEvent(f: (Eo) -> Eon): Decider<C, Si, So, Ei, Eon>`
- `Decider<C, Si, So, Ei, Eo>.dimapOnState(
  fl: (Sin) -> Si, fr: (So) -> Son
  ): Decider<C, Sin, Son, Ei, Eo>`
- `Decider<C, Si, So, Ei, Eo>.mapLeftOnState(f: (Sin) -> Si): Decider<C, Sin, So, Ei, Eo>`
- `Decider<C, Si, So, Ei, Eo>.mapOnState(f: (So) -> Son): Decider<C, Si, Son, Ei, Eo>`

#### Applicative

- `rjustOnS(so: So): Decider<C, Si, So, Ei, Eo>`
- `Decider<C, Si, So, Ei, Eo>.applyOnState(ff: Decider<C, Si, (So) -> Son, Ei, Eo>): Decider<C, Si, Son, Ei, Eo>`
- `Decider<C, Si, So, Ei, Eo>.productOnState(fb: Decider<C, Si, Son, Ei, Eo>): Decider<C, Si, Pair<So, Son>, Ei, Eo>`

#### Monoid

- `Decider<in C?, in Si, out So, in Ei?, out Eo>.combine(
  y: Decider<in Cn?, in Sin, out Son, in Ein?, out Eon>
  ): Decider<C_SUPER, Pair<Si, Sin>, Pair<So, Son>, Ei_SUPER, Eo_SUPER>`

- with identity element `Decider<Nothing?, Unit, Nothing?>`

> A monoid is a type together with a binary operation (combine) over that type, satisfying associativity and having an identity/empty element.
> Associativity facilitates parallelization by giving us the freedom to break problems into chunks that can be computed in parallel.


We can now construct event-sourcing or/and state-storing aggregate by using the same `decider`.

### Event-sourcing aggregate

Event sourcing aggregate is
using a `Decider` to handle commands and produce events. It belongs to the Application layer. In order to
handle the command, aggregate needs to fetch the current state (represented as a list of events)
via `EventRepository.fetchEvents` function, and then delegate the command to the decider which can produce new events as
a result. Produced events are then stored via `EventRepository.save` suspending function.

![event sourced aggregate](https://github.com/fraktalio/fmodel/raw/main/.assets/es-aggregate.png)

`EventSourcingAggregate` extends `IDecider` (decision-making) and `EventRepository` (fetch-save) interfaces, clearly communicating that it is composed
out of these two behaviours.

```kotlin
interface EventSourcingAggregate<C, S, E> : IDecider<C, S, E>, EventRepository<C, E>
```

The Delegation pattern has proven to be a good alternative to `implementation inheritance`, and Kotlin supports it
natively requiring zero boilerplate code.
`eventSourcingAggregate` function is a good example:

```kotlin
fun <C, S, E> eventSourcingAggregate(
    decider: IDecider<C, S, E>,
    eventRepository: EventRepository<C, E>
): EventSourcingAggregate<C, S, E> =
    object :
        EventSourcingAggregate<C, S, E>,
        EventRepository<C, E> by eventRepository,
        IDecider<C, S, E> by decider {}
```

**Example:**

```kotlin
typealias RestaurantOrderAggregate = EventSourcingAggregate<RestaurantOrderCommand?, RestaurantOrder?, RestaurantOrderEvent?>

fun restaurantOrderAggregate(
    restaurantOrderDecider: RestaurantOrderDecider,
    eventRepository: EventRepository<RestaurantOrderCommand?, RestaurantOrderEvent?>
): RestaurantOrderAggregate = eventSourcingAggregate(
    decider = restaurantOrderDecider,
    eventRepository = eventRepository,
)
```

### State-stored aggregate

State stored aggregate is
using a `Decider` to handle commands and produce new state. It belongs to the Application layer. In order to
handle the command, aggregate needs to fetch the current state via `StateRepository.fetchState` function first, and then
delegate the command to the decider which can produce new state as a result. New state is then stored
via `StateRepository.save` suspending function.

![state storedaggregate](https://github.com/fraktalio/fmodel/raw/main/.assets/ss-aggregate.png)

`StateStoredAggregate` extends `IDecider` (decision-making) and `StateRepository` (fetch-save) interfaces, clearly communicating that it is composed
out of these two behaviours.


```kotlin
interface StateStoredAggregate<C, S, E> : IDecider<C, S, E>, StateRepository<C, S>
```

The Delegation pattern has proven to be a good alternative to `implementation inheritance`, and Kotlin supports it
natively requiring zero boilerplate code.
`stateStoredAggregate` function is a good example:

```kotlin
fun <C, S, E> stateStoredAggregate(
    decider: IDecider<C, S, E>,
    stateRepository: StateRepository<C, S>
): StateStoredAggregate<C, S, E> =
    object :
        StateStoredAggregate<C, S, E>,
        StateRepository<C, S> by stateRepository,
        IDecider<C, S, E> by decider {}
```
**Example:**

```kotlin
typealias RestaurantOrderAggregate = StateStoredAggregate<RestaurantOrderCommand?, RestaurantOrder?, RestaurantOrderEvent?>

fun restaurantOrderAggregate(
    restaurantOrderDecider: RestaurantOrderDecider,
    aggregateRepository: StateRepository<RestaurantOrderCommand?, RestaurantOrder?>
): RestaurantOrderAggregate = stateStoredAggregate(
    decider = restaurantOrderDecider,
    stateRepository = aggregateRepository
)
```

*The logic is orchestrated on the application layer. The components/functions are composed in different ways to support variety of requirements.*

![aggregates-application-layer](https://github.com/fraktalio/fmodel/raw/main/.assets/aggregates.png)

## View

`_View`  is a datatype that represents the event handling algorithm, responsible for translating the events into
denormalized state, which is more adequate for querying. It belongs to the Domain layer. It is usually used to create
the view/query side of the CQRS pattern. Obviously, the command side of the CQRS is usually event-sourced aggregate.

It has three generic parameters `Si`, `So`, `E`, representing the type of the values that `_View` may contain or use.
`_View` can be specialized for any type of `Si`, `So`, `E` because these types do not affect its behavior.
`_View` behaves the same for `E`=`Int` or `E`=`YourCustomType`, for example.

`_View` is a pure domain component.

- `Si` - input State
- `So` - output State
- `E`  - Event

We make a difference between input and output types, and we are more general in this case. We can always specialize down
to the 2 generic parameters: `typealias View<S, E> = _View<S, S, E>`

```kotlin
data class _View<Si, So, E>(
    val evolve: (Si, E) -> So,
    val initialState: So,
) : I_View<Si, So, E>

typealias View<S, E> = _View<S, S, E>
typealias IView<S, E> = I_View<S, S, E>
```

Notice that `View` implements an interface `IView` to communicate the contract.

**Example:**

```kotlin
fun restaurantOrderView() = View<RestaurantOrderViewState?, RestaurantOrderEvent?>(
// Initial state of the [RestaurantOrderViewState] is `null`. It does not exist.
initialState = null,
// Exhaustive event-sourcing handling part: for each event of type [RestaurantOrderEvent] you are going to evolve from the current state/s of the [RestaurantOrderViewState] to a new state of the [RestaurantOrderViewState].
evolve = { s, e ->
    when (e) {
        is RestaurantOrderCreatedEvent -> RestaurantOrderViewState(
            e.identifier,
            e.restaurantId,
            CREATED,
            e.lineItems
        )
        is RestaurantOrderPreparedEvent -> s?.copy(status = PREPARED)
        is RestaurantOrderErrorEvent -> s // We ignore the `error` event by returning current State/s.
        null -> s // We ignore the `null` event by returning current State/s. Only the View that can handle `null` event can be combined (Monoid) with other Views.

    }
}
)
```

![view image](https://github.com/fraktalio/fmodel/raw/main/.assets/view.png)

### View extensions and functions

#### Contravariant

- `View<Si, So, E>.mapLeftOnEvent(f: (En) -> E): View<Si, So, En>`

#### Profunctor (Contravariant and Covariant)

- `View<Si, So, E>.dimapOnState(
  fl: (Sin) -> Si, fr: (So) -> Son
  ): View<Sin, Son, E>`
- `View<Si, So, E>.mapLeftOnState(f: (Sin) -> Si): View<Sin, So, E>`
- `View<Si, So, E>.mapOnState(f: (So) -> Son): View<Si, Son, E>`

#### Applicative

- `View<Si, So, E>.applyOnState(ff: View<Si, (So) -> Son, E>): View<Si, Son, E>`
- `justOnState(so: So): View<Si, So, E>`

#### Monoid

- `View<in Si, out So, in E?>.combine(y: View<in Si2, out So2, in E2?>): View<Pair<Si, Si2>, Pair<So, So2>, E_SUPER>`
- with identity element `View<Unit, Nothing?>`

> A monoid is a type together with a binary operation (combine) over that type, satisfying associativity and having an identity/empty element.
> Associativity facilitates parallelization by giving us the freedom to break problems into chunks that can be computed in parallel.

We can now construct `materialized` view by using this `view`.

### Materialized View

A Materialized view is
using a `View` to handle events of type `E` and to maintain a state of denormalized projection(s) as a
result. Essentially, it represents the query/view side of the CQRS pattern. It belongs to the Application layer.

In order to handle the event, materialized view needs to fetch the current state via `ViewStateRepository.fetchState`
suspending function first, and then delegate the event to the view, which can produce new state as a result. New state
is then stored via `ViewStateRepository.save` suspending function.

`MaterializedView` extends `IView` (decision-making) and `ViewStateRepository` (fetch-save) interfaces, clearly communicating that it is composed out
of these two behaviours.

```kotlin
interface MaterializedView<S, E> : IView<S, E>, ViewStateRepository<E, S>
```

The Delegation pattern has proven to be a good alternative to `implementation inheritance`, and Kotlin supports it
natively requiring zero boilerplate code.
`materializedView` function is a good example:

```kotlin
fun <S, E> materializedView(
    view: IView<S, E>,
    viewStateRepository: ViewStateRepository<E, S>,
): MaterializedView<S, E> =
    object : MaterializedView<S, E>, ViewStateRepository<E, S> by viewStateRepository, IView<S, E> by view {}
```

**Example:**
```kotlin
typealias RestaurantOrderMaterializedView = MaterializedView<RestaurantOrderViewState?, RestaurantOrderEvent?>

fun restaurantOrderMaterializedView(
    restaurantOrderView: RestaurantOrderView,
    viewStateRepository: ViewStateRepository<RestaurantOrderEvent?, RestaurantOrderViewState?>
): RestaurantOrderMaterializedView = materializedView(
    view = restaurantOrderView,
    viewStateRepository = viewStateRepository
)
```

*The logic is orchestrated on the application layer. The components/functions are composed in different ways to support variety of requirements.*

![materialized-views-application-layer](https://github.com/fraktalio/fmodel/raw/main/.assets/mviews.png)

## Saga

`_Saga` is a datatype that represents the central point of control, deciding what to execute next (`A`). It is
responsible for mapping different events from many aggregates into action results `AR` that the `_Saga` then can use to
calculate the next actions `A` to be mapped to commands of other aggregates.

`_Saga` is stateless, it does not maintain the state.

It has two generic parameters `AR`, `A`, representing the type of the values that `_Saga` may contain or use.
`_Saga` can be specialized for any type of `AR`, `A` because these types do not affect its behavior.
`_Saga` behaves the same for `AR`=`Int` or `AR`=`YourCustomType`, for example.

`_Saga` is a pure domain component.

- `AR` - Action Result
- `A`  - Action

```kotlin
data class _Saga<AR, A>(
    val react: (AR) -> Flow<A>
) : I_Saga<AR, A>

typealias Saga<AR, A> = _Saga<AR, A>
typealias ISaga<AR, A> = I_Saga<AR, A>
```

Notice that `Saga` implements an interface `ISaga` to communicate the contract.

**Example:**
```kotlin
fun restaurantOrderSaga() = Saga<RestaurantEvent?, RestaurantOrderCommand>(
    react = { e ->
        when (e) {
            is RestaurantOrderPlacedAtRestaurantEvent -> flowOf(
                CreateRestaurantOrderCommand(
                    e.restaurantOrderId,
                    e.identifier,
                    e.lineItems
                )
            )
            is RestaurantCreatedEvent -> emptyFlow() // We choose to ignore this event, in our case.
            is RestaurantMenuActivatedEvent -> emptyFlow() // We choose to ignore this event, in our case.
            is RestaurantMenuChangedEvent -> emptyFlow() // We choose to ignore this event, in our case.
            is RestaurantMenuPassivatedEvent -> emptyFlow() // We choose to ignore this event, in our case.
            is RestaurantErrorEvent -> emptyFlow() // We choose to ignore this event, in our case.
            null -> emptyFlow() // We ignore the `null` event by returning the empty flow of commands. Only the Saga that can handle `null` event/action-result can be combined (Monoid) with other Sagas.
        }
    }
)

fun restaurantSaga() = Saga<RestaurantOrderEvent?, RestaurantCommand>(
    react = { e ->
        when (e) {
            //TODO evolve the example ;), it does not do much at the moment.
            is RestaurantOrderCreatedEvent -> emptyFlow()
            is RestaurantOrderPreparedEvent -> emptyFlow()
            is RestaurantOrderErrorEvent -> emptyFlow()
            null -> emptyFlow() // We ignore the `null` event by returning the empty flow of commands. Only the Saga that can handle `null` event/action-result can be combined (Monoid) with other Sagas.
        }
    }
)
```

![saga image](https://github.com/fraktalio/fmodel/raw/main/.assets/saga.png)

### Saga extensions and functions

#### Contravariant

- `Saga<AR, A>.mapLeftOnActionResult(f: (ARn) -> AR): Saga<ARn, A>`

#### Covariant

- `Saga<AR, A>.mapOnAction(f: (A) -> An): Saga<AR, An>`

#### Monoid

- `Saga<in AR?, out A>.combine(y: _Saga<in ARn?, out An>): Saga<AR_SUPER, A_SUPER>`
- with identity element `Saga<Nothing?, Nothing?>`

We can now construct `Saga Manager` by using this `saga`.

### Saga Manager

Saga manager is a stateless process
orchestrator. It is reacting on Action Results of type `AR` and produces new actions `A` based on them.

Saga manager is using a `Saga` to react on Action Results of type `AR` and produce new actions `A` which are
going to be published via `ActionPublisher.publish` suspending function.

It belongs to the Application layer.

`SagaManager` extends `ISaga` (decision-making) and `ActionPublisher` (publishing) interfaces, clearly communicating that it is composed out of these
two behaviours.

```kotlin
interface SagaManager<AR, A> : ISaga<AR, A>, ActionPublisher<A>
```

The Delegation pattern has proven to be a good alternative to `implementation inheritance`, and Kotlin supports it
natively requiring zero boilerplate code.
`sagaManager` function is a good example:

```kotlin
fun <AR, A> sagaManager(
    saga: ISaga<AR, A>,
    actionPublisher: ActionPublisher<A>
): SagaManager<AR, A> =
    object : SagaManager<AR, A>, ActionPublisher<A> by actionPublisher, ISaga<AR, A> by saga {}
```

**Example:**
```kotlin
typealias OrderRestaurantSagaManager = SagaManager<Event?, Command>

fun sagaManager(
    restaurantOrderSaga: RestaurantOrderSaga,
    restaurantSaga: RestaurantSaga,
    actionPublisher: ActionPublisher<Command>
): OrderRestaurantSagaManager = sagaManager(
    // Combining individual choreography Sagas into one orchestrating Saga.
    saga = restaurantOrderSaga.combine(restaurantSaga),
    // How and where do you want to publish new commands.
    actionPublisher = actionPublisher
)
```

### Experimental features

#### Actors (only on [JVM](https://github.com/fraktalio/fmodel/tree/main/application-vanilla/src/jvmMain/kotlin/com/fraktalio/fmodel/application))

Coroutines can be executed parallelly. It presents all the usual parallelism problems. The main problem being
synchronization of access to shared mutable
state. [Actors](https://kotlinlang.org/docs/shared-mutable-state-and-concurrency.html#actors) to the rescue!

Application layer components are handling the messages, delegating the computation to domain components and storing the new state (shared and mutable).
The handling process can be executed concurrently.


![kotlin actors](https://github.com/fraktalio/fmodel/raw/main/.assets/kotlin-actors.png)

[Dive into the implementation ...](https://github.com/fraktalio/fmodel/tree/main/application-vanilla/src/jvmMain/kotlin/com/fraktalio/fmodel/application)



> [Actors](https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines.channels/actor.html) are marked as @ObsoleteCoroutinesApi by Kotlin at the moment.


## Kotlin

*"Kotlin has both object-oriented and functional constructs. You can use it in both OO and FP styles, or mix elements of
the two. With first-class support for features such as higher-order functions, function types and lambdas, Kotlin is a
great choice if you’re doing or exploring functional programming."*

## Start using the libraries

All `fmodel` components/libraries are released to [Maven Central](https://repo1.maven.org/maven2/com/fraktalio/fmodel/)

### Maven coordinates

```
 <dependency>
    <groupId>com.fraktalio.fmodel</groupId>
    <artifactId>domain</artifactId>
    <version>3.2.0</version>
 </dependency>

 <dependency>
    <groupId>com.fraktalio.fmodel</groupId>
    <artifactId>application-vanilla</artifactId>
    <version>3.2.0</version>
 </dependency>
 
 <dependency>
    <groupId>com.fraktalio.fmodel</groupId>
    <artifactId>application-arrow</artifactId>
    <version>3.2.0</version>
 </dependency>
```

### Examples

- Learn by example on the [playground](https://fraktalio.com/blog/playground)
- Read the [blog](https://fraktalio.com/blog/)
- Check the [demos](https://github.com/fraktalio/fmodel-demos)



## Credits

Special credits to `Jérémie Chassaing` for sharing his [research](https://www.youtube.com/watch?v=kgYGMVDHQHs)
and `Adam Dymitruk` for hosting the meetup.

---
Created with love by [Fraktalio](https://fraktalio.com/)
