---
title: Release Notes
---

import Tabs from '@theme/Tabs';
import TabItem from '@theme/TabItem';

# Release Notes

## 3.5.1

Artifacts are available on Maven Central

- [https://search.maven.org/artifact/com.fraktalio.fmodel/domain/3.5.1/jar](https://search.maven.org/artifact/com.fraktalio.fmodel/domain/3.5.1/jar)
- [https://search.maven.org/artifact/com.fraktalio.fmodel/application-vanilla/3.5.1/jar](https://search.maven.org/artifact/com.fraktalio.fmodel/application-vanilla/3.5.1/jar)
- [https://search.maven.org/artifact/com.fraktalio.fmodel/application-arrow/3.5.1/jar](https://search.maven.org/artifact/com.fraktalio.fmodel/application-arrow/3.5.1/jar)

### What's changed

In this `minor` release, we have fixed the bug on the Event Sourced aggregate within the `application` module - https://github.com/fraktalio/fmodel/pull/291.
It is reproducible only in the orchestrating scenarios where you communicate two deciders back and forth, a couple of times.

**Full Changelog**: https://github.com/fraktalio/fmodel/compare/v3.5.0...v3.5.1

### Include the dependencies

<Tabs groupId="build" queryString="build-type">
<TabItem value="gradleKotlin" label="Gradle (Kotlin)">

```kotlin
dependencies {
  implementation("com.fraktalio.fmodel:domain:3.5.1")
  implementation("com.fraktalio.fmodel:application-vanilla:3.5.1")
  implementation("com.fraktalio.fmodel:application-arrow:3.5.1")
}
```

</TabItem>
<TabItem value="gradleGroovy" label="Gradle (Groovy)">

```groovy
dependencies {
  implementation 'com.fraktalio.fmodel:domain:3.5.1'
  implementation 'com.fraktalio.fmodel:application-vanilla:3.5.1'
  implementation 'com.fraktalio.fmodel:application-arrow:3.5.1'
}
```

</TabItem>
<TabItem value="maven" label="Maven">

```xml

<dependency>
    <groupId>com.fraktalio.fmodel</groupId>
    <artifactId>domain</artifactId>
    <version>3.5.1</version>
</dependency>

<dependency>
    <groupId>com.fraktalio.fmodel</groupId>
    <artifactId>application-vanilla</artifactId>
    <version>3.5.1</version>
</dependency>

<dependency>
    <groupId>com.fraktalio.fmodel</groupId>
    <artifactId>application-arrow</artifactId>
    <version>3.5.1</version>
</dependency>
```

</TabItem>
</Tabs>

## 3.5.0

Artifacts are available on Maven Central

- [https://search.maven.org/artifact/com.fraktalio.fmodel/domain/3.5.0/jar](https://search.maven.org/artifact/com.fraktalio.fmodel/domain/3.5.0/jar)
- [https://search.maven.org/artifact/com.fraktalio.fmodel/application-vanilla/3.5.0/jar](https://search.maven.org/artifact/com.fraktalio.fmodel/application-vanilla/3.5.0/jar)
- [https://search.maven.org/artifact/com.fraktalio.fmodel/application-arrow/3.5.0/jar](https://search.maven.org/artifact/com.fraktalio.fmodel/application-arrow/3.5.0/jar)

### What's changed

In this release, we have upgraded the Kotlin Arrow library to [1.2.0](https://github.com/arrow-kt/arrow/releases/tag/1.2.0). It is a breaking change for the `application-arrow` module/extension.

We have introduced and configured a binary compatibility validator into our build process.
```
The tool allows dumping binary API of a JVM part of a Kotlin library that is public in the sense of Kotlin visibilities and ensures that the public binary API wasn't changed in a way that makes this change binary incompatible.
```

We extended the case of using `actor` functions to parallelize the message handling processes. (available on JVM target only)
```kotlin
/**
 * Extension function - Handles the flow of command messages of type [C] by concurrently distributing the load across finite number of actors/handlers
 *
 * @param commands [Flow] of Command messages of type [C]
 * @param numberOfActors total number of actors/workers available for distributing the load. Minimum one.
 * @param actorsCapacity capacity of the actors channel's buffer
 * @param actorsStart actors coroutine start option
 * @param actorsContext additional to [CoroutineScope.coroutineContext] context of the actor coroutines.
 * @param partitionKey a function that calculates the partition key/routing key of command - commands with the same partition key will be handled with the same 'actor' to keep the ordering
 * @return [Flow] of stored Events of type [E]
 *
 */
fun <C, S, E> EventSourcingAggregate<C, S, E>.handleConcurrently(
    commands: Flow<C>,
    numberOfActors: Int = 100,
    actorsCapacity: Int = Channel.BUFFERED,
    actorsStart: CoroutineStart = CoroutineStart.LAZY,
    actorsContext: CoroutineContext = EmptyCoroutineContext,
    partitionKey: (C) -> Int
): Flow<E>
```

Deprecating factory functions in favor of constructor-like functions.

```kotlin
@Deprecated(
    message = "Use EventSourcingLockingAggregate constructor-like function instead",
    replaceWith = ReplaceWith("EventSourcingLockingAggregate(decider, eventRepository)")
)
fun <C, S, E, V> eventSourcingLockingAggregate(
    decider: IDecider<C, S, E>,
    eventRepository: EventLockingRepository<C, E, V>
): EventSourcingLockingAggregate<C, S, E, V> =
    object : EventSourcingLockingAggregate<C, S, E, V>,
        EventLockingRepository<C, E, V> by eventRepository,
        IDecider<C, S, E> by decider {}

```


**Full Changelog**: https://github.com/fraktalio/fmodel/compare/v3.4.0...v3.5.0

### Include the dependencies

<Tabs groupId="build" queryString="build-type">
<TabItem value="gradleKotlin" label="Gradle (Kotlin)">

```kotlin
dependencies {
  implementation("com.fraktalio.fmodel:domain:3.5.0")
  implementation("com.fraktalio.fmodel:application-vanilla:3.5.0")
  implementation("com.fraktalio.fmodel:application-arrow:3.5.0")
}
```

</TabItem>
<TabItem value="gradleGroovy" label="Gradle (Groovy)">

```groovy
dependencies {
  implementation 'com.fraktalio.fmodel:domain:3.5.0'
  implementation 'com.fraktalio.fmodel:application-vanilla:3.5.0'
  implementation 'com.fraktalio.fmodel:application-arrow:3.5.0'
}
```

</TabItem>
<TabItem value="maven" label="Maven">

```xml

<dependency>
    <groupId>com.fraktalio.fmodel</groupId>
    <artifactId>domain</artifactId>
    <version>3.5.0</version>
</dependency>

<dependency>
    <groupId>com.fraktalio.fmodel</groupId>
    <artifactId>application-vanilla</artifactId>
    <version>3.5.0</version>
</dependency>

<dependency>
    <groupId>com.fraktalio.fmodel</groupId>
    <artifactId>application-arrow</artifactId>
    <version>3.5.0</version>
</dependency>
```

</TabItem>
</Tabs>

## 3.4.0

Artifacts are available on Maven Central

- [https://search.maven.org/artifact/com.fraktalio.fmodel/domain/3.4.0/jar](https://search.maven.org/artifact/com.fraktalio.fmodel/domain/3.4.0/jar)
- [https://search.maven.org/artifact/com.fraktalio.fmodel/application-vanilla/3.4.0/jar](https://search.maven.org/artifact/com.fraktalio.fmodel/application-vanilla/3.4.0/jar)
- [https://search.maven.org/artifact/com.fraktalio.fmodel/application-arrow/3.4.0/jar](https://search.maven.org/artifact/com.fraktalio.fmodel/application-arrow/3.4.0/jar)

### What's changed

New native targets are available:
linuxX64(), mingwX64(), macosX64(), macosArm64(), tvos(), tvosSimulatorArm64(), watchosArm32(), watchosArm64(),
watchosX86(), watchosX64(), watchosSimulatorArm64(), iosX64(), iosArm64(), iosArm32(), iosSimulatorArm64()

**Full Changelog**: https://github.com/fraktalio/fmodel/compare/v3.3.0...v3.4.0

### Include the dependencies

<Tabs groupId="build" queryString="build-type">
<TabItem value="gradleKotlin" label="Gradle (Kotlin)">

```kotlin
dependencies {
  implementation("com.fraktalio.fmodel:domain:3.4.0")
  implementation("com.fraktalio.fmodel:application-vanilla:3.4.0")
  implementation("com.fraktalio.fmodel:application-arrow:3.4.0")
}
```

</TabItem>
<TabItem value="gradleGroovy" label="Gradle (Groovy)">

```groovy
dependencies {
  implementation 'com.fraktalio.fmodel:domain:3.4.0'
  implementation 'com.fraktalio.fmodel:application-vanilla:3.4.0'
  implementation 'com.fraktalio.fmodel:application-arrow:3.4.0'
}
```

</TabItem>
<TabItem value="maven" label="Maven">

```xml

<dependency>
    <groupId>com.fraktalio.fmodel</groupId>
    <artifactId>domain</artifactId>
    <version>3.4.0</version>
</dependency>

<dependency>
    <groupId>com.fraktalio.fmodel</groupId>
    <artifactId>application-vanilla</artifactId>
    <version>3.4.0</version>
</dependency>

<dependency>
    <groupId>com.fraktalio.fmodel</groupId>
    <artifactId>application-arrow</artifactId>
    <version>3.4.0</version>
</dependency>
```

</TabItem>
</Tabs>

## 3.3.0

Artifacts are available on Maven Central

- [https://search.maven.org/artifact/com.fraktalio.fmodel/domain/3.3.0/jar](https://search.maven.org/artifact/com.fraktalio.fmodel/domain/3.3.0/jar)
- [https://search.maven.org/artifact/com.fraktalio.fmodel/application-vanilla/3.3.0/jar](https://search.maven.org/artifact/com.fraktalio.fmodel/application-vanilla/3.3.0/jar)
- [https://search.maven.org/artifact/com.fraktalio.fmodel/application-arrow/3.3.0/jar](https://search.maven.org/artifact/com.fraktalio.fmodel/application-arrow/3.3.0/jar)

### What's changed

#### A convenient DSL (builder) for the domain components

```kotlin
fun evenNumberDecider(): Decider<EvenNumberCommand?, EvenNumberState, EvenNumberEvent?> =
    decider {
        initialState {
            evenNumberState {
                descriptionString { "Initial state" }
                valueInt { 0 }
            }
        }
        decide { c, s ->
            when (c) {
                is AddEvenNumber -> flowOf(
                    evenNumberAdded {
                        description { c.description }
                        value { s.value + c.value }
                    }
                )

                is SubtractEvenNumber -> flowOf(
                    evenNumberSubtracted {
                        description { c.description }
                        value { s.value - c.value }
                    }
                )

                null -> emptyFlow()
            }
        }
        evolve { s, e ->
            when (e) {
                is EvenNumberAdded ->
                    evenNumberState {
                        description { s.description + e.description }
                        value { e.value }
                    }

                is EvenNumberSubtracted ->
                    evenNumberState {
                        description { s.description - e.description }
                        value { e.value }
                    }

                null -> s
            }
        }
    }
``` 

#### Minimizing the API

- `_Decider<C, Si, So, Ei, Eo>` is internal now
- `_View<Si, So, E>` is internal now

There was no true usage of this API, so we have decided to make it internal, in favor of `Decider<C, S, E>`
and `View<S, E>`.
Previously, `Decider` was just a type alias of `_Decider`, but these are different types actually, and we want to
promote that.

We hope to minimize the complexity of the API, and make the right thing to do the easy thing to do.

**Full Changelog**: https://github.com/fraktalio/fmodel/compare/v3.2.0...v3.3.0

### Include the dependencies

<Tabs groupId="build" queryString="build-type">
<TabItem value="gradleKotlin" label="Gradle (Kotlin)">

```kotlin
dependencies {
  implementation("com.fraktalio.fmodel:domain:3.3.0")
  implementation("com.fraktalio.fmodel:application-vanilla:3.3.0")
  implementation("com.fraktalio.fmodel:application-arrow:3.3.0")
}
```

</TabItem>
<TabItem value="gradleGroovy" label="Gradle (Groovy)">

```groovy
dependencies {
  implementation 'com.fraktalio.fmodel:domain:3.3.0'
  implementation 'com.fraktalio.fmodel:application-vanilla:3.3.0'
  implementation 'com.fraktalio.fmodel:application-arrow:3.3.0'
}
```

</TabItem>
<TabItem value="maven" label="Maven">

```xml

<dependency>
    <groupId>com.fraktalio.fmodel</groupId>
    <artifactId>domain</artifactId>
    <version>3.3.0</version>
</dependency>

<dependency>
    <groupId>com.fraktalio.fmodel</groupId>
    <artifactId>application-vanilla</artifactId>
    <version>3.3.0</version>
</dependency>

<dependency>
    <groupId>com.fraktalio.fmodel</groupId>
    <artifactId>application-arrow</artifactId>
    <version>3.3.0</version>
</dependency>
```

</TabItem>
</Tabs>

## 3.2.0

Artifacts are available on Maven Central

- [https://search.maven.org/artifact/com.fraktalio.fmodel/domain/3.2.0/jar](https://search.maven.org/artifact/com.fraktalio.fmodel/domain/3.2.0/jar)
- [https://search.maven.org/artifact/com.fraktalio.fmodel/application-vanilla/3.2.0/jar](https://search.maven.org/artifact/com.fraktalio.fmodel/application-vanilla/3.2.0/jar)
- [https://search.maven.org/artifact/com.fraktalio.fmodel/application-arrow/3.2.0/jar](https://search.maven.org/artifact/com.fraktalio.fmodel/application-arrow/3.2.0/jar)

### What's changed

#### Optimistic Locking

Optimistic locking, also referred to as optimistic concurrency control, allows multiple concurrent users to attempt to
update the same resource.

There are two common ways to implement optimistic locking: version number and timestamp. The version number is generally
considered to be a better option because the server clock can be inaccurate over time, but we do not want to restrict it
to only one option, so we have the generic parameter V acting as a Version.

The optimistic locking mechanism is not leaking into the core Domain layer.

Application modules provide more interfaces and extensions, giving you additional options to compose your unique Domain
components with Optimistic Locking formally in place, without changing the Domain components whatsoever.

**example (state-stored aggregate / traditional):**

```kotlin
  stateStoredLockingAggregate(
      decider = myDecider,
      stateRepository = myLockingRepository
  ).handleOptimistically(myCommand)
```

where `myDecider` is of type `IDecider<C, S, E>`, `myLockingRepository` is of type `StateLockingRepository<C, S, V>`
and `myCommand` is of type `C`

**example (event-sourced aggregate / event-driven):**

```kotlin

  eventSourcingLockingAggregate(
      decider = myDecider,
      stateRepository = myLockingRepository
  ).handleOptimistically(myCommand)
```

where `myDecider` is of type `IDecider<C, S, E>`, `myLockingRepository` is of type `EventLockingRepository<C, E, V>`
and `myCommand` is of type `C`

**Full Changelog**: https://github.com/fraktalio/fmodel/compare/v3.1.0...v3.2.0

### Include the dependencies

<Tabs groupId="build" queryString="build-type">
<TabItem value="gradleKotlin" label="Gradle (Kotlin)">

```kotlin
dependencies {
  implementation("com.fraktalio.fmodel:domain:3.2.0")
  implementation("com.fraktalio.fmodel:application-vanilla:3.2.0")
  implementation("com.fraktalio.fmodel:application-arrow:3.2.0")
}
```

</TabItem>
<TabItem value="gradleGroovy" label="Gradle (Groovy)">

```groovy
dependencies {
  implementation 'com.fraktalio.fmodel:domain:3.2.0'
  implementation 'com.fraktalio.fmodel:application-vanilla:3.2.0'
  implementation 'com.fraktalio.fmodel:application-arrow:3.2.0'
}
```

</TabItem>
<TabItem value="maven" label="Maven">

```xml

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

</TabItem>
</Tabs>

## 3.1.0

Artifacts are available on Maven Central

- [https://search.maven.org/artifact/com.fraktalio.fmodel/domain/3.1.0/jar](https://search.maven.org/artifact/com.fraktalio.fmodel/domain/3.1.0/jar)
- [https://search.maven.org/artifact/com.fraktalio.fmodel/application-vanilla/3.1.0/jar](https://search.maven.org/artifact/com.fraktalio.fmodel/application-vanilla/3.1.0/jar)
- [https://search.maven.org/artifact/com.fraktalio.fmodel/application-arrow/3.1.0/jar](https://search.maven.org/artifact/com.fraktalio.fmodel/application-arrow/3.1.0/jar)

### What's changed

#### Experimental Actors (JVM only)

* Kotlin Actors (experimental) - concurrently handling messages by `idugalic`
  in https://github.com/fraktalio/fmodel/pull/70

![kotlin actors](https://raw.githubusercontent.com/fraktalio/fmodel/main/.assets/kotlin-actors.png)

```kotlin
@ExperimentalContracts
@FlowPreview
fun <C, S, E> EventSourcingAggregate<C, S, E>.handleConcurrently(
    commands: Flow<C>,
    numberOfActors: Int = 100,
    actorsCapacity: Int = Channel.BUFFERED,
    actorsStart: CoroutineStart = CoroutineStart.LAZY,
    actorsContext: CoroutineContext = EmptyCoroutineContext,
    partitionKey: (C) -> Int
): Flow<E> = channelFlow {
    val actors: List<SendChannel<C>> = (1..numberOfActors).map {
        commandActor(channel, actorsCapacity, actorsStart, actorsContext) { handle(it) }

    }
    commands
        .onCompletion {
            actors.forEach {
                it.close()
            }
        }
        .collect {
            val partition = partitionKey(it).absoluteValue % numberOfActors.coerceAtLeast(1)
            actors[partition].send(it)
        }
}

```

**Full Changelog**: https://github.com/fraktalio/fmodel/compare/v3.0.0...v3.1.0

### Include the dependencies

<Tabs groupId="build" queryString="build-type">
<TabItem value="gradleKotlin" label="Gradle (Kotlin)">

```kotlin
dependencies {
  implementation("com.fraktalio.fmodel:domain:3.1.0")
  implementation("com.fraktalio.fmodel:application-vanilla:3.1.0")
  implementation("com.fraktalio.fmodel:application-arrow:3.1.0")
}
```

</TabItem>
<TabItem value="gradleGroovy" label="Gradle (Groovy)">

```groovy
dependencies {
  implementation 'com.fraktalio.fmodel:domain:3.1.0'
  implementation 'com.fraktalio.fmodel:application-vanilla:3.1.0'
  implementation 'com.fraktalio.fmodel:application-arrow:3.1.0'
}
```

</TabItem>
<TabItem value="maven" label="Maven">

```xml

<dependency>
    <groupId>com.fraktalio.fmodel</groupId>
    <artifactId>domain</artifactId>
    <version>3.1.0</version>
</dependency>

<dependency>
    <groupId>com.fraktalio.fmodel</groupId>
    <artifactId>application-vanilla</artifactId>
    <version>3.1.0</version>
</dependency>

<dependency>
    <groupId>com.fraktalio.fmodel</groupId>
    <artifactId>application-arrow</artifactId>
    <version>3.1.0</version>
</dependency>
```

</TabItem>
</Tabs>

## 3.0.0

Artifacts are available on Maven Central

- [https://search.maven.org/artifact/com.fraktalio.fmodel/domain/3.0.0/jar](https://search.maven.org/artifact/com.fraktalio.fmodel/domain/3.0.0/jar)
- [https://search.maven.org/artifact/com.fraktalio.fmodel/application-vanilla/3.0.0/jar](https://search.maven.org/artifact/com.fraktalio.fmodel/application-vanilla/3.0.0/jar)
- [https://search.maven.org/artifact/com.fraktalio.fmodel/application-arrow/3.0.0/jar](https://search.maven.org/artifact/com.fraktalio.fmodel/application-arrow/3.0.0/jar)

### What's changed

* A [multiplatform support (jvm, js, native)](https://kotlinlang.org/docs/multiplatform.html) included
* Switched from Spek to Kotest test framework
* Switched from Maven to Gradle

#### Tests example

```kotlin
class DeciderTest : FunSpec({
    val evenDecider = evenNumberDecider()
    val oddDecider = oddNumberDecider()

    test("Event-sourced Decider - add even number") {
        with(evenDecider) {
            givenEvents(emptyList()) {
                whenCommand(AddEvenNumber(Description("2"), NumberValue(2)))
            } thenEvents listOf(EvenNumberAdded(Description("2"), NumberValue(2)))
        }
    }

    test("Event-sourced Decider - given previous state, add even number") {
        with(evenDecider) {
            givenEvents(listOf(EvenNumberAdded(Description("2"), NumberValue(2)))) {
                whenCommand(AddEvenNumber(Description("4"), NumberValue(4)))
            } thenEvents listOf(EvenNumberAdded(Description("4"), NumberValue(4)))
        }
    }
})
```

**Full Changelog**: https://github.com/fraktalio/fmodel/compare/v3.0.0...v3.1.0

### Include the dependencies

<Tabs groupId="build" queryString="build-type">
<TabItem value="gradleKotlin" label="Gradle (Kotlin)">

```kotlin
dependencies {
  implementation("com.fraktalio.fmodel:domain:3.0.0")
  implementation("com.fraktalio.fmodel:application-vanilla:3.0.0")
  implementation("com.fraktalio.fmodel:application-arrow:3.0.0")
}
```

</TabItem>
<TabItem value="gradleGroovy" label="Gradle (Groovy)">

```groovy
dependencies {
  implementation 'com.fraktalio.fmodel:domain:3.0.0'
  implementation 'com.fraktalio.fmodel:application-vanilla:3.0.0'
  implementation 'com.fraktalio.fmodel:application-arrow:3.0.0'
}
```

</TabItem>
<TabItem value="maven" label="Maven">

```xml

<dependency>
    <groupId>com.fraktalio.fmodel</groupId>
    <artifactId>domain</artifactId>
    <version>3.0.0</version>
</dependency>

<dependency>
    <groupId>com.fraktalio.fmodel</groupId>
    <artifactId>application-vanilla</artifactId>
    <version>3.0.0</version>
</dependency>

<dependency>
    <groupId>com.fraktalio.fmodel</groupId>
    <artifactId>application-arrow</artifactId>
    <version>3.0.0</version>
</dependency>
```

</TabItem>
</Tabs>
