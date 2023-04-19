---
sidebar_position: 2
---

# Structuring the data

In computer programming, especially functional programming, and type theory, an algebraic data type is a kind of
composite / a type formed by combining other types.

Two standard classes of algebraic types are:

- `product` types (i.e., tuples, pair, and data classes)
- `sum` types (i.e., enums and sealed interfaces/classes).

**They provide the necessary abstraction for structuring the various data of our domain model.**

- Whereas `sum` types let you model the variations within a particular data type,
- `product` types help cluster related data into a larger abstraction.


- `Sum` type models an `OR` relationship,
- and `Product` type models an `AND` relationship.

**So, the `OR` and `AND` operations constitute the algebra of our data types.**

It is becoming clear that we have various classes of algebraic data types in our domain. Let's categorize them:

- Command / C
- Event / E
- State / S

## Commands

Commands represent the intent to change the state of the information system.

![command image](/img/command.svg)

### Sum/OR

The `sealed` class in Kotlin represents data composition, also known as a `Sum` type (models the `OR` relationship).

The key benefit of using `sealed` classes comes into play when using them in a `when` expression. If it's possible to
verify that the statement covers all cases, **you don't need to add an `else` clause to the statement**.

We model our commands as a `Sum` type (`OR` relationship) by using the `sealed` class. In this example, we have five
possible
concrete sub-classes of `Command` which are known at compile
time: `CreateRestaurantCommand`, `ChangeRestaurantMenuCommand`, `PlaceOrderCommand`, `CreateOrderCommand`, `MarkOrderAsPreparedCommand`.

Additionally, commands are categorized as Restaurant and Order commands which are respectfully matching two concepts presented on the blueprint / swim-lanes at the bottom / yellow sticky notes.

![restaurant model](/img/restaurant-model.jpg)

```kotlin
sealed class Command

sealed class RestaurantCommand : Command() {
    abstract val identifier: RestaurantId
}

sealed class OrderCommand : Command() {
    abstract val identifier: OrderId
}

data class CreateRestaurantCommand(
    override val identifier: RestaurantId = RestaurantId(),
    val name: RestaurantName,
    val menu: RestaurantMenu,
) : RestaurantCommand()

data class ChangeRestaurantMenuCommand(
    override val identifier: RestaurantId = RestaurantId(),
    val menu: RestaurantMenu,
) : RestaurantCommand()

data class PlaceOrderCommand(
    override val identifier: RestaurantId,
    val orderIdentifier: OrderId = OrderId(),
    val lineItems: ImmutableList<OrderLineItem>,
) : RestaurantCommand()

data class CreateOrderCommand(
    override val identifier: OrderId,
    val restaurantIdentifier: RestaurantId = RestaurantId(),
    val lineItems: ImmutableList<OrderLineItem>,
) : OrderCommand()

data class MarkOrderAsPreparedCommand(
    override val identifier: OrderId,
) : OrderCommand()
```

### Product/AND

If you zoom in into the concrete command types, for example, `CreateRestaurantCommand,` you will notice that it is
formed by combining other types: `RestaurantId`, `RestaurantName`, `RestaurantMenu`.
Essentially, `CreateRestaurantCommand` data class is a `Product` type which models `AND` relationship:

```
CreateRestaurantOrderCommand = RestaurantId & RestaurantName & RestaurantMenu
```

Our model is well typed! Notice how `RestaurantId`, `RestaurantName` and `RestaurantMenu` are not of type String which
would provide no value and has no meaning from the type system perspective.
Rather, we model these information as Kotlin `value` classes (or alternatively as `data` classes)

```kotlin
@JvmInline
value class RestaurantId(val value: UUID = UUID.randomUUID())

@JvmInline
value class RestaurantName(val value: String)

data class RestaurantMenu(
    val menuItems: ImmutableList<MenuItem>,
    val menuId: UUID = UUID.randomUUID(),
    val cuisine: RestaurantMenuCuisine = RestaurantMenuCuisine.GENERAL
)
```

:::info
Kotlin `value` classes are causing no additional overhead, since value/inline class is erased at runtime.
:::

## Events

Events represent the state change itself, a fact.
These events represent decisions that have already happened (past tense).

![event image](/img/event.svg)

### Sum/OR

We model our events as a `Sum` type (`OR` relationship) by using `sealed` class. In this example, we have ten possible
sub-classes of `Event` which are known at compile
time: `RestaurantCreatedEvent`, `RestaurantNotCreatedEvent`, `RestaurantMenuChangedEvent`, `RestaurantMenuNotChangedEvent`, `OrderPlacedAtRestaurantEvent`, `OrderNotPlacedAtRestaurantEvent`, `OrderRejectedByRestaurantEvent`, `OrderCreatedEvent`, `OrderPreparedEvent`, `OrderRejectedEvent`.

Additionally, events are categorized as Restaurant and Order events which are respectfully matching two concepts presented on the blueprint / swim-lanes at the bottom / yellow sticky notes.

![restaurant model](/img/restaurant-model.jpg)

