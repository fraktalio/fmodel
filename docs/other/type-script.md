---
sidebar_position: 2
---

# TypeScript

[TypeScript](https://www.typescriptlang.org/) understands JavaScript and uses type inference to give you great tooling without additional code.

TypeScript is ideal thanks to its language features and type system, which enforce correctness and reduce the likelihood of bugs. 
By modeling the domain accurately, we aim to use the TypeScript transpiler to catch errors early and prevent them from propagating to runtime.

 - You can find the source code for the `fmodel-ts` [here](https://github.com/fraktalio/fmodel-ts)
 - Publicly available at [npmjs.com](https://www.npmjs.com/package/@fraktalio/fmodel-ts)

## Decide

`(command: C, state: S) => readonly E[]`

On a higher level of abstraction, any information system is responsible for handling the intent (`Command`) and based on
the current `State`, produce new facts (`Events`):

- given the current `State/S` *on the input*,
- when `Command/C` is handled *on the input*,
- expect `list` of new `Events/E` to be published/emitted *on the output*

## Evolve

`(state: S, event: E) => S`

The new state is always evolved out of the current state `S` and the current event `E`:

- given the current `State/S` *on the input*,
- when `Event/E` is handled *on the input*,
- expect new `State/S` to be published *on the output*

Two functions are wrapped in a datatype class (algebraic data structure), which is generalized with three generic
parameters:

```typescript
export class Decider<C, S, E> implements IDecider<C, S, E> {
    constructor(
        readonly decide: (c: C, s: S) => readonly E[],
        readonly evolve: (s: S, e: E) => S,
        readonly initialState: S
    ) {
    }
}
```

## Further reading

[**Read more**](https://fraktalio.com/fmodel-ts/)