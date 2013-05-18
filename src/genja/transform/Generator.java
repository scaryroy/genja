package genja.transform;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import japa.parser.ast.body.Parameter;
import japa.parser.ast.body.VariableDeclaratorId;
import japa.parser.ast.expr.AssignExpr;
import japa.parser.ast.expr.BooleanLiteralExpr;
import japa.parser.ast.expr.IntegerLiteralExpr;
import japa.parser.ast.expr.NameExpr;
import japa.parser.ast.stmt.BlockStmt;
import japa.parser.ast.stmt.BreakStmt;
import japa.parser.ast.stmt.CatchClause;
import japa.parser.ast.stmt.ExpressionStmt;
import japa.parser.ast.stmt.IfStmt;
import japa.parser.ast.stmt.Statement;
import japa.parser.ast.stmt.SwitchEntryStmt;
import japa.parser.ast.stmt.SwitchStmt;
import japa.parser.ast.stmt.ThrowStmt;
import japa.parser.ast.stmt.TryStmt;
import japa.parser.ast.type.ClassOrInterfaceType;
import japa.parser.ast.type.Type;

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

    public static final NameExpr EXCEPTION_VAR = new NameExpr("$exception");
    
    /**
     * Current implicit loop label number.
     */
    int loopNum;

    /**
     * Current implicit switch number.
     */
    int switchNum;

    /**
     * Current try state number.
     */
    int currentTryState;
    
    /**
     * The current label we're in.
     */
    Label label;

    /**
     * The states.
     */
    List<SwitchEntryStmt> states;

    /**
     * The exception handling states, mapping an exception type to an entry
     * state to an exit state.
     */
    Map<String, Map<Integer, ExceptionHandler>> exceptionHandlers;

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
        this.exceptionHandlers = new HashMap<String, Map<Integer, ExceptionHandler>>();
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
     * Get the current state number.
     */
    int getCurrentState() {
        return this.states.size() - 1;
    }

    /**
     * Add a statement to the current state.
     */
    void addStatement(Statement stmt) {
        this.addStatement(this.getCurrentState(), stmt);
    }

    /**
     * Add a statement to the given state.
     */
    void addStatement(int state, Statement stmt) {
        this.states.get(state).getStmts().add(stmt);
    }

    /**
     * Add some statements to the current state.
     */
    void addAllStatements(List<Statement> stmts) {
        this.addAllStatements(this.getCurrentState(), stmts);
    }

    /**
     * Add some statement to the given state.
     */
    void addAllStatements(int state, List<Statement> stmts) {
        for (Statement stmt : stmts) {
            this.addStatement(state, stmt);
        }
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
     * Add an exception handler. The return value indicates if the exception
     * handler is permissible.
     */
    boolean addExceptionHandler(Type excType, int entryState, ExceptionHandler handler) {
        String typeName = excType.toString();
        
        if (!this.exceptionHandlers.containsKey(typeName)) {
            this.exceptionHandlers.put(typeName, new HashMap<Integer, ExceptionHandler>());
        }

        // Don't let outer exception handlers override inner ones.
        if (this.exceptionHandlers.get(typeName).containsKey(entryState)) {
            return false;
        }

        this.exceptionHandlers.get(typeName).put(entryState, handler);
        return true;
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
        List<CatchClause> catches = new ArrayList<CatchClause>();

        for (Map.Entry<String, Map<Integer, ExceptionHandler>> typeEntry : this.exceptionHandlers.entrySet()) {
            List<SwitchEntryStmt> entries = new ArrayList<SwitchEntryStmt>();

            for (Map.Entry<Integer, ExceptionHandler> handlerEntry : typeEntry.getValue().entrySet()) {
                List<Statement> handlerStmts = new ArrayList<Statement>();
                handlerStmts.add(new ExpressionStmt(new AssignExpr(new NameExpr(handlerEntry.getValue().name), new NameExpr("e"), AssignExpr.Operator.assign)));
                handlerStmts.add(Generator.generateJump(handlerEntry.getValue().catchPoint));
                entries.add(new SwitchEntryStmt(new IntegerLiteralExpr("" + handlerEntry.getKey()), handlerStmts));
            }

            List<Statement> handlerStmts = new ArrayList<Statement>();
            handlerStmts.add(new ExpressionStmt(new AssignExpr(Generator.STATE_VAR, new IntegerLiteralExpr("-2"), AssignExpr.Operator.assign)));
            handlerStmts.add(new ThrowStmt(EXCEPTION_VAR));
            entries.add(new SwitchEntryStmt(null, handlerStmts));

            
            List<Statement> stmts = new ArrayList<Statement>();
            stmts.add(new ExpressionStmt(new AssignExpr(Generator.EXCEPTION_VAR, new NameExpr("e"), AssignExpr.Operator.assign)));
            stmts.add(new SwitchStmt(Generator.STATE_VAR, entries));
            catches.add(new CatchClause(
                    new Parameter(new ClassOrInterfaceType(typeEntry.getKey()), new VariableDeclaratorId("e")),
                    new BlockStmt(stmts)
            ));
        }

        List<Statement> innerStmts = new ArrayList<Statement>();
        Statement innerLoop = LoopDesugarTransform.makeLoopStmt(new SwitchStmt(STATE_VAR,
                                                                this.states));
        innerStmts.add(innerLoop);

        TryStmt tryStmt = new TryStmt(new BlockStmt(innerStmts), catches, new BlockStmt());

        List<Statement> stmts = new ArrayList<Statement>();
        stmts.add(LoopDesugarTransform.makeLoopStmt(tryStmt));
        return stmts;
    }
}
