package genja.transform;

import java.util.ArrayList;
import java.util.List;

import japa.parser.ast.Node;
import japa.parser.ast.body.VariableDeclarator;
import japa.parser.ast.expr.AssignExpr;
import japa.parser.ast.expr.Expression;
import japa.parser.ast.expr.NameExpr;
import japa.parser.ast.expr.UnaryExpr;
import japa.parser.ast.expr.VariableDeclarationExpr;
import japa.parser.ast.stmt.BlockStmt;
import japa.parser.ast.stmt.BreakStmt;
import japa.parser.ast.stmt.DoStmt;
import japa.parser.ast.stmt.ExpressionStmt;
import japa.parser.ast.stmt.ForStmt;
import japa.parser.ast.stmt.IfStmt;
import japa.parser.ast.stmt.Statement;
import japa.parser.ast.stmt.WhileStmt;
import japa.parser.ast.visitor.ModifierVisitorAdapter;

public class LoopDesugarTransform extends ModifierVisitorAdapter<Void> {
    /**
     * Make a loop condition enforced.
     */
    private static Statement makeLoopCondition(Expression cond) {
        if (cond == null) return null;

        return new IfStmt(new UnaryExpr(cond, UnaryExpr.Operator.not),
                          new BreakStmt(null), null);
    }

    /**
     * Make an infinite loop expression. This is the only kind of loop compatible with
     * transformLoop.
     */
    public static Statement makeLoopStmt(Statement body) {
        return new ForStmt(null, null, null, body);
    }

    @Override
    public Node visit(DoStmt n, Void arg) {
        super.visit(n, arg);

        // Reword the loop.
        List<Statement> stmts = new ArrayList<Statement>();
        stmts.add(n.getBody());
        stmts.add(makeLoopCondition(n.getCondition()));
        BlockStmt b = new BlockStmt(stmts);
        return makeLoopStmt(b);
    }

    @Override
    public Node visit(WhileStmt n, Void arg) {
        super.visit(n, arg);

        // Reword the loop.
        List<Statement> stmts = new ArrayList<Statement>();
        stmts.add(makeLoopCondition(n.getCondition()));
        stmts.add(n.getBody());
        BlockStmt b = new BlockStmt(stmts);
        return makeLoopStmt(b);
    }

    @Override
    public Node visit(ForStmt n, Void arg) {
        super.visit(n, arg);

        if (n.getInit() != null || n.getCompare() != null ||
            n.getUpdate() != null) {
            // Reword the loop.
            List<Statement> stmts = new ArrayList<Statement>();

            // We initialize in a scope above the actual loop body.
            for (Expression e : n.getInit()) {
                stmts.add(new ExpressionStmt(e));

                if (e.getClass().equals(VariableDeclarationExpr.class)) {
                    VariableDeclarationExpr decl = (VariableDeclarationExpr) e;
                    for (VariableDeclarator declarator : decl.getVars()) {
                        if (declarator.getInit() != null) {
                            stmts.add(new ExpressionStmt(
                                    new AssignExpr(new NameExpr(declarator.getId().getName()),
                                                   declarator.getInit(),
                                                   AssignExpr.Operator.assign)));
                        }
                    }
                }
            }

            // We move the check, body and update into a sub-block.
            List<Statement> substmts = new ArrayList<Statement>();
            Statement loopCond = makeLoopCondition(n.getCompare());
            if (loopCond != null)
                substmts.add(loopCond);
            substmts.add(n.getBody());

            for (Expression e : n.getUpdate()) {
                substmts.add(new ExpressionStmt(e));
            }

            stmts.add(makeLoopStmt(new BlockStmt(substmts)));

            return new BlockStmt(stmts);
        }

        return n;
    }

}
