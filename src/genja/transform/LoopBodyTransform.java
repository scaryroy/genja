package genja.transform;

import java.util.ArrayList;
import java.util.List;

import japa.parser.ast.stmt.BlockStmt;
import japa.parser.ast.stmt.BreakStmt;
import japa.parser.ast.stmt.ContinueStmt;
import japa.parser.ast.stmt.Statement;
import japa.parser.ast.stmt.SwitchEntryStmt;
import japa.parser.ast.visitor.GenericVisitorAdapter;

/**
 * The loop body rewriter finds all the continues and breaks and replaces them with the correct
 * jumps.
 */
class LoopBodyTransform extends GenericVisitorAdapter<List<Statement>, LoopMarker> {
    Generator s;

    public LoopBodyTransform(Generator s) {
        this.s = s;
    }

    private void replaceStatements(List<Statement> stmts, LoopMarker loop) {
        List<Statement> out = new ArrayList<Statement>();

        for (Statement s : stmts) {
            List<Statement> result = s.accept(this, loop);
            if (result == null) {
                out.add(s);
            } else {
                // We want to replace some nodes here, apparently.
                out.addAll(result);
            }
        }

        stmts.clear();
        stmts.addAll(out);
    }

    public List<Statement> visit(SwitchEntryStmt n, LoopMarker loop) {
        if (n.getStmts() != null) {
            replaceStatements(n.getStmts(), loop);
        }
        return null;
    }

    public List<Statement> visit(BlockStmt n, LoopMarker loop) {
        if (n.getStmts() != null) {
            replaceStatements(n.getStmts(), loop);
        }
        return null;
    }

    public List<Statement> visit(BreakStmt n, LoopMarker loop) {
        if (n.getId() == null) {
            // This is probably a jump break or something.
            return null;
        }

        if (!n.getId().equals(".loop")) {
            // This gets computed in the LabeledBreakTransform, so we don't care about it here.
            return null;
        }

        return loop.generateBreakJump();
    }

    public List<Statement> visit(ContinueStmt n, LoopMarker loop) {
        if (n.getId() == null) {
            return null;
        }

        if (!n.getId().equals(".loop")) {
            // TODO: Ensure we're not jumping somewhere really weird.
            return Generator.generateJump(s.labels.get(n.getId()).continuePoint);
        }
        return loop.generateContinueJump();
    }
}