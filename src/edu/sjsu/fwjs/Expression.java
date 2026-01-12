package edu.sjsu.fwjs;

import java.util.ArrayList;
import java.util.List;

/**
 * FWJS expressions.
 */
public interface Expression {
    /**
     * Evaluate the expression in the context of the specified environment.
     */
    public Value evaluate(Environment env);
}

// NOTE: Using package access so that all implementations of Expression
// can be included in the same file.

/**
 * Constant literal
 */
class ValueExpr implements Expression {
    private Value val;

    public ValueExpr(Value v) {
        this.val = v;
    }

    public Value evaluate(Environment env) {
        return this.val;
    }
}

/**
 * Expressions that are a FWJS variable.
 */
class VarExpr implements Expression {
    private String varName;

    public VarExpr(String varName) {
        this.varName = varName;
    }

    public Value evaluate(Environment env) {
        Value r = env.resolveVar(varName);
        if (r == null) {
            return new NullVal();
        } else {
            return r;
        }
    }
}

/**
 * A print expression.
 */
class PrintExpr implements Expression {
    private Expression exp;

    public PrintExpr(Expression exp) {
        this.exp = exp;
    }

    public Value evaluate(Environment env) {
        Value v = exp.evaluate(env);
        System.out.println(v.toString());
        return v;
    }
}

/**
 * Binary operators (+, -, *, etc).
 * Currently only numbers are supported.
 */
class BinOpExpr implements Expression {
    private Op op;
    private Expression e1;
    private Expression e2;

    public BinOpExpr(Op op, Expression e1, Expression e2) {
        this.op = op;
        this.e1 = e1;
        this.e2 = e2;
    }

    @SuppressWarnings("incomplete-switch")
    public Value evaluate(Environment env) {
        Value varE1 = e1.evaluate(env);
        Value varE2 = e2.evaluate(env);

        // Handle EQ for all types first (before the IntVal check)
        if (op == Op.EQ) {
            // Check if both are null
            if (varE1 instanceof NullVal && varE2 instanceof NullVal) {
                return new BoolVal(true);
            }
            // Check if one is null and the other isn't
            if (varE1 instanceof NullVal || varE2 instanceof NullVal) {
                return new BoolVal(false);
            }
            // For IntVals, compare their values
            if (varE1 instanceof IntVal && varE2 instanceof IntVal) {
                int num1 = ((IntVal) varE1).toInt();
                int num2 = ((IntVal) varE2).toInt();
                return new BoolVal(num1 == num2);
            }
            // For BoolVals, compare their values
            if (varE1 instanceof BoolVal && varE2 instanceof BoolVal) {
                boolean bool1 = ((BoolVal) varE1).toBoolean();
                boolean bool2 = ((BoolVal) varE2).toBoolean();
                return new BoolVal(bool1 == bool2);
            }
            // For other types (ClosureVal, etc.), use reference equality
            return new BoolVal(varE1 == varE2);
        }

        // Rest of your code for numeric operations
        if (varE1 instanceof IntVal && varE2 instanceof IntVal) {
            int num1 = ((IntVal) varE1).toInt();
            int num2 = ((IntVal) varE2).toInt();

            switch (op) {
                case ADD:
                    return new IntVal(num1 + num2);
                case SUBTRACT:
                    return new IntVal(num1 - num2);
                case MULTIPLY:
                    return new IntVal(num1 * num2);
                case DIVIDE:
                    if (num2 == 0) {
                        throw new RuntimeException("Division by zero");
                    }
                    return new IntVal(num1 / num2);
                case MOD:
                    return new IntVal(num1 % num2);
                case GT:
                    return new BoolVal(num1 > num2);
                case GE:
                    return new BoolVal(num1 >= num2);
                case LT:
                    return new BoolVal(num1 < num2);
                case LE:
                    return new BoolVal(num1 <= num2);
            }
        }

        throw new RuntimeException("Type error in binary operation");
    }
}

/**
 * If-then-else expressions.
 * Unlike JS, if expressions return a value.
 */
class IfExpr implements Expression {
    private Expression cond;
    private Expression thn;
    private Expression els; // can be null

