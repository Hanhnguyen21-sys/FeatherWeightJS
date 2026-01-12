# FeatherWeightJS

Featherweight JavaScript (FWJS) is a minimal version of JavaScript designed to
illustrate core language features such as expressions, functions, closures,
and scoping.

---

## Language Features

### Expressions
FWJS supports the following expressions:

- If expressions  
  Evaluating an `if` expression returns a value (unlike JavaScript).

- While expressions  
  Evaluating a `while` expression returns `null`.

- Sequence expressions  
  Multiple expressions may be written in sequence.

- Binary operations  
  Examples include addition, subtraction, and other arithmetic operations.

- Variable declarations

- Assignment statements

- Function definitions  
  Evaluating a function returns a closure.

- Function application  
  Functions support implicit returns, similar to Scheme and Ruby.

- Print expressions  
  Similar to `console.log` in JavaScript; evaluates an expression and prints the result.

---

## Values

The language supports the following value types:

- Booleans
- Integers
- A special `null` value
- Closures

A closure consists of:
- A function definition
- The environment in which the function was defined
- Any free variables referenced in the function body

---

## Implementation Overview

### Expression Evaluation

- `Expression.java` contains representations of all FWJS expressions.
- Each expression class implements an `evaluate` method.
- The `evaluate` method:
  - Takes an `Environment`
  - Returns a `Value`
- All value types are defined in `Value.java`.

---

### Functions and Closures

- `FunctionDeclExpr`
  - Takes a list of parameter names and a function body
  - Evaluates to a closure that captures its defining environment

- `FunctionAppExpr`
  - Takes:
    - An expression that evaluates to a closure
    - A list of argument expressions
  - When evaluated:
    - The current environment is ignored
    - A new environment is created
    - Parameters are bound to evaluated argument values
    - The closure is then applied

---

### Variable Scoping and Environments

- `Environment.java` manages variable resolution and updates.

Variable lookup rules:

- If a variable is not found in the current environment:
  - Look in the outer environment
- If a variable is unresolved in the global environment:
  - Return `null`
- If a variable is updated but undefined in the global scope:
  - Create a new global variable

This behavior mirrors JavaScript, where omitting `var` may create a global variable.

---

## Build and Run Instructions

This project uses a `Makefile` to automate common tasks.

### Available Commands

- Generate the parser:
  ```bash
  make generate
