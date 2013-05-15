package genja.transform;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import japa.parser.ast.expr.AssignExpr;
import japa.parser.ast.expr.BooleanLiteralExpr;
import japa.parser.ast.expr.IntegerLiteralExpr;
import japa.parser.ast.expr.NameExpr;
import japa.parser.ast.stmt.BlockStmt;
import japa.parser.ast.stmt.BreakStmt;
import japa.parser.ast.stmt.ExpressionStmt;
import japa.parser.ast.stmt.IfStmt;
import japa.parser.ast.stmt.Statement;
import japa.parser.ast.stmt.SwitchEntryStmt;
import japa.parser.ast.stmt.SwitchStmt;

/**
 * The generator class contains the state required to create a generator, as well as utility
 * methods for populating its state.
 */
class Generator {
    /**
     * The variable name containing the current state number.
     */
    public static final NameExpr STATE_VAR = new NameExpr("$state");

    /**
     * The variable name containing the currently yielded value.
     */
    public static final NameExpr CURRENT_VAR = new NameExpr("$current");

    /**
     * The current loop we're transforming.
     */
    LoopMarker loop;

    /**
     * The states.
     */
    List<SwitchEntryStmt> states;

    /**
     * List of labels.
     */
    Map<String, TransitionPoint> labels;

    /**
     * Create a new generator.
     */
    public Generator() {
        this.states = new ArrayList<SwitchEntryStmt>();
        this.loop = null;
        this.labels = new HashMap<String, TransitionPoint>();

        this.forceNewState();
    }

    /**
     * Generate a deferred jump. This sets the state to a given state number, but doesn't perform
     * the jump.
     */
    public static Statement generateDeferredJump(int state) {
        return new ExpressionStmt(new AssignExpr(STATE_VAR,
                                                 new IntegerLiteralExpr("" + state),
                                                 AssignExpr.Operator.assign));
    }

    /**
     * Generate an immediate jump.
     */
    public static Statement generateJump(int state) {
        List<Statement> stmts = new ArrayList<Statement>();
        stmts.add(generateDeferredJump(state));
        stmts.add(new BreakStmt(null));
        return new IfStmt(new BooleanLiteralExpr(true),
                          new BlockStmt(stmts), null);
    }

    /**
     * Forcibly allocate a new state.
     */
    void forceNewState() {
        List<Statement> stmts = new ArrayList<Statement>();
        this.states.add(new SwitchEntryStmt(new IntegerLiteralExpr("" + this.states.size()),
                                            stmts));
    }

    /**
     * Allocate a new state.
     */
    void newState() {
        if (this.getCurrentStateNode().getStmts().size() > 0) {
            this.forceNewState();
        }
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
        if (this.getCurrentState() == -1) return null;
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
        stmts.add(LoopDesugaringTransform.makeLoopStmt(new SwitchStmt(STATE_VAR,
                                                                      this.states)));
        return stmts;
    }
}
