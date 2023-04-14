---
sidebar_position: 1
---

import Tabs from '@theme/Tabs';
import TabItem from '@theme/TabItem';

# Intro

Domain modeling is a powerful tool for clarifying and refining our understanding of a subject area, and for identifying
opportunities for optimization and improvement. By carefully considering the needs of stakeholders and the nature of the
domain, it is possible to create effective and useful models that can facilitate communication and drive progress.

Fmodel aims to bring functional, algebraic and reactive domain modeling to Kotlin.

It is inspired by DDD, EventSourcing and Functional programming communities, yet implements these ideas and concepts in
idiomatic Kotlin, which in turn makes our code

- less error-prone,
- easier to understand,
- easier to test,
- type-safe and
- thread-safe.


**FModel promotes clear separation between data and behaviour**:

- Data
  - `Command` - An intent to change the state of the system
  - `Event` - The state change itself, a fact. It represents a decision that has already happened.
  - `State` - The current state of the system. It is evolved out of past events.
- Behaviour
  - `Decide` - A pure function that takes `command` and current `state` as parameters, and returns the flow of new `events`.
  - `Evolve` - A pure function that takes `event` and current `state` as parameters, and returns the new `state` of the system.
  - `React` - A pure function that takes `event` as parameter, and returns the flow of `commands`, deciding what to execute next.

**The focus is on implementing robust information systems (back-end)**:

- traditional - state-stored systems / storing the new `state` by overwriting the previous `state`.
- event-driven - event-sourced systems / storing the `events` in immutable storage by only appending.
- or you can have both flavors within your systems landscape, and easily transit from one to another 


## What you'll need

- [Java](https://foojay.io/) version 11 or above.

## Getting Started

Get started by **including dependencies**.

:::info

All Fmodel libraries are Multiplatform-ready, so you can use them in all of your
[KMP](https://kotlinlang.org/docs/multiplatform.html) projects.

:::

### Enable the Maven Central repository

Fmodel is published in [Maven Central](https://search.maven.org/), so you need to
enable it as a source of dependencies in your build.

<Tabs groupId="build" queryString="build-type">
  <TabItem value="gradleKotlin" label="Gradle (Kotlin)">

  ```kotlin
  repositories {
    mavenCentral()
}
  ```

  </TabItem>
  <TabItem value="gradleGroovy" label="Gradle (Groovy)">

  ```groovy
  repositories {
    mavenCentral()
}
  ```

  </TabItem>
  <TabItem value="maven" label="Maven">

:::info

Maven includes the Maven Central repository by default.

:::

  </TabItem>
</Tabs>

### Include the dependencies

You're now ready to include Fmodel in your project.

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

## Create a new application

You have great frameworks at your service to bootstrap development:

- [Ktor](https://ktor.io/quickstart/)
- [Spring](https://spring.io/guides/tutorials/spring-boot-kotlin/)

:::info
Fmodel does not require usage of any framework whatsoever. It is completely up to you!
:::