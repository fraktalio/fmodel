---
sidebar_position: 1
---

# Discovering the Domain

There are numerous techniques to discover a domain.
Event Storming is a particularly interesting one.
It is a workshop format for quickly exploring business domains, engaging both Domain Experts and Software Developers.

[Event Modeling](https://eventmodeling.org/posts/what-is-event-modeling/) adopts Event Storming sticky notes. The final piece was the UI/UX aspects to complete what more resembles a movie story board (white board - or digital white board).
While Event Storming focuses in discovering the problem space, **Event Modeling creates a blueprint for a solution**.

- It is a method of describing systems using an example of how information has changed within them over time.
- It is a scenario-based and UX-driven approach to defining requirements.

On a higher level of abstraction, any information system is responsible for handling the intent (`Command`) and, based
on the current `State,` produce new facts (`Events`). The system’s **new** `State` is then evolved out of these `Events.`

![event modeling](/img/event-modeling.png)

- User submits the form on the page by clicking on the button
- The intent to change the system is explicitly captured/modeled as a Command/`C`.
- Command is handled by the decider component, which State/`S` (yellow) is represented in the swim-lane at the bottom.
- Based on the current State and the Command it received, the Decider will make new decisions/Events/`E`
- New Events will update/evolve the State of the Decider (yellow), and the View (green)
- The View state is constructed per need to serve specific pages with data. Every page can have its View.

'FModel' is offering implementation of this blueprint in a very general way.
The implementation is parametrized with C/`Command`, E/`Event`, and S/`State` parameters. 

The responsibility of the business is to specialize in their case by specifying concrete Commands, Events, and State.
For example, `Commands`=CreateOrder, MarkOrderAsPrepared; `Events`=OrderCreated, OrderPrepared, `State`=Order(with list
of Items).

![restaurant model](/img/restaurant-model.jpg)

:::note
*Customers use the web application to place food orders at local restaurants. Application coordinates a
restaurant/kitchen order preparation.*
:::

Let's learn how to [structure the data/information](structuring-the-data.md) and how to
effectively [model the behaviour](modeling-the-behaviour.md) in Kotlin, by example!

