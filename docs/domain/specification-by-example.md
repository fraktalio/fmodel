---
sidebar_position: 5
---

import Tabs from '@theme/Tabs';
import TabItem from '@theme/TabItem';

# Specification By Example

- It is a collaborative approach to software analysis and testing.
- It is the fastest way to align people from different roles on what exactly we need to build and how to test it.

## Illustrating requirements using examples

The requirements are presented as scenarios.
A scenario is an example of the system’s behavior from the users’ perspective,
and they are specified using the `Given-When-Then` structure to create a testable specification:

- Given `< some precondition(s) >`
- When `< an action/trigger occurs >`
- Then `< some post condition >`

We face business with specific questions they should be able to answer.
We are not facing them with abstractions or generalizations.
We are dealing only with data that are formally representing preconditions (events), actions (commands) and post
conditions (new events):

- Given `< some event(s) / current state of our system > `
- When `< a command occurs >`
- Then `< some new event(s) / evolves to the new state of our system >`

It also represents an acceptance criterion of the system, and acts as a documentation.

## Refining specifications

Let's pivot the blueprint for 90 degrees, and refine our requirements further.

![restaurant model](/img/restaurant-model.jpg)

We need to go through all the scenarios, successes and errors.
For example, for an `OrderDecider` with given `OrderCreatedEvent`  event as a precondition, when
command `MarkOrderAsPreparedCommand` is triggered, then Order is successfully prepared (`OrderPreparedEvent`).
But, without `OrderCreatedEvent` given as precondition, handling the same command `MarkOrderAsPreparedCommand` will
produce different result/failure (`OrderNotPreparedEvent`).

It means that order can be marked as prepared only if it was previously created/placed.

![spec image](/img/spec-by-example.jpg)

## Automating tests based on examples

Functions/lambda offers the algebra of manipulating the data (commands, events, state) in a compositional manner,
effectively modeling the behavior.
This leads to modularity in design and a clear separation of the entity’s structure and functions/behaviour of the
entity. **It makes it is easy to test!**

You can create a small DSL in Kotlin to write and run specifications in `Given-When-Then` structure (testable
specification):

<Tabs groupId="component-type" queryString="component-type">
  <TabItem value="decider" label="Decider">

```kotlin
fun <C, S, E> IDecider<C, S, E>.givenEvents(events: Iterable<E>, command: () -> C): Flow<E> =
    decide(command(), events.fold(initialState) { s, e -> evolve(s, e) })

fun <C> whenCommand(command: C): C = command

suspend infix fun <E> Flow<E>.thenEvents(expected: Iterable<E>) = assertIterableEquals(expected, toList())
```

  </TabItem>
  <TabItem value="view" label="View">

```kotlin
fun <S, E> IView<S, E>.givenEvents(events: Iterable<E>) = events.fold(initialState) { s, e -> evolve(s, e) }

infix fun <S, U : S> S.thenState(expected: U?) = assertEquals(expected, this)
```

  </TabItem>
  <TabItem value="saga" label="Saga">

```kotlin
fun <AR, A> ISaga<AR, A>.whenActionResult(actionResults: AR) = react(actionResults)

suspend infix fun <A> Flow<A>.expectActions(expected: Iterable<A>) = assertIterableEquals(expected, toList())

```

  </TabItem>
</Tabs>


Runnable tests:

<Tabs groupId="component-type" queryString="component-type">
  <TabItem value="decider" label="Decider">

```kotlin
@Test
fun testCreateOrder(): Unit = runBlocking {
        val createOrderCommand = CreateOrderCommand(orderId, restaurantId, orderLineItems)
        val orderCreatedEvent = OrderCreatedEvent(orderId, orderLineItems, restaurantId)
        
        with(orderDecider) {
            givenEvents(emptyList()) {                      // PRE CONDITIONS
                whenCommand(createOrderCommand)             // ACTION
            } thenEvents listOf(orderCreatedEvent)          // POST CONDITIONS
        }
    }

@Test
fun testMarkOrderAsPrepared(): Unit = runBlocking {
    val orderCreatedEvent = OrderCreatedEvent(orderId, orderLineItems, restaurantId)
    val markOrderAsPreparedCommand = MarkOrderAsPreparedCommand(orderId)
    val orderPreparedEvent = OrderPreparedEvent(orderId)

    with(orderDecider) {
        givenEvents(listOf(orderCreatedEvent)) {         // PRE CONDITIONS
            whenCommand(markOrderAsPreparedCommand)      // ACTION
        } thenEvents listOf(orderPreparedEvent)          // POST CONDITIONS
    }
}

@Test
fun testMarkOrderAsPreparedDoesNotExistError(): Unit = runBlocking {
    val markOrderAsPreparedCommand = MarkOrderAsPreparedCommand(orderId)
    val orderNotPreparedEvent = OrderNotPreparedEvent(orderId, Reason("Order does not exist"))

    with(orderDecider) {
        givenEvents(emptyList()) {                       // PRE CONDITIONS
            whenCommand(markOrderAsPreparedCommand)      // ACTION
        } thenEvents listOf(orderNotPreparedEvent)       // POST CONDITIONS
    }
}
```

  </TabItem>
  <TabItem value="view" label="View">

```kotlin
@Test
fun testOrderCreated(): Unit = runBlocking {
        val orderCreatedEvent = OrderCreatedEvent(orderId, orderLineItems, restaurantId)
        val orderViewState = OrderViewState(orderId, restaurantId, orderCreatedEvent.status, orderLineItems)

        with(orderView) {
            givenEvents(
                listOf(orderCreatedEvent)
            ) thenState orderViewState
        }
    }

@Test
fun testOrderPrepared(): Unit = runBlocking {
    val orderCreatedEvent = OrderCreatedEvent(orderId, orderLineItems, restaurantId)
    val orderPreparedEvent = OrderPreparedEvent(orderId)
    val orderViewState = OrderViewState(orderId, restaurantId, orderPreparedEvent.status, orderLineItems)

    with(orderView) {
        givenEvents(
            listOf(orderCreatedEvent, orderPreparedEvent)
        ) thenState orderViewState
    }
}

@Test
fun testOrderPreparedDoesNotExistOrderError(): Unit = runBlocking {
    val orderPreparedEvent = OrderPreparedEvent(orderId)
    with(orderView) {
        givenEvents(
            listOf(orderPreparedEvent)
        ) thenState null
    }
}
```

  </TabItem>
  <TabItem value="saga" label="Saga">

```kotlin
@Test
fun testOrderPlacedAtRestaurantEvent(): Unit = runBlocking {
        val orderPlacedAtRestaurantEvent = OrderPlacedAtRestaurantEvent(restaurantId, orderLineItems, orderId)
        val createOrderCommand = CreateOrderCommand(orderId, restaurantId, orderLineItems)

        with(orderSaga) {
            whenActionResult(
                orderPlacedAtRestaurantEvent
            ) expectActions listOf(createOrderCommand)
        }
    }
```

  </TabItem>
</Tabs>
