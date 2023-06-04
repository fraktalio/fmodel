---
sidebar_position: 3
---

# Examples

All example/demo application are publicly available on Github.
They are all modeling the same imaginary `restaurant and order management` information system by using different technology stacks to demonstrate flexibility of the Fmodel libraries.

The domain model is explicitly modeling `events` and `state`, and this is enabling you to: 

- compose the `event-sourced` system
- compose the `state-stored` system

![event-sourced vs state-stored](/img/es-ss-diagram.svg)

The source code snippets in this reference guide are taken from these applications.

## Spring

- `event-sourced`: [https://github.com/fraktalio/fmodel-spring-demo](https://github.com/fraktalio/fmodel-spring-demo) / #EventSourcing, #SpringBoot, #Reactor, #RSocket, #R2DBC, #SQL, #Testcontainers
- `state-stored`: [https://github.com/fraktalio/fmodel-spring-state-stored-demo](https://github.com/fraktalio/fmodel-spring-state-stored-demo) / #SpringBoot, #Reactor, #RSocket, #R2DBC, #SQL, #Testcontainers

## Ktor
- `event-sourced`: [https://github.com/fraktalio/fmodel-ktor-demo](https://github.com/fraktalio/fmodel-ktor-demo) / #EventSourcing, #Ktor, #R2DBC, #SQL
- `state-stored`: TODO

more to come...
