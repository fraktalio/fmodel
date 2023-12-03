---
sidebar_position: 1
---


# Rust

Domain modeling, influenced by functional programming principles, aims to represent the business domain in the code accurately.
[Rust](https://www.rust-lang.org/) is ideal thanks to its ownership model and type system, which enforce correctness and reliability - enabling you to eliminate many classes of bugs at compile-time, guarantying memory-safety and thread-safety.

 - You can find the source code for the `fmodel-rust` [here](https://github.com/fraktalio/fmodel-rust)
 - Publicly available at [crates.io](https://crates.io/crates/fmodel-rust) and 
 - [docs.rs](https://docs.rs/fmodel-rust/latest/fmodel_rust/)

## Decide

`type DecideFunction<'a, C, S, E> = Box<dyn Fn(&C, &S) -> Vec<E> + 'a + Send + Sync>`

On a higher level of abstraction, any information system is responsible for handling the intent (`Command`) and based on
the current `State`, produce new facts (`Events`):

- given the current `State/S` *on the input*,
- when `Command/C` is handled *on the input*,
- expect `Vec` of new `Events/E` to be published/emitted *on the output*

## Evolve

`type EvolveFunction<'a, S, E> = Box<dyn Fn(&S, &E) -> S + 'a + Send + Sync>`

The new state is always evolved out of the current state `S` and the current event `E`:

- given the current `State/S` *on the input*,
- when `Event/E` is handled *on the input*,
- expect new `State/S` to be published *on the output*

Two functions are wrapped in a datatype class (algebraic data structure), which is generalized with three generic
parameters:

```rust
pub struct Decider<'a, C: 'a, S: 'a, E: 'a> {
    pub decide: DecideFunction<'a, C, S, E>,
    pub evolve: EvolveFunction<'a, S, E>,
    pub initial_state: InitialStateFunction<'a, S>,
}
```

## Further reading

[**Read more**](https://docs.rs/fmodel-rust/latest/fmodel_rust/)