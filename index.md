# [fraktalio](https://fraktalio.com/) / **f`(`model`)`** - Functional and Reactive domain modeling


![workflow-github](https://github.com/fraktalio/fmodel/actions/workflows/maven-test-build-publish-to-github.yml/badge.svg)
![workflow-maven-central](https://github.com/fraktalio/fmodel/actions/workflows/maven-test-build-publish-to-maven-central.yml/badge.svg)

When you’re developing an information system to automate the activities of the business, you are modeling the business.
The abstractions that you design, the behaviors that you implement, and the UI interactions that you build all reflect
the business — together, they constitute the model of the domain.

## `IOR<Library, Inspiration>`

This project can be used as a library, or as an inspiration, or both. It provides just enough tactical Domain-Driven
Design patterns, optimised for Event Sourcing and CQRS.

- The `domain` model library is fully isolated from the application layer and API-related concerns. It represents a pure
  declaration of the program logic. It is written in [Kotlin](https://kotlinlang.org/) programming language, without
  additional
  dependencies. [![Maven Central - domain](https://img.shields.io/maven-central/v/com.fraktalio.fmodel/domain.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22com.fraktalio.fmodel%22%20AND%20a:%22domain%22)
- The `application` library orchestrates the execution of the logic by loading state, executing `domain` components and
  storing new state. It is written in [Kotlin](https://kotlinlang.org/) programming language,
  with [Arrow](https://arrow-kt.io/) as additional
  dependency. [![Maven Central - application](https://img.shields.io/maven-central/v/com.fraktalio.fmodel/application.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22com.fraktalio.fmodel%22%20AND%20a:%22application%22)

## Table of Contents

* [<strong>f(model)</strong> - Functional domain modeling](#fmodel---functional-domain-modeling)
    * [Abstraction and generalization](#abstraction-and-generalization)
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

## Abstraction and generalization

Abstractions can hide irrelevant details and use names to reference objects. It emphasizes what an object is or does
rather than how it is represented or how it works.

Generalization reduces complexity by replacing multiple entities which perform similar functions with a single
construct.

Abstraction and generalization are often used together. Abstracts are generalized through parameterization to provide
more excellent utility.

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

### A statement:

Both types of systems can be designed by using only these two functions and three generic parameters:

- `decide: (C, S) -> Flow<E>`
- `evolve: (S, E) -> S`

![event sourced vs state stored](https://github.com/fraktalio/fmodel/raw/main/.assets/es-ss-system.png)

There is more to it! You can switch from one system type to another or have both flavors included within your systems
landscape.  

### A proof:

We can fold/recreate the new state out of the flow of events by using `evolve` function `(S, E) -> S` and providing the
initialState of type S as a starting point.

- `Flow<E>.fold(initialState: S, ((S, E) -> S)): S`

Essentially, this `fold` is a function that is mapping a flow of Events to the State:

- `(Flow<E>) -> S`

We can now use this function `(Flow<E>) -> S` to:

- contra-map our `decide` function (`(C, S) -> Flow<E>`) over `S` type to: `(C, Flow<E>) -> Flow<E>`  - **
  this is an event-sourced system**
- or to map our `decide` function (`(C, S) -> Flow<E>`) over `E` type to: `(C, S) -> S` - **this is a state-stored
  system**

We can verify that we can design any information system (event-sourced or/and state-stored) in this way by using these
two functions wrapped in a datatype class (algebraic data structure), which is generalized with three generic
parameters:

```kotlin
data class Decider<C, S, E>(
    val decide: (C, S) -> Flow<E>,
    val evolve: (S, E) -> S,
)
```

`Decider` is the most important datatype, but it is not the only one. There are others:

![onion architecture image](https://github.com/fraktalio/fmodel/raw/main/.assets/onion.png)

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
)

typealias Decider<C, S, E> = _Decider<C, S, S, E, E>
```

Additionally, `initialState` of the Decider is introduced to gain more control over the initial state of the Decider.

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

[Event sourcing aggregate](application/src/main/kotlin/com/fraktalio/fmodel/application/EventSourcingAggregate.kt) is
using a `Decider` to handle commands and produce events. It belongs to the Application layer. In order to
handle the command, aggregate needs to fetch the current state (represented as a list of events)
via `EventRepository.fetchEvents` function, and then delegate the command to the decider which can produce new events as
a result. Produced events are then stored via `EventRepository.save` suspending function.

![event sourced aggregate](https://github.com/fraktalio/fmodel/raw/main/.assets/es-aggregate.png)

`EventSourcingAggregate` extends an interface `EventRepository`.

A convenient extension factory function is available:


```kotlin
fun <C, S, E> eventSourcingAggregate(
    decider: Decider<C, S, E>,
    eventRepository: EventRepository<C, E>
): EventSourcingAggregate<C, S, E> =
    object : EventSourcingAggregate<C, S, E>, EventRepository<C, E> by eventRepository {
        override val decider = decider
    }
```


### State-stored aggregate

[State stored aggregate](application/src/main/kotlin/com/fraktalio/fmodel/application/StateStoredAggregate.kt) is
using a `Decider` to handle commands and produce new state. It belongs to the Application layer. In order to
handle the command, aggregate needs to fetch the current state via `StateRepository.fetchState` function first, and then
delegate the command to the decider which can produce new state as a result. New state is then stored
via `StateRepository.save` suspending function.

![state storedaggregate](https://github.com/fraktalio/fmodel/raw/main/.assets/ss-aggregate.png)

`StateStoredAggregate` extends an interface `StateRepository`.


A convenient extension factory function is available:

```kotlin
fun <C, S, E> stateStoredAggregate(
    decider: Decider<C, S, E>,
    stateRepository: StateRepository<C, S>
): StateStoredAggregate<C, S, E> =
    object : StateStoredAggregate<C, S, E>, StateRepository<C, S> by stateRepository {
        override val decider = decider
    }
```


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
)

typealias View<S, E> = _View<S, S, E>
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

A [Materialized view](application/src/main/kotlin/com/fraktalio/fmodel/application/MaterializedView.kt) is
using a `View` to handle events of type `E` and to maintain a state of denormalized projection(s) as a
result. Essentially, it represents the query/view side of the CQRS pattern. It belongs to the Application layer.

In order to handle the event, materialized view needs to fetch the current state via `ViewStateRepository.fetchState`
suspending function first, and then delegate the event to the view, which can produce new state as a result. New state
is then stored via `ViewStateRepository.save` suspending function.

`MaterializedView` extends an interface `ViewStateRepository`.

A convenient extension factory function is available:

```kotlin
fun <S, E> materializedView(
    view: View<S, E>,
    viewStateRepository: ViewStateRepository<E, S>,
): MaterializedView<S, E> =
    object : MaterializedView<S, E>, ViewStateRepository<E, S> by viewStateRepository {
        override val view = view
    }
```

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
)

typealias Saga<AR, A> = _Saga<AR, A>
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

[Saga manager](application/src/main/kotlin/com/fraktalio/fmodel/application/SagaManager.kt) is a stateless process
orchestrator. It is reacting on Action Results of type `AR` and produces new actions `A` based on them.

Saga manager is using a `Saga` to react on Action Results of type `AR` and produce new actions `A` which are
going to be published via `ActionPublisher.publish` suspending function.

It belongs to the Application layer.

`SagaManager` extends an interface `ActionPublisher`.

A convenient extension factory function is available:

```kotlin
fun <AR, A> sagaManager(
    saga: Saga<AR, A>,
    actionPublisher: ActionPublisher<A>
): SagaManager<AR, A> =
    object : SagaManager<AR, A>, ActionPublisher<A> by actionPublisher {
        override val saga = saga
    }
```

> The Delegation pattern has proven to be a good alternative to implementation inheritance, and Kotlin supports it natively requiring zero boilerplate code.


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
    <version>2.1.1</version>
</dependency>

<dependency>
    <groupId>com.fraktalio.fmodel</groupId>
    <artifactId>application</artifactId>
    <version>2.1.1</version>
</dependency>
```

### Examples

 - Envision how information system will look like and behave like by modeling the flow of information - [event modeling](https://eventmodeling.org/posts/what-is-event-modeling/)
 - The result is a blueprint of the overall solution

![event-modeling](https://github.com/fraktalio/fmodel/raw/main/.assets/event-modeling.png)

 - Translate the blueprint into the [source code](https://github.com/fraktalio/fmodel-demos)

[https://github.com/fraktalio/fmodel-demos](https://github.com/fraktalio/fmodel-demos)


<iframe width="560" height="315" src="https://www.youtube.com/embed/U8NzcWV8b4Y" title="YouTube video player" frameborder="0" allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture" allowfullscreen></iframe>

## Credits

Special credits to `Jérémie Chassaing` for sharing his [research](https://www.youtube.com/watch?v=kgYGMVDHQHs)
and `Adam Dymitruk` for hosting the meetup.

---
Created with love by [Fraktalio](https://fraktalio.com/)
