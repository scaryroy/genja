package genja.transform;

import japa.parser.ast.Node;
import japa.parser.ast.stmt.BreakStmt;
import japa.parser.ast.stmt.ContinueStmt;
import japa.parser.ast.visitor.ModifierVisitorAdapter;

/**
 * The intra-loop jump transform turns all breaks and continues found inside a
 * loop into jumps.
 */
class IntraLoopJumpTransform extends ModifierVisitorAdapter<Label> {
    Generator s;

    public IntraLoopJumpTransform(Generator s) {
        this.s = s;
    }

    @Override
    public Node visit(BreakStmt n, Label loop) {
        if (n.getId() == null) {
            // This is probably a jump break or something.
            return n;
        }

        if (!n.getId().equals(".loop")) {
            // This gets computed in the LabeledBreakTransform, so we don't care about it here.
            return n;
        }

        return loop.generateBreakJump();
    }

    @Override
    public Node visit(ContinueStmt n, Label loop) {
        if (n.getId() == null) {
            return n;
        }

        if (!n.getId().equals(".loop")) {
            // TODO: Ensure we're not jumping somewhere really weird.
            return Generator.generateJump(s.labels.get(n.getId()).continuePoint);
        }

        return loop.generateContinueJump();
    }
}