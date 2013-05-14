package genja.transform;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import japa.parser.ast.expr.AssignExpr;
import japa.parser.ast.expr.IntegerLiteralExpr;
import japa.parser.ast.expr.NameExpr;
import japa.parser.ast.stmt.BreakStmt;
import japa.parser.ast.stmt.ExpressionStmt;
import japa.parser.ast.stmt.Statement;
import japa.parser.ast.stmt.SwitchEntryStmt;
import japa.parser.ast.stmt.SwitchStmt;

/**
 * The generator class contains the state required to create a generator, as well as utility
 * methods for populating its state.
 */
public class Generator {
    /**
     * The variable name containing the current state number.
     */
    public static final NameExpr STATE_VAR = new NameExpr(-1, -1, "$state");

    /**
     * The variable name containing the currently yielded value.
     */
    public static final NameExpr CURRENT_VAR = new NameExpr(-1, -1, "$current");

    /**
     * The current loop we're transforming.
     */
    LoopMarker loop;

    /**
     * The scope we're transforming.
     */
    BlockScope block;

    /**
     * The states.
     */
    List<SwitchEntryStmt> states;

    /**
     * List of labels.
     */
    Map<String, TransitionPoint> labels;

    /**
     * The next scope number for allocation. This gets reset to 0 when we enter a new scope, and
     * incremented when we exit one.
     */
    int nextBlock;

    /**
     * Create a new generator.
     */
    public Generator() {
        this.states = new ArrayList<SwitchEntryStmt>();
        this.loop = null;
        this.block = null;
        this.nextBlock = 0;

        this.newState();
    }

    /**
     * Generate a deferred jump. This sets the state to a given state number, but doesn't perform
     * the jump.
     */
    public static List<Statement> generateDeferredJump(int state) {
        List<Statement> stmts = new ArrayList<Statement>();
        stmts.add(new ExpressionStmt(-1, -1, new AssignExpr(-1, -1, STATE_VAR,
                                                            new IntegerLiteralExpr(-1, -1, "" + state),
                                                            AssignExpr.Operator.assign)));
        return stmts;
    }

    /**
     * Generate an immediate jump.
     */
    public static List<Statement> generateJump(int state) {
        List<Statement> stmts = generateDeferredJump(state);
        stmts.add(new BreakStmt(-1, -1, null));
        return stmts;
    }

    /**
     * Get the scope-mangled prefix for the current scope.
     */
    String getScopePrefix() {
        StringBuilder sb = new StringBuilder();
        BlockScope c = this.block;
        while (c != null) {
            sb.append("scope" + c.num + "$");
            c = c.back;
        }

        return sb.toString();
    }

    /**
     * Allocate a new state.
     */
    void newState() {
        List<Statement> stmts = new ArrayList<Statement>();
        this.states.add(new SwitchEntryStmt(-1, -1,
                                            new IntegerLiteralExpr(-1, -1, "" + this.states.size()),
                                            stmts));
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

    /**
     * Enter a loop. This should be paired with exitLoop to generate the appropriate jump out.
     */
    void enterLoop() {
        this.loop = new LoopMarker(this.states.size(), this.loop);
        this.newState();
    }

    /**
     * Exit a loop, rewriting the breaks and continues in the loop body.
     */
    void exitLoop() {
        this.loop.endState = this.states.size();

        // We now want to rewrite all the continue and breaks using this loop marker.
        for (int i = this.loop.startState; i < this.loop.endState; ++i) {
            LoopBodyTransform r = new LoopBodyTransform(this);
            this.states.get(i).accept(r, this.loop);
        }

        this.newState();
        this.loop = this.loop.back;
    }

    /**
     * Get the current state node.
     */
    SwitchEntryStmt getCurrentStateNode() {
        return this.states.get(this.getCurrentState());
    }

    /**
     * Get the current state number.
     */
    int getCurrentState() {
        return this.states.size() - 1;
    }

    /**
     * Add a statement to the current state.
     */
    void addStatement(Statement stmt) {
        this.getCurrentStateNode().getStmts().add(stmt);
    }

    /**
     * Add a statement to the current state.
     */
    void addAllStatements(List<Statement> stmts) {
        this.getCurrentStateNode().getStmts().addAll(stmts);
    }

    /**
     * Add a label corresponding to the current state.
     */
    void addLabel(String name, TransitionPoint p) {
        this.labels.put(name, p);
    }

    /**
     * Generate the statements for the generator.
     */
    public List<Statement> generate() {
        List<Statement> stmts = new ArrayList<Statement>();
        stmts.add(GeneratorTransform.makeLoopStmt(new SwitchStmt(-1, -1, STATE_VAR,
                                                                this.states)));
        return stmts;
    }
}