```kotlin
sealed class Event {
    abstract val final: Boolean
}

sealed class RestaurantEvent : Event() {
    abstract val identifier: RestaurantId
}

sealed class RestaurantErrorEvent : RestaurantEvent() {
    abstract val reason: Reason
}

sealed class OrderEvent : Event() {
    abstract val identifier: OrderId
}

sealed class OrderErrorEvent : OrderEvent() {
    abstract val reason: Reason
}

data class RestaurantCreatedEvent(
    override val identifier: RestaurantId,
    val name: RestaurantName,
    val menu: RestaurantMenu,
    override val final: Boolean = false,
) : RestaurantEvent()

data class RestaurantNotCreatedEvent(
    override val identifier: RestaurantId,
    val name: RestaurantName,
    val menu: RestaurantMenu,
    override val reason: Reason,
    override val final: Boolean = false,
) : RestaurantErrorEvent()

data class RestaurantMenuChangedEvent(
    override val identifier: RestaurantId,
    val menu: RestaurantMenu,
    override val final: Boolean = false,
) : RestaurantEvent()

data class RestaurantMenuNotChangedEvent(
    override val identifier: RestaurantId,
    val menu: RestaurantMenu,
    override val reason: Reason,
    override val final: Boolean = false,
) : RestaurantErrorEvent()

data class OrderPlacedAtRestaurantEvent(
    override val identifier: RestaurantId,
    val lineItems: ImmutableList<OrderLineItem>,
    val orderId: OrderId,
    override val final: Boolean = false,
) : RestaurantEvent()

data class OrderNotPlacedAtRestaurantEvent(
    override val identifier: RestaurantId,
    val lineItems: ImmutableList<OrderLineItem>,
    val orderId: OrderId,
    override val reason: Reason,
    override val final: Boolean = false,
) : RestaurantErrorEvent()

data class OrderRejectedByRestaurantEvent(
    override val identifier: RestaurantId,
    val orderId: OrderId,
    override val reason: Reason,
    override val final: Boolean = false,
) : RestaurantErrorEvent()

data class OrderCreatedEvent(
    override val identifier: OrderId,
    val lineItems: ImmutableList<OrderLineItem>,
    val restaurantId: RestaurantId,
    override val final: Boolean = false,
) : OrderEvent() {
    val status: OrderStatus = OrderStatus.CREATED
}

data class OrderPreparedEvent(
    override val identifier: OrderId,
    override val final: Boolean = false,
) : OrderEvent() {
    val status: OrderStatus = OrderStatus.PREPARED
}

data class OrderRejectedEvent(
    override val identifier: OrderId,
    override val reason: Reason,
    override val final: Boolean = false,
) : OrderErrorEvent() {
    val status: OrderStatus = OrderStatus.REJECTED
}
```

### Product/AND

If you zoom in into the concrete event types, for example, `OrderPlacedAtRestaurantEvent,` you will notice that it is
formed by combining other types: `RestaurantId`, `OrderId`, `OrderLineItem`.
Essentially, `OrderPlacedAtRestaurantEvent` data class is a `Product` type which models `AND` relationship:

```
OrderPlacedAtRestaurantEvent = RestaurantId & OrderId & list of [OrderLineItem]
```

As with `commands`, the kotlin `value` and `data` classes are used to model `events`, making our model robust, well
typed and less error-prone.

## State

The current state of the information system is evolved out of past events/facts.

![state image](/img/state.svg)

```kotlin
data class Restaurant(
    val id: RestaurantId,
    val name: RestaurantName,
    val menu: RestaurantMenu
)

data class Order(
    val id: OrderId,
    val restaurantId: RestaurantId,
    val status: OrderStatus,
    val lineItems: ImmutableList<OrderLineItem>
)
```

### Product/AND

If you zoom in into the concrete state types, for example, `Restaurant,` you will notice that it is formed by
combining other types: `RestaurantId`, `RestaurantName`, `RestaurantMenu`.
Essentially, `Restaurant` data class is a `Product` type that models `AND` relationship:

```
Restaurant = RestaurantId & RestaurantName & RestaurantMenu
```

## Embrace Immutability

Kotlin encourages developers to write immutably, by using `val` in your data types.
Immutable objects are thread safe. No race conditions, no concurrency problems, no need to synchronize.

We can [afford it](https://elizarov.medium.com/immutability-we-can-afford-10c0dcb8351d)!

## Encapsulation

:::info
One might object that algebraic data types violate encapsulation by making public the internal representation of a type.
In functional programming, we approach concerns about encapsulation differently / we don’t typically have
a `delicate mutable state` which could lead to bugs or violation of invariants if exposed publicly.
Exposing the data constructors of a data type is often fine, and the decision to do so is approached much like any other
decision about what the public API of a data type should be.

[Book - Functional Programming in Kotlin](https://www.manning.com/books/functional-programming-in-kotlin)
:::

In order to achieve better encapsulation, one could use interfaces instead of data classes as a public API by
restricting data classes to `package private`.
Taking into account that we achieved a great deal of immutability, this might not be needed.
