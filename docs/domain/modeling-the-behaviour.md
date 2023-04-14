---
sidebar_position: 3
---

import Tabs from '@theme/Tabs';
import TabItem from '@theme/TabItem';

# Modeling the Behaviour

- algebraic data types form the `structure` of our entities (commands, state, and events)
- functions/lambda offers the algebra of manipulating the entities in a compositional manner, effectively modeling
  the `behavior.`

This leads to modularity in design and a clear separation of the entity’s structure and functions/`behaviour` of the
entity.

:::info
Kotlin functions are first-class, which means they can be stored in variables and data structures, and can be passed as
arguments to and returned from other higher-order functions. Kotlin
uses [function types](https://kotlinlang.org/docs/lambdas.html#function-types), such as `(Int) -> String`, for
declarations that deal with functions.
:::

Fmodel offers generic and abstract components to specialize in for your specific case/expected behavior:

- Decider
- View
- Saga

## Decider

The decider is a data type that represents the main decision-making algorithm.

- `initialState` - A starting point / An initial state
- `decide` (Exhaustive / pattern matching command handler) - A function/lambda that takes command and input state as
  parameters, and returns/emits the flow of output events
- `evolve` (Exhaustive / pattern matching event-sourcing handler) - A function/lambda that takes input state and input
  event as parameters, and returns the output/new state

![decider image](/img/decider-kotlin.png)

<Tabs groupId="concept" queryString="concept">
  <TabItem value="restaurant" label="Restaurant">

```kotlin
typealias RestaurantDecider = Decider<RestaurantCommand?, Restaurant?, RestaurantEvent?>

fun restaurantDecider() = RestaurantDecider(
    initialState = null,
    decide = { c, s ->
        when (c) {
            is CreateRestaurantCommand ->
                if (s == null) flowOf(RestaurantCreatedEvent(c.identifier, c.name, c.menu))
                else flowOf(
                    RestaurantNotCreatedEvent(
                        c.identifier,
                        c.name,
                        c.menu,
                        Reason("Restaurant already exists"),
                        true
                    )
                )

            is ChangeRestaurantMenuCommand ->
                if (s == null) flowOf(
                    RestaurantMenuNotChangedEvent(
                        c.identifier,
                        c.menu,
                        Reason("Restaurant does not exist"),
                    )
                )
                else flowOf(RestaurantMenuChangedEvent(c.identifier, c.menu))

            is PlaceOrderCommand ->
                if (s == null) flowOf(
                    OrderNotPlacedAtRestaurantEvent(
                        c.identifier,
                        c.lineItems,
                        c.orderIdentifier,
                        Reason("Restaurant does not exist"),
                    )
                )
                else if (!s.isValid(c)) flowOf(
                    OrderRejectedByRestaurantEvent(
                        c.identifier,
                        c.orderIdentifier,
                        Reason("Not on the menu"),
                    )
                )
                else flowOf(
                    OrderPlacedAtRestaurantEvent(c.identifier, c.lineItems, c.orderIdentifier)
                )

            null -> emptyFlow() // We ignore the `null` command by emitting the empty flow. Only the Decider that can handle `null` command can be combined (Monoid) with other Deciders.
        }
    },
    evolve = { s, e ->
        when (e) {
            is RestaurantCreatedEvent -> Restaurant(e.identifier, e.name, e.menu)
            is RestaurantMenuChangedEvent -> s?.copy(menu = e.menu)
            is OrderPlacedAtRestaurantEvent -> s
            is RestaurantErrorEvent -> s // Error events are not changing the state in our/this case.
            null -> s // Null events are not changing the state / We return current state instead. Only the Decider that can handle `null` event can be combined (Monoid) with other Deciders.
        }
    }
)

private fun Restaurant.isValid(command: PlaceOrderCommand): Boolean =
    (menu.menuItems.stream().map { mi -> mi.menuItemId }.collect(Collectors.toList())
        .containsAll(command.lineItems.stream().map { li -> li.menuItemId }.collect(Collectors.toList())))
```

:::note
Notice how the state of the `Restaurant` [is modeled as a data class](structuring-the-data.md#state)
:::

  </TabItem>
  <TabItem value="order" label="Order">

```kotlin
typealias OrderDecider = Decider<OrderCommand?, Order?, OrderEvent?>

fun orderDecider() = OrderDecider(
    initialState = null,
    decide = { c, s ->
        when (c) {
            is CreateOrderCommand ->
                if (s == null) flowOf(OrderCreatedEvent(c.identifier, c.lineItems, c.restaurantIdentifier))
                else flowOf(OrderRejectedEvent(c.identifier, Reason("Order already exists")))

            is MarkOrderAsPreparedCommand ->
                if (s == null) flowOf(OrderNotPreparedEvent(c.identifier, Reason("Order does not exist")))
                else if (OrderStatus.CREATED != s.status) flowOf(
                    OrderNotPreparedEvent(
                        c.identifier,
                        Reason("Order not in CREATED status"),
                    )
                )
                else flowOf(OrderPreparedEvent(c.identifier))

            null -> emptyFlow() // We ignore the `null` command by emitting the empty flow. Only the Decider that can handle `null` command can be combined (Monoid) with other Deciders.
        }
    },
    evolve = { s, e ->
        when (e) {
            is OrderCreatedEvent -> Order(e.identifier, e.restaurantId, e.status, e.lineItems)
            is OrderPreparedEvent -> s?.copy(status = e.status)
            is OrderRejectedEvent -> s?.copy(status = e.status)
            is OrderErrorEvent -> s // Error events are not changing the state in our/this case.
            null -> s // Null events are not changing the state / We return current state instead. Only the Decider that can handle `null` event can be combined (Monoid) with other Deciders.
        }
    }
)
```

:::note
Notice how the state of the `Order` [is modeled as a data class](structuring-the-data.md#state)
:::

</TabItem>
</Tabs>

## View

The view is a data type that represents the event handling algorithm responsible for translating the events into the
denormalized state, which is adequate for querying.

- `initialState` - A starting point / An initial state
- `evolve` (Exhaustive / pattern matching event handler) - A function/lambda that takes input state and input event as
  parameters, and returns the output/new state

![view image](/img/view-kotlin.png)

<Tabs groupId="concept" queryString="concept">
  <TabItem value="restaurant" label="Restaurant">

```kotlin
typealias RestaurantView = View<RestaurantViewState?, RestaurantEvent?>

fun restaurantView() = RestaurantView(
    initialState = null,
    evolve = { s, e ->
        when (e) {
            is RestaurantCreatedEvent -> RestaurantViewState(e.identifier, e.name, e.menu)
            is RestaurantMenuChangedEvent -> s?.copy(menu = e.menu)
            is OrderPlacedAtRestaurantEvent -> s
            is RestaurantErrorEvent -> s // Error events are not changing the state in our/this case.
            null -> s // Null events are not changing the state / We return current state instead. Only the Decider that can handle `null` event can be combined (Monoid) with other Deciders.
        }
    }
)

data class RestaurantViewState(
    val id: RestaurantId,
    val name: RestaurantName,
    val menu: RestaurantMenu
)
```

  </TabItem>
  <TabItem value="order" label="Order">

```kotlin
typealias OrderView = View<OrderViewState?, OrderEvent?>

fun orderView() = OrderView(
    initialState = null,
    evolve = { s, e ->
        when (e) {
            is OrderCreatedEvent -> OrderViewState(e.identifier, e.restaurantId, e.status, e.lineItems)
            is OrderPreparedEvent -> s?.copy(status = e.status)
            is OrderRejectedEvent -> s?.copy(status = e.status)
            is OrderErrorEvent -> s // Error events are not changing the state in our/this case.
            null -> s // Null events are not changing the state / We return current state instead. Only the Decider that can handle `null` event can be combined (Monoid) with other Deciders.
        }
    }
)

data class OrderViewState(
    val id: OrderId,
    val restaurantId: RestaurantId,
    val status: OrderStatus,
    val lineItems: ImmutableList<OrderLineItem>
)

```

</TabItem>
</Tabs>

## Saga

Saga is a data type that represents the central point of control, deciding what to execute next. It is responsible for
mapping different events from deciders into action results that the Saga then can use to calculate the subsequent
actions to be
mapped to the command of other deciders.

In the context of smart endpoints and dumb pipes, deciders would be smart endpoints, and saga would be a dumb pipe.

- `react` - A function/lambda that takes input action-result/event, and returns the flow of actions/commands that should
  be published.

![saga image](/img/saga-kotlin.png)

<Tabs groupId="concept" queryString="concept">
  <TabItem value="restaurant" label="Restaurant">

```kotlin
typealias RestaurantSaga = Saga<OrderEvent?, RestaurantCommand>

fun restaurantSaga() = RestaurantSaga(
    react = { e ->
        when (e) {
            is OrderCreatedEvent -> emptyFlow()
            is OrderPreparedEvent -> emptyFlow()
            is OrderErrorEvent -> emptyFlow()
            null -> emptyFlow() // We ignore the `null` event by returning the empty flow of commands. Only the Saga that can handle `null` event/action-result can be combined (Monoid) with other Sagas.
        }
    }
)

```

  </TabItem>
  <TabItem value="order" label="Order">

```kotlin
typealias OrderSaga = Saga<RestaurantEvent?, OrderCommand>

fun orderSaga() = OrderSaga(
    react = { e ->
        when (e) {
            is OrderPlacedAtRestaurantEvent -> flowOf(
                CreateOrderCommand(
                    e.orderId,
                    e.identifier,
                    e.lineItems
                )
            )

            is RestaurantCreatedEvent -> emptyFlow()
            is RestaurantMenuChangedEvent -> emptyFlow()
            is RestaurantErrorEvent -> emptyFlow()
            null -> emptyFlow() // We ignore the `null` event by returning the empty flow of commands. Only the Saga that can handle `null` event/action-result can be combined (Monoid) with other Sagas.
        }
    }
)
```

</TabItem>
</Tabs>

## Totality

A function is `total` if it is defined for all of its possible inputs.

By having algebraic data types modeling the `Sum/OR` relationship with `sealed` class, it's possible to verify that
the `when` expression covers all cases, **you don't need to add an `else` clause to the statement**.
This is known as `Kotlin matching`. Many modern languages have support for some kind of `pattern matching`.

The compiler will yell at you if you add a new command or event into the model/project (`when` expression goes red), and
you will have to fix it immediately.
It will positively influence the function (`decide`, `evolve`, `react`) totality giving more guarantees about code
correctness.

:::info
The essence of functional programming lies in the power of pure functions. Add static types to the mix, and you have
algebraic abstractions—functions operating on types and honoring certain laws. Make the functions generic on types, and
you have parametricity. The function becomes polymorphic, which implies more reusability, and if you’re disciplined
enough not to leak any implementation details by sneaking in specialized types (or unmanaged hazards such as
exceptions), you get free theorems.
:::