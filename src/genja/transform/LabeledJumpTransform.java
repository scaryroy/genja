package genja.transform;

import japa.parser.ast.Node;
import japa.parser.ast.stmt.BreakStmt;
import japa.parser.ast.stmt.ContinueStmt;
import japa.parser.ast.visitor.ModifierVisitorAdapter;

/**
 * The labeled break transformer rewrites all breaks with labels and replaces them with jumps.
 */
class LabeledJumpTransform extends ModifierVisitorAdapter<Generator> {
    @Override
    public Node visit(BreakStmt n, Generator s) {
        if (n.getId() != null && s.labels.containsKey(n.getId())) {
            // Generate a jump to another label, if the label is inside our state machine.
            return Generator.generateJump(s.labels.get(n.getId()).breakPoint);
        }

        // Otherwise we just move the statement out.
        return n;
    }

    @Override
    public Node visit(ContinueStmt n, Generator s) {
        if (n.getId() != null && s.labels.containsKey(n.getId())) {
            return Generator.generateJump(s.labels.get(n.getId()).continuePoint);
        }

         // Otherwise we just move the statement out.
        return n;
    }
}