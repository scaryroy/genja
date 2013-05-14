package genja.transform;

import japa.parser.ast.stmt.BreakStmt;
import japa.parser.ast.stmt.Statement;
import japa.parser.ast.visitor.GenericVisitorAdapter;

/**
 * The labeled break transformer rewrites all breaks with labels and replaces them with jumps.
 */
class LabeledBreakTransform extends GenericVisitorAdapter<Statement, Generator> {
    public Statement visit(BreakStmt n, Generator s) {
        if (n.getId() != null && s.labels.containsKey(n.getId())) {
            // Generate a jump to another label, if the label is inside our state machine.
            //
            // TODO: Ensure we're not jumping somewhere really weird.
            return Generator.generateJump(s.labels.get(n.getId()).breakPoint);
        }

        // Otherwise we just move the statement out.
        return n;
    }
}