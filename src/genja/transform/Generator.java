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
     * Current implicit loop label number.
     */
    int loopNum;

    /**
     * Current implicit switch number.
     */
    int switchNum;

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
        this.loopNum = 0;
        this.switchNum = 0;
        this.states = new ArrayList<SwitchEntryStmt>();
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
     * Enter a loop. This should be paired with exitLabel to generate the appropriate jump out.
     */
    void enterLoop() {
        this.enterLabel(".loop" + this.loopNum++);
        this.label.loop = true;
    }
    /**
     * Enter a switch. This should be paired with exitLable to generate the appropriate jump out.
     */
    void enterSwitch() {
        this.enterLabel(".switch" + this.switchNum++);
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
        this.label = new Label(name, this.getCurrentState(), false, this.label);
        this.labels.put(name, this.label);
    }

    /**
     * Exit a label context.
     */
    void exitLabel() {
        this.newState();
        this.label.breakPoint = this.getCurrentState();
        this.label = this.label.back;
    }

    /**
     * Find an ancestral break and continue point for a given label.
     */
    Label ancestralLabelFor(String name) {
        Label c = this.label;
        while (c != null) {
            if (c.name.equals(name)) {
                return c;
            }
            c = c.back;
        }
        return null;
    }

    /**
     * Check if a delimited break is possible to the given label.
     */
    boolean canBreakTo(String name) {
        return this.ancestralLabelFor(name) != null;
    }

    /**
     * We can only continue to loops, so we check if a label corresponds to a
     * loop.
     */
    boolean canContinueTo(String name) {
        Label label = this.ancestralLabelFor(name);
        return label.loop && this.canBreakTo(name);
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
