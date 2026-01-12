package edu.sjsu.fwjs;

import java.util.List;

/**
 * Values in FWJS.
 * Evaluating a FWJS expression should return a FWJS value.
 */
public interface Value {
}

// NOTE: Using package access so that all implementations of Value
// can be included in the same file.

/**
 * Boolean values.
 */
class BoolVal implements Value {
    private boolean boolVal;

    public BoolVal(boolean b) {
        this.boolVal = b;
    }

    public boolean toBoolean() {
        return this.boolVal;
    }

    @Override
    public boolean equals(Object that) {
        if (!(that instanceof BoolVal))
            return false;
        return this.boolVal == ((BoolVal) that).boolVal;
    }

    @Override
    public String toString() {
        return "" + this.boolVal;
    }
}

/**
 * Numbers. Only integers are supported.
 */
class IntVal implements Value {
    private int i;

    public IntVal(int i) {
        this.i = i;
    }

    public int toInt() {
        return this.i;
    }

    @Override
    public boolean equals(Object that) {
        if (!(that instanceof IntVal))
            return false;
        return this.i == ((IntVal) that).i;
    }

    @Override
    public String toString() {
        return "" + this.i;
    }
}

class NullVal implements Value {
    @Override
    public boolean equals(Object that) {
        return (that instanceof NullVal);
    }

    @Override
    public String toString() {
        return "null";
    }
}

/**
 * A closure: remembers variables in surrounding scope.
 * Ex: let x = 10;
 * function f(y) { return x + y; }
 * it remembers x =10 => f(4) = 14
 * that why we need outerEnv
 */
class ClosureVal implements Value {
    private List<String> params;
    private Expression body; // represents code inside function
    private Environment outerEnv; // env where function is defined

    /**
     * The environment is the environment where the function was created.
     * This design is what makes this expression a closure.
     */
    public ClosureVal(List<String> params, Expression body, Environment env) {
        this.params = params;
        this.body = body;
        this.outerEnv = env;
    }

    public String toString() {
        String s = "function(";
        String sep = "";
        for (int i = 0; i < params.size(); i++) {
            s += sep + params.get(i);
            sep = ",";
        }
        s += ") {...};";
        return s;
    }

    /**
     * To apply a closure, first create a new local environment, with an outer scope
     * of the environment where the function was created. Each parameter should
     * be bound to its matching argument and added to the new local environment.
     * Ex: function add(x, y) {return x + y;}
     * } * add(3,5); => match x with 3 and y with 5
     */
    public Value apply(List<Value> argVals) {
        // create a new local environment
        Environment local = new Environment(this.outerEnv);
        // bouding each parameter name ot its argument value
        for (int i = 0; i < params.size(); i++) {
            local.createVar(params.get(i), argVals.get(i));
        }
        return body.evaluate(local);
    }

}
