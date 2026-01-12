package edu.sjsu.fwjs;

import java.util.ArrayList;
import java.util.List;

import edu.sjsu.fwjs.parser.FeatherweightJavaScriptBaseVisitor;
import edu.sjsu.fwjs.parser.FeatherweightJavaScriptParser;

public class ExpressionBuilderVisitor extends FeatherweightJavaScriptBaseVisitor<Expression> {
    @Override
    public Expression visitProg(FeatherweightJavaScriptParser.ProgContext ctx) {
        List<Expression> stmts = new ArrayList<Expression>();
        for (int i = 0; i < ctx.stat().size(); i++) {
            Expression exp = visit(ctx.stat(i));
            if (exp != null)
                stmts.add(exp);
        }
        return listToSeqExp(stmts);
    }

    @Override
    public Expression visitBareExpr(FeatherweightJavaScriptParser.BareExprContext ctx) {
        return visit(ctx.expr());
    }

    @Override
    public Expression visitIfThenElse(FeatherweightJavaScriptParser.IfThenElseContext ctx) {
        Expression cond = visit(ctx.expr());
        Expression thn = visit(ctx.block(0));
        Expression els = visit(ctx.block(1));
        return new IfExpr(cond, thn, els);
    }

    @Override
    public Expression visitIfThen(FeatherweightJavaScriptParser.IfThenContext ctx) {
        Expression cond = visit(ctx.expr());
        Expression thn = visit(ctx.block());
        return new IfExpr(cond, thn, null);
    }

    @Override
    public Expression visitWhileStatement(FeatherweightJavaScriptParser.WhileStatementContext ctx) {
        Expression cond = visit(ctx.expr());
        Expression body = visit(ctx.block());
        return new WhileExpr(cond, body);
    }

    @Override
    public Expression visitEmptyStatement(FeatherweightJavaScriptParser.EmptyStatementContext ctx) {
        return null;
    }

    @Override
    public Expression visitPrintStatement(FeatherweightJavaScriptParser.PrintStatementContext ctx) {
        return new PrintExpr(visit(ctx.expr()));
    }

    @Override
    public Expression visitVarDeclaration(FeatherweightJavaScriptParser.VarDeclarationContext ctx) {
        String name = ctx.ID().getText();
        Expression value = visit(ctx.assignment());
        return new VarDeclExpr(name, value);
    }

    @Override
    public Expression visitVarReference(FeatherweightJavaScriptParser.VarReferenceContext ctx) {
        String name = ctx.ID().getText();
        Expression value = visit(ctx.assignment());
        return new AssignExpr(name, value);
    }

    @Override
    public Expression visitOperationExpr(FeatherweightJavaScriptParser.OperationExprContext ctx) {
        return visit(ctx.compare());
    }

    // function change operation to text. Ex: + -> ADD in the Op numerate
    private Op op_to_text(String text) {
        switch (text) {
            case "+":
                return Op.ADD;
            case "-":
                return Op.SUBTRACT;
            case "*":
                return Op.MULTIPLY;
            case "/":
                return Op.DIVIDE;
            case "%":
                return Op.MOD;
            case ">":
                return Op.GT;
            case ">=":
                return Op.GE;
            case "<":
                return Op.LT;
            case "<=":
                return Op.LE;
            case "==":
                return Op.EQ;
            default:
                throw new RuntimeException("Unknown operator: " + text);
        }
    }

    @Override
    public Expression visitCompare(FeatherweightJavaScriptParser.CompareContext ctx) {
        Expression left = visit(ctx.addSub(0));
        for (int i = 1; i < ctx.addSub().size(); i++) {
            Expression right = visit(ctx.addSub(i));
            String opText = ctx.getChild(2 * i - 1).getText(); // operator between terms
            left = new BinOpExpr(op_to_text(opText), left, right);
        }
        return left;
    }

    @Override
    public Expression visitAddSub(FeatherweightJavaScriptParser.AddSubContext ctx) {
        Expression left = visit(ctx.mulDiv(0));
        for (int i = 1; i < ctx.mulDiv().size(); i++) {
            Expression right = visit(ctx.mulDiv(i));
            String opText = ctx.getChild(2 * i - 1).getText();
            left = new BinOpExpr(op_to_text(opText), left, right);
        }
        return left;
    }

    @Override
    public Expression visitMulDiv(FeatherweightJavaScriptParser.MulDivContext ctx) {
        Expression left = visit(ctx.callExpr(0));
        for (int i = 1; i < ctx.callExpr().size(); i++) {
            Expression right = visit(ctx.callExpr(i));
            String opText = ctx.getChild(2 * i - 1).getText();
            left = new BinOpExpr(op_to_text(opText), left, right);
        }
        return left;
    }

    @Override
    public Expression visitPrimary(FeatherweightJavaScriptParser.PrimaryContext ctx) {
        if (ctx.INT() != null)
            return new ValueExpr(new IntVal(Integer.parseInt(ctx.INT().getText())));
        if (ctx.BOOL() != null)
            return new ValueExpr(new BoolVal(Boolean.parseBoolean(ctx.BOOL().getText())));
        if (ctx.NULL() != null)
            return new ValueExpr(new NullVal());
        if (ctx.ID() != null)
            return new VarExpr(ctx.ID().getText());
        if (ctx.expr() != null)
            return visit(ctx.expr());
        if (ctx.funcExpr() != null)
            return visit(ctx.funcExpr());
        return null;
    }

    @Override
    public Expression visitCallExpr(FeatherweightJavaScriptParser.CallExprContext ctx) {
        // primary ( '(' argsList? ')' )*
        // primary part of function
        Expression first = visit(ctx.primary());
        // number of time that function is called
        int cnt = ctx.LPAREN().size();
        for (int i = 0; i < cnt; i++) {
            List<Expression> args = new ArrayList<>();
            FeatherweightJavaScriptParser.ArgsListContext al = ctx.argsList(i);
            if (al != null) {
                for (FeatherweightJavaScriptParser.ExprContext e : al.expr()) {
                    args.add(visit(e));
                }
            }

            first = new FunctionAppExpr(first, args);
        }
        return first;
    }

    @Override
    public Expression visitFuncExpr(FeatherweightJavaScriptParser.FuncExprContext ctx) {
        List<String> params = new ArrayList<>();
        if (ctx.parametersList() != null) {
            for (org.antlr.v4.runtime.tree.TerminalNode id : ctx.parametersList().ID()) {
                params.add(id.getText());
            }
        }
        Expression body = visit(ctx.block());
        return new FunctionDeclExpr(params, body);
    }

    @Override
    public Expression visitFullBlock(FeatherweightJavaScriptParser.FullBlockContext ctx) {
        List<Expression> stmts = new ArrayList<Expression>();
        for (int i = 1; i < ctx.getChildCount() - 1; i++) {
            Expression exp = visit(ctx.getChild(i));
            stmts.add(exp);
        }
        return listToSeqExp(stmts);
    }

    /**
     * Converts a list of expressions to one sequence expression,
     * if the list contained more than one expression.
     */
    private Expression listToSeqExp(List<Expression> stmts) {
        if (stmts.isEmpty())
            return null;
        Expression exp = stmts.get(0);
        for (int i = 1; i < stmts.size(); i++) {
            exp = new SeqExpr(exp, stmts.get(i));
        }
        return exp;
    }

    @Override
    public Expression visitSimpBlock(FeatherweightJavaScriptParser.SimpBlockContext ctx) {
        return visit(ctx.stat());
    }
}