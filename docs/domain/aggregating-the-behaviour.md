---
sidebar_position: 4
---

import Tabs from '@theme/Tabs';
import TabItem from '@theme/TabItem';

# Aggregating the Behaviour

The `combine` is a binary operation over the `decider`, `view` and `saga`, satisfying associativity and having an
identity/empty element.

**Associativity facilitates parallelization by giving us the freedom to break problems into chunks that can be computed
in parallel.**

Functional paradigm and category theory define this algebra as a `Monoid`.
Stated tersely, a `monoid` is a type together with a binary operation (`combine`) over that type, satisfying
associativity
and having an identity element (zero/empty).

**`combine` operation is also commutative** / *commutative* `monoid`

<Tabs groupId="component-type" queryString="component-type">
  <TabItem value="decider" label="Decider">

```
associative: (decider1 + decider2) + decider3 = decider1 + (decider2 + decider3)
commutative: decider1 + decider2 = decider2 + decider1
zero:        decider1 + decider0 = decider1
```

By combining two or more deciders you get the new decider.

This is a formal signature of the `combine` extension function defined on the `decider`:

```kotlin
inline infix fun <reified C1 : C_SUPER, S1, reified E1 : E_SUPER, reified C2 : C_SUPER, S2, reified E2 : E_SUPER, C_SUPER, E_SUPER> Decider<C1?, S1, E1?>.combine(
    y: Decider<C2?, S2, E2?>
): Decider<C_SUPER, Pair<S1, S2>, E_SUPER>
```

Type parameters are restricted by generic constraints. Notice the upper
bounds `C1 : C_SUPER`, `E1 : E_SUPER`, `C2 : C_SUPER`, `E2 : E_SUPER`.

It is only possible to use the `combine` function when:

- `E1` and `E2` have common superclass `E_SUPER`
- `C1` and `C2`  have common superclass `C_SUPER`
- `C1?`, `C2?`,  `E1?` and `E2?` are nullable.

```kotlin
val orderDecider: Decider<OrderCommand?, Order?, OrderEvent?> = orderDecider()
val restaurantDecider: Decider<RestaurantCommand?, Restaurant?, RestaurantEvent?> = restaurantDecider()

// Combining two deciders into one big decider that can handle all commands of the system.
val allDecider: Decider<Command?, Pair<Order?, Restaurant?>, Event?> = orderDecider combine restaurantDecider
```

If the constraints are not met, the `combine` function will not be available for usage!

  </TabItem>
  <TabItem value="view" label="View">

```
associative: (view1 + view2) + view3 = view1 + (view2 + view3)
commutative: view1 + view2 = view2 + view1
zero:        view1 + view0 = view1
```

By combining two or more views you get the new view.

This is a formal signature of the `combine` extension function defined on the `view`:

```kotlin
inline infix fun <Sx, reified Ex : E_SUPER, Sy, reified Ey : E_SUPER, E_SUPER> View<Sx, Ex?>.combine(y: View<Sy, Ey?>): View<Pair<Sx, Sy>, E_SUPER>
```

Type parameters are restricted by generic constraints. Notice the upper bounds `Ex : E_SUPER`, `Ey : E_SUPER`.

It is only possible to use the `combine` function when:

- `Ex` and `Ey` have common superclass `E_SUPER`
- `Ex?` and `Ey?` are nullable.

```kotlin
val orderView: View<OrderViewState?, OrderEvent?> = orderView()
val restaurantView: View<RestaurantViewState?, RestaurantEvent?> = restaurantView()

// Combining two views into one big view that can handle all events of the system.
val allView: View<Pair<OrderViewState?, RestaurantViewState?>, Event?> = orderView combine restaurantView
```

If the constraints are not met, the `combine` function will not be available for usage!

  </TabItem>
  <TabItem value="saga" label="Saga">

```
associative: (saga1 + saga2) + saga3 = saga1 + (saga2 + saga3)
commutative: saga1 + saga2 = saga2 + saga1
zero:        saga1 + saga0 = saga1
```

By combining two or more sagas you get the new saga.

This is a formal signature of the `combine` extension function defined on the `saga`:

```kotlin
inline infix fun <reified ARx : AR_SUPER, Ax : A_SUPER, reified ARy : AR_SUPER, Ay : A_SUPER, AR_SUPER, A_SUPER> Saga<in ARx?, out Ax>.combine(
    y: Saga<in ARy?, out Ay>
): Saga<AR_SUPER, A_SUPER>
```

Type parameters are restricted by generic constraints. Notice the upper
bounds `ARx : AR_SUPER`, `ARy : AR_SUPER`, `Ax : A_SUPER`, `Ay : A_SUPER`.

It is only possible to use the `combine` function when:

- `ARx` and `ARy` have common superclass `AR_SUPER`
- `Ax` and `Ay` have common superclass `A_SUPER`
- `ARx?` and `ARy?` are nullable.

```kotlin
val orderSaga: Saga<RestaurantEvent?, OrderCommand> = orderSaga()
val restaurantSaga: Saga<OrderEvent?, RestaurantCommand> = restaurantSaga()

// Combining two choreography sagas into one big system orchestrating saga.
val allSaga: Saga<Event?, Command> = orderSaga combine restaurantSaga
```

If the constraints are not met, the `combine` function will not be available for usage!

  </TabItem>
</Tabs>

