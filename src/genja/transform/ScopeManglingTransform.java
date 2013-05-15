package genja.transform;

import genja.transform.BlockScope.TypedVariableDeclarator;

import java.util.Map;

import japa.parser.ast.Node;
import japa.parser.ast.body.VariableDeclarator;
import japa.parser.ast.expr.Expression;
import japa.parser.ast.expr.NameExpr;
import japa.parser.ast.expr.VariableDeclarationExpr;
import japa.parser.ast.stmt.BlockStmt;
import japa.parser.ast.stmt.ExpressionStmt;
import japa.parser.ast.visitor.ModifierVisitorAdapter;

/**
 * The scope mangling transform renames all variables to move them out of
 * variable scope into class scope.
 */
public class ScopeManglingTransform extends ModifierVisitorAdapter<Map<String, TypedVariableDeclarator>> {
    /**
     * The scope we're transforming.
     */
    BlockScope block;

    /**
     * The next scope number for allocation. This gets reset to 0 when we enter a new scope, and
     * incremented when we exit one.
     */
    int nextBlock;

    public ScopeManglingTransform() {
        this.block = null;
        this.nextBlock = 0;
    }

    /**
     * Enter a scope. This should be paired with exitBlock.
     */
    void enterBlock() {
        this.block = new BlockScope(this.nextBlock, this.block);
        this.nextBlock = 0;
    }

    /**
     * Exit a scope.
     */
    void exitBlock() {
        this.nextBlock = this.block.num + 1;
        this.block = this.block.back;
    }

    @Override
    public Node visit(BlockStmt n, Map<String, TypedVariableDeclarator> arg) {
        enterBlock();
        try {
            return super.visit(n, arg);
        } finally {
            exitBlock();
        }
    }

    @Override
    public Node visit(ExpressionStmt n, Map<String, TypedVariableDeclarator> arg) {
        Node r = n.getExpression().accept(this, arg);
        if (r == null) {
            return null;
        }
        return new ExpressionStmt((Expression) r);
    }

    @Override
    public Node visit(VariableDeclarationExpr n, Map<String, TypedVariableDeclarator> s) {
        String prefix = this.block.getScopePrefix();
        for (VariableDeclarator d : n.getVars()) {
            // We introduce the typed variable in two places: the full name in
            // map we're carrying around, and the scoped name in the block.
            TypedVariableDeclarator t = new TypedVariableDeclarator(n.getType(), d);
            s.put(prefix + d.getId().getName(), t);
            this.block.vars.put(d.getId().getName(), t);
        }
        return null;
    }

    @Override
    public Node visit(NameExpr n, Map<String, TypedVariableDeclarator> s) {
        BlockScope block = this.block.findScopeForVariable(n.getName());
        if (block == null) {
            return n;
        }
        String name = block.getScopePrefix() + n.getName();
        return new NameExpr(name);
    }
}
