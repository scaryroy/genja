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
    Label loop;

    /**
     * The current label we're in.
     */
    Label label;

    /**
     * The states.
     */
    List<SwitchEntryStmt> states;

    /**
     * List of labels.
     */
    Map<String, Label> labels;

    /**
     * Create a new generator.
     */
    public Generator() {
        this.states = new ArrayList<SwitchEntryStmt>();
        this.loop = null;
        this.label = null;
        this.labels = new HashMap<String, Label>();

        this.newState();
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
     * Allocate a new state.
     */
    void newState() {
        List<Statement> stmts = new ArrayList<Statement>();
        this.states.add(new SwitchEntryStmt(new IntegerLiteralExpr("" + this.states.size()),
                                            stmts));
    }

    /**
     * Enter a loop. This should be paired with exitLoop to generate the appropriate jump out.
     */
    void enterLoop() {
        this.loop = new Label(null, this.states.size(), this.loop);
        this.newState();
    }

    /**
     * Exit a loop, rewriting the breaks and continues in the loop body.
     */
    void exitLoop() {
        this.loop.breakPoint = this.states.size();

        // We now want to rewrite all the continue and breaks using this loop marker.
        for (int i = this.loop.continuePoint; i < this.loop.breakPoint; ++i) {
            IntraLoopJumpTransform r = new IntraLoopJumpTransform(this);
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
     * Enter a label context corresponding to the current state.
     */
    void enterLabel(String name) {
        this.label = new Label(name, this.getCurrentState(), this.label);
        this.labels.put(name, this.label);
    }

    /**
     * Exit a label context.
     */
    void exitLabel() {
        this.label = this.label.back;
    }

    /**
     * Check if a delimited jump is possible to the given label.
     */
    boolean canJumpTo(String name) {
        Label c = this.label;
        while (c != null) {
            if (c.name.equals(name)) {
                return true;
            }
            c = c.back;
        }
        return false;
    }
    
    /**
     * Generate the statements for the generator.
     */
    public List<Statement> generate() {
        List<Statement> stmts = new ArrayList<Statement>();
        stmts.add(LoopDesugarTransform.makeLoopStmt(new SwitchStmt(STATE_VAR,
                                                                      this.states)));
        return stmts;
    }
}