    public IfExpr(Expression cond, Expression thn, Expression els) {
        this.cond = cond;
        this.thn = thn;
        this.els = els;
    }

    @Override
    public Value evaluate(Environment env) {

        Value c = cond.evaluate(env);

        if (!(c instanceof BoolVal)) {
            throw new RuntimeException("Condition in if expression must be a boolean");
        }

        boolean b = ((BoolVal) c).toBoolean();
        if (b) {
            return thn.evaluate(env);
        } else if (els != null) {
            // evaluate else if it exists
            return els.evaluate(env);
        } else {
            // If there's no else clause, just return NullVal
            return new NullVal();
        }
    }
}

/**
 * While statements (treated as expressions in FWJS, unlike JS).
 */
class WhileExpr implements Expression {
    private Expression cond;
    private Expression body;

    public WhileExpr(Expression cond, Expression body) {
        this.cond = cond;
        this.body = body;
    }

    public Value evaluate(Environment env) {
        Value c = cond.evaluate(env);
        if (!(c instanceof BoolVal)) {
            throw new RuntimeException("Condition should be boolean type");
        }
        boolean b = ((BoolVal) c).toBoolean();
        Value lastStep = null;
        while (b) {
            lastStep = body.evaluate(env);
            // updating condition
            c = cond.evaluate(env);
            if (!(c instanceof BoolVal)) {
                throw new RuntimeException("Condition should be boolean type");
            }
            b = ((BoolVal) c).toBoolean();
        }
        return lastStep;
    }
}

/**
 * Sequence expressions (i.e. 2 back-to-back expressions).
 */
class SeqExpr implements Expression {
    private Expression e1;
    private Expression e2;

    public SeqExpr(Expression e1, Expression e2) {
        this.e1 = e1;
        this.e2 = e2;
    }

    public Value evaluate(Environment env) {
        e1.evaluate(env);
        Value result2 = e2.evaluate(env);
        return result2;
    }
}

/**
 * Declaring a variable in the local scope.
 */
class VarDeclExpr implements Expression {
    private String varName;
    private Expression exp;

    public VarDeclExpr(String varName, Expression exp) {
        this.varName = varName;
        this.exp = exp;
    }

    public Value evaluate(Environment env) {
        Value v = exp.evaluate(env);
        env.createVar(varName, v);
        return v;
    }
}

/**
 * Updating an existing variable.
 * If the variable is not set already, it is added
 * to the global scope.
 */
class AssignExpr implements Expression {
    private String varName;
    private Expression e;

    public AssignExpr(String varName, Expression e) {
        this.varName = varName;
        this.e = e;
    }

    public Value evaluate(Environment env) {
        Value val = e.evaluate(env);
        // Updating an existing variable.
        if (env.resolveVar(varName) != null) {
            env.updateVar(varName, val);
        } else {
            // create new one in global scope
            env.createVar(varName, val);
        }

        return val;
    }
}

/**
 * A function declaration, it returns a closure included
 * the environment used when it was defined.
 */
class FunctionDeclExpr implements Expression {
    private List<String> params;
    private Expression body;

    public FunctionDeclExpr(List<String> params, Expression body) {
        this.params = params;
        this.body = body;
    }

    public Value evaluate(Environment env) {
        // evaluate function body
        ClosureVal closure = new ClosureVal(params, body, env);
        return closure;
    }
}

/**
 * Function application/ Call function
 */
class FunctionAppExpr implements Expression {
    private Expression f;
    private List<Expression> args;

    public FunctionAppExpr(Expression f, List<Expression> args) {
        this.f = f;
        this.args = args;
    }

    public Value evaluate(Environment env) {
        // evaluate to get closure (ClosureVal)
        Value v = f.evaluate(env);
        // make sure v is ClosureVal
        if (!(v instanceof ClosureVal)) {
            throw new RuntimeException("Should be a function to be called");
        }
        ClosureVal closure = (ClosureVal) v;
        // convert from a list of Expression to a list of Value
        // which means evaluating expressions
        List<Value> val_args = new ArrayList<>();
        for (int i = 0; i < args.size(); i++) {
            val_args.add(args.get(i).evaluate(env));
        }
        return closure.apply(val_args);
    }
}
