package edu.sjsu.fwjs;

import java.util.Map;

import java.util.HashMap;
// Environment.java handles the resolution of JavaScript variables.  Update the resolveVar and updateVar methods to handle variable lookup correctly.
// *If a variable is undefined in the current environment, look for it in the outer scope.
//   (This holds for both updateVar and resolveVar).
// *When resolving a variable that is undefined in the global scope, return null.  (In JavaScript, we would
//   return undefined, but we will simplify the behavior).
// *When updating a variable that is undefined in the global scope, create a new variable.
//   (JavaScript follows this behavior; hence, if you forget to type 'var' when creating a new local
//    variable, you might create a global variable instead).

public class Environment {
    private Map<String, Value> env = new HashMap<String, Value>();
    private Environment outerEnv;

    /**
     * Constructor for global environment
     */
    public Environment() {
    }

    /**
     * Constructor for local environment of a function
     */
    public Environment(Environment outerEnv) {
        this.outerEnv = outerEnv;
    }

    /**
     * Find a value of variable
     * If the variable name is in the current scope, it is returned.
     * Otherwise, search for the variable in the outer scope.
     * If we are at the outermost scope (global scope)
     * null is returned
     */
    public Value resolveVar(String varName) {
        // If the variable name is in the current scope, it is returned.
        if (this.env.containsKey(varName)) {
            return env.get(varName);
        }
        if (outerEnv != null) {
            // outerEnv is an object of Environment => can call method in Environment class
            return outerEnv.resolveVar(varName);
        }
        return null;
    }

    /**
     * Updating existing variables.
     * if a variable not found -> create new one in global scope
     */
    public void updateVar(String key, Value v) {
        // updating current scope
        if (this.env.containsKey(key)) {
            env.put(key, v);
        } else if (outerEnv != null) {
            outerEnv.updateVar(key, v);
        } else {
            // put in global scope
            env.put(key, v);
        }
    }

    /**
     * Creates a new variable in the local scope.
     * If the variable has been defined in the current scope previously,
     * a RuntimeException is thrown.
     */
    public void createVar(String key, Value v) {
        // if already defined -> throw error
        if (this.env.containsKey(key)) {
            throw new RuntimeException("Variable already in current scope");
        }
        this.env.put(key, v);
    }
}
